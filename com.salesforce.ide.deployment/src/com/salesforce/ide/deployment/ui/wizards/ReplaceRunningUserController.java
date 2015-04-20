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
package com.salesforce.ide.deployment.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSet;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.sforce.soap.partner.wsc.QueryResult;

/**
 * This controller facilitate the replacing running user process for selected dashboards during deployment process.
 * TODO: Consider extends from DeploymentController.
 *
 * @author fchang
 */
public class ReplaceRunningUserController extends Controller {
    private static final Logger logger = Logger.getLogger(ReplaceRunningUserController.class);
    private DeploymentComponentSet replaceRunningUserSet;
    private final DeploymentController deploymentController;

    public ReplaceRunningUserController(DeploymentController deploymentController) {
        super();
        this.deploymentController = deploymentController;
    }

    public void execute(Shell deploymentPage) {
        try {
            launchEvaluateRunningUserForDashboards();
        } catch (Exception e) {
            logger.warn("Exception while validating running user for dashboards "
                    + getReplaceRunningUserSet().getLogContent() + ": "
                    + ForceExceptionUtils.getStrippedRootCauseMessage(e));
            Utils.openError(e, true, DeploymentMessages.getString("DeploymentWizard.Validate.RunningUser.error"));
            return;
        }
        generateReplaceRunningUserResultsView(deploymentPage);
    }

    private void launchEvaluateRunningUserForDashboards() throws InvocationTargetException {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(false, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Validating runningUser for dashboard components...", 3);
                    try {
                        evaluateRunningUserForSelectedDashboards(new SubProgressMonitor(monitor, 3));
                        monitor.worked(1);
                    } catch (Exception e) {
                        logger.error("Unable to validating deployment plan", e);
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.subTask("Done");
                    }
                }
            });
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        }
    }

    private void generateReplaceRunningUserResultsView(Shell deploymentPage) {
        if (Utils.isEmpty(getReplaceRunningUserSet())) {
            logger.info("runningUser(s) is available for all to-be-deployed dashboards. No replacement needed!");
            return;
        }
        //open result view
        ReplaceRunningUserViewShell replaceRunningUserViewShell = new ReplaceRunningUserViewShell(deploymentPage, this);
        replaceRunningUserViewShell.open();
    }

    private void evaluateRunningUserForSelectedDashboards(SubProgressMonitor subProgressMonitor)
            throws ForceConnectionException, ForceRemoteException {
        DeploymentComponentSet selectedDashboards =
                deploymentController.getDeploymentPayload().getDeploySelectedComponentsByType(Constants.DASHBOARD);
        replaceRunningUserSet = new DeploymentComponentSet();

        ForceProject destinationOrg = deploymentController.getDeploymentWizardModel().getDestinationOrg();
        Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(destinationOrg);
        for (DeploymentComponent selectedDashboard : selectedDashboards) {
            String currentRunningUser = selectedDashboard.getRunningUser();
            QueryResult result =
                    connection.query("select userName from user where userName='" + currentRunningUser + "'");
            if (Utils.isEmpty(result) || result.getRecords().length == 0) {
                replaceRunningUserSet.add(selectedDashboard);
            }
        }
    }

    /**
     * Validate user input running user is valid then replace all selected dashboards.
     * @param userInputRunningUser
     * @return
     * @throws ForceRemoteException 
     */
    public boolean replaceRunningUserWith(String userInputRunningUser) {
        ForceProject destinationOrg = deploymentController.getDeploymentWizardModel().getDestinationOrg();
        Connection connection;
        QueryResult result = null;
        try {
            connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(destinationOrg);
            result = connection.query("select userName from user where userName='" + userInputRunningUser + "'");
        } catch (Exception e) {
            logger.warn(e);
            return false;
        }
        if (Utils.isEmpty(result) || result.getRecords().length == 0 || Utils.isEmpty(replaceRunningUserSet)) {
            return false;
        }

        final String username = getDestinationOrgUsername();
        for (DeploymentComponent replaceRunningUser : replaceRunningUserSet) {
            final Component component = replaceRunningUser.getComponent();
            String replaced = component.getBody().replaceAll(
                "<runningUser>.*</runningUser>",
                "<runningUser>" + username + "</runningUser>"
            );
            component.setBody(replaced);
        }
        return true;
    }

    public DeploymentComponentSet getReplaceRunningUserSet() {
        return replaceRunningUserSet;
    }

    public String getDestinationOrgUsername() {
        return deploymentController.getDeploymentPayload().getDestinationOrgUsername();
    }

    @Override
    public void dispose() {}

    @Override
    public void finish(IProgressMonitor monitor) throws Exception {}

    @Override
    public void init() throws ForceProjectException {}

}
