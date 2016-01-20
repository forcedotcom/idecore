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

    // Config dialogs
    public static String Tab_ProjectDialogTitle;
    public static String Tab_ProjectDialogInstruction;
    public static String Tab_ClassDialogTitle;
    public static String Tab_ClassDialogInstruction;
    public static String Tab_MethodDialogTitle;
    public static String Tab_MethodDialogInstruction;
    
    // Project config
    public static String Tab_ProjectTabTitle;
    public static String Tab_ProjectGroupTitle;
    
    // Log config
    public static String Tab_LogGroupTitle;
    public static String Tab_LogEnableLogging;
    public static String Tab_LogCategoryDatabase;
    public static String Tab_LogCategoryWorkflow;
    public static String Tab_LogCategoryValidation;
    public static String Tab_LogCategoryCallout;
    public static String Tab_LogCategoryApexCode;
    public static String Tab_LogCategoryApexProfiling;
    public static String Tab_LogCategoryVisualforce;
    public static String Tab_LogCategorySystem;
    
    // Test config
    public static String Tab_TestsTabTitle;
    public static String Tab_TestsGroupTitle;
    public static String Tab_TestClassGroupTitle;
    public static String Tab_TestMethodGroupTitle;
    public static String Tab_AllClasses;
    public static String Tab_AllMethods;
    
    // Suite config
    public static String Tab_SuiteGroupTitle;
    public static String Tab_UseSuites;
    public static String Tab_SuiteColumnName;
    
    // Config error messages
    public static String Tab_EmptyProjectErrorMessage;
    public static String Tab_InvalidForceProjectErrorMessage;
    public static String Tab_NonExistingProjectErrorMessage;
    public static String Tab_ChooseAtLeastOneSuiteErrorMessage;
    
    // Config general
    public static String Tab_SearchButtonText;
    
    // Launch delegate
    public static String LaunchDelegate_ConfirmDialogTitle;
    public static String LaunchDelegate_CannotLaunchDebugModeErrorMessage;
    public static String LaunchDelegate_CannotLaunchAnotherConfig;
    public static String LaunchDelegate_CannotLaunchAsyncWhileDebugging;
    public static String LaunchDelegate_ExistingTraceFlag;
    public static String LaunchDelegate_CannotLaunchInvalidForceProject;
    
    // RunTestsView
    public static String View_Name;
    public static String View_Clear;
    public static String View_StackTrace;
    public static String View_SystemLog;
    public static String View_UserLog;
    public static String View_CodeCoverage;
    public static String View_CodeCoverageOverall;
    public static String View_CodeCoverageClass;
    public static String View_CodeCoveragePercent;
    public static String View_CodeCoverageLines;
    public static String View_LineNotCovered;
    public static String View_ErrorStartingTestsTitle;
    public static String View_ErrorStartingTestsSolution;
    public static String View_ErrorGetAsyncTestResultsTitle;
    public static String View_ErrorGetAsyncTestResultsSolution;
    public static String View_ErrorOpenSourceTitle;
    public static String View_ErrorOpenSourceSolution;
    public static String View_ErrorInvalidSuites;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
