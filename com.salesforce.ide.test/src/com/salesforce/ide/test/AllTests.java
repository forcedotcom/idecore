package com.salesforce.ide.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {

	public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
	
	public static Test suite() {
		return new AllTests();
	}
	
	public AllTests() {
		addTest(new com.salesforce.ide.test.MainAllUnitTests());
		addTest(new com.salesforce.ide.test.MainAllPdeUITests());
	}
}
