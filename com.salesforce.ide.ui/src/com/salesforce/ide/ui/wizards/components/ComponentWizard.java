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
package com.salesforce.ide.ui.wizards.components;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.wizards.BaseWizard;

/**
 * 
 * 
 * @author cwall
 */
public abstract class ComponentWizard extends BaseWizard implements IComponentCreateWizard {
    private static final Logger logger = Logger.getLogger(ComponentWizard.class);

    private ComponentWizardPage wizardPage = null;

    //   C O N S T R U C T O R S
    public ComponentWizard() {
        super();
    }

    //   M E T H O D S

    public String getComponentType() {
        return getComponentController().getComponentType();
    }

    public String getComponentTypeDisplayName() {
        return getComponentController().getComponent().getDisplayName();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setNeedsProgressMonitor(false);
        addImage();

        if (null != selection) {
            Object obj = selection.getFirstElement();
            if (obj instanceof IResource) {
                IProject project = ((IResource) obj).getProject();
                getComponentController().setResources(project);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " wizard initialized");
        }
    }

    @Override
    protected String getWindowTitleString() {
        return UIMessages.getString("NewComponent.title", new String[] { getComponentTypeDisplayName() });
    }

    @Override
    public void addPages() {
        // only proceed if a project was derived
        if (getPageCount() == 0 && getComponentWizardModel() != null) {
            wizardPage = getComponentWizardPageInstance();
            addPage(wizardPage);
        }
    }

    protected abstract ComponentWizardPage getComponentWizardPageInstance();

    @Override
    public ComponentModel getComponentWizardModel() {
        return getComponentController().getComponentWizardModel();
    }

    public ComponentController getComponentController() {
        return (ComponentController) controller;
    }

    public ComponentWizardPage getWizardPage() {
        return wizardPage;
    }

    public void setWizardPage(ComponentWizardPage wizardPage) {
        this.wizardPage = wizardPage;
    }

    protected void addImage() {
        ImageDescriptor id = ForceImages.getDesc(ForceImages.APEX_WIZARD_IMAGE);
        setDefaultPageImageDescriptor(id);
    }

    @Override
    public final boolean performCancel() {
        getComponentController().setCanComplete(false);
        return true;
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using
     * wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        if (!getComponentController().canComplete()) {
            if (logger.isInfoEnabled()) {
                logger.info("Component control is not complete");
            }
            return false;
        }

        // create component based on given user input
        try {
            wizardPage.saveUserInput();
            wizardPage.clearMessages();

            // set focus on label or name if focus is returned, eg remote name check fails
            if (wizardPage.getBaseComponentWizardComposite().getTxtLabel() != null) {
                wizardPage.getBaseComponentWizardComposite().getTxtLabel().setFocus();
            } else if (wizardPage.getBaseComponentWizardComposite().getTxtName() != null) {
                wizardPage.getBaseComponentWizardComposite().getTxtName().setFocus();
            }

            return executeCreateOperation();
        } catch (Exception e) {
            logger.error("Unable to create " + getComponentType(), e);
            Utils.openError(e, true, "Unable to create " + getComponentType() + ".");
            return false;
        }
    }

    public boolean executeCreateOperation() {
        ComponentCreateOperation createComponent = new ComponentCreateOperation(getComponentController(), this);
        try {
            IProgressService service = PlatformUI.getWorkbench().getProgressService();
            service.run(false, true, createComponent);
            return true;
        } catch (InterruptedException e) {
            return false;
        } catch (Exception e) {
            logger.error("Unable to create " + getComponentType(), e);
            Utils.openError(e, true, "Unable to create " + getComponentType() + ".");
            return false;
        }
    }
}
