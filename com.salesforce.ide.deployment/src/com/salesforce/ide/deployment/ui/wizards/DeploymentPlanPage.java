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
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSet;
import com.salesforce.ide.deployment.internal.DeploymentPayload;
import com.salesforce.ide.deployment.internal.DeploymentResult;
import com.salesforce.ide.deployment.internal.DeploymentSummary;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 *
 * 
 * @author cwall
 */
public class DeploymentPlanPage extends BaseDeploymentPage {

    private static final Logger logger = Logger.getLogger(DeploymentPlanPage.class);

    protected final Color GREEN = new Color(Display.getCurrent(), 102, 204, 0);
    protected final Color YELLOW = new Color(Display.getCurrent(), 255, 255, 51);
    protected final Color RED = new Color(Display.getCurrent(), 255, 51, 0);
    protected final Color GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    private static final String WIZARDPAGE_ID = "deploymentPlanWizardPage";

    DeploymentPlanComposite deploymentPlanComposite;

    public DeploymentPlanPage(DeploymentWizard deploymentWizard) {
        super(WIZARDPAGE_ID, deploymentWizard);
    }

    @Override
    public void createControl(Composite parent) {
        deploymentPlanComposite = new DeploymentPlanComposite(parent, SWT.NULL, this);
        setControl(deploymentPlanComposite);
        deploymentPlanComposite.pack();
        setPageComplete(true);

        UIUtils.setHelpContext(deploymentPlanComposite, this.getClass().getSimpleName());
    }

