/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.ui.views.runtest;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.salesforce.ide.ui.views.runtest.messages"; //$NON-NLS-1$

    public static String GenericTab_ProjectDialogTitle;
    public static String GenericTab_ClassDialogTitle;
    public static String GenericTab_MethodDialogTitle;
    public static String GenericTab_EmptyProjectErrorMessage;
    public static String GenericTab_InvalidForceProjectErrorMessage;
    public static String GenericTab_NonExistingProjectErrorMessage;
    public static String GenericTab_SearchButtonText;
    public static String GenericTab_ProjectGroupTitle;
    public static String GenericTab_AllClasses;
    public static String GenericTab_AllMethods;

    public static String RunTestsTab_TabTitle;
    public static String RunTestsTab_TabGroupTitle;
    public static String RunTestsTab_ModeGroupTitle;
    public static String RunTestsTab_ProjectDialogInstruction;
    public static String RunTestsTab_TestClassGroupTitle;
    public static String RunTestsTab_ClassDialogInstruction;
    public static String RunTestsTab_TestMethodGroupTitle;
    public static String RunTestsTab_MethodDialogInstruction;
    public static String RunTestsTab_RunAsync;
    public static String RunTestsTab_RunSync;
    public static String RunTestsTab_TooManyTestClassesForSyncErrorMessage;
    public static String RunTestsTab_LogGroupTitle;
    public static String RunTestsTab_LogEnableLogging;
    public static String RunTestsTab_LogCategoryDatabase;
    public static String RunTestsTab_LogCategoryWorkflow;
    public static String RunTestsTab_LogCategoryValidation;
    public static String RunTestsTab_LogCategoryCallout;
    public static String RunTestsTab_LogCategoryApexCode;
    public static String RunTestsTab_LogCategoryApexProfiling;
    public static String RunTestsTab_LogCategoryVisualforce;
    public static String RunTestsTab_LogCategorySystem;
    
    public static String RunTestsLaunchConfigurationDelegate_ConfirmDialogTitle;
    public static String RunTestsLaunchConfigurationDelegate_CannotLaunchDebugModeErrorMessage;
    public static String RunTestsLaunchConfigurationDelegate_CannotLaunchAnotherConfig;
    public static String RunTestsLaunchConfigurationDelegate_CannotLaunchAsyncWhileDebugging;
    public static String RunTestsLaunchConfigurationDelegate_ExistingTraceFlag;
    public static String RunTestsLaunchConfigurationDelegate_CannotLaunchInvalidForceProject;
    
    public static String RunTestView_Name;
    public static String RunTestView_Clear;
    public static String RunTestView_StackTrace;
    public static String RunTestView_SystemLog;
    public static String RunTestView_UserLog;
    public static String RunTestView_CodeCoverage;
    public static String RunTestView_CodeCoverageOverall;
    public static String RunTestView_CodeCoverageClass;
    public static String RunTestView_CodeCoveragePercent;
    public static String RunTestView_CodeCoverageLines;
    public static String RunTestsView_ErrorStartingTestsTitle;
    public static String RunTestsView_ErrorStartingTestsSolution;
    public static String RunTestsView_ErrorGetAsyncTestResultsTitle;
    public static String RunTestsView_ErrorGetAsyncTestResultsSolution;
    public static String RunTestsView_ErrorOpenSourceTitle;
    public static String RunTestsView_ErrorOpenSourceSolution;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
