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
package com.salesforce.ide.ui.actions;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;

/**
 *
 * 
 * @author cwall
 */
public class NewComponentActionController extends ActionController {

    private static final Logger logger = Logger.getLogger(NewComponentActionController.class);

    private Hashtable<String, ComponentWizard> newComponentWizards = null;
    private WizardDialog newComponentWizardDialog = null;
    private Component component = null;

    private Resources resources;

    public NewComponentActionController() throws ForceProjectException {
        this(new Resources());
    }
    
    public NewComponentActionController(Resources resources) throws ForceProjectException {
        this.resources = resources;
    }

    public Hashtable<String, ComponentWizard> getNewComponentWizards() {
        return newComponentWizards;
    }

    public void setNewComponentWizards(Hashtable<String, ComponentWizard> newComponentWizards) {
        this.newComponentWizards = newComponentWizards;
    }

    public WizardDialog getNewComponentWizardDialog() {
        return newComponentWizardDialog;
    }

    public void setNewComponentWizardDialog(WizardDialog newComponentWizardDialog) {
        this.newComponentWizardDialog = newComponentWizardDialog;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    @Override
    public boolean preRun(IAction action) {
        final String title = resources.getDialogTitle_Error();
        if (isNonNullOnlineProject()) {
            logger.warn("Project is not associated with " + DefaultNature.NATURE_ID);
            openErrorInUI(title, resources.getMessage_notForceComProject());
            return false;
        }

        final IResource selectedResource = getSelectedResource();
        if (selectedResource == null) {
            logger.warn("Unable to open New Force.com Component - folder is null.");
            openErrorInUI(title,resources.getMessage_unknownFolderMessage());
            return false;
        }

        if (isSelectedResourceInReferencedfolder(selectedResource)) {
            logger.warn("Unable to open New Force.com Component - component folder is part of a referenced packaged.");
            openErrorInUI(title, resources.getMessage_ReferencedPkg());
            return false;
        }

        if (isInvalidAction(action)) {
            logger.error("Unable to determine component type from action");
            openErrorInUI(title,resources.getMessage_invalidAction());
            return false;
        }

        // get component instance based on
        component = deriveComponentFromAction(action.getId());

        if (component == null) {
            logger.error("Unable to determine component type from action '" + action.getId() + "'");
            openErrorInUI(title,resources.getMessage_invalidDerivedComponent());
            return false;
        }

        return true;
    }

    protected boolean isInvalidAction(IAction action) {
        return action == null || Utils.isEmpty(action.getId())
                || !action.getId().startsWith(UIConstants.NEW_COMPONENT_ACTION_ID_PREFIX);
    }

    protected boolean isSelectedResourceInReferencedfolder(final IResource selectedResource) {
        return getProjectService().isReferencedPackageResource(selectedResource);
    }

    protected boolean isNonNullOnlineProject() {
        return project == null || !getProjectService().isManagedProject(project);
    }

    protected void openErrorInUI(final String title, final String message) {
        Utils.openError(title, message);
    }

    protected void openInfoDialogInUI(final String title, final String message) {
        Utils.openInfo(title, message);
    }

    protected Component deriveComponentFromAction(String actionId) {
        String componentType = actionId.substring(actionId.lastIndexOf(".") + 1);
        if (Utils.isEmpty(componentType)) {
            logger.warn("Unable to derive component folder");
            return null;
        }

        Component component = null;
        try {
            component = getComponentFactory().getComponentByComponentType(componentType);
        } catch (FactoryException e) {
            logger.warn("Unable to derive component folder - unable to get object type from action id '" + actionId
                    + "'");
        }

        if (component == null) {
            logger.warn("Unable to derive component folder");
        }

        return component;
    }

    public void openNewComponentWizard() {
        if (component == null) {
            logger.error("Unable to open new component wizard - component is null");
            Utils.openError("New Force.com Component Error",
                "Unable to open new component wizard.  The component for action is not supported.");
            return;
        }

        try {
            prepareAndOpenWizardDialog();
        } catch (Exception e) {
            logger.error("Unable to open New Force.com Component wizard for " + component.getComponentType(), e);
            Utils.openError(e, UIMessages.getString("NewComponentAction.MessageBox.title"), UIMessages
                    .getString("NewComponentAction.GeneralError.message"));
        }
    }

    // extract this piece out for testing
    public void prepareAndOpenWizardDialog() {
        if (component == null) {
            logger.error("Unable to open new component wizard - component is null");
            Utils.openError("New Force.com Component Error",
                "Unable to open new component wizard.  The component for action is not supported.");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("***   C O M P O N E N T  W I Z A R D   ***");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Opening component wizard '" + component.getWizardClassName() + "'");
        }

        ComponentWizard componentWizard = null;
        try {
            Class<?> componentWizardClass = Class.forName(component.getWizardClassName());

            if (componentWizardClass == null) {
                logger.error("Unable to open new component wizard '" + component.getWizardClassName() + "'");
                Utils.openError("New Force.com Component Error",
                    "Unable to open new component wizard.  The component for action is not supported.");
                return;
            }

            componentWizard = (ComponentWizard) componentWizardClass.newInstance();
            componentWizard.init(getWorkbench(), getStructuredSelection());
        } catch (ClassNotFoundException e) {
            logger.error("Unable to open new component wizard '" + component.getWizardClassName() + "'");
            Utils.openError("New Force.com Component Error",
                "Unable to open new component wizard.  The component for action is not supported.");
            return;
        } catch (InstantiationException e) {
            logger.error("Unable to open new component wizard '" + component.getWizardClassName() + "'");
            Utils.openError("New Force.com Component Error",
                "Unable to open new component wizard.  The component for action is not supported.");
            return;
        } catch (IllegalAccessException e) {
            logger.error("Unable to open new component wizard '" + component.getWizardClassName() + "'");
            Utils.openError("New Force.com Component Error",
                "Unable to open new component wizard.  The component for action is not supported.");
            return;
        }

        // create and open the wizard dialog
        newComponentWizardDialog = new WizardDialog(null, componentWizard);
        newComponentWizardDialog.open();
    }

    private IStructuredSelection getStructuredSelection() {
        if (selection != null) {
            return (IStructuredSelection) selection;
        }
		return new StructuredSelection(getSelectedResource());
    }

    @Override
    public WizardDialog getWizardDialog() {
        return null;
    }

    @Override
    public void postRun(IAction action) {

    }
    
    public static class Resources {
        public String getDialogTitle_Error() {
            return UIMessages.getString("NewComponentAction.MessageBox.title")+" Error";
        }

        public String getMessage_invalidDerivedComponent() {
            return "Unable to open new component wizard.  The component for action is not supported.";
        }

        public String getMessage_invalidAction() {
            return "Unable to open new component wizard.  The component for action is not supported.";
        }

        public String getMessage_notForceComProject() {
            return "Force.com Components can only be created for Force.com projects.";
        }

        public String getMessage_ReferencedPkg() {
            return UIMessages.getString("NewComponentAction.ReferencedPackage.message");
        }

        public String getMessage_unknownFolderMessage() {
            return UIMessages.getString("NewComponentAction.UnknownFolder.message");
        }
        
    }
}
