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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.google.common.annotations.VisibleForTesting;
import com.salesforce.ide.api.metadata.types.ApexTestSuite;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizardPage;

/**
 * A page in ApexTestSuite wizard that allows naming the ApexTestSuite component.
 * 
 * @author jwidjaja
 */
public class ApexTestSuiteWizardPage extends GenericComponentWizardPage {
	
	private ApexTestSuite testSuite;
	
	public ApexTestSuiteWizardPage(ComponentWizard componentWizard) {
		super(componentWizard);
	}

	@Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }
	
	@Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite = createComponentWizardComposite(parent, component.getDisplayName());
        componentWizardComposite.setComponentWizardPage(this);
        
        testSuite = (ApexTestSuite) component.getDefaultMetadataExtInstance();
    }
	
	@VisibleForTesting
	public ApexTestSuiteWizardComposite createComponentWizardComposite(Composite parent, String displayName) {
		return new ApexTestSuiteWizardComposite(parent, SWT.NULL, displayName);
	}
	
	@Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        ApexTestSuiteWizardComposite apexTestSuiteWizardComposite = (ApexTestSuiteWizardComposite) componentWizardComposite;
        testSuite.setFullName(apexTestSuiteWizardComposite.getNameString());
    }
	
	public void addTestClasses(List<String> testClasses) {
		this.testSuite.getTestClassName().clear();;
		this.testSuite.getTestClassName().addAll(testClasses);
	}
}
