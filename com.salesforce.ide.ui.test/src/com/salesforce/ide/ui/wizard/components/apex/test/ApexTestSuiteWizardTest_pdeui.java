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
package com.salesforce.ide.ui.wizard.components.apex.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.IWizardPage;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.salesforce.ide.core.internal.components.apex.test.ApexTestSuiteComponentController;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.test.common.IdeSetupTest;
import com.salesforce.ide.test.common.IdeTestCase;
import com.salesforce.ide.ui.wizards.components.apex.test.ApexTestSuiteWizard;
import com.salesforce.ide.ui.wizards.components.apex.test.ApexTestSuiteWizardPage;
import com.salesforce.ide.ui.wizards.components.apex.test.ApexTestSuiteWizardSelectionPage;

@IdeSetupTest(needOrg = false, needProject = false)
public class ApexTestSuiteWizardTest_pdeui extends IdeTestCase {
	
	@Test
	public void testControllerType() throws Exception {
		ApexTestSuiteWizard wiz = new ApexTestSuiteWizard();
		assertTrue(wiz.getComponentController() instanceof ApexTestSuiteComponentController);
	}
	
	@Test
	public void testPages() throws Exception {
		ApexTestSuiteWizard wiz = new ApexTestSuiteWizard();
		
		wiz.addPages();
		
		IWizardPage[] pages = wiz.getPages();
		assertEquals(2, pages.length);
		assertTrue(pages[0] instanceof ApexTestSuiteWizardPage);
		assertTrue(pages[1] instanceof ApexTestSuiteWizardSelectionPage);
	}
	
	@Test
	public void testPerformFinish() throws Exception {
		ApexTestSuiteWizard wiz = spy(new ApexTestSuiteWizard());
		ApexTestSuiteWizardPage firstPage = mock(ApexTestSuiteWizardPage.class);
		ApexTestSuiteWizardSelectionPage secondPage = mock(ApexTestSuiteWizardSelectionPage.class);
		when(wiz.createFirstPage()).thenReturn(firstPage);
		when(wiz.createSecondPage()).thenReturn(secondPage);
		List<String> selectedTests = Lists.newArrayList();
		when(secondPage.getSelectedTests()).thenReturn(selectedTests);
		doNothing().when(firstPage).addTestClasses(selectedTests);
		
		wiz.addPages();
		wiz.performFinish();
		
		verify(firstPage, times(1)).addTestClasses(selectedTests);
		verify(secondPage, times(1)).getSelectedTests();
	}
	
	@Test
	public void testGetTestClassesWithNullProject() throws Exception {
		ApexTestSuiteWizard wiz = new ApexTestSuiteWizard();
		
		assertEquals(0, wiz.getTestClasses().size());
	}
	
	@Test
	public void testGetTestClassesWithoutNamespace() throws Exception {
		ApexTestSuiteWizard wiz = spy(new ApexTestSuiteWizard());
		ForceProject fp = mock(ForceProject.class);
		when(fp.getNamespacePrefix()).thenReturn(null);
		when(wiz.materializeForceProject()).thenReturn(fp);
				
		Map<IResource, List<String>> testsFound = Maps.newLinkedHashMap();
		IResource firstRes = mock(IResource.class);
		when(firstRes.getName()).thenReturn("FirstClass.cls");
		IResource secondRes = mock(IResource.class);
		when(secondRes.getName()).thenReturn("SecondClass.cls");
		testsFound.put(firstRes, Lists.newArrayList());
		testsFound.put(secondRes, Lists.newArrayList());
		when(wiz.getTestClassesInProject()).thenReturn(testsFound);
		
		List<String> testClasses = wiz.getTestClasses();
		
		assertEquals(2, testClasses.size());
		assertEquals("FirstClass", testClasses.get(0));
		assertEquals("SecondClass", testClasses.get(1));
	}
	
	@Test
	public void testGetTestClassesWithNamespace() throws Exception {
		ApexTestSuiteWizard wiz = spy(new ApexTestSuiteWizard());
		ForceProject fp = mock(ForceProject.class);
		when(fp.getNamespacePrefix()).thenReturn("MyNamespace");
		when(wiz.materializeForceProject()).thenReturn(fp);
				
		Map<IResource, List<String>> testsFound = Maps.newLinkedHashMap();
		IResource firstRes = mock(IResource.class);
		when(firstRes.getName()).thenReturn("FirstClass.cls");
		IResource secondRes = mock(IResource.class);
		when(secondRes.getName()).thenReturn("SecondClass.cls");
		testsFound.put(firstRes, Lists.newArrayList());
		testsFound.put(secondRes, Lists.newArrayList());
		when(wiz.getTestClassesInProject()).thenReturn(testsFound);
		
		List<String> testClasses = wiz.getTestClasses();
		
		assertEquals(2, testClasses.size());
		assertEquals("MyNamespace.FirstClass", testClasses.get(0));
		assertEquals("MyNamespace.SecondClass", testClasses.get(1));
	}
}
