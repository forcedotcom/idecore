/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.core.remote.tooling;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "tests" })
public class RunTests {

	@JsonProperty("tests")
	private List<Test> tests = new ArrayList<Test>();

	/**
	 * 
	 * @return The tests
	 */
	@JsonProperty("tests")
	public List<Test> getTests() {
		return tests;
	}

	/**
	 * 
	 * @param tests
	 *            The tests
	 */
	@JsonProperty("tests")
	public void setTests(List<Test> tests) {
		this.tests = tests;
	}
	
	/**
	 * Clone the RunTests object
	 */
	public RunTests clone() {
		RunTests rt = new RunTests();
		List<Test> clonedTests = new ArrayList<Test>();
		
		for (Test oldTest : tests) {
			clonedTests.add(oldTest.clone());
		}
		
		rt.setTests(clonedTests);
		return rt;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonPropertyOrder({ "classId", "testMethods" })
	public class Test {

		@JsonProperty("classId")
		private String classId;
		@JsonIgnore
		private String className;
		@JsonProperty("testMethods")
		private List<String> testMethods = new ArrayList<String>();

		/**
		 * 
		 * @return The classId
		 */
		@JsonProperty("classId")
		public String getClassId() {
			return classId;
		}
		
		/**
		 * 
		 * @return The className
		 */
		public String getClassName() {
			return className;
		}

		/**
		 * 
		 * @param classId
		 *            The classId
		 */
		@JsonProperty("classId")
		public void setClassId(String classId) {
			this.classId = classId;
		}
		
		/**
		 * 
		 * @param className
		 *            The className
		 */
		public void setClassName(String className) {
			this.className = className;
		}

		/**
		 * 
		 * @return The testMethods
		 */
		@JsonProperty("testMethods")
		public List<String> getTestMethods() {
			return testMethods;
		}

		/**
		 * 
		 * @param testMethods
		 *            The testMethods
		 */
		@JsonProperty("testMethods")
		public void setTestMethods(List<String> testMethods) {
			this.testMethods = testMethods;
		}
		
		/**
		 * Clone the Test object
		 */
		public Test clone() {
			Test newTest = new Test();
			newTest.setClassId(getClassId());
			newTest.setClassName(getClassName());
			
			List<String> clonedTestMethods = new ArrayList<String>();
			for (String testMethod : testMethods) {
				clonedTestMethods.add(testMethod);
			}
			
			newTest.setTestMethods(clonedTestMethods);
			return newTest;
		}
	}
}