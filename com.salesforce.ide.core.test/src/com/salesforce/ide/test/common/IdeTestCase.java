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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.test.common.utils.IdeProjectContentTypeEnum;
import com.salesforce.ide.test.common.utils.IdeProjectFixture;
import com.salesforce.ide.test.common.utils.IdeTestConstants;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.salesforce.ide.test.common.utils.OrgTypeEnum;
import com.salesforce.ide.test.common.utils.PackageTypeEnum;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;
import com.salesforce.ide.test.common.utils.IdeProjectFixture.ProjInfo;

/**
 * This class serves as the Base class for tests. All tests should in some way inherit from this class. Based on the
 * Test Class or methods' annotation preference this class will:
 * 
 * Setup the test: Create an Org. Won't re-create unless forced to.
 * 
 * Load additional MetaData if required.
 * 
 * Create a Project if required.
 * 
 * Run the test.
 * 
 * Teardown the test: Delete data from the Org. Destroy the project(s).
 * 
 * look at Ide*command.java to see how things are being setup and tore down. look at *Fixtures.java to know about the
 * helper classes that are being used.
 * 
 * @author ssasalatti
 */
public class IdeTestCase extends TestCase {

    private static final Logger logger = Logger.getLogger(IdeTestCase.class);
    
    /**
     * A null Progress Monitor.
     */
    protected final IProgressMonitor nullProgressmonitor = new NullProgressMonitor();

    /**
     * null if test doesn't need org. test method should check this before using it.
     */
    private OrgInfo orgInfoForCurrentTest = null;

    /**
     * null if test doesn't need org. test method should check this before using it.
     */
    private ProjInfo projInfoCurrentTest = null;

    /**
     * the invoker that contains the queue of commands used in setup and teardown.
     */
    private IdeTestCommandInvoker invoker;

    /**
     * A test shall time out after these many milliseconds
     */
    private long testTimeout;

    /**
     * the annotation on the test method or the class
     */
    private IdeSetupTest ideSetupTestAnnotation;

    /**
     * the org being used in this test. caller should check for nullness before proceeding.
     * 
     * @return null if one isn't being used
     */
    public OrgInfo getOrgInfoForCurrentTest() {
        return orgInfoForCurrentTest;
    }

    /**
     * the project being used in this test. caller should check for nullness before proceeding.
     * 
     * @return null if one isn't being used.
     */
    public ProjInfo getProjInfoCurrentTest() {
        return projInfoCurrentTest;
    }

    // ----------------------Test Methods------------------------------
    @Override
    public void setUp() throws Exception {
        logger.info("Setting up test:" + getName());

        // set auto build to desired setting; defaults to false
        //Moving to commands that take care of creating/importing projects. See IdeTestSetup*ProjectCommand.java.
        // IdeProjectFixture.getInstance().switchAutoBuild(ideSetupTestAnnotation.autoBuildOn());

        //Set the system property to specify which instance the test is running against
        System
                .setProperty(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX, ideSetupTestAnnotation
                        .useOrgOnInstancePrefix());

        invoker = new IdeTestCommandInvoker(ideSetupTestAnnotation);

        // if this is a ui test. then we need to append a ui setup to it.
        String className = getClass().getSimpleName();
        if (className.endsWith("_botui") || className.endsWith("_pdeui"))
            invoker.insertCommandIntoQueue(new IdeTestSetupUITestCommand(ideSetupTestAnnotation));

        //go and setup up everything.
        invoker.invokeExecuteSetup();

        // set the Org context for this test.
        if (ideSetupTestAnnotation.needOrg() || IdeTestUtil.isNotEmpty(ideSetupTestAnnotation.importProjectFromPath())) {
            this.orgInfoForCurrentTest =
                    IdeTestOrgFactory.getOrgFixture().getOrgCacheInstance().getOrgInfoFromCache(
                        ideSetupTestAnnotation.runForOrgType());
            if (IdeTestUtil.isEmpty(this.orgInfoForCurrentTest))
                throw IdeTestException.getWrappedException("Could not set orgInfo for current Test");
        }
        // set the project info for this test. If multiple projects were found
        // for a given org info type, the first one is returned.
        if (ideSetupTestAnnotation.needProject()) {
            this.projInfoCurrentTest =
                    IdeProjectFixture.getInstance().getProjectInfoForOrgType(this.orgInfoForCurrentTest);
            if (IdeTestUtil.isEmpty(this.projInfoCurrentTest))
                throw IdeTestException.getWrappedException("Could not set Project Info for current Test");
        }

        logger.info("Setting up test:" + getName() + "- DONE");
    }

