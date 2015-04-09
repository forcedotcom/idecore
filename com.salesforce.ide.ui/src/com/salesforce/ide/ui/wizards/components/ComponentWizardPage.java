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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.internal.wizards.BaseWizardPage;

/**
 * 
 * 
 * @author cwall
 */
public abstract class ComponentWizardPage extends BaseWizardPage implements IComponentWizardPage {
    private static final Logger logger = Logger.getLogger(ComponentWizardPage.class);

    protected IComponentWizardComposite componentWizardComposite = null;
    protected ComponentWizard componentWizard = null;
    protected boolean pageComplete = true;
    protected String status = null;
    protected boolean componentNameChanged = true;
    protected boolean performUniqueNameCheck = true;
    protected boolean unique = false;

    protected Combo projectField = null;
    private final SelectionListener projectSelectionListener = new SelectionListener() {
        @Override
        @SuppressWarnings("unchecked")
        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            Combo tmpCboProject = (Combo) e.widget;
            if (tmpCboProject.getData() != null && tmpCboProject.getData() instanceof List) {
                List<IProject> projects = (List<IProject>) tmpCboProject.getData();
                if (Utils.isNotEmpty(projects)) {
                    int selectionIndex = ((Combo) e.widget).getSelectionIndex();
                    IProject selectedProject = projects.get(selectionIndex);
                    if (selectedProject != null) {
                        selectedProjectChanged(selectedProject);
                        validateUserInput();
                    }

                }
            }
        }

