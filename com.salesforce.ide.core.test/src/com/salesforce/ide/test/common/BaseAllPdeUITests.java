package com.salesforce.ide.test.common;

public abstract class BaseAllPdeUITests extends BaseAllTests {

	public static final String ALL_TEST_CLASS_NAME  = "AllPdeUITests";
    public static final String SUFFIX  = "_pdeui";

    public BaseAllPdeUITests() {
        super();
    }

    @Override
    protected String getAllTestClassName() {
        return ALL_TEST_CLASS_NAME;
    }

    @Override
    protected String getSuffix() {
        return SUFFIX;
    }
}
