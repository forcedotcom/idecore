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
package com.salesforce.ide.ui.internal.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DeployMessageExtractor;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.remote.ICodeCoverageResultExt;
import com.salesforce.ide.core.remote.ICodeLocationExt;
import com.salesforce.ide.core.remote.IRunTestsResultExt;
import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.salesforce.ide.core.remote.metadata.RunTestsResultExt;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.ui.internal.ForceImages;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.RunTestFailure;

public class DeployResultsViewAssembler {
    private static final Logger logger = Logger.getLogger(DeployResultsViewAssembler.class);

    protected IRunTestsResultExt runTestResult = null;
    protected IDeployResultExt deployResultHandler = null;
    protected Tree resultsTree = null;
    protected IProject project = null;
    private final Image failureIcon = ForceImages.get(ForceImages.IMAGE_FAILURE);
    private final Image warningIcon = ForceImages.get(ForceImages.IMAGE_WARNING);
    private final Image confirmIcon = ForceImages.get(ForceImages.IMAGE_CONFIRM);

    private final ProjectService projectService;

    //    C O N S T R U C T O R S
    public DeployResultsViewAssembler(IDeployResultExt deployResultHandler, Tree resultsTree, IProject project,
            ProjectService projectService) {
        this.deployResultHandler = deployResultHandler;
        this.runTestResult = new RunTestsResultExt(deployResultHandler.getRunTestsResult());
        this.resultsTree = resultsTree;
        this.project = project;
        this.projectService = projectService;
    }

    //   M E T H O D S
    public void assembleDeployResultsTree() {
        if (resultsTree == null || deployResultHandler == null) {
            throw new IllegalArgumentException("Deploy results and/or result tree cannot be null");
        }
        resultsTree.removeAll();
        handleFailureAndWarningResults();
        handleCodeCoverageResults();

        if (resultsTree.getItemCount() == 0) {
            TreeItem noResultsTreeItem = new TreeItem(resultsTree, SWT.NONE);
            setNoResultsText(noResultsTreeItem);
            noResultsTreeItem.setImage(confirmIcon);
        }
    }

    protected void setNoResultsText(TreeItem noResultsTreeItem) {
        if (deployResultHandler != null) {
            noResultsTreeItem.setText(UIMessages.getString("Deployment.ResultsView.NoResults.message"));
        } else {
            noResultsTreeItem.setText(UIMessages.getString("RunTestsHandler.ResultsView.NoResults.message"));
        }
    }

    public void assembleRunTestsResultsTree() {
        if (resultsTree == null || runTestResult == null) {
            throw new IllegalArgumentException("Run test results and/or result tree cannot be null");
        }

        resultsTree.removeAll();
        handleFailureAndWarningResults();
        handleCodeCoverageResults();
    }

    protected void handleFailureAndWarningResults() {
        DeployMessageExtractor extractor = new DeployMessageExtractor(deployResultHandler);

        FontRegistry registry = new FontRegistry();
        Font boldFont = registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());

        Collection<DeployMessage> deploySuccesses = extractor.getDeploySuccesses();
        handleDeploySuccessMessages(deploySuccesses);

        Collection<DeployMessage> deployFailures = extractor.getDeployFailures();
        Collection<RunTestFailure> testFailures = extractor.getTestFailures();
        if (!deployFailures.isEmpty() || !testFailures.isEmpty()) {
            TreeItem rootFailureTreeItem = new TreeItem(resultsTree, SWT.NONE);
            rootFailureTreeItem.setText("Failures");
            rootFailureTreeItem.setImage(failureIcon);
            rootFailureTreeItem.setForeground(rootFailureTreeItem.getDisplay().getSystemColor(SWT.COLOR_RED));
            rootFailureTreeItem.setFont(boldFont);
            rootFailureTreeItem.setExpanded(true);

            handleDeployFailureMessages(deployFailures, rootFailureTreeItem);
            handleDeployTestFailureMessages(testFailures, rootFailureTreeItem);
        }

