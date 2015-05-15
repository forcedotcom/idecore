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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * The base class for ftests that don't need an org.
 * 
 * @author agupta
 * @deprecated should use ideTestCase only.
 */
public class SimpleTestCase extends TestCase {

    /** see setExtraOwners */
    private String[] extraOwners;
    private String originalThreadName;
    private String testMethodName;

    public SimpleTestCase() {}

    public SimpleTestCase(String name) {
        super(name);
        this.testMethodName = name;
    }

    /**
     * Call to register that, until you call clearExtraOwners or this test case ends, the usernames in
     * <code>extraOwners</code> also "own" the test (i.e., should get emailed if the test fails).
     */
    protected void setExtraOwners(String... extraOwners) {
        this.extraOwners = extraOwners;
    }

    /**
     * Call to add to the list of extra owners (see setExtraOwners).
     */
    protected void addExtraOwners(String... extraOwners) {
        if (extraOwners == null)
            return;

        Set<String> extraOwnerSet =
                this.extraOwners == null ? new TreeSet<String>() : new TreeSet<String>(Arrays.asList(this.extraOwners));
        for (String owner : extraOwners) {
            if (owner != null)
                extraOwnerSet.add(owner);
        }
        this.extraOwners = extraOwnerSet.toArray(new String[extraOwnerSet.size()]);
    }

    /**
     * Call to remove from the list of extra owners (see setExtraOwners).
     */
    protected void removeExtraOwners(String... extraOwners) {
        if (extraOwners == null)
            return;

        Set<String> extraOwnerSet =
                this.extraOwners == null ? new TreeSet<String>() : new TreeSet<String>(Arrays.asList(this.extraOwners));
        for (String owner : extraOwners) {
            if (owner != null)
                extraOwnerSet.remove(owner);
        }
        this.extraOwners = extraOwnerSet.toArray(new String[extraOwnerSet.size()]);
    }

    /**
     * Clears the list of extra owners.
     */
    protected void clearExtraOwners() {
        this.extraOwners = null;
    }

    public String[] getExtraOwners() {
        return this.extraOwners;
    }

    /*
     * Used for dynamically created test cases such as the formula tests to provide any added test labels.
     */
    public Set<String> getExtraTestLabels() {
        return null;
    }

    @Override
    protected void setUp() throws Exception {
        try {
            this.originalThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName(getName());
        } finally {
            super.setUp();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            if (this.originalThreadName != null)
                Thread.currentThread().setName(this.originalThreadName);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target( { ElementType.TYPE, ElementType.METHOD })
    public @interface QaTestCase {
        String subject() default "";

        String description() default "TODO: Describe me please!";

        String testPlanId();

        String status() default "Active";

        String action();

        String relatedTo();

        String priority() default "Medium";

        String expectedResults() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target( { ElementType.TYPE, ElementType.METHOD })
    public @interface QaTestCaseSet {
        public QaTestCase[] value();
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        this.testMethodName = name;
    }

    protected void disableAutoBuild() {
        switchAutoBuild(false);
    }

    protected void switchAutoBuild(boolean bool) {
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = ws.getDescription();
        desc.setAutoBuilding(bool);
        try {
            ws.setDescription(desc);
        } catch (CoreException e) {
            System.err.println("Unable to disable auto building: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