    /*
     * (non-Javadoc) overriding solely for logging and exception handling and implementing a test timeout.
     * 
     * @see junit.framework.TestCase#runBare()
     */
    @Override
    public void runBare() throws Throwable {
        runTestPreWorker();
        ExecutorService service = Executors.newSingleThreadExecutor();
        Callable<Object> callable = new Callable<Object>() { 
            @Override
            public Object call() throws Exception {
                Throwable exception = null;
                String exceptionTracePrefix = null;
                String projectStructure = null;
                logStart(getName());
                try {
                    try {
                        setUp();
                        runTest();
                    } catch (Throwable running) {
                        exception =
                                running instanceof AssertionFailedError ? running : ForceExceptionUtils
                                        .getRootCause(running);
                        exceptionTracePrefix =
                                "Hit " + running.getClass().getSimpleName() + " while setup or running test '"
                                        + getName() + "'";
                    } finally {
                        if (projInfoCurrentTest != null && projInfoCurrentTest.getForceProject() != null
                                && projInfoCurrentTest.getForceProject().getProject() != null) {
                            projectStructure =
                                    projInfoCurrentTest.getForceProject().getProject().getName()
                                            + IdeTestUtil.getProjectContentsTree(projInfoCurrentTest.getForceProject()
                                                    .getProject());
                        }

                        try {
                            tearDown();
                        } catch (Throwable tearingDown) {
                            if (exception == null) {
                                exception = ForceExceptionUtils.getRootCause(tearingDown);
                                exceptionTracePrefix =
                                        "Hit " + tearingDown.getClass().getSimpleName() + " while tearing down test '"
                                                + getName() + "'.  Exception might cause problems for future tests.";
                            }
                        }
                    }

                    // add more info to exception message
                    if (exception != null) {
                        logException(exception, projectStructure);

                        StringBuffer buff =
                                new StringBuffer((IdeTestUtil.isNotEmpty(exceptionTracePrefix) ? exceptionTracePrefix
                                        : ""));
                        String rootCauseMessage =
                                exception instanceof AssertionFailedError ? exception.getMessage()
                                        : getRootCauseMessage(exception);

                        buff.append("\n\n----Root Cause----\n");
                        buff.append("Message:  ");
                        buff.append(IdeTestUtil.isNotEmpty(rootCauseMessage) ? rootCauseMessage : "n/a");
                        buff.append("\n--------------------------\n");

                        //org info
                        if (orgInfoForCurrentTest != null && IdeTestUtil.isNotEmpty(orgInfoForCurrentTest.toString())) {
                            buff.append(orgInfoForCurrentTest.toString());
                            buff.append("\n");
                        } else {
                            buff.append("\n----Org from cache wasn't used----");
                        }

                        //project info
                        if (projInfoCurrentTest != null && IdeTestUtil.isNotEmpty(projInfoCurrentTest.toString())) {
                            buff.append(projInfoCurrentTest.toString());
                        } else {
                            buff.append("\n----Project wasn't created in cache----");
                        }

                        //eclipse version
                        buff.append("\n----Eclipse Version:").append(IdeTestUtil.getCurrentEclipseMajorVersion())
                                .append("----");

                        //OS
                        buff.append("\n----OS Info:").append(IdeTestUtil.getCurrentOperatingSystemInfo())
                                .append("----");

                        //stack trace
                        buff.append("\n\n----Stacktrace----");

                        // create new exception instance w/ fully loaded message
                        String message = buff.toString();
                        Throwable origException = ForceExceptionUtils.getRootCause(exception);
                        if (exception instanceof AssertionFailedError) {
                            exception = new AssertionFailedError(message);
                            exception.setStackTrace(origException.getStackTrace());
                        } else {
                            try {
                                exception = origException.getClass().getConstructor(String.class).newInstance(message);
                                exception.setStackTrace(origException.getStackTrace());
                            } catch (Exception e1) {
                                exception = new Throwable(message, origException);
                            }
                        }
                    }

                    return exception;

                } finally {
                    logEnd(getName());
                }
            }
        };

        Object testResult = null;

        // if timeout is 0 then don't launch a new thread. 
        // used for pde_ui tests. see runTestPreWorker()
        // disable if jvm arg test-timeout=false set
        if (testTimeout <= 0
            || (System.getProperty(IdeTestConstants.JVM_ARG_TIMEOUT) != null 
            	&& !(new Boolean(System.getProperty(IdeTestConstants.JVM_ARG_TIMEOUT)).booleanValue()))) {
            testResult = callable.call();
        } else {

            Future<Object> result = service.submit(callable);
            service.shutdown();
            try {

                boolean terminated = service.isTerminated();
                if (!terminated) //no need to wait if its already dead
                    terminated = service.awaitTermination(testTimeout, TimeUnit.MILLISECONDS);
                if (!terminated) //at this point if its still false then we've timed out
                    service.shutdownNow();
                testResult = result.get(0, TimeUnit.MILLISECONDS); // throws the exception if one occurred during the invocation
            } catch (TimeoutException e) {
                //can we teardown here?
                try {
                    tearDown();
                } catch (Throwable tearingDown) {
                    logException(tearingDown, null);
                    throw new Throwable("Caught " + tearingDown.getClass().getSimpleName()
                            + " while tearing down after test timeout. Future tests might cause problems.", tearingDown);
                }
                logException(e, null);
                throw new Throwable(String.format("Test timed out after %d milliseconds", testTimeout),
                        ForceExceptionUtils.getRootCause(e));
            }
        }
        if (testResult instanceof Throwable)
            throw (Throwable) testResult;
    }

