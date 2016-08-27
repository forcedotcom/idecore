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
package com.salesforce.ide.ui.wizards.components.apex.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.salesforce.ide.apex.internal.core.ApexSourceUtils;
import com.salesforce.ide.core.internal.components.apex.test.*;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizard;

/**
 * A wizard to create ApexTestSuite component with specified
 * suite name & test classes.
 * 
 * @author jwidjaja
 */
public class ApexTestSuiteWizard extends GenericComponentWizard {
	
	private static final Logger logger = Logger.getLogger(ApexTestSuiteWizard.class);
	
	private ApexTestSuiteWizardPage componentPage;
	private ApexTestSuiteWizardSelectionPage selectionPage;
	
	private IProject project;
	private ForceProject forceProject;

	public ApexTestSuiteWizard() throws ForceProjectException {
		super();
		controller = new ApexTestSuiteComponentController();
	}

	@Override
	public ComponentWizardPage getComponentWizardPageInstance() {
		if (componentPage == null) {
			componentPage = createFirstPage();
		}
		
		return componentPage;
	}
	
	@Override
    public void addPages() {
		super.addPages();
		
		selectionPage = createSecondPage();
		super.addPage(selectionPage);
	}
	
	@Override
    public boolean performFinish() {
		// Add the selected tests to ApexTestSuite component
		componentPage.addTestClasses(selectionPage.getSelectedTests());
		return super.performFinish();
	}
	
	@VisibleForTesting
	public ApexTestSuiteWizardPage createFirstPage() {
		return new ApexTestSuiteWizardPage(this);
	}
	
	@VisibleForTesting
	public ApexTestSuiteWizardSelectionPage createSecondPage() {
		return new ApexTestSuiteWizardSelectionPage();
	}
	
	/**
	 * Create ForceProject from IProject.
	 */
	@VisibleForTesting
	public ForceProject materializeForceProject() {
		if (forceProject != null) return forceProject;
		
		project = controller.getModel().getProject();
		if (project == null || !project.exists()) return forceProject;

        forceProject =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(project);
        return forceProject;
	}
	
	/**
	 * Get test classes in the workspace.
	 */
	public List<String> getTestClasses() {
		forceProject = materializeForceProject();
		
		List<String> testClasses = new ArrayList<String>();
		if (forceProject == null) {
			return testClasses;
		}
		
		try {
			final String myOrgNS = forceProject.getNamespacePrefix();
			// We only care about the sources they have in Force.com IDE. We don't bother querying
			// the org for Apex classes.
			Map<IResource, List<String>> testsFound = getTestClassesInProject();
			String testClassName;
			for (IResource res : testsFound.keySet()) {
				// If the org has a namespace, then the full test class name is namespace.className.
				// Otherwise, it's just className.
				testClassName = (myOrgNS != null && !myOrgNS.isEmpty() ? myOrgNS + "." : "") + res.getName().replace(ApexSourceUtils.CLS_SUFFIX, "");
				testClasses.add(testClassName);
			}
		} catch (Exception e) {
			logger.error("Failed to get test classes", e);
		}
		
		return testClasses;
	}
	
	@VisibleForTesting
	public Map<IResource, List<String>> getTestClassesInProject() {
		if (project == null || !project.exists()) {
			return Maps.newLinkedHashMap();
		}
		
		return ApexSourceUtils.INSTANCE.findTestClassesInProject(project);
	}
}
