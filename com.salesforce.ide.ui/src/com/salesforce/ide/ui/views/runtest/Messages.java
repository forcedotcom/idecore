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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.salesforce.ide.ui.views.runtest.messages"; //$NON-NLS-1$

    public static String GenericTab_ProjectDialogTitle;
    public static String GenericTab_ClassDialogTitle;
    public static String GenericTab_MethodDialogTitle;
    public static String GenericTab_EmptyProjectErrorMessage;
    public static String GenericTab_InvalidForceProjectErrorMessage;
    public static String GenericTab_NonExistingProjectErrorMessage;
    public static String GenericTab_BrowseButtonText;
    public static String GenericTab_SearchButtonText;
    public static String GenericTab_ProjectGroupTitle;
    public static String GenericTab_AllClasses;
    public static String GenericTab_AllMethods;

    public static String RunTestsTab_TabTitle;
    public static String RunTestsTab_TabGroupTitle;
    public static String RunTestsTab_ProjectDialogInstruction;
    public static String RunTestsTab_TestClassGroupTitle;
    public static String RunTestsTab_ClassDialogInstruction;
    public static String RunTestsTab_TestMethodGroupTitle;
    public static String RunTestsTab_MethodDialogInstruction;

    public static String RunTestsLaunchConfigurationDelegate_CannotLaunchDebugModeErrorMessage;
    public static String RunTestsLaunchConfigurationDelegate_CannotLaunchAnotherConfig;
    public static String LaunchConfigurationDelegate_CannotLaunchInvalidForceProject;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
