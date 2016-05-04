/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.ui.views.runtest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.salesforce.ide.apex.internal.core.ApexSourceUtils;
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.internal.utils.ResourceProperties;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.tooling.RunTests.TestsHolder;
import com.salesforce.ide.core.remote.tooling.RunTests.TestsHolder.Test;
import com.sforce.soap.tooling.TestLevel;

/**
 * Builds the necessary data structures for test selection in the
 * TestConfigurationTab.
 * 
 * @author nchen, jwidjaja
 *
 */
public class TestConfigurationsBuilder {
	private final ApexSourceUtils sourceLookup;

	public static final TestConfigurationsBuilder INSTANCE = new TestConfigurationsBuilder(ApexSourceUtils.INSTANCE);
	
	@VisibleForTesting
	public TestConfigurationsBuilder(ApexSourceUtils sourceLookup) {
		this.sourceLookup = sourceLookup;
	}

	/**
	 * Retrieve test classes and methods for a specific project. This should
	 * only be called when opening the config for the first time or when user
	 * changes the project.
	 * 
	 * @return RunTests POJO
	 */
	public TestsHolder buildTestsForProject(IProject project) {
		TestsHolder rt = new TestsHolder();
		List<Test> testClasses = Lists.newArrayList();

		Map<IResource, List<String>> allTests = sourceLookup.findTestClassesInProject(project);
		for (IResource resource : allTests.keySet()) {
			List<String> testMethods = allTests.get(resource);
			if (Utils.isEmpty(testMethods)) {
				continue;
			}

			// If there is more than one test method in the test class, add the
			// 'all methods' option
			if (testMethods.size() > 1) {
				testMethods.add(0, Messages.Tab_AllMethods);
			}

			String resourceId = ResourceProperties.getProperty(resource, QualifiedNames.QN_ID);

			Test testClass = new TestsHolder.Test();
			testClass.setClassId(resourceId);
			testClass.setClassName(resource.getName());
			testClass.setTestMethods(testMethods);

			testClasses.add(testClass);
		}

		// If there is more than one test class in the project, add the 'all
		// classes' option
		if (testClasses != null && testClasses.size() > 1) {
			Test allClasses = new TestsHolder.Test();
			allClasses.setClassId(Messages.Tab_AllClasses);
			allClasses.setClassName(Messages.Tab_AllClasses);
			List<String> allMethods = Lists.newArrayList();
			allMethods.add(Messages.Tab_AllMethods);
			allClasses.setTestMethods(allMethods);

			testClasses.add(0, allClasses);
		}

		rt.setTests(testClasses);
		return rt;
	}
	
	/**
	 * Create the JSON object of tests to run. This is a pruning algorithm.
	 * Based on the selection of everything that is possible, we prune down to a smaller set.
	 * The algorithm is not necessarily optimal – we will arrive at a smaller set but not necessarily the smallest representation.
	 */
	public TestsHolder buildTestsForConfig(
			IProject selectedProject,
			Map<IProject, TestsHolder> allTests,
			String testClassName,
			String testMethodName) {
    	if (Utils.isEmpty(selectedProject)) return null;
    	/*
    	 * Clone the original RunTests because the following logic
    	 * will filter out unwanted test classes/methods. We need to maintain the
    	 * original so we don't to re-build when user changes test class/method.
    	 */
    	TestsHolder testsHolder = allTests.containsKey(selectedProject) ? allTests.get(selectedProject).clone() : null;
    	if (Utils.isEmpty(testsHolder)) return null;
    	
    	boolean oneTestClass = !testClassName.equals(Messages.Tab_AllClasses);
    	boolean oneTestMethod = !testMethodName.equals(Messages.Tab_AllMethods);
    	
    	// Use the all tests enum if user wants to run all test classes
    	if (!oneTestClass) {
    		testsHolder.setTestLevel(TestLevel.RunLocalTests.toString());
    		return testsHolder;
    	}

		// Iterate through the test classes
		for (Iterator<Test> classIter = testsHolder.getTests().iterator(); classIter.hasNext();) {
			Test curClass = classIter.next();
			/*
			 * Remove this Test object if:
			 * - User wants all test classes and this test class says 'all'
			 * - User wants one test class and this is not the one user wants
			 */
			if ((!oneTestClass && curClass.getClassName().equals(Messages.Tab_AllClasses)) 
				|| (oneTestClass && !curClass.getClassName().equals(testClassName))) {
				classIter.remove();
				continue;
			}
			
			// User wants all test methods and this test method says 'all'
			// See https://developer.salesforce.com/docs/atlas.en-us.api_tooling.meta/api_tooling/intro_rest_resources.htm
		    // A null or missing testMethods array specifies that all test methods in the test class are run.
			if(!oneTestMethod) {
				// Null it out to run all test methods
				curClass.setTestMethods(null);
				continue;
			}
			
			// Iterate through the test methods
			for (Iterator<String> methodIter = curClass.getTestMethods().iterator(); methodIter.hasNext();) {
				String curMethod = methodIter.next();
				/*
				 * Remove this test method if: - User wants all test methods and
				 * this test method says 'all' - User wants one test method and
				 * this is not the one user wants
				 */
				if ((!oneTestMethod && curMethod.equals(Messages.Tab_AllMethods))
					|| (oneTestMethod && !curMethod.equals(testMethodName))) {
					methodIter.remove();
				}
			}
		}
		
    	return testsHolder;
    }
}
