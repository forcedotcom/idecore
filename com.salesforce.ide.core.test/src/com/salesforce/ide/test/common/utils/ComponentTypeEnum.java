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

import java.util.ArrayList;
import java.util.List;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.apex.clazz.ApexClassComponentController;
import com.salesforce.ide.core.internal.components.apex.component.ApexComponentComponentController;
import com.salesforce.ide.core.internal.components.apex.page.ApexPageComponentController;
import com.salesforce.ide.core.internal.components.apex.test.ApexTestSuiteComponentController;
import com.salesforce.ide.core.internal.components.apex.trigger.ApexTriggerComponentController;
import com.salesforce.ide.core.internal.components.application.CustomApplicationComponentController;
import com.salesforce.ide.core.internal.components.homepage.component.HomePageComponentComponentController;
import com.salesforce.ide.core.internal.components.homepage.layout.HomePageLayoutComponentController;
import com.salesforce.ide.core.internal.components.layout.LayoutComponentController;
import com.salesforce.ide.core.internal.components.letterhead.LetterheadComponentController;
import com.salesforce.ide.core.internal.components.object.CustomObjectComponentController;
import com.salesforce.ide.core.internal.components.profile.ProfileComponentController;
import com.salesforce.ide.core.internal.components.reportType.ReportTypeComponentController;
import com.salesforce.ide.core.internal.components.scontrol.SControlComponentController;
import com.salesforce.ide.core.internal.components.workflow.WorkflowComponentController;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;

/**
 * Holds information about components for tests
 * 
 */
@SuppressWarnings({ "nls" })
public enum ComponentTypeEnum {
    
    ApexClass(
        "ApexClass",
        ApexClassComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo("Apex Class", "classes", ".cls", "ApexClass", true)),
        