        @Override
        public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
            widgetSelected(e);
        }
    };

    //   C O N S T R U C T O R
    protected ComponentWizardPage(ComponentWizard componentWizard) {
        super("Create New " + componentWizard.getComponentTypeDisplayName());
        this.componentWizard = componentWizard;
        setTitle(getWizardTitle());
        setDescription(getWizardDescription());
    }

    //   M E T H O D S
    public ComponentController getComponentController() {
        return componentWizard.getComponentController();
    }

    public ComponentModel getComponentWizardModel() {
        return getComponentController().getComponentWizardModel();
    }

    public Component getComponent() {
        return getComponentController().getComponentWizardModel().getComponent();
    }

    @Override
    public String getPageName() {
        return "Create " + getComponent().getDisplayName();
    }

    @Override
    public String getComponentTypeName() {
        return componentWizard.getComponentTypeDisplayName();
    }

    @Override
    public String getWizardDescription() {
        return "This wizard creates a new " + getComponentTypeName() + ".";
    }

    @Override
    public String getWizardTitle() {
        return "Create New " + getComponentTypeName();
    }

    @Override
    public boolean hasComponentNameChanged() {
        return componentNameChanged;
    }

    @Override
    public void setComponentNameChanged(boolean componentNameChanged) {
        this.componentNameChanged = componentNameChanged;
    }

    @Override
    public final void createControl(Composite parent) {
        final Composite outer = new Composite(parent, SWT.NONE);
        outer.setLayout(new GridLayout());
        createProjectComposite(outer);
        initialize(outer);
        setControl(outer);
        setComplete(false);
    }

    protected final void initialize(Composite parent) {
        clearMessages();

        createComposite(parent);

        UIUtils.setHelpContext((Composite) componentWizardComposite, getClass().getSimpleName());

        setComplete(false);

        additionalInitialize(parent);

        // the new component shortcut does not have paths to exist (NewWizardAction not intended to be subclasses).
        // so we have allow for opening component wizards that are completely disabled when a project is not provided.
        if (getComponentWizardModel().getProject() == null) {
            updateErrorStatus(UIMessages.getString("NewComponent.ProjectRequired.message"));
        }
    }

    public ComponentWizardComposite getBaseComponentWizardComposite() {
        return (ComponentWizardComposite) componentWizardComposite;
    }

    protected void additionalInitialize(Composite parent) {
        setProject(getComponentWizardModel().getProject());
    }

    protected void selectedProjectChanged(final IProject project) {
        getComponentWizardModel().setProject(project);
    }

    protected void createProjectComposite(Composite parent) {
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        group.setLayout(new GridLayout(3, false));
        final CLabel label = new CLabel(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING));
        label.setText("Project:");
        projectField = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        projectField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        projectField.addSelectionListener(projectSelectionListener);

        final List<IProject> projects = getProjects();
        projectField.setData(projects);
        projectField.setEnabled(!projects.isEmpty());
        for (final IProject project : projects) {
            projectField.add(project.getName());
        }
    }

    protected void setProject(final IProject project) {
        if (null == projectField) {
            return;
        }

        @SuppressWarnings("unchecked")
        final List<IProject> projects = (List<IProject>) projectField.getData();
        projectField.select(projects.indexOf(project));
    }

    private static List<IProject> getProjects() {
        final ArrayList<IProject> projects = new ArrayList<>();
        for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) { // TODO: Inject the workspace.
            if (hasDefaultNature(project)) {
                projects.add(project);
            }
        }
        Collections.sort(projects, new Comparator<IProject>() {
            @Override
            public int compare(IProject o1, IProject o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });
        return projects;
    }

    private static boolean hasDefaultNature(final IProject project) {
        try {
            return project.isAccessible() && project.hasNature(DefaultNature.NATURE_ID);
        } catch (final CoreException e) {
            // This should never happen.
            return false;
        }
    }

    protected abstract void createComposite(Composite parent);

    public abstract void saveUserInput() throws InstantiationException, IllegalAccessException;

    protected void refreshObjects() {
        // not implemented
    }

    protected void setControl(IComponentWizardComposite composite) {
        setControl((Control) composite);
    }

    @Override
    public void validateUserInput() {
        if (componentWizard.getContainer() instanceof WizardDialog) {
            if (((WizardDialog) componentWizard.getContainer()).getReturnCode() == Window.CANCEL) {
                return;
            }
        }

        status = null;

        if (!initialDialogChanged(this)) {
            return;
        }

        // not all components have labels
        if (getBaseComponentWizardComposite().getTxtLabel() != null) {
            // label is the friendly name given to the component
            String label = getBaseComponentWizardComposite().getLabelString();
            if (Utils.isEmpty(label)) {
                updateInfoStatus(getComponentDisplayNamePrefixMessage("NewComponent.LabelRequired.message"));
                setComplete(false);
                return;
            }

            else if (label.length() > this.getComponent().getMaxLabelLength()) {
                updateErrorStatus(getComponentDisplayNamePrefixMessage(
                    "NewComponent.LabelLengthExceedsMaximum.message", new String[] { ""
                            + getComponent().getMaxLabelLength() }));
                setComplete(false);
                return;
            }
        }

        // optional, for now, as asian lang's don't require plural
        // W-585810
        // W-586272
        /*if (getBaseComponentWizardComposite().getTxtPluralLabel() != null) {
            // plural label is label value in plural form
            String label = getBaseComponentWizardComposite().getPluralLabelString();
            if (Utils.isEmpty(label)) {
                updateInfoStatus(getComponentDisplayNamePrefixMessage("NewComponent.PluralLabelRequired.message"));
                setPageComplete(false);
                return;
            }
        }*/

        if (getBaseComponentWizardComposite().getTxtName() != null) {
            // name is the filename (developer name)
            String componentName = getBaseComponentWizardComposite().getNameString();
            getComponentWizardModel().setFullName(componentName);

            if (Utils.isEmpty(componentName)) {
                updateInfoStatus(getComponentDisplayNamePrefixMessage("NewComponent.NameRequired.message"));
                setComplete(false);
                return;
            } else if (componentName.length() > getComponent().getMaxNameLength()) {
                updateErrorStatus(getComponentDisplayNamePrefixMessage("NewComponent.NameLengthExceedsMaximum.message",
                    new String[] { "" + getComponent().getMaxNameLength() }));
                setComplete(false);
                return;
            }

            if (isAlphaNumericRequred() && !Utils.isAlphaNumericValid(componentName)) {
                updateErrorStatus(getComponentDisplayNamePrefixMessage("NewComponent.NameAlphaNumeric.message"));
                setComplete(false);
                return;
            }

            if (isAlphaNumericRequred() && Utils.startsWithNumeric(componentName)) {
                updateErrorStatus(getComponentDisplayNamePrefixMessage("NewComponent.NameAlphaNumeric.message"));
                setComplete(false);
                return;
            }

            if (hasComponentNameChanged() && performUniqueNameCheck
                    && !getComponentController().isNameUniqueLocalCheck()) {
                updateErrorStatus(getComponentDisplayNamePrefixMessage("NewComponent.NameMustBeUnique.message",
                    new String[] { componentName }));
                setComplete(false);
                return;
            }

            String fileName = componentName + '.' + getComponent().getFileExtension();
            String metaName = componentName + '.' + getComponent().getMetadataFileExtension();
            IWorkspace workspace = getComponentWizardModel().getProject().getWorkspace();

            boolean nameErrorEncountered = false;

            // Check fileName
            IStatus fileNameStatus = workspace.validateName(fileName, IResource.FILE);
            if (fileNameStatus.getCode() != IStatus.OK) {
                logger.info(String.format("Creating a file name of %s is invalid because: %s", fileName,
                    fileNameStatus.getMessage()));
                nameErrorEncountered = true;
            }

            // Check metaName
            IStatus metaNameStatus = workspace.validateName(metaName, IResource.FILE);
            if (metaNameStatus.getCode() != IStatus.OK) {
                logger.info(String.format("Creating a meta file name of %s is invalid because: %s", fileName,
                    fileNameStatus.getMessage()));
                nameErrorEncountered = true;
            }

            if (nameErrorEncountered) {
                updateErrorStatus(getComponentDisplayNamePrefixMessage("NewComponent.OSFileNameInvalid.message"));
                setComplete(false);
                return;
            }

        }

        if (!finalDialogChanged(this)) {
            return;
        }

        updateInfoStatus(status);

        setComplete(true);
    }

    protected String getComponentDisplayNamePrefixMessage(String tag) {
        return getComponentDisplayNamePrefixMessage(tag, null);
    }

    protected String getComponentDisplayNamePrefixMessage(String tag, String[] params) {
        return getComponentTypeName() + " "
                + (Utils.isNotEmpty(params) ? UIMessages.getString(tag, params) : UIMessages.getString(tag));
    }

    protected abstract boolean isAlphaNumericRequred();

    protected String getStatus() {
        return status;
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    protected abstract boolean initialDialogChanged(IComponentWizardPage componentWizardPage);

    protected abstract boolean finalDialogChanged(IComponentWizardPage componentWizardPage);

    protected boolean nameUniqueCheck() {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Verifying name uniqueness", 3);
                    monitor.subTask("Checking against existing " + getComponent().getDisplayName() + "s...");
                    try {
                        unique = getComponentController().isNameUnique(monitor);
                        monitor.worked(1);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.subTask("Done");
                    }
                }
            });
        } catch (Exception e) {
            logger.warn("Unable to validate name uniqueness for component", e);
            Utils.openError(getShell(), e, true,
                "Unable to verify name uniqueness:\n\n " + ForceExceptionUtils.getRootCauseMessage(e));
        }
        return unique;
    }

    protected void setComplete(boolean complete) {
        pageComplete = complete;
        getComponentController().setCanComplete(complete);
        setPageComplete(complete);
    }

    @Override
    public void clearMessages() {
        updateInfoStatus(null);
        updateErrorStatus(null);
    }

    // U T I L I T I E S
    protected IResource getResource(String filePath) {
        IResource resource = null;
        if (pageComplete) {
            resource = getComponentWizardModel().getProject().findMember(filePath);
        }
        return resource;
    }

}
