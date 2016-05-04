/*******************************************************************************
* Copyright (c) 2016 Salesforce.com, inc..
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors:
*     Salesforce.com, inc. - initial API and implementation
*******************************************************************************/
package com.salesforce.ide.ui.views.runtest.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.salesforce.ide.apex.internal.core.ApexSourceUtils;
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.remote.tooling.RunTests.TestsHolder;
import com.salesforce.ide.ui.views.runtest.Messages;
import com.salesforce.ide.ui.views.runtest.TestConfigurationsBuilder;
import com.sforce.soap.tooling.TestLevel;

import junit.framework.TestCase;

/**
 * @author nchen
 */
public class TestConfigurationsBuilderTest_unit extends TestCase {
	private TestConfigurationsBuilder testBuilder;
	private Map<IResource, List<String>> mAllTests;
	@Mock ApexSourceUtils sourceUtils;
	
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testBuilder = new TestConfigurationsBuilder(sourceUtils);
		mAllTests = Maps.newLinkedHashMap();
		IResource testClassA = mock(IResource.class);
		when(testClassA.getPersistentProperty(QualifiedNames.QN_ID)).thenReturn("001ptestClassA");
		when(testClassA.getName()).thenReturn("testClassA");
		mAllTests.put(testClassA, Lists.newArrayList("testMethodA1", "testMethodA2"));
		IResource testClassB = mock(IResource.class);
		when(testClassB.getPersistentProperty(QualifiedNames.QN_ID)).thenReturn("001ptestClassB");
		when(testClassB.getName()).thenReturn("testClassB");
		mAllTests.put(testClassB, Lists.newArrayList("testMethodB1", "testMethodB2"));
    }
	
	// BuildTestsForProject
	///////////////////////
	@Test
	public void testAllMethodsOptionAvailable() throws Exception {
		IProject mProject = mock(IProject.class);
		when(sourceUtils.findTestClassesInProject(mProject)).thenReturn(mAllTests);
		
		TestsHolder result = testBuilder.buildTestsForProject(mProject);
		
		assertEquals(3, result.getTests().size());
		assertTrue(result.getTests().get(0).getTestMethods().containsAll(Lists.newArrayList(Messages.Tab_AllMethods)));
		assertTrue(result.getTests().get(1).getTestMethods().containsAll(Lists.newArrayList(Messages.Tab_AllMethods, "testMethodA1", "testMethodA2")));
		assertTrue(result.getTests().get(2).getTestMethods().containsAll(Lists.newArrayList(Messages.Tab_AllMethods, "testMethodB1", "testMethodB2")));
	}

	@Test
	public void testAllClassesOptionAvailable() throws Exception {
		IProject mProject = mock(IProject.class);
		when(sourceUtils.findTestClassesInProject(mProject)).thenReturn(mAllTests);
		
		TestsHolder result = testBuilder.buildTestsForProject(mProject);
		
		List<String> testClasses = result.getTests().stream().map(t -> t.getClassName()).collect(Collectors.toList());
		assertTrue(testClasses.containsAll(Lists.newArrayList(Messages.Tab_AllClasses, "testClassA", "testClassB")));
	}
	
	// BuildTestForConfig
	/////////////////////
	
	@Test
	public void testRemovesAllMethodOptionWhenOneMethodSelected() throws Exception {
		IProject mProject = mock(IProject.class);
		when(sourceUtils.findTestClassesInProject(mProject)).thenReturn(mAllTests);
		
		TestsHolder testsHolder = testBuilder.buildTestsForProject(mProject);
		Map<IProject, TestsHolder> allTests = Maps.newHashMap();
		allTests.put(mProject, testsHolder);

		TestsHolder result = testBuilder.buildTestsForConfig(
				mProject,
				allTests,
				"testClassA",
				"testMethodA1");
		
		assertEquals(1, result.getTotalTests());
		assertEquals("testClassA", result.getTests().get(0).getClassName());
		assertTrue(result.getTests().get(0).getTestMethods().contains("testMethodA1"));
		assertEquals(TestLevel.RunSpecifiedTests.toString(), result.getTestLevel());
	}

	@Test
	public void testNullsOutMethodWhenAllMethodSelected() throws Exception {
		IProject mProject = mock(IProject.class);
		when(sourceUtils.findTestClassesInProject(mProject)).thenReturn(mAllTests);
		
		TestsHolder testsHolder = testBuilder.buildTestsForProject(mProject);
		Map<IProject, TestsHolder> allTests = Maps.newHashMap();
		allTests.put(mProject, testsHolder);

		TestsHolder result = testBuilder.buildTestsForConfig(
				mProject,
				allTests,
				"testClassA",
				Messages.Tab_AllMethods);
		
		assertEquals(1, result.getTotalTests());
		assertNull(result.getTests().get(0).getTestMethods());
		assertEquals(TestLevel.RunSpecifiedTests.toString(), result.getTestLevel());
	}

	@Test
	public void testNullsOutMethodWhenAllClassesSelected() throws Exception {
		IProject mProject = mock(IProject.class);
		when(sourceUtils.findTestClassesInProject(mProject)).thenReturn(mAllTests);
		
		TestsHolder testsHolder = testBuilder.buildTestsForProject(mProject);
		Map<IProject, TestsHolder> allTests = Maps.newHashMap();
		allTests.put(mProject, testsHolder);

		TestsHolder result = testBuilder.buildTestsForConfig(
				mProject,
				allTests,
				Messages.Tab_AllClasses,
				Messages.Tab_AllMethods);
		
		assertNull("If we're running all tests, we should not enumerate the test classes", result.getTests());
		assertEquals(0, result.getTotalTests());
		assertEquals(TestLevel.RunLocalTests.toString(), result.getTestLevel());
	}
}