    private void logException(Throwable exception, String projectStructure) {
        if (exception == null) {
            return;
        }

        // basics
        StringBuffer strBuff = new StringBuffer("\n   ########  Error/Failure  ########");
        strBuff.append("\n## Test: ").append(getName()).append("\n## Exception: ").append(
            exception.getClass().getSimpleName()).append("\n## Message:\n").append(exception.getMessage());

        // log stacktrace
        String trace = getStackTrace(exception);
        if (IdeTestUtil.isNotEmpty(trace)) {
            strBuff.append("\n## Stacktrace:\n").append(trace);
        }

        if (orgInfoForCurrentTest != null && IdeTestUtil.isNotEmpty(orgInfoForCurrentTest.toString())) {
            strBuff.append("\n## Org:").append(orgInfoForCurrentTest.toString());
        }

        // project structure
        if (IdeTestUtil.isNotEmpty(projectStructure)) {
            strBuff.append("\n## Project:\n").append(projectStructure);
        }

        strBuff.append("   #################################");

        logger.error(strBuff.toString());
    }

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    private String getRootCauseMessage(Throwable exception) {
        String rootCauseMessage = ForceExceptionUtils.getRootCauseMessage(exception, false);
        if (IdeTestUtil.isNotEmpty(rootCauseMessage) && rootCauseMessage.startsWith("java.lang.Throwable: ")) {
            rootCauseMessage = rootCauseMessage.substring("java.lang.Throwable: ".length());
        }
        return rootCauseMessage;
    }

    @Override
    public void tearDown() throws Exception {
        logger.info("Tearing down test:" + getName());
        if (IdeTestUtil.isNotEmpty(invoker))
            invoker.invokeExecuteTeardown();
        logger.info("Tearing down test:" + getName() + "- DONE");

    }

