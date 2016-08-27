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
package com.salesforce.ide.ui.editors.internal;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public abstract class BaseMultiPageEditorPart extends MultiPageEditorPart {

    private static final Logger logger = Logger.getLogger(BaseMultiPageEditorPart.class);

    protected IProject project = null;
    protected ServiceLocator serviceLocator = null;
    protected FactoryLocator factoryLocator = null;

    // C O N S T R U C T O R S
    public BaseMultiPageEditorPart() {
        super();
        setTitleImage(getImage());
        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();
        factoryLocator = ContainerDelegate.getInstance().getFactoryLocator();
    }

    // M E T H O D S
    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public void setProject() {
        IResource resource = Utils.getCurrentSelectionResource();
        if (resource != null) {
            project = resource.getProject();
        }
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public FactoryLocator getFactoryLocator() {
        return factoryLocator;
    }

    public ConnectionFactory getConnectionFactory() {
        return factoryLocator.getConnectionFactory();
    }

    public ComponentFactory getComponentFactory() {
        return factoryLocator.getComponentFactory();
    }

    public ProjectService getProjectService() {
        return serviceLocator.getProjectService();
    }

    protected Image getImage() {
        return ForceImages.get(ForceImages.APEX_TITLE_IMAGE);
    }

    protected abstract String getEditorName();

    protected void initSObjects() {
        Job job = new Job("Pre-fetch SObjects") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (project == null) {
                    IResource resource = Utils.getCurrentSelectionResource();
                    if (resource != null) {
                        project = resource.getProject();
                    }
                }

                if (project != null) {
                    try {
                        getProjectService().getDescribeObjectRegistry().getCachedDescribeSObjects(project);
                    } catch (final InvalidLoginException e) {
                        // log failure
                        logger.warn("Unable to get describe object for project '" + project.getName() + "':"
                                + ForceExceptionUtils.getRootCauseMessage(e));
                        // choose further project create direction
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.getInstance().invalidLoginDialog(
                                    ForceExceptionUtils.getRootCauseMessage(e), project.getName(), true);
                            }
                        });
                    } catch (Exception e) {
                        logger.error("Unable to get describe object for project '" + project.getName() + "'", e);
                        return new Status(IStatus.ERROR, ForceIdeUIPlugin.PLUGIN_ID, IStatus.ERROR,
                                "Unable to get describe object for project '" + project.getName() + "'", e);
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.SHORT); // start as soon as possible
        job.schedule();
    }

    protected void updateTitle() {
        IEditorInput input = getEditorInput();
        setPartName(input.getName());
        setTitleToolTip(input.getToolTipText());
    }

    @Override
    public int addPage(Control control) {
        UIUtils.setHelpContext(control, this.getClass().getSimpleName());
        return super.addPage(control);
    }

    public IFile getEditorInputFile() {
        return ((IFileEditorInput) getEditorInput()).getFile();
    }

    @Override
    public String getTitle() {
        String title = super.getTitle();
        if ((title == null) && (getEditorInput() != null)) {
            title = getEditorInput().getName();
        }
        return title;
    }

    @Override
    protected IEditorPart getEditor(int pageIndex) {
        return super.getEditor(pageIndex);
    }

    @Override
    public void doSaveAs() {
        // not implemented: isSaveAsAllowed = false
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
}
