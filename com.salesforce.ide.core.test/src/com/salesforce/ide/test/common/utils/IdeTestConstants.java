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
package com.salesforce.ide.test.common.utils;

import java.io.File;

public interface IdeTestConstants {

    String MESSAGE_RESOURCE_BUNDLE_ID = "com.salesforce.ide.test.common.utils.ideTestMessages";
    String PACKAGE_NAME_TOKEN = "@packagename@";
    String PACKAGE_FULL_NAME_ELEMENT_WITH_TOKEN = "<fullName>" + PACKAGE_NAME_TOKEN + "</fullName>";
    String DEFAULT_PACKAGED_NAME = "unpackaged";

    // pre-canned managed/unmanaged installed packages on scratch@ideAutoTest_de_na1.com @ na1-blitz03.soma.salesforce.com
    String DEFAULT_MANAGED_INSTALLED_PKG_NAME = "managed_installed_pkg";
    String DEFAULT_MANAGED_INSTALLED_PKG2_NAME = "managed_installed_pkg_2";
    String DEFAULT_UNMANAGED_INSTALLED_PKG_NAME = "unmanaged_installed_pkg";
    String DEFAULT_UNMANAGED_INSTALLED_PKG2_NAME = "unmanaged_installed_pkg_2";

    //JVM related flags
    String JVM_ARG_SFDC_INTERNAL = "sfdc-internal";
    String JVM_ARG_TIMEOUT = "test-timeout";

    //test.properties keys.
    String TEST_PROPS_FILE = "/config/test.properties";
    String SYS_PROP_CUSTOM_ORG_PREFIX = "custom-org-prefix";
    String SYS_PROP_CUSTOM_ORG_PREFIX_VALUE_LOCAL = "__LOCALHOST";
    String TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_ENDPOINT = "endpoint";
    String TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_USERNAME = "username";
    String TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_PASSWD = "password";
    String TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_NAMESPACE = "namespace";
    String TEST_PROPERTIES_RELEASE_HOME = "release.home";
    String TEST_PROPERTIES_TEST_HOME = "test.home";

    //ide context menu strings
    String forceMenuOption = "Force&.com";
    String refreshFromServerMenuOption = forceMenuOption+"#&Refresh from Server";
    String saveToServerMenuOption = forceMenuOption+"#&Save to Server";
    String projectPropertyMenuOption = "P&roperties";
    String forceProjectPropertiesMenuOption = forceMenuOption+"#&Project Properties";
    String forceProjectContentPropertiesMenuOption =forceMenuOption+"#&Add/Remove Metadata Components...";
    String upgradeProjectMenuOption = forceMenuOption+"#&Upgrade Project...";
    String showInSalesforceWebOption = forceMenuOption+"#Sho&w in Salesforce Web";

    //other constants
    String DEFAULT_PASSWORD = "123456";
    String TEST_FRAGMENT = "com.salesforce.toolkit.test";
    String FILEMETADATA_ROOT = "/filemetadata/";
    String FILEMETADATA_BASIC_SUPPORT_DIR = "basic_support/";
    String FILEMETADATA_ALL_UNPACKAGED_DESTRUCTIVE_RELPATH =FILEMETADATA_ROOT + "delete/EvertyhingUnpackaged";
    String FILEMETADATA_COMPLETE = "complete";
    String FILEMETADATA_SIMPLE = "simple";
    String FILEMETADATA_MANIFESTS = "manifests";
    String LAUNCH_PME_FROM_PRJ_PROP_BTN_NAME = "Add/Remove...";
    String expectApexClassName = "alphaClass";
    String expectApexClass = expectApexClassName + ".cls";
    String expectApexClassFilePath = "classes/" + expectApexClass;
    String expectRunTestApexClassName = "AccountMerge";
    String expectRunTestApexClass = expectRunTestApexClassName + ".cls";
    String expectRunTestApexClassFilePath = "classes/" + expectRunTestApexClass;
    String expectScontrol_Feed = "Feed.scf";
    String expectedScontrolFilePath_Feed = "scontrols/" + expectScontrol_Feed;
    
    
    //TIMEOUTS
    /**
     * 40000 ms: used for refresh/save/deploy bigger test org.
     */
    int LONGER_TEST_WAIT_TIMEOUT = 40000;
    /**
     * 10000 ms
     */
    int TEST_WAIT_TIMEOUT = 10000;

    /**
     * 500 ms
     */
    int TEST_WAIT_INTERVAL = 500;

    /**
     * 7 minutes
     */
    long TEST_TIMEOUT = 420000L;

    //ORG PERMS
    /**
     * AuthorApex
     */
    String AUTHOR_APEX_PERM = "AuthorApex";

    /**
     * AuthorApexPages
     */
    String AUTHOR_APEX_PAGES_PERM = "AuthorApexPages";
    String CREATE_CUSTOM_DATATYPE_PERM = "CreateCustomDataType";

    /**
     * Deploy test data runtime replacement tokens. Usage: used in IdeOrgFixture.substituteStringsIfRequired() to
     * replace corresponding token in filemetadata files before deploy.
     */
    String RUNNING_USER_REPLACEMENT_TOKEN = "@runningUser@";
    String DEV_NAME_REPLACEMENT_TOKEN = "@devName@";

    //PERSPECTIVE AND VIEW IDs
    /**
     * "org.eclipse.jdt.ui.JavaPerspective"
     */
    String ECLIPSE_JAVA_PERSPECTIVE_ID = "org.eclipse.jdt.ui.JavaPerspective";
    /**
     * "com.salesforce.ide.perspective"
     */
    String ECLIPSE_FORCE_COM_PERSPECTIVE_ID = "com.salesforce.ide.perspective";
    /**
     * "com.salesforce.ide.ui.view.runTest"
     */
    String IDE_RUNTESTVIEW_ID = "com.salesforce.ide.ui.view.runTest";
    /**
     * "com.salesforce.ide.ui.view.debugLog"
     */
    String IDE_DEBUGLOGVIEW_ID = "com.salesforce.ide.ui.view.debugLog";
    /**
     * "org.eclipse.ui.views.ProblemView"
     */
    String ECLIPSE_PROBLEMSVIEW_ID = "org.eclipse.ui.views.ProblemView";
    /**
     * "org.eclipse.team.sync.views.SynchronizeView"
     */
    String ECLIPSE_SYNCVIEW_ID = "org.eclipse.team.sync.views.SynchronizeView";

    /**
     * "org.eclipse.ui.cheatsheets.views.CheatSheetView"
     */
    String ECLIPSE_CHEATSHEETVIEW_ID = "org.eclipse.ui.cheatsheets.views.CheatSheetView";

    /**
     * "org.eclipse.jdt.ui.PackageExplorer"
     */
    String  ECLIPSE_PACKAGE_EXPLORER_VIEW_ID= "org.eclipse.jdt.ui.PackageExplorer";
    /**
     * "org.eclipse.jdt.ui.PackageExplorer"
     */
    String  ECLIPSE_NAVIGATOR_VIEW_ID= "org.eclipse.ui.navigator.ProjectExplorer";

    /** Feed.scf and Feed.scf-meta.xml is default scontrol created by server **/
    int NUM_DEFAULT_SCONTROL_CREATED_BY_SERVER = 2;

    int NUM_DEFAULT_APEXCLASS_CREATED_BY_SERVER = 0;
    
    /**
     * The vm argument to pass for appserver i.e. -Dappserver-for-test 
     * or the field that'll be looked for in test.properties.
     */
    String APP_SERVER_FOR_TEST_KEY ="appserver-for-test";
}
