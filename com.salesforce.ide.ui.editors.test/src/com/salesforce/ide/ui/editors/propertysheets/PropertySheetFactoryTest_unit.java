/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.propertysheets;

import junit.framework.TestCase;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexClassPropertySheet;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexComponentPropertySheet;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexPagePropertySheet;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexTriggerPropertySheet;
import com.salesforce.ide.ui.editors.properysheets.MetadataFormPage;
import com.salesforce.ide.ui.editors.properysheets.PropertySheetsFactory;

/**
 * Simple test to check that we can return the correct property sheets based on component type.
 * 
 * @author nchen
 *
 */
public class PropertySheetFactoryTest_unit extends TestCase {

    private Component component;
    private MetadataFormPage page;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        component = new Component();
    }

    public void testApexClassPropertySheet() {
        component.setComponentType("apexclass");
        page = PropertySheetsFactory.createPropertySheet(component, null);
        assertTrue(page != null);
        assertTrue(page instanceof ApexClassPropertySheet);
    }

    public void testApexTriggerPropertySheet() {
        component.setComponentType("apextrigger");
        page = PropertySheetsFactory.createPropertySheet(component, null);
        assertTrue(page != null);
        assertTrue(page instanceof ApexTriggerPropertySheet);
    }

    public void testApexPagePropertySheet() {
        component.setComponentType("apexpage");
        page = PropertySheetsFactory.createPropertySheet(component, null);
        assertTrue(page != null);
        assertTrue(page instanceof ApexPagePropertySheet);
    }

    public void testApexComponentPropertySheet() {
        component.setComponentType("apexcomponent");
        page = PropertySheetsFactory.createPropertySheet(component, null);
        assertTrue(page != null);
        assertTrue(page instanceof ApexComponentPropertySheet);
    }
}