        Collection<DeployMessage> deployWarnings = extractor.getDeployWarnings();
        List<CodeCoverageWarning> testWarnings = extractor.getTestWarnings();
        if (!deployWarnings.isEmpty()) {
        	TreeItem rootWarningTreeItem = new TreeItem(resultsTree, SWT.NONE);
            rootWarningTreeItem.setText("Deploy Warnings");
            rootWarningTreeItem.setImage(warningIcon);
            rootWarningTreeItem.setForeground(rootWarningTreeItem.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
            rootWarningTreeItem.setFont(boldFont);
            rootWarningTreeItem.setExpanded(true);
            handleDeployWarningMessages(deployWarnings, rootWarningTreeItem);
        }

        if(!testWarnings.isEmpty()) {
        	TreeItem rootWarningTreeItem = new TreeItem(resultsTree, SWT.NONE);
        	rootWarningTreeItem.setText("Test Warnings");
            rootWarningTreeItem.setImage(warningIcon);
            rootWarningTreeItem.setForeground(rootWarningTreeItem.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
            rootWarningTreeItem.setFont(boldFont);
            rootWarningTreeItem.setExpanded(true);
        	handleCodeCoverageWarnings(testWarnings, rootWarningTreeItem);
        }
    }

    protected void handleDeployFailureMessages(Collection<DeployMessage> messages, TreeItem rootFailureTreeItem) {
        for (DeployMessage deployMessage : messages) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deployment of '" + getDisplayName(deployMessage) + "' FAILED - "
                        + deployMessage.getProblem());
            }

