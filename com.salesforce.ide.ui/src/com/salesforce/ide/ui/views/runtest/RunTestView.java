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
package com.salesforce.ide.ui.views.runtest;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.IRunTestsResultExt;
import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.salesforce.ide.core.remote.metadata.RunTestsResultExt;
import com.salesforce.ide.ui.internal.utils.DeployResultsViewAssembler;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.views.BaseViewPart;

/**
 * Legacy class
 *
 * @author cwall
 */
public class RunTestView extends BaseViewPart {
    private static final Logger logger = Logger.getLogger(RunTestView.class);

    public static final String VIEW_NAME = "Apex Test Runner";
    public static final String DEBUG_LOG = "Debug Log";
    public static final String DEBUG_USER_LOG = "Debug User Log";
    protected RunTestViewComposite runTestComposite = null;
    protected IResource resource = null;
    protected IProject project = null;
    protected ISelectionListener fPostSelectionListener = null;

    // C O N S T R U C T O R S
    public RunTestView() throws ForceProjectException {
        super();
        setSelectionListener();
    }

    // M E T H O D S
    public void setProject(IProject project) {
        projectChange(project);
    }

    public IProject getProject() {
        return project;
    }

    public RunTestViewComposite getRunTestComposite() {
        return runTestComposite;
    }

    private void setSelectionListener() {
        fPostSelectionListener = new ISelectionListener() {
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                project = getProjectService().getProject(selection);
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    Object selElement = ss.getFirstElement();
                    if (selElement instanceof IResource) {
                        projectChange(((IResource) selElement).getProject());
                    }
                }
            }
        };
    }

    /**
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removeSelectionListener(fPostSelectionListener);
    }

    /**
     *
     * @param project
     */
    public void projectChange(IProject project) {
        this.project = project;
        runTestComposite.enableComposite();
    }

    /**
     *
     */
    @Override
    public void createPartControl(Composite parent) {
        runTestComposite = new RunTestViewComposite(parent, SWT.NONE, this);
        setPartName(VIEW_NAME);
        setTitleImage(getImage());

        UIUtils.setHelpContext(runTestComposite, this.getClass().getSimpleName());
    }

    @Override
    public void setFocus() {
        if (runTestComposite != null) {
            runTestComposite.setFocus();
        }
    }

    public void processRunTestResults(IDeployResultExt deployResult, IResource resource) {
        // set this static variable for re-run
        this.resource = resource;

        //populate result view tree
        DeployResultsViewAssembler assembler =
                new DeployResultsViewAssembler(deployResult, runTestComposite.getTree(), resource.getProject(),
                        serviceLocator.getProjectService());
        assembler.assembleRunTestsResultsTree();

        // set debug logs
        IRunTestsResultExt runTestResults = new RunTestsResultExt(deployResult);
        if (runTestResults.getDebugInfo() != null && Utils.isNotEmpty(runTestResults.getDebugInfo().getDebugLog())) {
            runTestComposite.getTextArea().setText(DEBUG_LOG + ":\n\n" + runTestResults.getDebugInfo().getDebugLog());
            String finalResult = "";
            String debugResult = runTestResults.getDebugInfo().getDebugLog();
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
            runTestComposite.getUserLogsTextArea().setText(DEBUG_USER_LOG + ":\n\n" + finalResult);
        } else {
            runTestComposite.getTextArea().setText("No Debug Logs");
            runTestComposite.getUserLogsTextArea().setText("No Debug Logs");
        }

        if (project != null) {
            runTestComposite.setProject(project);
        }

        runTestComposite.getBtnRun().setEnabled(true);
    }

    public void reRunTests() {
        if (project == null) {
            Utils.openWarn("Project Unknown", "Unable to run tests - project not provided.");
            return;
        }

        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(false, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Running tests...", 4);
                    try {
                        IDeployResultExt results = serviceLocator.getRunTestsService().runTests(resource, monitor);
                        processRunTestResults(results, resource);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.subTask("Done");
                    }
                }
            });
        } catch (InvocationTargetException e) {
            logger.error("Unable to run tests.", ForceExceptionUtils.getRootCause(e));
            Utils.openError(ForceExceptionUtils.getRootCause(e), true, "Unable to run tests.");
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        }
    }

    public void highlightLine(ApexCodeLocation location) {
        if (location == null || location.getFile() == null || !location.getFile().exists()) {
            Utils.openWarn("Highlight Failed", "Unable to highlight test file - file is unknown.");
            return;
        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put(IMarker.LINE_NUMBER, location.getLine());
        try {
            IMarker marker = location.getFile().createMarker(IMarker.TEXT);
            marker.setAttributes(map);
            IDE.openEditor(getSite().getWorkbenchWindow().getActivePage(), marker);
        } catch (Exception e) {
            logger.error("Unable to highlight line.", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to highlight line.");
        }
    }
}
