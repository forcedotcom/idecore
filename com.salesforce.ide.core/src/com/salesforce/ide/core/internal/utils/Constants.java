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
package com.salesforce.ide.core.internal.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Constants {

    // P L U G I N & C O N T R I B U T I O N I D S
    String BRAND_NAME = "Force.com";
    String LABEL_NAME = "Force&amp;.com";
    String PLUGIN_NAME = BRAND_NAME + " IDE";
    String PRODUCT_NAME = BRAND_NAME + " IDE";
    String SFDC_PREFIX = "com.salesforce";
    String FORCE_PLUGIN_PREFIX = SFDC_PREFIX + ".ide";
    String PLUGIN_PREFIX = FORCE_PLUGIN_PREFIX + ".core";
    String DOCUMENTATION_PLUGIN_PREFIX = FORCE_PLUGIN_PREFIX + ".documentation";
    String FILE_PROP_PREFIX_ID = FORCE_PLUGIN_PREFIX;
    String INTERNAL_PLUGIN_PREFIX = PLUGIN_PREFIX + ".internal";
    String RESOURCE_BUNDLE_ID = INTERNAL_PLUGIN_PREFIX + ".utils.messages";
    String FORCE_DEFAULT_ENCODING_CHARSET = "UTF-8";
    String PREV_IDE_BUNDLE_NAME = "com.salesforce.toolkit";
    String BUNDLE_ATTRIBUTE_NAME = "Bundle-Name";
    String BUNDLE_ATTRIBUTE_VERSION = "Bundle-Version";
    String FORCE_PLUGIN_CONTEXT_ID = FORCE_PLUGIN_PREFIX + ".context";
    String FORCE_PLUGIN_PERSPECTIVE = FORCE_PLUGIN_PREFIX + ".perspective";
    
    // B E A N S
    String SERVICE_LOCATOR_BEAN = "serviceLocator";
    String FACTORY_LOCATOR_BEAN = "factoryLocator";

    String COMPONENT_TYPE_API_CLASS_PACKAGE = "com.salesforce.ide.api.metadata.types";

    // O B J E C T T Y P E S
    String ACCOUNT_SHARING_RULES = "AccountSharingRules";
    String ACCOUNT_OWNER_SHARING_RULE = "AccountOwnerSharingRule";
    String ACCOUNT_CRITERIA_BASED_SHARING_RULE = "AccountCriteriaBasedSharingRule";
    String ACCOUNT_TERRITORY_SHARING_RULES = "AccountTerritorySharingRules";
    String ACCOUNT_TERRITORY_SHARING_RULE = "AccountTerritorySharingRule";
    String ANALYTIC_SNAPSHOT = "AnalyticSnapshot";
    String APEX_CLASS = "ApexClass";
    String APEX_COMPONENT = "ApexComponent";
    String APEX_TEST_SUITE = "ApexTestSuite";
    String APEX_TRIGGER = "ApexTrigger";
    String APEX_TRIGGER_COUPLING = "ApexTriggerCoupling";
    String APEX_PAGE = "ApexPage";
    String AURA = "aura";
    String AURA_DEFINITION_BUNDLE = "AuraDefinitionBundle";
    String APP_MENU = "AppMenu";
    String APPROVAL_PROCESS = "ApprovalProcess";
    String AUTH_PROVIDER = "AuthProvider";
    String CALL_CENTER = "CallCenter";
    String CAMPAIGN_SHARING_RULES = "CampaignSharingRules";
    String CAMPAIGN_OWNER_SHARING_RULES = "CampaignOwnerSharingRule";
    String CAMPAIGN_CRITERIA_BASED_SHARING_RULES = "CampaignCriteriaBasedSharingRule";
    String CASE_SHARING_RULES = "CaseSharingRules";
    String CASE_OWNER_SHARING_RULES = "CaseOwnerSharingRule";
    String CASE_CRITERIA_BASED_SHARING_RULES = "CaseCriteriaBasedSharingRule";
    String COMMUNITY = "Community";
    String CONNECTED_APP = "ConnectedApp";
    String CONTACT_SHARING_RULES = "ContactSharingRules";
    String CONTACT_OWNER_SHARING_RULE = "ContactOwnerSharingRule";
    String CONTACT_CRITERIA_BASED_SHARING_RULE = "ContactCriteriaBasedSharingRule";
    String CUSTOM_APPLICATION = "CustomApplication";
    String CUSTOM_APPLICATION_COMPONENT = "CustomApplicationComponent";
    String CUSTOM_DATA_TYPE = "CustomDataType";
    String CUSTOM_FIELD = "CustomField";
    String CUSTOM_FIELD_TRANSLATION = "CustomFieldTranslation";
    String CUSTOM_LABELS = "CustomLabels";
    String CUSTOM_METADATA = "CustomeMetadata";
    String CUSTOM_OBJECT = "CustomObject";
    String CUSTOM_OBJECT_TRANSLATION = "CustomObjectTranslation";
    String CUSTOM_OBJECT_SHARING_RULES = "CustomObjectSharingRules";
    String CUSTOM_OBJECT_OWNER_SHARING_RULE = "CustomObjectOwnerSharingRule";
    String CUSTOM_OBJECT_CRITERIA_BASED_SHARING_RULE = "CustomObjectCriteriaBasedSharingRule";
    String CUSTOM_PAGE_WEB_LINK = "CustomPageWebLink";
    String CUSTOM_PERMISSION = "CustomPermission";
    String CUSTOM_SITE = "CustomSite";
    String CUSTOM_TAB = "CustomTab";
    String DASHBOARD = "Dashboard";
    String DATACATEGORYGROUP = "DataCategoryGroup";
    String DOCUMENT = "Document";
    String EMAIL_TEMPLATE = "EmailTemplate";
    String ENTITLEMENT_PROCESS = "EntitlementProcess";
    String ENTITLEMENT_TEMPLATE = "EntitlementTemplate";
    String FLEXI_PAGE = "FlexiPage";
    String FLOW = "Flow";
    String FOLDER = "Folder";
    String GROUP = "Group";
    String LAYOUT = "Layout";
    String LETTERHEAD = "Letterhead";
    String HOME_PAGE_COMPONENT = "HomePageComponent";
    String HOME_PAGE_LAYOUT = "HomePageLayout";
    String LEAD_SHARING_RULES = "LeadSharingRules";
    String LEAD_OWNER_SHARING_RULE = "LeadOwnerSharingRule";
    String LEAD_CRITERIA_BASED_SHARING_RULE = "LeadCriteriaBasedSharingRule";
    String LIVE_CHAT_AGENT_CONFIG = "LiveChatAgentConfig";
    String LIVE_CHAT_BUTTON = "LiveChatButton";
    String LIVE_CHAT_DEPLOYMENT = "LiveChatDeployment";
    String MILESTONE_TYPE = "MilestoneType";
    String NETWORK = "Network";
    String OPPORTUNITY_SHARING_RULES = "OpportunitySharingRules";
    String OPPORTUNITY_OWNER_SHARING_RULE = "OpportunityOwnerSharingRule";
    String OPPORTUNITY_CRITERIA_BASED_SHARING_RULE = "OpportunityCriteriaBasedSharingRule";
    String PACKAGE_MANIFEST = "PackageManifest";
    String PORTAL = "Portal";
    String POST_TEMPLATE = "PostTemplate";
    String PROFILE = "Profile";
    String PERMISSIONSET = "PermissionSet";
    String QUICK_ACTION = "QuickAction";
    String QUEUE = "Queue";
    String REMOTE_SITE_SETTING = "RemoteSiteSetting";
    String REPORT = "Report";
    String REPORT_TYPE = "ReportType";
    String REPORT_CHART = "ReportChart";
    String REPORT_COLOR_RANGE = "ReportColorRange";
    String REPORT_COLUMN = "ReportColumn";
    String REPORT_FILTER = "ReportFilter";
    String REPORT_GROUPING = "ReportGrouping";
    String REPORT_LAYOUT_SECTION = "ReportLayoutSection";
    String REPORT_PARAM = "ReportParam";
    String REPORT_TIMEFRAME_FILTER = "ReportTimeFrameFilter";
    String REPORT_TYPE_COLUMN = "ReportTypeColumn";
    String REPORT_TYPE_COLUMN_TRANSLATION = "ReportTypeColumnTranslation";
    String REPORT_TYPE_SECTION_TRANSLATION = "ReportTypeSectionTranslation";
    String REPORT_TYPE_TRANSLATION = "ReportTypeTranslation";
    String ROLE = "Role";
    String SAML_SSO_CONFIG = "SamlSsoConfig";
    String SETTING = "Setting";
    String SCONTROL = "Scontrol";
    String SITE_DOT_COM = "SiteDotCom";
    String SKILL = "Skill";
    String SECURITY_SETTINGS = "SecuritySettings";
    String SHARING_SET = "SharingSet";
    String STANDARD_OBJECT = "StandardObject";
    String STATIC_RESOURCE = "StaticResource";
    String TERRITORY = "Territory";
    String TRANSLATIONS = "Translations";
    String USER_SHARING_RULES = "UserSharingRules";
    String USER_MEMBERSHIP_SHARING_RULE = "UserMembershipSharingRule";
    String USER_CRITERIA_BASED_SHARING_RULE = "UserCriteriaBasedSharingRule";
    String VALIDATION_RULE = "ValidationRule";
    String VALIDATION_RULE_TRANSLATION = "ValidationRuleTranslation";
    String WEBLINK = "WebLink";
    String WEBLINK_TRANSLATION = "WebLinkTranslation";
    String WORKFLOW = "Workflow";
    String WORKFLOW_ALERT = "WorkflowAlert";
    String WORKFLOW_FIELD_UPDATE = "WorkflowFieldUpdate";
    String WORKFLOW_OUTBOUND_MESSAGE = "WorkflowOutBoundMessage";
    String WORKFLOW_SEND = "WorkflowSend";
    String WORKFLOW_RULE = "WorkflowRule";
    String X_ORG_HUB = "XOrgHub";
    String UNKNOWN_COMPONENT_TYPE = "Unknown";

    String AUTORESPONSERULES = "AutoResponseRules";
    String AUTORESPONSERULE = "AutoResponseRule";
    String ASSIGNMENTRULES = "AssignmentRules";
    String ASSIGNMENTRULE = "AssignmentRule";
    String ESCALATIONRULES = "EscalationRules";
    String ESCALATIONRULE = "EscalationRule";

    String PROFILE_APEX_CLASS_ACCESS = "ProfileApexClassAccess";
    String PROFILE_APEX_PAGE_ACCESS = "ProfileApexPageAccess";
    String PROFILE_APPLICATION_VISIBILITY = "ProfileApplicationVisibility";
    String PROFILE_FIELD_LEVEL_SECURITY = "ProfileFieldLevelSecurity";
    String PROFILE_LAYOUT_ASSIGNMENTS = "ProfileLayoutAssignments";
    String PROFILE_OBJECT_PERMISSIONS = "ProfileObjectPermissions";
    String PROFILE_RECORD_TYPE_VISIBILITY = "ProfileRecordTypeVisibility";
    String PROFILE_TAB_VISIBILITY = "ProfileTabVisibility";

    String RECORD_TYPE = "RecordType";
    // O B J E C T T Y P E S G R O U P S
    String[] DEV_CODE_COMPONENT_TYPES = { APEX_CLASS, APEX_COMPONENT, APEX_TRIGGER, APEX_TEST_SUITE, APEX_PAGE, AURA_DEFINITION_BUNDLE, STATIC_RESOURCE };

    List<String> RULE_TYPES = Collections.unmodifiableList(
        Arrays.asList(
            new String[] { 
                AUTORESPONSERULES,
                ASSIGNMENTRULES,
                ESCALATIONRULES }));

    List<String> ABSTRACT_SHARING_RULE_TYPES = Collections.unmodifiableList(
        Arrays.asList(
            new String[] {
                ACCOUNT_SHARING_RULES,
                ACCOUNT_TERRITORY_SHARING_RULES,
                CAMPAIGN_SHARING_RULES,
                CASE_SHARING_RULES,
                CONTACT_SHARING_RULES,
                CUSTOM_OBJECT_SHARING_RULES,
                LEAD_SHARING_RULES,
                OPPORTUNITY_SHARING_RULES,
                USER_SHARING_RULES }));

    List<String> SHARING_RULE_TYPES = Collections.unmodifiableList(
        Arrays.asList(
            new String[] {
                ACCOUNT_OWNER_SHARING_RULE,
                ACCOUNT_CRITERIA_BASED_SHARING_RULE,
                ACCOUNT_TERRITORY_SHARING_RULE,
                CAMPAIGN_OWNER_SHARING_RULES,
                CAMPAIGN_CRITERIA_BASED_SHARING_RULES,
                CASE_OWNER_SHARING_RULES,
                CASE_CRITERIA_BASED_SHARING_RULES,
                CONTACT_OWNER_SHARING_RULE,
                CONTACT_CRITERIA_BASED_SHARING_RULE,
                CUSTOM_OBJECT_OWNER_SHARING_RULE,
                CUSTOM_OBJECT_CRITERIA_BASED_SHARING_RULE,
                LEAD_OWNER_SHARING_RULE,
                LEAD_CRITERIA_BASED_SHARING_RULE,
                OPPORTUNITY_OWNER_SHARING_RULE,
                OPPORTUNITY_CRITERIA_BASED_SHARING_RULE,
                USER_MEMBERSHIP_SHARING_RULE,
                USER_CRITERIA_BASED_SHARING_RULE }));

    // A P E X
    String APEX_PREFIX = "Apex";

    // P R O P E R T I E S
    String PROP_USERNAME = "username";
    String PROP_PASSWORD = "password";
    
    String PROP_SESSION_ID ="sessionId";    
    String PROP_TOKEN = "token";
    String PROP_NAMESPACE_PREFIX = "namespacePrefix";
    String PROP_KEEP_ENDPOINT = "keependpoint";
    String PROP_HTTPS_PROTOCOL = "httpsProtocol";
    String PROP_ENDPOINT = "endpoint";
    String PROP_ENDPOINT_SERVER = "endpointServer";
    String PROP_ENDPOINT_ENVIRONMENT = "endpointEnvironment";
    String PROP_PACKAGE_NAME = "packageName";
    String PROP_ENDPOINT_API_VERSION = "endpointApiVersion";
    String PROP_METADATA_FORMAT_VERSION = "metadataFormatVersion";
    String PROP_READ_TIMEOUT = "readTimeout";
    String PROP_PROXY_USE_PROXY = "useProxy";
    String PROP_PROXY_USERNAME = "proxyUsername";
    String PROP_PROXY_PASSWORD = "proxyPassword";
    String PROP_PROXY_SERVER = "proxyServer";
    String PROP_PROXY_PORT = "proxyPort";
    String PROP_LOGGING_LEVEL = "loggingLevel";
    String PROP_REST = "rest";
    String PROP_IDE_VERSION = "ideVersion";
    String PROP_PROJECT_IDENTIFIER = "projectIdentifier";
    String PROP_PREFER_TOOLING_DEPLOYMENT = "preferToolingDeployment";

    // P R O J E C T
    String LOGGING_LEVEL = "loggingLevel";
    String FRONTDOOR = "frontdoor";
    String PROJECT_FILE = ".project";
    String PROJECT_SETTINGS_DIR = ".settings";
    String PROJECT_SETTINGS_FILE = PROJECT_SETTINGS_DIR + "/" + PLUGIN_PREFIX + ".prefs";

    // A P I
    String SEP = " , ";
    int SECONDS_TO_MILISECONDS = 1000;
    int READ_TIMEOUT_IN_SECONDS_MAX = 600;
    int READ_TIMEOUT_IN_SECONDS_MIN = 3;
    int READ_TIMEOUT_IN_SECONDS_DEFAULT = 400; // read timeouts
    int INTERNAL_TIMEOUT_SECS = 60;
    int INTERNAL_TIMEOUT_MILLIS = INTERNAL_TIMEOUT_SECS * 1000;
    String API_URL_PART = "-api";
    String CUSTOM_OBJECT_SUFFIX = "__c";

    // C O N F I G F I L E S
    String LOG_CONFIG_FILE = "/log4j/log4j.xml";
    String APPLICATION_CONTEXT_FILE_SUFFIX = "application-context.xml";
    String APPLICATION_CONTEXT = "/config/core-" + APPLICATION_CONTEXT_FILE_SUFFIX;

    // F I L E P R O P E R T I E S
    String ID = "Id";
    String FILE_NAME = "FileName";
    String FULL_NAME = "FullName";
    String BODY = "Body";
    String BODY_CHECKSUM = "BodyCheckSum";
    String ORIGINAL_BODY = "OriginalBody";
    String ORIGINAL_BODY_CHECKSUM = "OriginalBodyCheckSum";
    String CREATED_BY_ID = "CreatedById";
    String CREATED_DATE = "CreatedDate";
    String LAST_MODIFIED_BY_ID = "LastModifiedById";
    String LAST_MODIFIED_DATE = "LastModifiedDate";
    String SYSTEM_MODSTAMP = "SystemModstamp";
    String NAMESPACE_PREFIX = "NamespacePrefix";
    String CREATED_BY_NAME = "CreatedByName";
    String LAST_MODIFIED_BY_NAME = "LastModifiedByName";
    String PACKAGE_NAME = "PackageName";
    String FETCH_DATE = "FetchDate";
    String STATE = "State";
    String FILENAME = "Filename";
    String DESCRIPTION = "Description";
    String DEVELOPER_NAME = "DeveloperName";
    String HTML_WRAPPER = "HtmlWrapper";
    String CONTENT_SOURCE = "ContentSource";
    String CACHE_FILENAME = "packageCache.xml"; //$NON-NLS-1$
    String UNFILED_PUBLIC_FOLDER_NAME = "unfiled$public";

    // P A C K A G E P R O P S
    String ZIP_EXTENSION = ".zip";
    String DEFAULT_PACKAGED_NAME = "unpackaged";
    String PACKAGE_MANIFEST_FILE_NAME = "package.xml";
    String DESTRUCTIVE_MANIFEST_FILE_NAME = "destructiveChanges.xml";
    String SOURCE_FOLDER_NAME = "src";
    String REFERENCED_PACKAGE_FOLDER_NAME = "Referenced Packages";
    String XML = "xml";
    String SCHEMA_FILENAME = "salesforce.schema";

    String PACKAGE_MANIFEST_NAMESPACE_URI = "http://soap.sforce.com/2006/04/metadata";
    String PACKAGE_MANIFEST_METADATA = "metadata";
    String PACKAGE_MANIFEST_PACKAGE = "Package";
    String PACKAGE_MANIFEST_TYPES = "types";
    String PACKAGE_MANIFEST_TYPE_MEMBERS = "members";
    String PACKAGE_MANIFEST_VERSION = "version";
    String PACKAGE_MANIFEST_TYPE_NAME = "name";
    String PACKAGE_MANIFEST_WILDCARD = "*";
    String PACKAGE_MANIFEST_NAME = "fullName";
    String DEFAULT_METADATA_FILE_EXTENSION = "-meta.xml";

    // M I S C E L L A N E O U S
    int ZERO = 0;
    int MAX_IP_PORT_NUMBER = 65535;
    int MIN_IP_PORT_NUMBER = 1;
    String EMPTY_STRING = "";
    String NONE_STRING = "none";
    String NEW_LINE = "\n";
    String CONTENT_PLACE_HOLDER = "place holder";
    double CRC_NULL = 0.0;
    String UTF_8 = "UTF8";
    String HTTP = "http";
    String HTTPS = "https";
    String NAMESPACE_SEPARATOR = "__";
    int ERROR_CODE__44 = 44;
    String LOG_FILE_NAME = "force-ide.log";
    String DEFAULT_TEMPLATE_NAME = "Default";
    String SUBSCRIBE_TO_ALL = "*";
    String DOT = ".";
    String FOWARD_SLASH = "/";
    String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // REVIEWME: replace w/ ProjectExplorer.VIEW_ID once 3.2 support is deprecated
    String PROJECT_EXPLORER_ID = "org.eclipse.ui.navigator.ProjectExplorer";
    String SVN_DIR = ".svn";

    // J V M A R G U M E N T S
    String SYS_SETTING_TEMP_DIR = "force-ide-temp";
    String SYS_SETTING_DEBUG = "force-ide-debug";
    String SYS_LOG_LEVEL = "force-ide-log-level";
    String SYS_SETTING_DEBUG_VALUE = "true";
    String SYS_SETTING_PROPERTIES = "force-ide-properties";
    String SYS_SETTING_SFDC_INTERNAL = "sfdc-internal";
    String SYS_SETTING_SFDC_INTERNAL_VALUE = "true";
    String SYS_SETTING_DEFAULT_API_VERSION = "force-ide-api-version";
    String SYS_SETTING_POLL_LIMIT_MILLIS = "force-ide-poll-limit";
    String SYS_SETTING_MANIFEST_LISTENER = "force-ide-pm-listener";
    String SYS_SETTING_SKIP_COMPATIBILITY_CHECK = "force-ide-skip-compatibility-check";
    String SYS_SETTING_SKIP_COMPATIBILITY_CHECK_VALUE = "true";
    String SYS_SETTING_UPGRADE_ENABLE = "force-ide-upgrade-enable";
    
    // D E B U G G E R
    String SYS_SETTING_SFDC_DEBUGGER = "sfdc-debugger";
    String SYS_SETTING_X_FORCE_PROXY = "x-force-proxy";
    String SYS_SETTING_APEX_MANIFEST_TIMEOUT = "manifestTimeout";
    int APEX_MANIFEST_TIMEOUT_IN_MS_DEFAULT = 60_000;

    // P R O J E C T
    String LAST_USERNAME_SELECTED = "lastUserSelected";
    String LAST_ENV_SELECTED = "lastEnvSelected";
    String LAST_SERVER_SELECTED = "lastServerSelected";
    String LAST_KEEP_ENDPOINT_SELECTED = "lastKeepEndpointSelected";
    String LAST_PROTOCOL_SELECTED = "lastProtocolSelected";
    String PACKAGE_MANIFEST_FOLDER_SEPARATOR = "/";

    // P R O X Y
    String PROXY_PREFERENCE_3_2_X = "org.eclipse.ui.net.NetPreferences";
    String PROXY_PREFERENCE_3_3_X = "org.eclipse.wst.internet.internal.proxy.InternetPreferencePage";
    String PROXY_BUNDLE_3_2 = "org.eclipse.wst.internet.proxy";
    String PROXY_BUNDLE_3_3 = "org.eclipse.core.net";

    // Project properties page ids
    String PROJECT_PROPERTIES_PAGE_ID = "com.salesforce.ide.ui.properties.project";
    String PROJECT_CONTENT_PROPERTIES_PAGE_ID = "com.salesforce.ide.ui.properties.project.content";

    // Deployment Summary Constants.
    // This was pulled out of Deployment Summary becauase Eclipse 3.4.2 had a bug.
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=263877
    String NO_ACTION = "No Action";
    String GREEN = "GREEN";
    String YELLOW = "YELLOW";
    String RED = "RED";
    String GRAY = "GRAY";

    String SHARING_RULE_FILE_EXTENSION = "sharingRules";
    String ESCALATION_RULES_FILE_EXTENSTION = "escalationRule";
    String ASSIGNMENT_RULES_FILE_EXTENSTION = "assignmentRule";
    String AUTO_RESPONSE_RULES_FILE_EXTENSTION = "autoResponseRule";

    List<String> RULE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(SHARING_RULE_FILE_EXTENSION));
}