            addMessageToTree(deployMessage, rootFailureTreeItem);
        }
    }

    protected void handleDeployWarningMessages(Collection<DeployMessage> messages, TreeItem rootTreeItem) {
        for (DeployMessage deployMessage : messages) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deployment of '" + getDisplayName(deployMessage) + "' contained warning - "
                        + deployMessage.getProblem());
            }

            addMessageToTree(deployMessage, rootTreeItem);
        }
    }

    private static void handleDeploySuccessMessages(Collection<DeployMessage> messages) {
        if (logger.isDebugEnabled()) {
            for (DeployMessage deployMessage : messages) {
                logger.debug("Deployment of '" + getDisplayName(deployMessage)
                        + "' was successful - remote component was " + getDeployAction(deployMessage));
            }
        }
    }

    private void addMessageToTree(DeployMessage deployMessage, TreeItem rootTreeItem) {
        String displayName = getDisplayName(deployMessage);
        // find existing node for given component; create new if not found
        TreeItem componentTreeItem = getChildTreeItem(rootTreeItem, displayName);
        componentTreeItem.setImage(deployMessage.isSuccess() ? warningIcon : failureIcon);
        // create message node and set data
        TreeItem messageTreeItem = new TreeItem(componentTreeItem, SWT.NONE);
        messageTreeItem.setText(deployMessage.getFullName() + " : " + deployMessage.getProblem());
        messageTreeItem.setData("line", deployMessage.getLineNumber());
        messageTreeItem.setData("column", deployMessage.getColumnNumber());
    }

    protected void handleDeployTestFailureMessages(Collection<RunTestFailure> messages, TreeItem rootFailureTreeItem) {
        for (RunTestFailure testFailure : messages) {
            generateTestFailureNode(testFailure, rootFailureTreeItem);
        }
    }

    protected void generateTestFailureNode(RunTestFailure testFailure, TreeItem rootFailureTreeItem) {
        String displayName = getDisplayName(testFailure.getNamespace(), testFailure.getName());

        TreeItem componentTreeItem = getChildTreeItem(rootFailureTreeItem, displayName);
        componentTreeItem.setImage(failureIcon);

        String methodName =
                Utils.isNotEmpty(testFailure.getMethodName()) ? testFailure.getMethodName()
                        : "Method Name Not Available";
        TreeItem methodTreeItem = getChildTreeItem(componentTreeItem, methodName);
        methodTreeItem.setImage(failureIcon);

        TreeItem messageTreeItem = new TreeItem(methodTreeItem, SWT.NONE);
        messageTreeItem.setText(testFailure.getMessage());
        messageTreeItem.setImage(failureIcon);
        // line and column #s and trace
        setStacktraceData(testFailure, messageTreeItem);
    }

    protected void handleCodeCoverageWarnings(List<CodeCoverageWarning> warnings, TreeItem rootFailureTreeItem) {
        if (warnings.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No code coverage warnings found");
            }
            return;
        }

        Collections.sort(warnings, new Comparator<CodeCoverageWarning>() {
            @Override
            public int compare(final CodeCoverageWarning w1, final CodeCoverageWarning w2) {
                if (Utils.isEmpty(w1.getName())) {
                    return -1;
                } else if (Utils.isNotEmpty(w1.getName()) && Utils.isEmpty(w2.getName())) {
                    return 1;
                } else {
                    return w1.getName().compareTo(w2.getName());
                }
            }
        });

        for (CodeCoverageWarning codeCoverageWarning : warnings) {
            generateCodeCoverageWarningNode(codeCoverageWarning, rootFailureTreeItem);
        }
    }

    protected void generateCodeCoverageWarningNode(CodeCoverageWarning codeCoverageWarning, TreeItem rootFailureTreeItem) {
        String displayName = getDisplayName(codeCoverageWarning.getNamespace(), codeCoverageWarning.getName());
        if (Utils.isEmpty(displayName)) {
            displayName = "General Warnings";
        }

        // find existing node for given component; create new if not found
        TreeItem warningNameTreeItem = getChildTreeItem(rootFailureTreeItem, displayName);
        warningNameTreeItem.setImage(warningIcon);
        // create message node
        TreeItem warningMessageTreeItem = new TreeItem(warningNameTreeItem, SWT.NONE);
        warningMessageTreeItem.setText(codeCoverageWarning.getMessage());
        warningMessageTreeItem.setImage(warningIcon);
    }

    protected void handleCodeCoverageResults() {
        if (Utils.isEmpty(runTestResult.getCodeCoverages())) {
            if (logger.isInfoEnabled()) {
                logger.info("No code coverage results found");
            }
            return;
        }

        TreeItem rootCodeCoverageTreeItem = new TreeItem(resultsTree, SWT.NONE);
        rootCodeCoverageTreeItem.setText("Code Coverage Results");

        // loop thru results for each method displaying code coverage and failures
        ICodeCoverageResultExt[] codeCoverageResults = runTestResult.getCodeCoverages();
        sortCodeCoverageResults(codeCoverageResults);

        for (ICodeCoverageResultExt codeCoverageResult : codeCoverageResults) {
            generateCodeCoverageWarningNode(codeCoverageResult, rootCodeCoverageTreeItem);
        }

        if (rootCodeCoverageTreeItem.getImage() == null) {
            rootCodeCoverageTreeItem.setImage(confirmIcon);
        }
    }

    protected void generateCodeCoverageWarningNode(ICodeCoverageResultExt codeCoverageResult,
            TreeItem rootCodeCoverageTreeItem) {
        IFile componentFile =
                getCoverageResultTargetComponentFile(codeCoverageResult.getName(), codeCoverageResult.getType());
        if (componentFile == null) {
            logger.warn("Unable to locate component file in project to display code coverage result '"
                    + codeCoverageResult.getName() + "'");
        }
        String displayName = getDisplayName(codeCoverageResult);

        int numLocations = codeCoverageResult.getNumLocations();
        int numLocationsNotCovered = codeCoverageResult.getNumLocationsNotCovered();
        int codeCoveragePrct = 100;

        if (numLocationsNotCovered > 0) {
            int covered = numLocations - numLocationsNotCovered;
            Double coverage = (Double.valueOf(covered) / Double.valueOf(numLocations)) * 100;
            codeCoveragePrct = Integer.valueOf((int) Math.round(coverage.doubleValue())).intValue();
        }

        TreeItem rootComponentTreeItem = new TreeItem(rootCodeCoverageTreeItem, SWT.NONE);
        rootComponentTreeItem.setText(displayName + " -- " + numLocationsNotCovered + " lines not tested, "
                + codeCoveragePrct + "% covered");

        // if coverage doesn't meet compliance, highlight nodes
        if (codeCoveragePrct < runTestResult.getCodeCompilanceCovagePercentage() && numLocationsNotCovered != 0) {
            // set root warning tree icon
            rootCodeCoverageTreeItem.setImage(warningIcon);
            rootCodeCoverageTreeItem.setForeground(rootCodeCoverageTreeItem.getDisplay().getSystemColor(
                SWT.COLOR_DARK_YELLOW));
            // set artifact warning tree icon
            rootComponentTreeItem.setImage(warningIcon);
            addUncoveredLines(componentFile, rootComponentTreeItem, codeCoverageResult.getLocationsNotCovered());
        } else {
            rootComponentTreeItem.setImage(confirmIcon);
            addUncoveredLines(componentFile, rootComponentTreeItem, codeCoverageResult.getLocationsNotCovered());
        }
    }

    private IFile getCoverageResultTargetComponentFile(String componentName, String componentType) {
        IFile file = null;

        try {
            file = projectService.getComponentFileByNameType(project, componentName, apexPrefixCheck(componentType));
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to find resource for '" + componentName + "( " + componentType + ")'  in package "
                    + project.getName() + ": " + logMessage, e);
        }
        return file;
    }

    private void addUncoveredLines(IFile componentFile, TreeItem rootComponentTreeItem,
            ICodeLocationExt[] codeLocationExt) {
        if (Utils.isEmpty(codeLocationExt)) {
            if (logger.isDebugEnabled()) {
                logger.debug("No code locations provided to displayed uncovered test locations");
            }
            return;
        }

        for (ICodeLocationExt codeLocation : codeLocationExt) {
            TreeItem uncoveredLineTreeItem = new TreeItem(rootComponentTreeItem, SWT.NONE);
            uncoveredLineTreeItem.setText("Line " + codeLocation.getLine() + ", Column " + codeLocation.getColumn()
                    + " not covered");
            ApexCodeLocation location =
                    new ApexCodeLocation(componentFile, codeLocation.getLine(), codeLocation.getColumn());
            uncoveredLineTreeItem.setData("location", location);
            uncoveredLineTreeItem.setData("line", codeLocation.getLine());
            uncoveredLineTreeItem.setImage(warningIcon);
        }
    }

    private static String apexPrefixCheck(String componentType) {
        if (Utils.isNotEmpty(componentType) && !componentType.startsWith(Constants.APEX_PREFIX)) {
            componentType = Constants.APEX_PREFIX + componentType;
        }
        return componentType;
    }

    private static void sortCodeCoverageResults(ICodeCoverageResultExt[] codeCoverageResults) {
        if (Utils.isEmpty(codeCoverageResults)) {
            return;
        }

        Arrays.sort(codeCoverageResults, new Comparator<ICodeCoverageResultExt>() {
            @Override
            public int compare(ICodeCoverageResultExt o1, ICodeCoverageResultExt o2) {
                String s1 = getDisplayName(o1.getNamespace(), o1.getName());
                String s2 = getDisplayName(o2.getNamespace(), o2.getName());
                return s1.compareTo(s2);
            }
        });
    }

    private static String getDisplayName(DeployMessage deployMessage) {
        // REVIEWME: full name or file name?
        return deployMessage.getFileName();
    }

    private String getDisplayName(ICodeCoverageResultExt codeCoverageResult) {
        String componentName = getDisplayName(codeCoverageResult.getNamespace(), codeCoverageResult.getName());
        Component component = projectService.getComponentFactory().getComponentByComponentType(
                    apexPrefixCheck(codeCoverageResult.getType()));
        return componentName
                + (Utils.isNotEmpty(component) ? " (" + component.getComponentType() + ")" : " ("
                        + codeCoverageResult.getType() + ")");
    }

    private static String getDisplayName(String namespace, String name) {
        StringBuffer strBuff = new StringBuffer();

        // prepend namespace
        if (Utils.isNotEmpty(namespace)) {
            strBuff.append(namespace + ".");
        }

        strBuff.append(Utils.isNotEmpty(name) ? name : "");

        return strBuff.toString();
    }

    private static String getDeployAction(DeployMessage deployMessage) {
        if (deployMessage.isCreated()) {
            return "created";
        } else if (deployMessage.isDeleted()) {
            return "deleted";
        } else if (deployMessage.isChanged()) {
            return "updated";
        } else {
            return "unknown action";
        }
    }

    private static TreeItem getChildTreeItem(TreeItem parentTreeItem, String name) {
        if (parentTreeItem.getItemCount() > 0) {
            TreeItem[] childTreeItems = parentTreeItem.getItems();
            for (TreeItem childTreeItem : childTreeItems) {
                if (childTreeItem.getText().equals(name)) {
                    return childTreeItem;
                }
            }
        }

        TreeItem tmpTreeItem = new TreeItem(parentTreeItem, SWT.NONE);
        tmpTreeItem.setText(name);

        return tmpTreeItem;
    }

    private void setStacktraceData(RunTestFailure testFailure, TreeItem messageTreeItem) {
        if (Utils.isEmpty(testFailure.getStackTrace())) {
            return;
        }

        String stacktrace = testFailure.getStackTrace();
        TreeItem stacktraceTreeItem = new TreeItem(messageTreeItem, SWT.NONE);
        ApexCodeLocation location = getLocationFromStackLine(testFailure.getName(), stacktrace);

        IFile file = getCoverageResultTargetComponentFile(testFailure.getName(), testFailure.getType());
        if (file != null) {
            location.setFile(file);
        }

        stacktraceTreeItem.setData("location", location);
        stacktraceTreeItem.setData("line", "1");
        stacktraceTreeItem.setText(stacktrace);
    }

    private static ApexCodeLocation getLocationFromStackLine(String name, String stackTrace) {
        if (Utils.isEmpty(name) || Utils.isEmpty(stackTrace)) {
            logger.warn("Unable to get location from stacktrace - name and/or stacktrace is null");
            return null;
        }

        String line = null;
        String column = null;
        try {
            String[] temp = stackTrace.split("line");
            line = temp[1].split(",")[0].trim();
            String c = temp[1].trim();
            column = c.split("column")[1].trim();
            if (Utils.isNotEmpty(column) && column.contains("\n")) {
                column = column.substring(0, column.indexOf("\n"));
            }
        } catch (Exception e) {
            logger.warn("Unable to get location from stacktrace", e);
        }
        return new ApexCodeLocation(name, line, column);
    }
}
