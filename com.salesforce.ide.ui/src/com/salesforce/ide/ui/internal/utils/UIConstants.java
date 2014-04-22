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

import com.salesforce.ide.core.internal.utils.Constants;

public interface UIConstants {

    // P L U G I N
    String PLUGIN_NAME = "Force.com IDE UI";
    String PLUGIN_PREFIX = Constants.FORCE_PLUGIN_PREFIX + ".ui";
    String INTERNAL_PLUGIN_PREFIX = PLUGIN_PREFIX + ".internal";
    String RESOURCE_BUNDLE_ID = INTERNAL_PLUGIN_PREFIX + ".utils.messages";
    String APPLICATION_CONTEXT = "/config/ui-application-context.xml";

    // C O N T R I B U T I O N I D S
    // views
    String RUN_TEST_VIEW_ID = PLUGIN_PREFIX + ".view.runTest";
    String DEBUG_LOG_VIEW_ID = PLUGIN_PREFIX + ".view.debugLog";
    String IDE_LOG_VIEW_ID = PLUGIN_PREFIX + ".view.log";

    // sync
    String SYNC_PARTICIPANT_ID = PLUGIN_PREFIX + ".sync.componentSyncParticipant";

    // perspective
    String FORCE_PERSPECTIVE_ID = Constants.FORCE_PLUGIN_PREFIX + ".perspective";

    // wizards
    String WIZARD_PREFIX_ID = PLUGIN_PREFIX + ".wizards";
    String CREATE_PROJECT_ID = WIZARD_PREFIX_ID + ".createProject";
    String CREATE_APEX_CLASS_ID = WIZARD_PREFIX_ID + ".createApexClass";
    String CREATE_APEX_TRIGGER_ID = WIZARD_PREFIX_ID + ".createApexTrigger";
    String CREATE_APEX_PAGE_ID = WIZARD_PREFIX_ID + ".createApexPage";
    String CREATE_CUSTOM_APP_ID = WIZARD_PREFIX_ID + ".createCustomApplication";
    String CREATE_CUSTOM_OBJ_ID = WIZARD_PREFIX_ID + ".createCustomObject";
    String CREATE_CUSTOM_TAB_ID = WIZARD_PREFIX_ID + ".createCustomTab";
    String CREATE_PROFILE_ID = WIZARD_PREFIX_ID + ".wizards.createProfile";
    String CREATE_SCONTROL_ID = WIZARD_PREFIX_ID + ".wizards.createScontrol";

    // actions
    String NEW_COMPONENT_ACTION_ID_PREFIX = PLUGIN_PREFIX + ".action.newComponent.";

    // L A B E L S
    String TITLE_DEFAULT = "NewProjectWizard.Title";
    String DESCRIPTION_DEFAULT = "NewProjectWizard.Description";
    String DESCRIPTION_ADD_NATURE = "DescriptionAddNature";
    String TITLE_ADD_NATURE = "TitleAddNature";
    String LABEL_ENDPOINT = "LabelEndpoint";
    String LABEL_PASSWORD = "LabelPassword";
    String LABEL_TOKEN = "LabelToken";
    String LABEL_USERNAME = "LabelUsername";
    String LABEL_SESSIONID = "LabelSessionId";
    String LABEL_AUTH = "LabelAuth";
    String LABEL_PROJECT_NAME = "LabelProjectName";
    String LABEL_RESET = "LabelReset";
    String LABEL_SNAPSHOT_PROJECT_NAME = "LabelSnapshotProjectName";
    String LABEL_DEPLOYMENT_DIRECTORY = "LabelDeploymentDirectory";
    String LABEL_SNAPSHOT_DIRECTORY = "LabelSnapshotDirectory";
    String LABEL_DEPLOYMENT_TO = "LabelDeploymentTo";
    String LABEL_SIGNUP = "ProjectCreateWizard.OrganizationPage.Signup.label";
    String LABEL_SIGNUP_LINK = "ProjectCreateWizard.OrganizationPage.Signup.link";
    String PROXY_LABEL = "ProjectCreateWizard.OrganizationPage.Proxy.label";

    // M E S S A G E S
    String MSG_PROJECT_EXISTS = "PropProjectExists";
    String MSG_PROJECT_NAME_UNIQUE = "PropProjectNameUnique";
    String MSG_PROJECT_NAME_EMPTY = "PropProjectNameEmpty";
    String MSG_USERNAME_EMPTY = "PropUsernameEmpty";
    String MSG_PASSWORD_EMPTY = "PropPasswordEmpty";
    String MSG_TOKEN_EMPTY = "PropTokenEmpty";
    String MSG_INVALID_TIMEOUT = "PropInvalidTimeout";
    String MSG_INVALID_PORT = "PropInvalidPort";
    String MSG_USERNAME = "PropUsername";
    String MSG_PASSWORD = "PropPassword";
    String MSG_ENDPOINT = "PropEndpoint";
    String MSG_REST = "PropRest";
    String MSG_PROXYUSERNAME = "PropProxyUsername";
    String MSG_PROXYPASSWORD = "PropProxyPassword";;
    String MSG_PROXYSERVER = "PropProxyServer";
    String MSG_PROXYPORT = "PropProxyPort";
    String MSG_READTIMEOUT = "PropReadTimeout";
    String MSG_USEPROXY = "PropUseProxy";
    String MSG_USEPROXYLOGIN = "PropUseProxyLogin";
    String MSG_CREATING_NEW = "NewProjectWizard_creating";
    String MSG_PROJECT = "NewProjectWizard_project";

    String NEW_PROJECT_PARENT_SHELL_TEXT = "New Project";
    String NEW_ORG_CREATE_LINK = "http://developer.force.com/join?d=70130000000Ekqd";

}