    /**
     * Prepare content and finalizes view.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            loadDeploymentView();
            setTitleAndDescription(DeploymentMessages.getString("DeploymentWizard.PlanPage.title") + " "
                    + getStepString(), DeploymentMessages.getString("DeploymentWizard.PlanPage.description"));
        }
        super.setVisible(visible);
    }

    public void reloadDeploymentView() {
        loadDeploymentView();
    }

    private void loadDeploymentView() {
        try {
            DeploymentController deploymentWizardController = getController();
            performDestinationComparison(deploymentWizardController);
            // only load payload on initial pass or reload if existing payload is empty
            // REVIEWME: we're taking an optimistic - changes on the destination server may change (need to push for locking)
            if (!deploymentWizardController.isDeploymentPayloadEmpty()) {
                finalizeViewComposite(deploymentWizardController);
            } else {
                Utils.openInfo("No Deployment Candidates Found",
                    "No permissible deployment candidates were found.  Ensure that the destination organization "
                            + " has adequate object type permissions.");
                clearTable();
                clearSummaryLabel();
            }
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                InsufficientPermissionsException insuffPerms = (InsufficientPermissionsException) cause;
                insuffPerms.setShowUpdateCredentialsMessage(false);
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(insuffPerms);
            } else {
                logger.error(DeploymentMessages.getString("DeploymentWizard.PlanPage.GeneratePlan.error"),
                    ForceExceptionUtils.getRootCause(e));
                Utils.openError(ForceExceptionUtils.getRootCause(e), true, DeploymentMessages
                    .getString("DeploymentWizard.PlanPage.GeneratePlan.error"));
            }
            setEnableButton();
        }
    }

    // compares destination artifacts with deployment candidates
    private static void performDestinationComparison(final DeploymentController deploymentWizardController)
            throws InvocationTargetException, InterruptedException {

        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(false, true, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask(DeploymentMessages.getString("DeploymentWizard.GeneratePayload.message"), 2);
                monitor.worked(1);
                try {
                    deploymentWizardController.generateDeploymentPayload(new SubProgressMonitor(monitor, 2));
                    monitor.worked(1);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.subTask("Done");
                }
            }
        });
    }

    // sets and adjusts table content - row colors, check state, etc - and sets listeners to handle user input
    private void finalizeViewComposite(DeploymentController deploymentWizardController) {
        final DeploymentPayload deploymentPayload = deploymentWizardController.getDeploymentPayload();

        // table input display generated deployment component candidate list
        clearTable();
        deploymentPlanComposite.getTblViewer().setInput(deploymentPayload);

        // tooltip listener
        Table tblDeploySelection = deploymentPlanComposite.getTable();
        tblDeploySelection.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TableItem selectedItem = (TableItem) event.item;
                String name = selectedItem.getText(DeploymentPlanComposite.NAME_COLUMN);
                String packageName = selectedItem.getText(DeploymentPlanComposite.PACKAGE_COLUMN);
                String type = selectedItem.getText(DeploymentPlanComposite.TYPE_COLUMN);

                if (Utils.isNotEmpty(name) && name.contains(Constants.FOWARD_SLASH)) {
                    name = name.substring(name.lastIndexOf(Constants.FOWARD_SLASH) + 1);
                }

                DeploymentComponent deploymentComponent =
                        deploymentPayload.getDeploymentComponent(name, packageName, type);
                if (deploymentComponent == null) {
                    deploymentPlanComposite.resetLblActionTooltip("No action description available.");
                } else {
                    // set tooltip summary
                    deploymentPlanComposite.resetLblActionTooltip(deploymentComponent.getDestinationSummary()
                        .getActionDescription());
                }
                // doing this again here because addCheckStateListener isn't
                setEnableButton();
            }
        });

        // auto-select associated deployment component listener
        CheckboxTableViewer tblViewer = deploymentPlanComposite.getTblViewer();
        tblViewer.addCheckStateListener(new FolderCheckStateListener(deploymentPayload));

        // set initial check/uncheck and esthetics
        TableItem[] tblItems = tblDeploySelection.getItems();
        for (int i = 0; i < tblItems.length; i++) {
            DeploymentComponent deploymentComponent = (DeploymentComponent) tblItems[i].getData();
            tblItems[i].setChecked(deploymentComponent.isDeploy());
            setTableEsthetics(deploymentComponent, tblItems[i]);
        }

        // test if checked-elements == 0, if so disable test run button
        setEnableButton();
    }

    public void setTableEsthetics(DeploymentComponent deploymentComponent, TableItem tblItem) {
        String initColor = deploymentComponent.getDestinationSummary().getInitColor();
        if (initColor.equals(Constants.GREEN)) {
            tblItem.setBackground(GREEN);
        } else if (initColor.equals(Constants.YELLOW)) {
            tblItem.setBackground(YELLOW);
        } else if (initColor.equals(Constants.RED)) {
            tblItem.setBackground(RED);
        } else {
            tblItem.setBackground(GRAY);
        }

        if (deploymentComponent.getDestinationSummary().getAction().equals(Constants.NO_ACTION)) {
            tblItem.setGrayed(true);
        }
    }

    // disable wizard buttons if all elements are unchecked
    protected void setEnableButton() {
        Object[] checkedElements = deploymentPlanComposite.getTblViewer().getCheckedElements();
        if (Utils.isEmpty(checkedElements)) {
            deploymentPlanComposite.setButtonEnablement(false);
            setEnableNext(false);
        } else {
            Table tblDeploySelection = deploymentPlanComposite.getTable();
            TableItem[] tblItems = tblDeploySelection.getItems();
            for (TableItem tableItem : tblItems) {
                if (tableItem.getChecked()
                        && DeploymentSummary.isDeployableAction(((DeploymentComponent) tableItem.getData())
                            .getDestinationSummary())) {
                    deploymentPlanComposite.setButtonEnablement(true);
                    setEnableNext(true);
                    break;
                }
            }
        }
    }

    public void setDeploySelectionForAll(boolean enabled) {
        DeploymentPayload deploymentPayload = getController().getDeploymentPayload();
        if (deploymentPayload != null) {
            deploymentPayload.enableDeploymentAll(enabled);
        }
    }

    private void setDeploySelections() {
        Object[] selectedDeploymentComponents = deploymentPlanComposite.getTblViewer().getCheckedElements();
        if (Utils.isEmpty(selectedDeploymentComponents)) {
            logger.warn("No deployment candidates selected");
            return;
        }

        // reset deploy flag
        DeploymentComponentSet deploymentComponents = getController().getDeploymentPayload().getDeploymentComponents();
        for (DeploymentComponent deploymentComponent : deploymentComponents) {
            deploymentComponent.setDeploy(false);
        }

        // set deploy flag to true for all selected components
        for (Object selectedDeploymentComponent : selectedDeploymentComponents) {
            if (selectedDeploymentComponent == null) {
                logger.warn("Selected deployment candidate is null");
                continue;
            }
            ((DeploymentComponent) selectedDeploymentComponent).setDeploy(true);
        }
    }

    public void setEnableNext(boolean next) {
        setPageComplete(next);
    }

    // tests deployment
    public void testDeployment() {
        // set deploy selections
        setDeploySelections();

        final DeploymentController deploymentWizardController = getController();
        if (deploymentWizardController.getDeploymentPayload().getDeploySelectedCount() == 0) {
            Utils.openInfo("No Components Selected", "Please select at least one component to be deployed.");
            return;
        }

        try {
            // NOTE: this feature is disabled until priority is restored.
            // W-572143

            // execute test deployment
            testDeployOperation();

            generateResultsView(deploymentWizardController);
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                InsufficientPermissionsException insuffPerms = (InsufficientPermissionsException) cause;
                insuffPerms.setShowUpdateCredentialsMessage(false);
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(insuffPerms);
            } else {
                DeploymentWizardModel deploymentWizardModel = deploymentWizardController.getDeploymentWizardModel();
                logger.warn("Exception while validation deployment plan for connection "
                        + deploymentWizardModel.getDestinationOrg().getLogDisplay() + ": "
                        + ForceExceptionUtils.getStrippedRootCauseMessage(e));
                Utils.openError(e, true, DeploymentMessages.getString("DeploymentWizard.PlanPage.TestDeploy.error"));
            }
        }
    }

    private void testDeployOperation() throws InvocationTargetException, InterruptedException {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(false, true, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Validating deployment plan...", 3);
                monitor.worked(1);

                final DeploymentController deploymentWizardController = getController();

                try {
                    deploymentWizardController.testDeploy(new SubProgressMonitor(monitor, 3));
                    monitor.worked(2);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    logger.error("Unable to validating deployment plan", e);
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.subTask("Done");
                }
            }
        });
    }

    private void generateResultsView(DeploymentController deploymentWizardController) {
        DeploymentResult deploymentResult = deploymentWizardController.getDeploymentResult();
        if (deploymentResult == null) {
            logger.warn("Unable to generate results view.  Deployment result is null");
            return;
        }

        try {
            ResultsViewShell resultsView =
                    new ResultsViewShell(getShell(), deploymentResult.getDeployLog(), deploymentResult
                        .getRemoteDeployLog(), deploymentWizardController.getDeploymentWizardModel()
                        .getProjectName());

            prepareResultsViewComposite(resultsView.getResultsComposite(), deploymentWizardController);

            // opens log result shell
            resultsView.open();
        } catch (Exception e) {
            DeploymentWizardModel deploymentWizardModel = deploymentWizardController.getDeploymentWizardModel();
            logger.error("Unable display deployment validation results to "
                    + deploymentWizardModel.getDestinationOrg().getLogDisplay(), e);
            Utils.openError(e, true, DeploymentMessages
                .getString("DeploymentWizard.PlanPage.DisplayTestDeployResults.error"));
        }
    }

    @Override
    public IWizardPage getNextPage() {
        return deploymentWizard.getNextPage(this);
    }

    @Override
    public boolean canFlipToNextPage() {
        return (isPageComplete() && getNextPage() != null && (!getController().isDeploymentPayloadEmpty()));
    }

    private void clearTable() {
        if (deploymentPlanComposite != null && deploymentPlanComposite.getTblViewer() != null) {
            deploymentPlanComposite.getTblViewer().getTable().removeAll();
        }
    }

    private void clearSummaryLabel() {
        if (deploymentPlanComposite != null) {
            deploymentPlanComposite.clearSummaryLabel();
        }
    }

    // checkbox listener
    class FolderCheckStateListener implements ICheckStateListener {

        private DeploymentPayload deploymentPayload = null;

        public FolderCheckStateListener(DeploymentPayload deploymentPayload) {
            this.deploymentPayload = deploymentPayload;
        }

        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            DeploymentComponent selectedDeploymentComponent = (DeploymentComponent) event.getElement();
            if (selectedDeploymentComponent == null || deploymentPayload == null) {
                return;
            }

            // select associated/dependent components too
            Component component = selectedDeploymentComponent.getComponent();
            if (component != null && component.isWithinFolder()) {
                if (!event.getChecked()
                        && selectedDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.DELETED)) {
                    handleUnCheckedSubFolderComponent(selectedDeploymentComponent);
                } else if (event.getChecked()
                        && selectedDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.NEW)) {
                    handleCheckedSubFolderComponent(selectedDeploymentComponent);
                }
            } else if (component != null && Constants.FOLDER.equals(component.getComponentType())) {
                if (!event.getChecked()) {
                    if (selectedDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.NEW)) {
                        handleUncheckedFolderComponent(selectedDeploymentComponent);
                    }
                } else if (event.getChecked()
                        && selectedDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.DELETED)) {
                    handleCheckedFolderComponent(selectedDeploymentComponent);
                }
            }
        }

        private void handleCheckedSubFolderComponent(DeploymentComponent selectedDeploymentComponent) {
            Component component = selectedDeploymentComponent.getComponent();

            // get component's folder
            String folderName = getFolderName(component);
            if (Utils.isEmpty(folderName)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to get folder for folder-base component " + component.getFullDisplayName());
                }
                return;
            }

            DeploymentComponent folderDeploymentComponent =
                    deploymentPayload.getDeploymentComponent(folderName, component.getPackageName(), Constants.FOLDER);

            if (folderDeploymentComponent == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to get deployment component for folder '" + folderName + "'");
                }
                return;
            }

            // select that guy
            Table tblDeploySelection = deploymentPlanComposite.getTable();
            TableItem[] tblItems = tblDeploySelection.getItems();
            for (TableItem tableItem : tblItems) {
                DeploymentComponent tmpDeploymentComponent = (DeploymentComponent) tableItem.getData();
                if (tmpDeploymentComponent.equals(folderDeploymentComponent)
                        && tmpDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.NEW)) {
                    tableItem.setChecked(true);
                    break;
                }
            }
        }

        private void handleUnCheckedSubFolderComponent(DeploymentComponent selectedDeploymentComponent) {
            Component component = selectedDeploymentComponent.getComponent();

            // get component's folder
            String folderName = getFolderName(component);
            if (Utils.isEmpty(folderName)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to get folder for folder-base component " + component.getFullDisplayName());
                }
                return;
            }

            DeploymentComponent folderDeploymentComponent =
                    deploymentPayload.getDeploymentComponent(folderName, component.getPackageName(), Constants.FOLDER);

            if (folderDeploymentComponent == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to get deployment component for folder '" + folderName + "'");
                }
                return;
            }

            // select that guy
            Table tblDeploySelection = deploymentPlanComposite.getTable();
            TableItem[] tblItems = tblDeploySelection.getItems();
            for (TableItem tableItem : tblItems) {
                DeploymentComponent tmpDeploymentComponent = (DeploymentComponent) tableItem.getData();
                if (tmpDeploymentComponent.equals(folderDeploymentComponent)
                        && tmpDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.DELETED)) {
                    if (tableItem.getChecked()) {
                        for (TableItem tableItem2 : tblItems) {
                            DeploymentComponent tmp2DeploymentComponent = (DeploymentComponent) tableItem2.getData();
                            if (tmp2DeploymentComponent.equals(selectedDeploymentComponent)
                                    && selectedDeploymentComponent.getDestinationSummary().equals(
                                        DeploymentSummary.DELETED)) {
                                tableItem2.setChecked(true);
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        private void handleUncheckedFolderComponent(DeploymentComponent deselectedDeploymentComponent) {
            Component component = deselectedDeploymentComponent.getComponent();

            String folderName = getFolderName(component);
            if (Utils.isEmpty(folderName)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to get folder for folder-base component " + component.getFullDisplayName());
                }
                return;
            }

            DeploymentComponentSet folderTypeDeploymentComponentSet = null;
            if (Utils.isNotEmpty(component.getSecondaryComponentType())) {
                folderTypeDeploymentComponentSet =
                        deploymentPayload.getDeploymentComponentsByType(component.getSecondaryComponentType());
            }

            if (null == folderTypeDeploymentComponentSet || folderTypeDeploymentComponentSet.isEmpty()) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to find subfolder components for folder '" + folderName + "'");
                }
                return;
            }

            // select that guy
            Table tblDeploySelection = deploymentPlanComposite.getTable();
            TableItem[] tblItems = tblDeploySelection.getItems();
            for (TableItem tableItem : tblItems) {
                if (!tableItem.getChecked()) {
                    continue;
                }

                DeploymentComponent tmpDeploymentComponent = (DeploymentComponent) tableItem.getData();
                for (Iterator<DeploymentComponent> iterator = folderTypeDeploymentComponentSet.iterator(); iterator
                        .hasNext();) {
                    DeploymentComponent folderDeploymentComponent = iterator.next();
                    Component tmpComponent = folderDeploymentComponent.getComponent();
                    if (folderName.equals(tmpComponent.getParentFolderNameIfComponentMustBeInFolder())
                            && tmpDeploymentComponent.equals(folderDeploymentComponent)
                            && tmpDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.NEW)) {
                        tableItem.setChecked(false);
                        tmpDeploymentComponent.setDeploy(false);
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        private void handleCheckedFolderComponent(DeploymentComponent selectedDeploymentComponent) {
            Component component = selectedDeploymentComponent.getComponent();

            String folderName = getFolderName(component);
            if (Utils.isEmpty(folderName)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to get folder for folder-base component " + component.getFullDisplayName());
                }
                return;
            }

            DeploymentComponentSet folderTypeDeploymentComponentSet = null;
            if (Utils.isNotEmpty(component.getSecondaryComponentType())) {
                folderTypeDeploymentComponentSet =
                        deploymentPayload.getDeploymentComponentsByType(component.getSecondaryComponentType());
            }

            if (null == folderTypeDeploymentComponentSet || folderTypeDeploymentComponentSet.isEmpty()) {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to find subfolder components for folder '" + folderName + "'");
                }
                return;
            }

            // select that guy
            Table tblDeploySelection = deploymentPlanComposite.getTable();
            TableItem[] tblItems = tblDeploySelection.getItems();
            for (TableItem tableItem : tblItems) {
                DeploymentComponent tmpDeploymentComponent = (DeploymentComponent) tableItem.getData();
                for (Iterator<DeploymentComponent> iterator = folderTypeDeploymentComponentSet.iterator(); iterator
                        .hasNext();) {
                    DeploymentComponent folderDeploymentComponent = iterator.next();
                    Component tmpComponent = folderDeploymentComponent.getComponent();
                    if (folderName.equals(tmpComponent.getParentFolderNameIfComponentMustBeInFolder())
                            && tmpDeploymentComponent.equals(folderDeploymentComponent)) {
                        if (tmpDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.NEW)) {
                            tableItem.setChecked(false);
                            tmpDeploymentComponent.setDeploy(false);
                            iterator.remove();
                            break;
                        } else if (tmpDeploymentComponent.getDestinationSummary().equals(DeploymentSummary.DELETED)) {
                            tableItem.setChecked(true);
                            tmpDeploymentComponent.setDeploy(true);
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        }

        private String getFolderName(Component component) {
            String folderName = component.getParentFolderNameIfComponentMustBeInFolder();
            if (Utils.isEmpty(folderName) && Utils.isEmpty(component.getMetadataFilePath())
                    && component.getMetadataFilePath().contains("/")) {
                String[] filePathParts = component.getMetadataFilePath().split("/");
                if (filePathParts.length > 2) {
                    folderName = filePathParts[1];
                }
            }
            return folderName;
        }
    }

    @Override
    public void dispose() {
        GREEN.dispose();
        YELLOW.dispose();
        RED.dispose();
        super.dispose();
    }
}
