/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.packagemanifest;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.ui.handlers.RefreshResourceHandler;
import com.salesforce.ide.ui.internal.Messages;
import com.salesforce.ide.ui.internal.startup.ForceStartup;

/**
 *
 * @author ataylor
 * 
 */
@SuppressWarnings( { "nls" })
public class PackageManifestChangeListener implements IResourceChangeListener {
    private static Logger logger = Logger.getLogger(PackageManifestChangeListener.class);
    private static PackageManifestFactory pmf = new PackageManifestFactory();

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        //we are only interested in POST_CHANGE events
        if (event.getType() != IResourceChangeEvent.POST_CHANGE)
            return;
        IResourceDelta rootDelta = event.getDelta();

        final Set<IResource> changed = new HashSet<>();
        IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) {
                int kind = delta.getKind();

                IResource resource = delta.getResource();
                //only interested in package manifest files
                if (!(resource.getType() == IResource.FILE && Constants.PACKAGE_MANIFEST_FILE_NAME
                        .equalsIgnoreCase(resource.getName()))) {
                    return true;
                }

                if (kind == IResourceDelta.ADDED) {
                    ForceStartup.getManifestCache().put(resource, getPackage(resource));
                } else if (kind == IResourceDelta.REMOVED) {
                    ForceStartup.getManifestCache().remove(resource);
                }

                if (kind != IResourceDelta.CHANGED)
                    return true;

                //only interested in content changes
                if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
                    return true;

                Package oldPackage = ForceStartup.getManifestCache().get(resource);
                Package newPackage = getPackage(resource);

                if (!isEqual(oldPackage, newPackage)) {
                    changed.add(resource);
                }
                return true;
            }
        };

        try {
            rootDelta.accept(visitor);

            if (!Utils.isEmpty(changed)) {
                for (final IResource resource : changed) {
                    final IProject project = resource.getProject();

                    if (project.hasNature(DefaultNature.NATURE_ID)) {

                        final Package newPackage = getPackage(resource);

                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                // TODO investigate whether new events should be combined with old events
                                final Package oldPackage = ForceStartup.getManifestCache().get(resource);

                                if (!isEqual(oldPackage, newPackage)) {
                                    ForceStartup.getManifestCache().put(resource, newPackage);

                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Manifest resource '"
                                                + resource.getFullPath().toPortableString()
                                                + "' found to be refresh-able");
                                    }

                                    if (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                                        Messages.PackageManifestChangeListener_dialog_title, NLS.bind(
                                            Messages.PackageManifestChangeListener_dialog_message, resource
                                                    .getFullPath(), project.getName()))) {
                                        RefreshResourceHandler.execute(PlatformUI.getWorkbench(), new StructuredSelection(project));
                                    }
                                }
                            }
                        });
                    }
                }
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("An error occured while trying to access files in ResourceDelta: " + logMessage);
        }
    }

    private static Package getPackage(IResource resource) {
        try {
            return pmf.getPackageManifestFromFile((IFile) resource);
        } catch (Exception e) {
            logger.warn("Unable to cache package.xml for " + resource.getProject().getName() + ": "
                    + ForceExceptionUtils.getRootCauseMessage(e));
        }

        return null;
    }

    private static Comparator<PackageTypeMembers> typeComparator = new Comparator<PackageTypeMembers>() {
        @Override
        public int compare(PackageTypeMembers o1, PackageTypeMembers o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
        }
    };

    private static boolean isEqual(Package p1, Package p2) {
        List<PackageTypeMembers> p1Types = p1.getTypes();
        List<PackageTypeMembers> p2Types = p2.getTypes();

        if (p1Types.size() != p2Types.size()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updated package manifest component type changed from  [" + p1Types.size() + "] to ["
                        + p2Types.size() + "]");
                try {
                    logger.debug("Original package manifest:\n" + p1.getXMLString() + "\nUpdate package manifest:\n"
                            + p2.getXMLString());
                } catch (JAXBException e) {}
            }
            return false;
        }

        Collections.sort(p1Types, typeComparator);
        Collections.sort(p2Types, typeComparator);

        for (int i = 0; i < p1Types.size(); i++) {
            if (!p1Types.get(i).getName().equals(p2Types.get(i).getName())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Updated package manifest component types differ");
                    try {
                        logger.debug("Original package manifest:\n" + p1.getXMLString()
                                + "\nUpdate package manifest:\n" + p2.getXMLString());
                    } catch (JAXBException e) {}
                }
                return false;
            }

            List<String> p1Members = p1Types.get(i).getMembers();
            List<String> p2Members = p2Types.get(i).getMembers();

            if (p1Members.size() != p2Members.size()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Updated package manifest component type members changed from  [" + p1Types.size()
                            + "] to [" + p2Types.size() + "]");
                    try {
                        logger.debug("Original package manifest:\n" + p1.getXMLString()
                                + "\nUpdate package manifest:\n" + p2.getXMLString());
                    } catch (JAXBException e) {}
                }
                return false;
            }

            Collections.sort(p1Members);
            Collections.sort(p2Members);

            for (int j = 0; j < p1Members.size(); j++) {
                if (!p1Members.get(j).equals(p2Members.get(j))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Updated package manifest members differ");
                        try {
                            logger.debug("Original package manifest:\n" + p1.getXMLString()
                                    + "\nUpdate package manifest:\n" + p2.getXMLString());
                        } catch (JAXBException e) {}
                    }
                    return false;
                }
            }
        }

        return true;
    }
}