    // ---------------------- PRIVATE HELPERS ------------------------------
    /**
     * Reads the annotation on the test method/class and checks it for errors. Also sets the timeout for the test.
     * 
     * @throws IdeTestException
     */
    private void runTestPreWorker() throws IdeTestException {
        //read the annotation.
        ideSetupTestAnnotation = getTestSetupConfigAnnotation();
        // do some quick sanity checks on the annotation before continuing.
        checkAnnotation(ideSetupTestAnnotation);
        //set the timeout
        IdeTimeoutTest testTimeoutAnnotation = getTestTimeoutAnnotation();
        testTimeout =
                (IdeTestUtil.isEmpty(testTimeoutAnnotation)) ? IdeTestConstants.TEST_TIMEOUT : testTimeoutAnnotation
                        .afterMilliSeconds();
        //if it's a pde ui test or a negative number is specified for the timeout, set to 0
        testTimeout =
                ((IdeTestUtil.isNotEmpty(testTimeoutAnnotation) && testTimeoutAnnotation.afterMilliSeconds() < 0) || (getClass()
                        .getName().endsWith("_pdeui"))) ? 0L : testTimeout;

    }

    /**
     * gets the IdeSetupTest Annotation on method or class
     * 
     * @return the
     * @throws NoSuchMethodException
     * @throws IdeTestException
     * 
     */
    private IdeSetupTest getTestSetupConfigAnnotation() throws IdeTestException {
        logger.info("seeking annotation on test method or class....");
        IdeSetupTest testConfig = null;
        Annotation[] annotations = getAnnotations();
        if (IdeTestUtil.isEmpty(annotations)) {
            throw IdeTestException
                    .getWrappedException("Test Method or encompassing class must have atleast the IdeSetupTest Annotation defined. Please add");
        }
        for (Annotation a : annotations) {
            if (a instanceof IdeSetupTest) {
                testConfig = (IdeSetupTest) a;
                break;
            }
        }
        if (IdeTestUtil.isEmpty(testConfig)) {
            throw IdeTestException
                    .getWrappedException("Test Method or encompassing class does not have IdeSetupTest annotation. Please add");
        }
        return testConfig;
    }

    /**
     * gets the ideTimeoutTest Annotation on method if Any.
     * 
     * @return the annotation if present.
     * @throws NoSuchMethodException
     * @throws IdeTestException
     * 
     */
    private IdeTimeoutTest getTestTimeoutAnnotation() throws IdeTestException {
        logger.info("seeking annotation on test method or class....");
        IdeTimeoutTest testTimeoutAnnotation = null;
        Annotation[] annotations = getAnnotations();
        if (IdeTestUtil.isEmpty(annotations)) {
            throw IdeTestException
                    .getWrappedException("Test Method or encompassing class must have atleast the IdeSetupTest Annotation defined. Please add");
        }
        for (Annotation a : annotations) {
            if (a instanceof IdeTimeoutTest) {
                testTimeoutAnnotation = (IdeTimeoutTest) a;
                break;
            }
        }
        return testTimeoutAnnotation;
    }

    /**
     * gets all the annotations on a method. If none specified on method, it'll return the ones on the class.
     * 
     * @return annotation[]
     * @throws IdeTestException
     */
    private Annotation[] getAnnotations() throws IdeTestException {
        String methodName = getName(); // returns the test method.
        assertFalse(IdeTestUtil.isEmpty(methodName));

        Method method;
        try {
            method = getClass().getMethod(methodName);
        } catch (SecurityException e) {
            throw IdeTestException.getWrappedException("Security Exception while trying to get test Method", e);
        } catch (NoSuchMethodException e) {
            throw IdeTestException.getWrappedException("No test Method found in class", e);
        }

        Annotation[] methodAnnotations = method.getAnnotations();
        Annotation[] classAnnotations = getClass().getAnnotations();
        HashMap<Class<? extends Annotation>, Annotation> retList =
                new HashMap<Class<? extends Annotation>, Annotation>();
        if (IdeTestUtil.isNotEmpty(classAnnotations)) {
            for (Annotation ca : classAnnotations) {
                retList.put(ca.annotationType(), ca);
            }
        }
        if (IdeTestUtil.isNotEmpty(methodAnnotations)) {
            for (Annotation ma : methodAnnotations) {
                retList.put(ma.annotationType(), ma);
            }
        }

        return retList.values().toArray(new Annotation[retList.values().size()]);
    }

