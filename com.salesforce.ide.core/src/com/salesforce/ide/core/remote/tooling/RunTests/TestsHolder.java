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

package com.salesforce.ide.core.remote.tooling.RunTests;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.tooling.TestLevel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "tests", "testLevel" })
public class TestsHolder {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("tests")
	private List<Test> tests = Lists.newArrayList();
	@JsonProperty("testLevel")
	private TestLevel testLevel = TestLevel.RunSpecifiedTests;

	/**
	 * 
	 * @return The tests
	 */
	@JsonProperty("tests")
	public List<Test> getTests() {
		return tests;
	}
	
	/**
	 * Get number of test classes
	 */
	public int getTotalTests() {
		if (tests != null && !tests.isEmpty()) {
			return tests.size();
		}
		
		return 0;
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
	 * 
	 * @return The testLevel
	 */
	@JsonProperty("testLevel")
	public String getTestLevel() {
		return testLevel.toString();
	}
	
	/**
	 * 
	 * @param testLevel
	 *            Use com.sforce.soap.tooling.TestLevel
	 */
	@JsonProperty("testLevel")
	public void setTestLevel(String testLevel) {
		this.testLevel = TestLevel.valueOf(testLevel);
		if (!isSpecificTests()) {
			setTests(null);
		}
	}
	
	/**
	 * True if the test level is RunSpecifiedTests. False otherwise.
	 * @return
	 */
	public boolean isSpecificTests() {
		return TestLevel.RunSpecifiedTests.equals(this.testLevel);
	}
	
	/**
	 * Clone the RunTests object
	 */
	public TestsHolder clone() {
		TestsHolder rt = new TestsHolder();
		List<Test> clonedTests = Lists.newArrayList();
		
		for (Test oldTest : tests) {
			clonedTests.add(oldTest.clone());
		}
		
		rt.setTests(clonedTests);
		return rt;
	}
	
	/**
     * Convert TestsHolder to JSON string
     * @param TestsHolder
     * @return JSON string
     */
    public static String serialize(TestsHolder th) {
    	String result = "";
    	if (Utils.isNotEmpty(th)) {
    		ObjectMapper mapper = new ObjectMapper();
        	try {
    			result = mapper.writeValueAsString(th);
    		} catch (JsonProcessingException e) {}
    	}
    	
    	return result;
    }
    
    /**
     * Convert JSON string to TestsHolder
     * @param JSON string
     * @return TestsHolder
     */
    public static TestsHolder deserialize(String json) {
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			return mapper.readValue(json, TestsHolder.class);
		} catch (IOException e) {}
    	
    	return null;
    }
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonPropertyOrder({ "classId", "testMethods" })
	public static class Test {

		@JsonProperty("classId")
		private String classId;
		@JsonIgnore
		private String className;
		@JsonProperty("testMethods")
		private List<String> testMethods = Lists.newArrayList();

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
			
			List<String> clonedTestMethods = Lists.newArrayList();
			for (String testMethod : testMethods) {
				clonedTestMethods.add(testMethod);
			}
			
			newTest.setTestMethods(clonedTestMethods);
			return newTest;
		}
	}
}