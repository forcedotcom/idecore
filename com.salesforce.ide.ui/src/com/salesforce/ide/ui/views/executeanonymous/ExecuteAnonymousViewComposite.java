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
package com.salesforce.ide.ui.views.executeanonymous;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.apex.ExecuteAnonymousResultExt;
import com.salesforce.ide.ui.internal.composite.BaseComposite;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.views.LoggingComposite;

/**
 * Legacy class
 * 
 * @author dcarroll
 */
public class ExecuteAnonymousViewComposite extends BaseComposite {

    protected SashForm sashForm = null;
    protected Composite cmpSource = null;
    protected Button btnExecute = null;
    protected StyledText txtSourceInput = null;
    protected StyledText txtResult = null;
    protected Composite projectAndLoggingContainerComposite = null;
    protected Composite projectComposite = null;
    protected LoggingComposite loggingComposite = null;
    protected Combo projectCombo = null;
    protected ExecuteAnonymousController executeAnonymousController = null;
    protected StyledText txtUserDebugLogs = null;
    private static final int DEFAULT_PROJ_SELECTION = 0;
    protected IResourceChangeListener resourceListener = null;
    protected IProject selectedProject = null;
    private static final Logger logger = Logger.getLogger(ExecuteAnonymousView.class);

    Color color = new Color(Display.getCurrent(), 240, 240, 240);