    /**
     * log start of test
     * 
     * @param testName
     */
    private void logStart(String testName) {
        logger.info("");
        String className = getClass().getSimpleName();
        logger.info("**** START -- " + className + "." + testName + " -- START ****");
    }

    /**
     * log end of test
     * 
     * @param testName
     */
    private void logEnd(String testName) {
        String className = getClass().getSimpleName();
        logger.info("**** END -- " + className + "." + testName + " -- END ****");
    }

    /**
     * checks the annotation for any irregularities.
     * 
     * @param testConfig
     * @throws IdeTestException
     */
    private void checkAnnotation(IdeSetupTest testConfig) throws IdeTestException {
        logger.info("processing annotation on test method or class for errors....");
        //----ORG RELATED
        if (!testConfig.needOrg()) {
            // if need org is false, can't force create it
            if (testConfig.forceOrgCreation())
                throw IdeTestException
                        .getWrappedException("Can't force org Creation if needOrg isn't set to true in the annotation.");

            //if need org is false, ignore flag doesn;t make sense
            if (testConfig.ignoreOrgCleanSanityCheck())
                throw IdeTestException
                        .getWrappedException("Can't set org clean sanity check if needOrg isn't set to true in the annotation.");

            // if need org is false, can't set forceRevokeOrgFromLocalOrgCacheAfterTest
            if (testConfig.forceRevokeOrgFromLocalOrgCacheAfterTest())
                throw IdeTestException
                        .getWrappedException("Can't set force Revoke org from cache if needOrg isn't set to true in the annotation.");

            // if need org is false, can't set runforOrgType
            if (!testConfig.runForOrgType().equals(OrgTypeEnum.Developer))
                throw IdeTestException
                        .getWrappedException("Can't run test for a particular org type if needOrg in the annotation.");

            // if need org is false, can't set useOrgOnInstancePrefix
            if (!(testConfig.useOrgOnInstancePrefix().toLowerCase()
                    .equalsIgnoreCase(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX_VALUE_LOCAL)))
                throw IdeTestException
                        .getWrappedException("Can't run test against a specific org needOrg isn't set to true in the annotation.");

        }
        if (testConfig.needOrg()) {
            //if the org is not a local org.
            if (!(testConfig.useOrgOnInstancePrefix().toLowerCase()
                    .equalsIgnoreCase(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX_VALUE_LOCAL))) {
                //can't force create an org on a non-local app instance
                if (testConfig.forceOrgCreation())
                    throw IdeTestException
                            .getWrappedException("Can't force org Creation if the org isn't being created on a local app run.");

                // if test isn't running against local app server, then forceRevokeOrgFromLocalOrgCacheAfterTest can't be specified.
                if (testConfig.forceRevokeOrgFromLocalOrgCacheAfterTest())
                    throw IdeTestException
                            .getWrappedException("Can't set force Revoke org from cache if if the org isn't being created on a local app run.");

            }

            //if the project is being imported, then the org isn't required.
            if (IdeTestUtil.isNotEmpty(testConfig.importProjectFromPath())) {
                throw IdeTestException
                        .getWrappedException("you don't need to specify org settings when a project is being imported. the project preferences will have those settings and they will be used.");

            }
        }

        //-------META DATA RELATED----

        if (!testConfig.needMoreMetadataDataInOrg()) {
            // if need needMoreMetadataDataInOrg is false, addMetaDataFromPath doesn't make sense
            if (IdeTestUtil.isNotEmpty(testConfig.addMetaDataFromPath()))
                throw IdeTestException
                        .getWrappedException("Can't add Meta Data to org if needMoreMetadataDataInOrg isn't set to true in the annotation.");
            // if needMoreMetadataDataInOrg is false, addMetadataDataAsPackage doesn't make sense
            if (!(testConfig.addMetadataDataAsPackage().equals(PackageTypeEnum.UNPACKAGED)))
                throw IdeTestException
                        .getWrappedException("Can't add Meta Data as a package to org if needMoreMetadataDataInOrg isn't set to true in the annotation.");
            // if needMoreMetadataDataInOrg is false, skipMetadataRemovalDuringTearDown doesn't make sense.
            if(testConfig.skipMetadataRemovalDuringTearDown()){
                throw IdeTestException
                .getWrappedException("Can't specify skip Meta Data Removal if needMoreMetadataDataInOrg isn't set to true in the annotation.");
            }
        }
        // if needData is true, there better be a path to it.
        if (testConfig.needMoreMetadataDataInOrg()) {
            if (IdeTestUtil.isEmpty(testConfig.addMetaDataFromPath()))
                throw IdeTestException
                        .getWrappedException("Can't add Data to org if needMoreMetadataDataInOrg isn't specified in the annotation.Add path from /filemetadata");
        }

        //--------PROJECT RELATED---------
        if (testConfig.needProject()) {
            // if you need a project, and need selective create on component, but
            // component list wasn't specified, complain.
            if (testConfig.setProjectContentConfig().equals(IdeProjectContentTypeEnum.SPECIFIC_COMPONENTS)
                    && IdeTestUtil.isEmpty(testConfig.setComponentListIfSelectiveProjectCreate()))
                throw IdeTestException
                        .getWrappedException("Can't create selective project if setProjectContentConfig was set to SPECIFIC_COMPONENTS but setComponentListIfSelectiveProjectCreate wasn't specified. ");

            // if you need a project, and do not need selective create on component,
            // but component list was specified, complain.
            if (!(testConfig.setProjectContentConfig().equals(IdeProjectContentTypeEnum.SPECIFIC_COMPONENTS))
                    && IdeTestUtil.isNotEmpty(testConfig.setComponentListIfSelectiveProjectCreate()))
                throw IdeTestException
                        .getWrappedException("Can't create selective project if setProjectContentConfig wasn't set to SPECIFIC_COMPONENTS but setComponentListIfSelectiveProjectCreate was specified. ");

            // if you need a project, and want only what is being uploaded, then you must add metadata to the org.
            if (testConfig.setProjectContentConfig().equals(IdeProjectContentTypeEnum.ONLY_WHAT_IS_BEING_UPLOADED)
                    && (!testConfig.needMoreMetadataDataInOrg() || IdeTestUtil
                            .isEmpty(testConfig.addMetaDataFromPath())))
                throw IdeTestException
                        .getWrappedException("Can't create project against what is being uploaded if needDataInOrg and addDataFromPath are not set.Add path from /filemetadata");

            //if using a pre-canned project, then can't set specific project content config or component list.
            if (IdeTestUtil.isNotEmpty(testConfig.importProjectFromPath())
                    && (IdeTestUtil.isNotEmpty(testConfig.setComponentListIfSelectiveProjectCreate()) || !(testConfig
                            .setProjectContentConfig().equals(IdeProjectContentTypeEnum.ALL))))
                throw IdeTestException
                        .getWrappedException("Can't choose contents of a project when using a pre created project.");
        }

        if (!testConfig.needProject()) {
            // if you don't need a project, then setComponentListIfSelectiveProjectCreate better be empty.
            if (IdeTestUtil.isNotEmpty(testConfig.setComponentListIfSelectiveProjectCreate()))
                throw IdeTestException
                        .getWrappedException("Can't create selective project if needProject wasn't specified in the annotation. ");

            // if you don't need a project, then setProjectContentConfig better be unchanged
            if (!(testConfig.setProjectContentConfig().equals(IdeProjectContentTypeEnum.ALL)))
                throw IdeTestException
                        .getWrappedException("Can't set project content type if needProject wasn't set in the annotation. ");

        }

    }

}
