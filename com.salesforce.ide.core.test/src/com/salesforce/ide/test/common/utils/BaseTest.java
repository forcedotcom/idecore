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

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

import junit.framework.AssertionFailedError;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.salesforce.ide.core.internal.utils.*;
import com.salesforce.ide.test.common.utils.OrgTestingUtil.*;
import com.salesforce.ide.test.common.utils.remote.RemoteAppCall;
import com.salesforce.ide.test.common.utils.remote.RemoteOperationEnum;
import com.salesforce.ide.test.common.utils.remote.RemoteAppCall.RequestObject;
import com.salesforce.ide.test.common.utils.remote.RemoteOperationEnum.OperationKey;

/**
 * Base Test class for IDE test Framework. All tests should extend this for runtime org creation
 * 
 * @author agupta
 * @deprecated use only idetestcase
 *  */
public class BaseTest extends SimpleTestCase {

    private static final Logger logger = Logger.getLogger(BaseTest.class);

    private static Map<OrgTypeEnum, OrgTestingUtil> createdOrgs = new HashMap<OrgTypeEnum, OrgTestingUtil>();

    private OrgTestingUtil orgUtil = null;
    private OrgTypeEnum orgType = null;
    private PackageInfo _package = null;

    public BaseTest() {}

    public BaseTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        orgUtil = getTestOrg(orgType);
        disableAutoBuild();
    }

    @Override
    protected void tearDown() throws Exception {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target( { ElementType.TYPE, ElementType.METHOD })
    public @interface TestOrgTypes {
        OrgTypeEnum[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target( { ElementType.TYPE, ElementType.METHOD })
    public @interface TestPackaging {
    }

    protected Throwable appendError(Throwable base, String msgPrefix, Throwable next) {
        StringBuffer strBuff = new StringBuffer("");
        if (base != null) {
            strBuff.append(base.getMessage()).append("\n");
        }

        Throwable rootTh = ForceExceptionUtils.getRootCause(next);
        String newMessage = ForceExceptionUtils.getRootCauseMessage(next);
        if (Utils.isNotEmpty(newMessage)) {
            newMessage = newMessage.replaceAll("\n", "\n\t");
        }

        strBuff.append(msgPrefix).append(".\n\t Exception: ").append(rootTh.getClass().getSimpleName()).append(
            "\n\t Cause: ").append(newMessage);

        // Get the full stack trace, including nested exceptions
        StackTraceElement[] rootStacktrace = rootTh.getStackTrace();

        base = new Throwable(strBuff.toString());
        base.setStackTrace(rootStacktrace);
        return base;
    }

    @Override
    public void runBare() throws Throwable {
        String methodName = getTestMethodName();
        if (Utils.isEmpty(methodName)) {
            throw new IllegalArgumentException("Test method name is null or empty");
        }

        logger.info("Executing test method '" + methodName + "'");
        Method method = getClass().getMethod(methodName);
        assert method != null;
        boolean throwError = false;
        boolean throwFailure = false;
        Throwable error = null;

        // Get the list of the Orgs to test against and run the test
        // once per each
        TestOrgTypes orgsToTest = method.getAnnotation(TestOrgTypes.class);

        if (orgsToTest == null) {
            // Inherit defaults from the test class itself
            orgsToTest = getClass().getAnnotation(TestOrgTypes.class);
        }

        OrgTypeEnum[] orgTypes = new OrgTypeEnum[] { OrgTypeEnum.Developer };
        if (null != orgsToTest) {
            if (orgsToTest.value().length == 1 && orgsToTest.value()[0] == OrgTypeEnum.ALL)
                orgTypes = OrgTypeEnum.values();
            else
                orgTypes = orgsToTest.value();
        }

        for (OrgTypeEnum orgType : orgTypes) {
            if (orgType != OrgTypeEnum.ALL && orgType !=OrgTypeEnum.Custom) {
                this.orgType = orgType;
                try {
                    super.runBare();
                } catch (Throwable t) {
                    if (t instanceof AssertionFailedError) {
                        throwFailure = true;
                    } else {
                        throwError = true;
                    }

                    error = appendError(error, "\nTest (" + getName() + ") failed for Org type: " + orgType.name(), t);
                }
            }
        }

        if (throwError) {
            throw error;
        } else if (throwFailure) {
            AssertionFailedError afe = new AssertionFailedError(error.getMessage());
            afe.setStackTrace(error.getStackTrace());
            throw afe;
        }
    }

    @Override
    public void runTest() throws Throwable {
        Method method = getClass().getMethod(getTestMethodName());
        assert method != null;
        boolean throwError = false;
        boolean throwFailure = false;
        Throwable error = null;

        // Get the list of the packages to test against and run the test
        // once per each
        TestPackaging packagingTest = method.getAnnotation(TestPackaging.class);
        if (packagingTest == null) {
            // Inherit defaults from the test class itself
            packagingTest = getClass().getAnnotation(TestPackaging.class);
        }

        List<PackageInfo> packagesToTest = new ArrayList<PackageInfo>();
        if (null != packagingTest) {
            packagesToTest.addAll(this.orgUtil.getPackages());
        }
        packagesToTest.add(0, null);

        for (PackageInfo pack : packagesToTest) {
            try {
                this._package = pack;
                super.runTest();
            } catch (Throwable t) {
                logger.error("Error occured while execution '" + getTestMethodName() + "'", t);
                if (t instanceof AssertionFailedError) {
                    throwFailure = true;
                } else {
                    throwFailure = true;
                }

                error =
                        appendError(error, "Test (" + getName() + ") failed for package: "
                                + (_package == null ? "unpackaged" : _package.toString()), t);
            }
        }

        if (throwError) {
            throw new Throwable(error);
        } else if (throwFailure) {
            AssertionFailedError afe = new AssertionFailedError(error.getMessage());
            afe.setStackTrace(error.getStackTrace());
            throw afe;
        }
    }

    protected OrgTestingUtil getTestOrg(OrgTypeEnum orgType) throws Exception {

        this.orgUtil = createdOrgs.get(orgType);
        if (this.orgUtil == null) {
            switch (orgType) {
            case Production:
            case Enterprise:
                this.orgUtil = createTestOrg(OrgEdition.EE, true);
                break;
            case Developer:
                this.orgUtil = createTestOrg(OrgEdition.DE, true);
                break;
            case Namespaced:
                this.orgUtil = createTestOrg(OrgEdition.DE, true);
                this.orgUtil.addUniqueNamespace();
                this.orgUtil.createUniquePackage(true);
                break;
            case ALL:
                break;
            case Custom:
                break;
            }
            this.orgUtil.createUniquePackage(false);
            createdOrgs.put(orgType, this.orgUtil);
        }

        return createdOrgs.get(orgType);
    }

    protected OrgTestingUtil createTestOrg(OrgEdition orgType, boolean createNew) throws Exception {

        logger.info("Creating a new Org for testing. Edition:" + orgType.toString());
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_EDITION.toString(), orgType.toString());
        RequestObject rObject =
                new RequestObject(null, createNew ? RemoteOperationEnum.CREATE_NEW_ORG
                        : RemoteOperationEnum.GET_OR_CREATE_ORG, params);
        Properties result = RemoteAppCall.sendRequest(rObject);
        OrgInfo newOrg = new OrgInfo();
        newOrg.setOrgEdition(orgType);
        newOrg.setOrgId(result.getProperty(OperationKey.ORG_ID.toString()));
        newOrg.setUsername(result.getProperty(OperationKey.USERNAME.toString()));
        // newOrg.setSoapEndPoint(BaseTestingUtil.getSoapEndpoint());
        newOrg.setSoapEndPointServer(IdeTestUtil.getAppServerToHitForTests(false));
        logger.info("Created a new test Org: " + newOrg.toString());
        OrgTestingUtil newOrgUtil = new OrgTestingUtil(newOrg);
        // logger.info("Updating Org whitelist for tests to access api");
        // newOrgUtil.addHostToWhiteList();
        // logger.info("Updated Org whitelist for tests to access api");
        return newOrgUtil;
    }

    protected void handleFailure(String message, Exception e) {
        logger.error(message + ": " + e.getMessage());
        Throwable rootException = ForceExceptionUtils.getRootCause(e);
        if (rootException != null) {
            rootException.printStackTrace();
            fail(message + ": " + rootException.getMessage());
        } else {
            e.printStackTrace();
            fail(message + ": " + e.getMessage());
        }
    }

    /**
     * To obtain the package against which the current test has to be run.
     * 
     * @return Returns null for unpackaged scenario
     */
    public PackageInfo getPackage() {
        return _package;
    }

    public OrgTestingUtil getOrgUtil() {
        return orgUtil;
    }

    protected int executeCommand(String cmd) {
        logger.info("Executing cmd '" + cmd + "'");
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

            if (proc.exitValue() == 0) {
                logger.info("Succesfully executing cmd '" + cmd + "'");
            } else {
                InputStream in = proc.getErrorStream();
                byte[] buffer = new byte[256];
                int bytes_read;
                int tb = 0;
                String output = "";
                while ((bytes_read = in.read(buffer)) != -1) {
                    output += new String(buffer);
                    tb += bytes_read;
                }

                String error = output.substring(0, tb);
                logger.error("Error executing cmd '" + cmd + "'\n" + error);
            }
            return proc.exitValue();
        } catch (Exception e) {
            logger.error("Error executing cmd '" + cmd + "'", e);
            return -1;
        }
    }

    protected String getContentString(IFile file) throws IOException, CoreException {
        String contentStr = null;
        if (file != null && file.exists()) {
            StringBuffer strBuff = new StringBuffer();
            BufferedReader reader = null;
            InputStream contents = null;
            try {
                contents = file.getContents();
                reader = new BufferedReader(new InputStreamReader(contents, Constants.UTF_8));
                String line = reader.readLine();
                if (line != null) {
                    strBuff.append(line);
                }
                while ((line = reader.readLine()) != null) {
                    strBuff.append(Constants.NEW_LINE);
                    strBuff.append(line);
                }
            } catch (IOException e) {
                logger.error("Unable to load body from file " + file.getName(), e);
                throw e;
            } catch (CoreException e) {
                throw e;
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    // do nothing
                }

                try {
                    if (contents != null) {
                        contents.close();
                    }
                } catch (IOException e) {
                    // do nothing
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Loaded body size [" + strBuff.toString().getBytes().length + "] bytes from file '"
                        + file.getName() + "'");
            }

            contentStr = strBuff.toString();
            try {
                contentStr = new String(strBuff.toString().getBytes(), Constants.UTF_8);
            } catch (UnsupportedEncodingException e) {
                logger.error("Unable to set body", e);
            }
        }

        return contentStr;
    }
}