    public ExecuteAnonymousViewComposite(Composite parent, int style,
            ExecuteAnonymousController executeAnonymousController) {
        super(parent, style);
        this.executeAnonymousController = executeAnonymousController;

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                color.dispose();
            }
        });
        initialize();
    }

    public void enableComposite(boolean enable) {
        if (txtSourceInput != null) {
            txtSourceInput.setEnabled(enable);
        }

        if (loggingComposite != null) {
            loggingComposite.enable(enable);
        }
    }

    protected void initialize() {
        GridLayout gridLayout = new GridLayout();
        setLayout(gridLayout);
        setSize(new Point(566, 1200));

        createLoggingComposite();
        createInputAndOutputComposite();

        loadProjects();
        setActiveProject(executeAnonymousController.getProject());
    }

    private void createInputAndOutputComposite() {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.heightHint = 650;

        sashForm = new SashForm(this, SWT.BORDER | SWT.HORIZONTAL);
        sashForm.setSashWidth(5);
        sashForm.setLayoutData(gridData);
        createSourceComposite(sashForm);

        Composite composite = new Composite(sashForm, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        createResultComposite(composite);
        createUserLogsComposite(composite);
    }

    private void createLoggingComposite() {
        projectAndLoggingContainerComposite = new Composite(this, SWT.NONE);
        projectAndLoggingContainerComposite.setLayout(new GridLayout(3, false));
        createProjectComposite(projectAndLoggingContainerComposite);
        loggingComposite =
                new LoggingComposite(projectAndLoggingContainerComposite,
                        ContainerDelegate.getInstance().getServiceLocator().getLoggingService(), SWT.NONE, false,
                        LoggingInfo.SupportedFeatureEnum.ExecuteAnonymous);
    }

    protected void createSourceComposite(Composite parent) {
        cmpSource = new Composite(parent, SWT.NONE);
        cmpSource.setLayout(new GridLayout(2, false));

        // Source to execute: label
        CLabel lblSource = new CLabel(cmpSource, SWT.NONE);
        lblSource.setText("Source to execute:");
        lblSource.setLayoutData(new GridData(GridData.CENTER));

        // Execute Anonymous button
        btnExecute = new Button(cmpSource, SWT.NONE);
        btnExecute.setText("Execute Anonymous");
        btnExecute.setEnabled(false);
        btnExecute.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        btnExecute.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                executeExecuteAnonymous();
            }
        });

        // Apex input text field        
        txtSourceInput = new StyledText(cmpSource, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        txtSourceInput.setEnabled(false);
        txtSourceInput.setLayoutData(getInputResultsGridData(2));
        txtSourceInput.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (txtSourceInput != null && btnExecute != null) {
                    btnExecute.setEnabled(Utils.isNotEmpty(txtSourceInput.getText()));
                }
            }
        });

    }

    private GridData getInputResultsGridData(int span) {
        Rectangle rect = UIUtils.getClientArea(getShell());

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = span;
        gridData.heightHint = (int) (rect.height * .4);
        gridData.verticalAlignment = GridData.FILL;
        return gridData;
    }

    public void executeExecuteAnonymous() {
        if (executeAnonymousController.getProject() == null) {
            Utils.openError("No Project Selected", "Please select a project from which to execute anonymous.");
            return;
        }

        txtResult.setText("Executing code...");
        txtUserDebugLogs.setText("Executing code...");
        txtResult.update();
        txtUserDebugLogs.update();

        final String code = txtSourceInput.getText();
        // Execute the code in a different thread to allow debugging (since DBGP takes up the main thread)
        Job job = new Job("Execute-Anonymous") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                ExecuteAnonymousResultExt result = executeAnonymousController.executeExecuteAnonymous(code);
                handleExecuteResults(result);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    protected void createResultComposite(Composite parent) {
        CLabel lblResult = new CLabel(parent, SWT.NONE);
        lblResult.setLayoutData(new GridData(GridData.BEGINNING));
        lblResult.setText("Results:");

        txtResult = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);
        txtResult.setBackground(color);
        txtResult.setLayoutData(getInputResultsGridData(1));
    }
    
    private void createUserLogsComposite(Composite parent) {
        CLabel lblResult = new CLabel(parent, SWT.NONE);
        lblResult.setLayoutData(new GridData(GridData.BEGINNING));
        lblResult.setText("User Debug Logs:");

        txtUserDebugLogs = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);
        txtUserDebugLogs.setBackground(color);
        txtUserDebugLogs.setLayoutData(getInputResultsGridData(1));
    }

    protected void createProjectComposite(Composite cmpSource) {
        projectComposite = new Composite(cmpSource, SWT.NONE);
        projectComposite.setLayoutData(new GridData(SWT.BEGINNING));
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        projectComposite.setLayout(gridLayout);

        CLabel lblProject = new CLabel(projectComposite, SWT.NONE);
        lblProject.setLayoutData(new GridData(SWT.BEGINNING));
        lblProject.setText("Active Project:");

        projectCombo = new Combo(projectComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        projectCombo.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                Combo tmpCboProject = (Combo) e.widget;
                if (tmpCboProject.getData() != null && tmpCboProject.getData() instanceof List) {
                    List<IProject> projects = (List<IProject>) tmpCboProject.getData();
                    if (Utils.isNotEmpty(projects)) {
                        int selectionIndex = ((Combo) e.widget).getSelectionIndex();
                        // Save selected project
                        selectedProject = projects.get(selectionIndex);
                        if (selectedProject != null) {
                            setActiveProject(selectedProject);
                        }

                    }
                }
            }

            @Override
            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    /**
     * Update the combo box and ExecuteAnonymousController
     * with the selected project
     * @param project
     */
    public void setActiveProject(IProject project) {
        if (project == null) {
            project = getFirstProject();
        }

        setSelectedProjectCombo(project);

        executeAnonymousController.setProject(project);

        if (loggingComposite != null) {
            loggingComposite.setProject(project);
        }

        if (project != null && project.getName().equals(projectCombo.getText())) {
            enableComposite(true);
        } else {
            enableComposite(false);
        }
    }

    @SuppressWarnings("unchecked")
    private IProject getFirstProject() {
        IProject firstProject = null;
        if (projectCombo.getData() != null && projectCombo.getData() instanceof List) {
            List<IProject> projects = (List<IProject>) projectCombo.getData();
            firstProject = Utils.isNotEmpty(projects) ? projects.get(0) : null;
        }
        return firstProject;
    }

    public void loadProjects() {
        if (executeAnonymousController == null || projectCombo == null) {
            return;
        }

        List<IProject> projects = executeAnonymousController.getForceProjects();
        if (Utils.isNotEmpty(projects)) {
            loadProjects(projects);
        } else {
            if (projectCombo.getItemCount() > 0)
                projectCombo.removeAll();

            projectCombo.setData(null);
            projectCombo.setEnabled(false);
            enableComposite(false);
        }

        layout(true, true);
        
        final IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) throws CoreException {
                IResource res = delta.getResource();
                if (res instanceof IProject) {
                    final IProject project = (IProject)res;
                    switch(delta.getKind()) {
                    case IResourceDelta.ADDED:
                        if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isForceProject(project)) {
                            updateProjectComboProjectedAdded();
                        }
                        break;
                    case IResourceDelta.REMOVED:
                        updateProjectComboProjectRemoved(project);
                        break;
                    case IResourceDelta.CHANGED:
                        if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isForceProject(project)) {
                            updateProjectComboProjectedAdded();
                        } else {
                            updateProjectComboProjectRemoved(project);
                        }
                        break;
                    }
                }

                return true;
            }
        };
        
        resourceListener = new IResourceChangeListener() {
            @Override
            public void resourceChanged(IResourceChangeEvent event) {
                switch (event.getType()) {
                case IResourceChangeEvent.POST_CHANGE:
                    try {
                        event.getDelta().accept(deltaVisitor);
                    } catch (CoreException e) {
                        String logMessage = Utils.generateCoreExceptionLog(e);
                        logger.warn("Unable to process: " + logMessage);
                    }
                    break;
                default:
                    break;

                }
            }
        };
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
    }

    private void loadProjects(List<IProject> projects) {
        if (projectCombo.getItemCount() > 0)
        	projectCombo.removeAll();
        
        projectCombo.setData(projects);
        Collections.sort(projects, new Comparator<IProject>() {
            @Override
            public int compare(IProject o1, IProject o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });

        for (IProject project : projects) {
            projectCombo.add(project.getName());
        }

        // Stay on the selected project even though we've
        // updated the list of active projects
        setActiveProject(selectedProject);
        projectCombo.setEnabled(true);
        
        // Sets the width of the combo box to the width of the longest string
        // Layouts everything
        projectAndLoggingContainerComposite.pack();
        projectAndLoggingContainerComposite.layout(true,true);
    }

    /**
     * Remove the project from the combo box and 
     * select first item in project list.
     */
    private void updateProjectComboProjectRemoved(final IProject project) {
        UIJob job = new UIJob("Update Exec Anon Projects Combo") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (projectCombo.getItemCount() > 0 && ArrayUtils.contains(projectCombo.getItems(), project.getName())) {
                	/*
                	 * When user selects a project in workspace to remove,
                	 * the combo box is updated to that project. So, after
                	 * we actually remove that project, we should
                	 * select the first item in the project combo. If there
                	 * is none, then disable the composite.
                	 */
                    projectCombo.remove(project.getName());
                    if (projectCombo.getItemCount() > 0) {
                        projectCombo.select(DEFAULT_PROJ_SELECTION);
                    } else {
                        projectCombo.setData(null);
                        projectCombo.setEnabled(false);
                        enableComposite(false);
                    }
                }
                return new Status(Status.OK, ForceIdeCorePlugin.PLUGIN_ID, "Successfully updated projects combo");
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    /**
     * Refresh the list of active projects, update
     * the combo box, and stay on the already selected
     * project
     */
    private void updateProjectComboProjectedAdded() {
        UIJob job = new UIJob("Update Exec Anon Projects Combo") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                List<IProject> projects = executeAnonymousController.getForceProjects();
                loadProjects(projects);
                return new Status(Status.OK, ForceIdeCorePlugin.PLUGIN_ID, "Successfully updated projects combo");
            }
        };
        job.setSystem(true);
        job.schedule();
    }  
    
    private void setSelectedProjectCombo(IProject selectedProject) {
        if (projectCombo != null && Utils.isNotEmpty(selectedProject)) {
            selectComboContent(selectedProject.getName(), projectCombo);
        } else {
            projectCombo.select(DEFAULT_PROJ_SELECTION);
        }
    }

    private void handleExecuteResults(final ExecuteAnonymousResultExt executeAnonymousResult) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (executeAnonymousResult.getCompiled()) {
                    if (executeAnonymousResult.getSuccess()) {
                        txtResult.setText("Anonymous execution was successful.\n\n");
                        txtUserDebugLogs.setText("Anonymous execution was successful.\n\n");
                        if (executeAnonymousResult.getDebugInfo() != null) {
                            String finalResult = "";
                            String debugResult = executeAnonymousResult.getDebugInfo().getDebugLog();
                            if (debugResult.contains("DEBUG")) {
                                String[] newDateWithSperators = debugResult.split("\\|");
                                for (int index = 0; index < newDateWithSperators.length; index++) {
                                    String newDateWithSperator = newDateWithSperators[index];
                                    if (newDateWithSperator.contains("USER_DEBUG")) {
                                        String debugData = newDateWithSperators[index + 3];
                                        debugData = debugData.substring(0, debugData.lastIndexOf('\n'));
                                        finalResult += "\n" + debugData + "\n";
                                    }

                                }
                            }
                            txtResult.setText(txtResult.getText() + executeAnonymousResult.getDebugInfo().getDebugLog());
                            txtUserDebugLogs.setText(finalResult);
                        }
                    } else {
                        StringBuffer errorMessage = new StringBuffer("DEBUG LOG\n");
                        if (executeAnonymousResult.getDebugInfo() != null) {
                            errorMessage.append(executeAnonymousResult.getDebugInfo().getDebugLog());
                        }
                        txtResult.setText(errorMessage.toString());
                        txtUserDebugLogs.setText(errorMessage.toString());
                    }
                } else {
                    StringBuilder strBuilder = new StringBuilder("Compile error at line ");
                    strBuilder.append(executeAnonymousResult.getLine()).append(" column ")
                            .append(executeAnonymousResult.getColumn()).append("\n")
                            .append(executeAnonymousResult.getCompileProblem());
                    txtResult.setText(strBuilder.toString());
                    txtUserDebugLogs.setText(strBuilder.toString());
                }
            }
        });
    }

    @Override
    public void validateUserInput() {}
    
    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
        super.dispose();
    }

}