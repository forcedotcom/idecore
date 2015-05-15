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
package com.salesforce.ide.test.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.salesforce.ide.test.common.utils.ComponentTypeEnum;
import com.salesforce.ide.test.common.utils.IdeProjectContentTypeEnum;
import com.salesforce.ide.test.common.utils.IdeTestConstants;
import com.salesforce.ide.test.common.utils.OrgTypeEnum;
import com.salesforce.ide.test.common.utils.PackageTypeEnum;

/**
 * <pre>
 * This Annotation should be provided for each test class/Method.
 * The annotation at the method level will override the annotation at the class level.
 * Specify the following
 * needOrg (default = false)
 *          specify true if the test needs an org.The org could be on any instance
 * 
 * useOrgOnInstancePrefix ( default = _LOCALHOST ) 
 *      specify 'prefix' entry from test.properties that will be used as the instance for this test.
 *         for ex: to run a test against an org, the following entries should exist in test.properties
 *         'prefix'.endpoint
 *         'prefix'.username
 *         'prefix'.password
 *         This needs to be used in conjunction with runForOrgType().
 *         The default type for runforOrgType is developer. And it can be possible that your specific org is of type Enterprise. 
 *         Therefore you need to specify the prefix and the orgType.
 *         
 *         test.properties already contains some orgs that you can use. See Developer.*, Namespace.*, Enterprise.* stanzas.
 *         So to use the Enterprise org. you would specify useOrgOnInstancePrefix="Enterprise" and runForOrgType = OrgTypeEnum.Enterprise.
 *         
 *         If you want to specify your own org, i.e. use your own prefix, then the runForOrgType must be set to Custom.
 *         
 * runForOrgType() default OrgTypeEnum.Developer ;
 *      NOTE : This setting will have no effect if you don't set needOrg
 *      
 * forceOrgCreation(default =false)
 *          will create org only when test run against localhost
 * 
 * needMoreMetadataDataInOrg (default = false)
 * 
 * addMetaDataFromPath (default = &quot;&quot;)
 * 
 * addMetadataDataAsPackage (default = packageTypeEnum.UNPACKAGED)
 *      NOTE : This setting will have no effect if you don't set needDataInOrg
 * 
 * needProject (default = false)
 * 
 * setProjectContentConfig ( default =  IdeProjectContentTypeEnum.ALL)
 *          use if you need a selective project create
 *          NOTE : This setting will have no effect if you don't set needProject
 * 
 * ComponentTypeEnum[] setComponentListIfSelectiveProjectCreate();
 *          -use if you are doing a selective project create and want only certain components.
 *          -this cannot be empty if you selected IdeProjectContentTypeEnum.SPECIFIC_COMPONENTS for setProjectContentConfig
 *          -if you specify values here and the setProjectContentConfig() is not IdeProjectContentTypeEnum.SPECIFIC_COMPONENTS, they will be ignored
 * 
 * ignoreOrgCleanSanityCheck (default = false)
 *      will ignore org clean sanity check at teardown - applicable for deploying component type which doesn't support remote delete
 * 
 * forceRevokeOrgFromLocalOrgCacheAfterTest (default = false)
 *      will force revoke current-in-use org type from cache; therefore, subsequent test will recreate org for that org type.
 *      - applicable for org that ends in unclean stage after test.
 *      - applicable for tests running against local orgs only.
 *      
 * errorDialogExpectedDuringTest (default = false)
 *      will tell the framework that an errorDialog is expected in the test and will not be checked for during teardown.
 *      This is done to differentiate between which error dialog is legitimate and which one is unexpected.
 * 
 * ingnoreProjectCleanedAfterTestCheck( default = false)
 *      will tell the framework not to check if the project has been removed after the test.
 * 
 * useDebugApexStreamingUser( default = false)
 *      Instead of enabling user perms, this will get or create a user with MAD, VAD, DebugApex,
 *      and streaming permissions on the ApexDebuggerEvent system push topic.  
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface IdeSetupTest {
    boolean needOrg() default false;

    String useOrgOnInstancePrefix() default IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX_VALUE_LOCAL;

    OrgTypeEnum runForOrgType() default OrgTypeEnum.Developer;

    boolean forceOrgCreation() default false;

    String[] enableOrgPerms() default {};

    String[] enableUserPerms() default {};
    
    boolean useDebugApexStreamingUser() default false;

    boolean ignoreOrgCleanSanityCheck() default false;

    boolean forceRevokeOrgFromLocalOrgCacheAfterTest() default false;

    boolean needMoreMetadataDataInOrg() default false;

    String addMetaDataFromPath() default "";

    PackageTypeEnum addMetadataDataAsPackage() default PackageTypeEnum.UNPACKAGED;

    boolean needProject() default false;

    String importProjectFromPath() default "";
    
    boolean ingnoreProjectCleanAfterTestCheck() default false;

    IdeProjectContentTypeEnum setProjectContentConfig() default IdeProjectContentTypeEnum.ALL;

    ComponentTypeEnum[] setComponentListIfSelectiveProjectCreate() default {};

    boolean errorDialogExpectedDuringTest() default false;

    boolean autoBuildOn() default false;
    
    boolean skipMetadataRemovalDuringTearDown() default false;

    // PackageTypeEnum[] runTestForPackages() default {
    // PackageTypeEnum.UNPACKAGED };

}