    ApexComponent(
        "ApexComponent",
        ApexComponentComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            "Visualforce Component",
            "components",
            ".component",
            "ApexComponent",
            true)),
            
    ApexPage(
        "ApexPage",
        ApexPageComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo("Visualforce Page", "pages", ".page", "ApexPage", true)),
        
    ApexTestSuite("ApexTestSuite", ApexTestSuiteComponentController.class, true, true, true, true,
    		new ComponentTypeEnum.ComponentTypeTestInfo("Apex Test Suite", "testSuites", ".testSuite", "ApexTestSuite", true)),
    
    ApexTrigger(
        "ApexTrigger",
        ApexTriggerComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo("Apex Trigger", "triggers", ".trigger", "ApexTrigger", true)),
        
    ApexTriggerCoupling("ApexTriggerCoupling", null, false, false, false, false),

    AuraDefinitionBundle("AuraDefinitionBundle", null, true, false, true, true),
    
    AnalyticSnapshot(
        "AnalyticSnapshot",
        null,
        false,
        true,
        true,
        true,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "analyticSnapshots",
            ".snapshot",
            Constants.ANALYTIC_SNAPSHOT,
            false)),
            
    AppMenu(
        "AppMenu",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "appMenus", ".appMenu", Constants.APP_MENU, true)),
        
    ApprovalProcess(
        "ApprovalProcess",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "approvalProcess",
            ".approvalProcess",
            Constants.APP_MENU,
            true)),
            
    AuthProvider(
        "AuthProvider",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "authproviders",
            ".authprovider",
            Constants.AUTH_PROVIDER,
            true)),
            
    CallCenter(
        "CallCenter",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "callCenters", ".callCenter", Constants.CALL_CENTER, true)),
        
    Community(
        "Community",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "communities", ".community", Constants.COMMUNITY, true)),
        
    ConnectedApp(
        "ConnectedApp",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "connectedapps",
            ".connectedapp",
            Constants.CONNECTED_APP,
            true)),
            
    CustomApplication(
        "CustomApplication",
        CustomApplicationComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            "Custom Application",
            "applications",
            ".app",
            Constants.CUSTOM_APPLICATION,
            true)),
            
    CustomApplicationComponent(
        "CustomApplicationComponent",
        CustomApplicationComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            "Custom Application Component",
            "customApplicationComponents",
            ".customApplicationComponent",
            Constants.CUSTOM_APPLICATION_COMPONENT,
            true)),
            
    CustomDataType(
        "CustomDataType",
        false,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "labels", ".labels", "CustomLabels", true)),
        
    CustomLabels(
        "CustomLabels",
        true,
        false,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "labels", ".labels", "CustomLabels", true)),
        
    CustomMetadata(
        "CustomMetadata",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "customMetadata", ".md", Constants.CUSTOM_METADATA, true)),
        
    CustomObject(
        "CustomObject",
        CustomObjectComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo("Custom Object", "objects", ".object", "CustomObject", true)),
        
    CustomObjectTranslation(
        "CustomObjectTranslation",
        false,
        false,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "objectTranslations",
            ".objectTranslation",
            "CustomObjectTranslation",
            true)),
            
    CustomPageWebLink(
        "CustomPageWebLink",
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "weblinks", ".weblink", "CustomPageWebLink", false)),
        
    CustomPermission(
        "CustomPermission",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "customPermissions",
            ".customPermission",
            Constants.CUSTOM_PERMISSION,
            true)),
            
    CustomSite(
        Constants.CUSTOM_SITE,
        false,
        false,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "sites", ".site", Constants.CUSTOM_SITE, true)),
        
    CustomTab(
        "CustomTab",
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "tabs", ".tab", "CustomTab", true)),
        
    DataCategoryGroup(
            "DataCategoryGroup",
            false,
            true,
            false,
            new ComponentTypeEnum.ComponentTypeTestInfo(
                null,
                "datacategorygroups",
                ".datacategorygroup",
                Constants.DATACATEGORYGROUP,
                true,
                new ComponentTypeWebUrlPart("/category/datacategorysetup.apexp"),
                new ComponentWebUrlPart("/category/datacategorysetup.apexp?"),
                new DisplayName("Data Category Group"))),
            
    Dashboard("Dashboard", false, false, true),
    
    Document(
        "Document",
        false,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "documents", ".document", "Document", false)),
        
    EntitlementProcess(
        "EntitlementProcess",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "entitlementProcesses",
            ".entitlementProcess",
            Constants.ENTITLEMENT_PROCESS,
            true)),
            
    EmailTemplate(
        "EmailTemplate",
        false,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "email", ".email", "EmailTemplate", false)),
        
    EntitlementTemplate(
        "EntitlementTemplate",
        false,
        true,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "entitlementTemplates",
            ".entitlementtemplate",
            Constants.ENTITLEMENT_TEMPLATE,
            true,
            new ComponentTypeWebUrlPart("/551"),
            new ComponentWebUrlPart(""),
            new DisplayName("Entitlement Template"))),
            
    Folder("Folder", false, true, true),
    
    FieldSet("FieldSet", null, true, false, false, false),
    
    FlexiPage(
        "FlexiPage",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "flexipages", ".flexipage", Constants.FLEXI_PAGE, true)),
        
    Group(
        "Group",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "groups", ".group", Constants.GROUP, true)),
        
    HomePageComponent(
        "HomePageComponent",
        HomePageComponentComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            "HomePage Component",
            "homePageComponents",
            ".homePageComponent",
            "HomePageComponent",
            true)),
            
    HomePageLayout(
        "HomePageLayout",
        HomePageLayoutComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            "HomePage Layout",
            "homePageLayouts",
            ".homePageLayout",
            "HomePageLayout",
            true)),
            
    InstalledPackage("InstalledPackage", false, false, false),
    
    Layout(
        "Layout",
        LayoutComponentController.class,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "layouts", ".layout", "Layout", true)), // create wizard is disabled
        
    Letterhead(
        "Letterhead",
        LetterheadComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo("Letterhead", "letterhead", ".letter", "Letterhead", true)),
        
    LiveChatAgentConfig(
        "LiveChatAgentConfig",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "liveChageAgentConfigs",
            ".liveChatAgentConfig",
            Constants.LIVE_CHAT_AGENT_CONFIG,
            true)),
            
    LiveChatButton(
        "LiveChatButton",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "liveChatButtons",
            ".liveChatButton",
            Constants.LIVE_CHAT_BUTTON,
            true)),
            
    LiveChatDeployment(
        "LiveChatDeployment",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "liveChatDeployments",
            ".liveChatDeployment",
            Constants.LIVE_CHAT_DEPLOYMENT,
            true)),
            
    MilestoneType(
        "MilestoneType",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "milestoneTypes",
            ".milestoneType",
            Constants.MILESTONE_TYPE,
            true)),
            
    Network(
        "Network",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "networks", ".networks", Constants.NETWORK, true)),
        
    PackageManifest("PackageManifest", true, true, true),
    
    Portal(
        Constants.PORTAL,
        false,
        false,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "portals", ".portal", Constants.PORTAL, true)),
        
    PostTemplate(
        "PostTemplate",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "postTemplates",
            ".postTemplate",
            Constants.POST_TEMPLATE,
            true)),
            
    Profile(
        "Profile",
        ProfileComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo("Profile", "profiles", ".profile", "Profile", true)),
        
    QuickAction(
        "QuickAction",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "quickActions",
            ".quickAction",
            Constants.QUICK_ACTION,
            true)),
            
    RemoteSiteSetting(
        "RemoteSiteSetting",
        false,
        true,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "remoteSiteSettings",
            ".remoteSite",
            Constants.REMOTE_SITE_SETTING,
            true,
            new ComponentTypeWebUrlPart("/0rp"),
            new ComponentWebUrlPart(""),
            new DisplayName("Remote Site Setting"))),
            
    Report("Report", false, false, true),
    
    ReportType(
        "ReportType",
        ReportTypeComponentController.class,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "reportTypes", ".reportType", "ReportType", true)), // create wizard is disabled
        
    Role(
        "Role",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "roles", ".role", Constants.ROLE, true)),
        
    SamlSsoConfig(
        "SamlSsoConfig",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "samlssoconfigs",
            ".samlssoconfig",
            Constants.SAML_SSO_CONFIG,
            true)),
            
    Scontrol(
        "Scontrol",
        SControlComponentController.class,
        false,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "scontrols", ".scf", "Scontrol", true)),
        
    Settings(
        "Settings",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "settings", ".setting", Constants.SETTING, true)),
        
    SharingSet(
        "SharingSet",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "sharingSets", ".sharingSet", Constants.SHARING_SET, true)),
        
    Skill(
        "Skill",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "skills", ".skill", Constants.SKILL, true)),
        
    SiteDotCom(
        "SiteDotCom",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "siteDotComSites", ".site", Constants.SITE_DOT_COM, true)),
        
    StandardObject("StandardObject", false, false, true),
    
    StaticResource(
        "StaticResource",
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "staticresources", ".resource", "StaticResource", true)),
        
    SharingReason(
        "SharingReason",
        CustomObjectComponentController.class,
        false,
        false,
        false,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "objects", ".object", "CustomObject", false)),
        
    Territory(
        "Territory",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "territories", ".territory", Constants.TERRITORY, true)),
        
    Translations(
        Constants.TRANSLATIONS,
        false,
        false,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "translations", ".translation", "Translations", true)),
        
    Unknown("Unknown", true, true, true),
    
    Workflow(
        "Workflow",
        WorkflowComponentController.class,
        true,
        false,
        false,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo("Workflow", "workflows", ".workflow", "Workflow", true)),
    Weblink(
        "WebLink",
        CustomObjectComponentController.class,
        false,
        false,
        false,
        false,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "objects", ".object", "CustomObject", false)),
        
    XOrgHub(
        "XOrgHub",
        null,
        false,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(null, "xorghubs", ".xorghub", Constants.X_ORG_HUB, true)),
        
    PermissionSet(
        "PermissionSet",
        null,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "permissionsets",
            ".permissionset",
            "PermissionSet",
            false,
            new ComponentTypeWebUrlPart("0PS"),
            new ComponentWebUrlPart(""),
            new DisplayName("Permission Set"))),
            
    Flow(
        "Flow",
        null,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "flows",
            ".flow",
            "Flow",
            false,
            new ComponentTypeWebUrlPart("300?setupid=InteractionProcesses"),
            new ComponentWebUrlPart("designer/designer.apexp#Id="),
            new DisplayName("Flow"))),
            
    Queue(
        "Queue",
        null,
        true,
        true,
        true,
        true,
        new ComponentTypeEnum.ComponentTypeTestInfo(
            null,
            "queues",
            ".queue",
            "Queue",
            false,
            new ComponentTypeWebUrlPart("p/own/Queue/d?setupid=Queues"),
            new ComponentWebUrlPart(""),
            new DisplayName("Queue"))),
            
    // Sharing Rules
    ////////////////
    
    AccountSharingRules("AccountSharingRules", false, false, false),
    AccountTerritorySharingRules("AccountTerritorySharingRules", false, false, false),
    AccountTerritorySharingRule("AccountTerritorySharingRule", false, false, false),
    CampaignSharingRules("CampaignSharingRules", false, false, false),
    CaseSharingRules("CaseSharingRules", false, false, false),
    ContactSharingRules("ContactSharingRules", false, false, false),
    CustomObjectSharingRules("CustomObjectSharingRules", false, false, false),
    LeadSharingRules("LeadSharingRules", false, false, false),
    OpportunitySharingRules("OpportunitySharingRules", false, false, false),
    UserSharingRules("UserSharingRules", false, false, false),
    
    AccountOwnerSharingRule("AccountOwnerSharingRule", false, false, false),
    AccountCriteriaBasedSharingRule("AccountCriteriaBasedSharingRule", false, false, false),
    CampaignOwnerSharingRule("CampaignOwnerSharingRule", false, false, false),
    CampaignCriteriaBasedSharingRule("CampaignCriteriaBasedSharingRule", false, false, false),
    CaseCriteriaBasedSharingRule("CaseCriteriaBasedSharingRule", false, false, false),
    CaseOwnerSharingRule("CaseOwnerSharingRule", false, false, false),
    ContactOwnerSharingRule("ContactOwnerSharingRule", false, false, false),
    ContactCriteriaBasedSharingRule("ContactCriteriaBasedSharingRule", false, false, false),
    CustomObjectOwnerSharingRule("CustomObjectOwnerSharingRule", false, false, false),
    CustomObjectCriteriaBasedSharingRule("CustomObjectCriteriaBasedSharingRule", false, false, false),
    LeadOwnerSharingRule("LeadOwnerSharingRule", false, false, false),
    LeadCriteriaBasedSharingRule("LeadCriteriaBasedSharingRule", false, false, false),
    OpportunityOwnerSharingRule("OpportunityOwnerSharingRule", false, false, false),
    OpportunityCriteriaBasedSharingRule("OpportunityCriteriaBasedSharingRule", false, false, false),
    UserMembershipSharingRule("UserMembershipSharingRule", false, false, false),
    UserCriteriaBasedSharingRule("UserCriteriaBasedSharingRule", false, false, false),
    
    AutoResponseRules("AutoResponseRules", false, false, false),
    AutoResponseRule("AutoResponseRule", false, false, false),
    
    AssignmentRules("AssignmentRules", false, false, false),
    AssignmentRule("AssignmentRule", false, false, false),
    
    EscalationRules("EscalationRules", false, false, false),
    EscalationRule("EscalationRule", false, false, false);
    
    public static List<ComponentTypeEnum> getCreatableTypes() {
        List<ComponentTypeEnum> creatableTypes = new ArrayList<ComponentTypeEnum>();
        for (ComponentTypeEnum type : ComponentTypeEnum.values()) {
            if (type.isCreatable())
                creatableTypes.add(type);
        }
        return creatableTypes;
    }
    
    public static List<String> getAllTypeNames() {
        List<String> allTypes = new ArrayList<String>();
        for (ComponentTypeEnum type : ComponentTypeEnum.values()) {
            allTypes.add(type.getTypeName());
        }
        return allTypes;
    }
    
    /**
     * returns the component types for which a wizard is defined. checks that the String WizardName in
     * componentTypeTestInfo isn't empty.
     * 
     * @return
     */
    public static List<ComponentTypeEnum> getTypesThatHaveWizards() {
        List<ComponentTypeEnum> hasWizardTypes = new ArrayList<ComponentTypeEnum>();
        for (ComponentTypeEnum type : ComponentTypeEnum.values()) {
            ComponentTypeTestInfo tempComponentinfo = type.getComponentTypeTestInfo();
            
            if (Utils.isNotEmpty(tempComponentinfo) && Utils.isNotEmpty(tempComponentinfo.getWizardNameIfAny())) {
                hasWizardTypes.add(type);
            }
        }
        return hasWizardTypes;
    }
    
    /**
     * returns the component types for which a wizard is defined. checks that the String WizardName in
     * componentTypeTestInfo isn't empty.
     * 
     * @return
     */
    public static List<ComponentTypeEnum> getTypesThatSupportWildcard() {
        List<ComponentTypeEnum> wildcardSupportedTypes = new ArrayList<ComponentTypeEnum>();
        for (ComponentTypeEnum type : ComponentTypeEnum.values()) {
            ComponentTypeTestInfo tempComponentinfo = type.getComponentTypeTestInfo();
            
            if (Utils.isNotEmpty(tempComponentinfo) && tempComponentinfo.isWildCardSupported()) {
                wildcardSupportedTypes.add(type);
            }
        }
        return wildcardSupportedTypes;
    }
    
    static class ComponentTypeWebUrlPart {
        
        private final String urlPart;
        
        public ComponentTypeWebUrlPart(String componentTypeWebUrlPart) {
            this.urlPart = componentTypeWebUrlPart;
        }
        
        @Override
        public String toString() {
            return this.urlPart;
        }
        
    }
    
    static class DisplayName {
        
        private final String displayName;
        
        public DisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return this.displayName;
        }
        
    }
    
    static class ComponentWebUrlPart {
        
        private final String urlPart;
        
        public ComponentWebUrlPart(String componentTypeWebUrlPart) {
            this.urlPart = componentTypeWebUrlPart;
        }
        
        @Override
        public String toString() {
            return this.urlPart;
        }
        
    }
    
    private final String typeName;
    private final boolean creatable;
    private final Class<? extends ComponentController> componentComponentController;
    private final boolean defaultRetrieve;
    private final boolean deletable;
    private final boolean packageable;
    private final boolean isException; //used if this components behaviour is different from the other components.
    private final ComponentTypeTestInfo componentTypeTestInfo;
    
    private ComponentTypeEnum(String type, boolean defaultRetrieve, boolean deletable, boolean packageable) {
        this.typeName = type;
        this.creatable = false;
        this.componentComponentController = null;
        this.defaultRetrieve = defaultRetrieve;
        this.deletable = deletable;
        this.packageable = packageable;
        this.isException = false;
        this.componentTypeTestInfo = null;
    }
    
    private ComponentTypeEnum(
        String type,
        boolean defaultRetrieve,
        boolean deletable,
        boolean packageable,
        ComponentTypeTestInfo componentTypeInfo) {
        this.typeName = type;
        this.creatable = false;
        this.componentComponentController = null;
        this.defaultRetrieve = defaultRetrieve;
        this.deletable = deletable;
        this.packageable = packageable;
        this.isException = false;
        this.componentTypeTestInfo = componentTypeInfo;
    }
    
    private ComponentTypeEnum(
        String type,
        Class<? extends ComponentController> componentComponentController,
        boolean creatable,
        boolean defaultRetrieve,
        boolean deletable,
        boolean packageable) {
        this.typeName = type;
        this.creatable = creatable;
        this.componentComponentController = componentComponentController;
        this.defaultRetrieve = defaultRetrieve;
        this.deletable = deletable;
        this.packageable = packageable;
        this.isException = false;
        this.componentTypeTestInfo = null;
        
    }
    
    private ComponentTypeEnum(
        String type,
        Class<? extends ComponentController> componentComponentController,
        boolean creatable,
        boolean defaultRetrieve,
        boolean deletable,
        boolean packageable,
        ComponentTypeTestInfo componentTypeInfo) {
        this.typeName = type;
        this.creatable = creatable;
        this.componentComponentController = componentComponentController;
        this.defaultRetrieve = defaultRetrieve;
        this.deletable = deletable;
        this.packageable = packageable;
        this.isException = false;
        this.componentTypeTestInfo = componentTypeInfo;
    }
    
    private ComponentTypeEnum(
        String type,
        Class<? extends ComponentController> componentComponentController,
        boolean creatable,
        boolean defaultRetrieve,
        boolean deletable,
        boolean packageable,
        boolean isException) {
        this.typeName = type;
        this.creatable = creatable;
        this.componentComponentController = componentComponentController;
        this.defaultRetrieve = defaultRetrieve;
        this.deletable = deletable;
        this.packageable = packageable;
        this.isException = isException;
        this.componentTypeTestInfo = null;
        
    }
    
    private ComponentTypeEnum(
        String type,
        Class<? extends ComponentController> componentComponentController,
        boolean creatable,
        boolean defaultRetrieve,
        boolean deletable,
        boolean packageable,
        boolean isException,
        ComponentTypeTestInfo componentTypeInfo) {
        this.typeName = type;
        this.creatable = creatable;
        this.componentComponentController = componentComponentController;
        this.defaultRetrieve = defaultRetrieve;
        this.deletable = deletable;
        this.packageable = packageable;
        this.isException = isException;
        this.componentTypeTestInfo = componentTypeInfo;
    }
    
    public ComponentController getComponentController() throws ForceProjectException {
        ComponentController componentController = null;
        try {
            componentController = componentComponentController.newInstance();
        } catch (InstantiationException e) {
            throw new ForceProjectException(e);
        } catch (IllegalAccessException e) {
            throw new ForceProjectException(e);
        }
        return componentController;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public boolean isCreatable() {
        return creatable;
    }
    
    public boolean isDefaultRetrieve() {
        return defaultRetrieve;
    }
    
    public boolean isDeletable() {
        return deletable;
    }
    
    public boolean isException() {
        return isException;
    }
    
    public boolean isPackageable() {
        return packageable;
    }
    
    public String getWizardNameIfAny() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo) ? componentTypeTestInfo.getWizardNameIfAny() : null;
    }
    
    public String getParentFolderName() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo) ? componentTypeTestInfo.getParentFolderName() : null;
    }
    
    public String getFileExtension() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo) ? componentTypeTestInfo.getFileExtension() : null;
    }
    
    public String getPackageXMLTypeIdentifier() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo)
            ? componentTypeTestInfo.getPackageXMLTypeIdentifier()
            : null;
    }
    
    public ComponentTypeTestInfo getComponentTypeTestInfo() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo) ? componentTypeTestInfo : null;
    }
    
    public String getComponentTypeWebUrlPart() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo)
            ? componentTypeTestInfo.getComponentTypeWebUrlPart().toString()
            : null;
    }
    
    public String getComponentWebUrlPart() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo)
            ? componentTypeTestInfo.getComponentWebUrlPart().toString()
            : null;
    }
    
    public String getDisplayName() {
        return IdeTestUtil.isNotEmpty(componentTypeTestInfo) ? componentTypeTestInfo.getDisplayName().toString() : null;
    }
    
    /**
     * Creating this special sub structure to hold more information about the component.
     * 
     * @author ssasalatti
     *         
     */
    public static class ComponentTypeTestInfo {
        String wizardNameIfAny;
        String parentFolderName;
        String fileExtension;
        String packageXMLTypeIdentifier;
        boolean isWildCardSupported;
        ComponentTypeWebUrlPart componentTypeWebUrlPart;
        ComponentWebUrlPart componentWebUrlPart;
        DisplayName displayName;
        
        public DisplayName getDisplayName() {
            return displayName;
        }
        
        public ComponentTypeWebUrlPart getComponentTypeWebUrlPart() {
            return componentTypeWebUrlPart;
        }
        
        public ComponentWebUrlPart getComponentWebUrlPart() {
            return componentWebUrlPart;
        }
        
        public ComponentTypeTestInfo(
            String wizardName,
            String parentFolderName,
            String fileExtension,
            String packageXMLTypeIdentifier,
            boolean isWildCardSupported) {
            this.wizardNameIfAny = wizardName;
            this.parentFolderName = parentFolderName;
            this.fileExtension = fileExtension;
            this.packageXMLTypeIdentifier = packageXMLTypeIdentifier;
            this.isWildCardSupported = isWildCardSupported;
            this.componentTypeWebUrlPart = null;
            this.componentWebUrlPart = null;
            this.displayName = null;
        }
        
        public ComponentTypeTestInfo(
            String wizardNameIfAny,
            String parentFolderName,
            String fileExtension,
            String packageXMLTypeIdentifier,
            boolean isWildCardSupported,
            ComponentTypeWebUrlPart componentTypeWebUrlPart,
            ComponentWebUrlPart componentWebUrlPart) {
            this(wizardNameIfAny, parentFolderName, fileExtension, packageXMLTypeIdentifier, isWildCardSupported);
            this.componentTypeWebUrlPart = componentTypeWebUrlPart;
            this.componentWebUrlPart = componentWebUrlPart;
        }
        
        public ComponentTypeTestInfo(
            String wizardNameIfAny,
            String parentFolderName,
            String fileExtension,
            String packageXMLTypeIdentifier,
            boolean isWildCardSupported,
            ComponentTypeWebUrlPart componentTypeWebUrlPart,
            ComponentWebUrlPart componentWebUrlPart,
            DisplayName displayName) {
            this(
                wizardNameIfAny,
                parentFolderName,
                fileExtension,
                packageXMLTypeIdentifier,
                isWildCardSupported,
                componentTypeWebUrlPart,
                componentWebUrlPart);
            this.displayName = displayName;
        }
        
        public String getWizardNameIfAny() {
            return wizardNameIfAny;
        }
        
        public String getParentFolderName() {
            return parentFolderName;
        }
        
        public String getFileExtension() {
            return fileExtension;
        }
        
        public String getPackageXMLTypeIdentifier() {
            return packageXMLTypeIdentifier;
        }
        
        public boolean isWildCardSupported() {
            return isWildCardSupported;
        }
        
    }
    
}
