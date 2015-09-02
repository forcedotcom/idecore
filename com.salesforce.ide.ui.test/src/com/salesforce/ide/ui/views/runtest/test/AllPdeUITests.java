package com.salesforce.ide.ui.views.runtest.test;

import junit.framework.Test;

import com.salesforce.ide.test.common.BaseAllPdeUITests;

public class AllPdeUITests extends BaseAllPdeUITests {

	public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new AllPdeUITests();
    }

    public AllPdeUITests() {
        super();
    }
    
	@Override
	protected String getPackageName() {
		return getClass().getPackage().getName();
	}

	@Override
	protected Class<?> getTestClass() {
		return this.getClass();
	}

	@Override
	protected ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}
}
