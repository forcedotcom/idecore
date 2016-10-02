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
package com.salesforce.ide.ui.internal.startup;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.WorkbenchShutdownListener;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.MessageDialogRunnable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.perspective.ForceIdePerspectiveListener;

/**
 * Forces initialization of ForceIdeUIPlugin so that component wizards are dynamically created.
 * 
 * Manages manifest listening - refresh project when certain package.xml edits occurr.
 * 
 * @author cwall
 * 
 */
public class ForceStartup implements IStartup {

    private static Logger logger = Logger.getLogger(ForceStartup.class);

    private static Map<IResource, Package> manifestCache = new HashMap<>();
    public static boolean PACKAGE_MANIFEST_LISTENER_FLAG = Utils.isManifestListenerEnabled();

    @Override
    public void earlyStartup() {
        if (!Utils.isSkipCompatibilityCheck()) {
            Bundle bundle = Platform.getBundle(Constants.PREV_IDE_BUNDLE_NAME);
            if (bundle != null) {
                int state = bundle.getState();
                if (logger.isDebugEnabled()) {
                    StringBuilder debugInfo = new StringBuilder("Current state of older version bundle '");
                    debugInfo.append(Constants.PREV_IDE_BUNDLE_NAME + "' is '" + state + "'");
                    debugInfo.append("Ref: UNINSTALLED=" + Bundle.UNINSTALLED + ", INSTALLED=" + Bundle.INSTALLED
                            + ", RESOLVED=" + Bundle.RESOLVED + ", STARTING=" + Bundle.STARTING + ", STOPPING="
                            + Bundle.STOPPING + ", ACTIVE=" + Bundle.ACTIVE + ")");
                    logger.debug(debugInfo);
                }

                if (state != Bundle.STOPPING || state != Bundle.UNINSTALLED) {
                    String title = UIMessages.getString("Force.Startup.Compatibility.Warning.title");

                    String bundleName = bundle.getHeaders().get(Constants.BUNDLE_ATTRIBUTE_NAME);
                    String bundleVersion = bundle.getHeaders().get(Constants.BUNDLE_ATTRIBUTE_VERSION);
                    String dialogMsg =
                            UIMessages.getString("Force.Startup.Compatibility.Warning.message", new String[] {
                                    bundleName, bundleVersion });

                    MessageDialogRunnable messageDialogRunnable =
                            new MessageDialogRunnable(title, null, dialogMsg, MessageDialog.WARNING,
                                    new String[] { IDialogConstants.OK_LABEL }, 0);
                    Display.getDefault().asyncExec(messageDialogRunnable);

                }
            }
            WorkbenchShutdownListener.installWorkbenchShutdownListener();
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                IPerspectiveDescriptor[] perspectives =
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSortedPerspectives();
                if (Utils.isNotEmpty(perspectives)) {
                    for (int i = 0; i < perspectives.length; i++) {
                    	IPerspectiveDescriptor perspectiveDescriptor = perspectives[i];
                        if (perspectiveDescriptor.getId().equals(Constants.FORCE_PLUGIN_PERSPECTIVE)) {
                        	ForceIdePerspectiveListener forcePerspectiveListener = new ForceIdePerspectiveListener();
                        	/**
                        	 *  Do it here so it's registered early enough to establish the context
                        	 *  But also only if it's the last one to be opened 
                        	 */
                        	if (i == perspectives.length - 1) {
                        		forcePerspectiveListener.activateForceContext();
                        	}
                        	UIUtils.addPerspectiveListener(forcePerspectiveListener);
                        	logger.debug("Found " + perspectiveDescriptor.getId() + " perspective"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
        });

        addPackageManifestChangeListener();
    }

    /*    private static void cacheManifests() {
            IProject[] projects = workspace.getRoot().getProjects();
            if (Utils.isEmpty(projects)) {
                return;
            }

            for (IProject project : projects) {
                cacheManifest(project);
            }
        }*/

    public static Map<IResource, Package> getManifestCache() {
        return manifestCache;
    }

    public static void addPackageManifestChangeListener() {
    //        if (PACKAGE_MANIFEST_LISTENER_FLAG) {
    //            cacheManifests();
    //            workspace.addResourceChangeListener(pmcl);
    //            if (logger.isDebugEnabled()) {
    //                logger.debug("Added PackageManifestChangeListener as workspace resource listener");
    //            }
    //        } else {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug("PackageManifestChangeListener is disabled");
    //            }
    //        }
    }

    public static void removePackageManifestChangeListener() {
    //        if (PACKAGE_MANIFEST_LISTENER_FLAG) {
    //            workspace.removeResourceChangeListener(pmcl);
    //            if (logger.isDebugEnabled()) {
    //                logger.debug("Removed PackageManifestChangeListener as workspace resource listener");
    //            }
    //        } else {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug("PackageManifestChangeListener is disabled");
    //            }
    //        }
    }

    public static void cacheManifest(IProject project) {
    //        try {
    //            if (project.isOpen() && project.hasNature(DefaultNature.NATURE_ID)) {
    //                IFile file = pmf.getPackageManifestFile(project);
    //                manifestCache.put(file, pmf.getPackageManifestFromFile(file));
    //
    //                if (logger.isDebugEnabled()) {
    //                    logger.debug("Cached project '" + project.getName() + "' manifest");
    //                }
    //            }
    //        } catch (Exception e) {
    //            logger.warn("Unable to cache package.xml for " + project.getName() + ": " + e.getMessage()); //$NON-NLS-1$
    //        }
    }
}
