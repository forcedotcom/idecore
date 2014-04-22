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
package com.salesforce.ide.ui.editors.properysheets;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexClassPropertySheet;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexComponentPropertySheet;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexPagePropertySheet;
import com.salesforce.ide.ui.editors.apex.propertysheets.ApexTriggerPropertySheet;
import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;

/**
 * A centralized class for creating different types of PropertyFormsPages. Can extend to be based on configuration files
 * as we add more property sheets but we can keep this interface.
 * 
 * @author nchen
 *
 */
public class PropertySheetsFactory {

    public static MetadataFormPage createPropertySheet(Component component,
            BaseComponentMultiPageEditorPart multiPageEditor) {

        MetadataFormPage propertySheet = null;

        if (component.getComponentType().equalsIgnoreCase("ApexClass")) {
            propertySheet = new ApexClassPropertySheet(multiPageEditor);
        }
        if (component.getComponentType().equalsIgnoreCase("ApexTrigger")) {
            propertySheet = new ApexTriggerPropertySheet(multiPageEditor);
        }
        if (component.getComponentType().equalsIgnoreCase("ApexPage")) {
            propertySheet = new ApexPagePropertySheet(multiPageEditor);
        }
        if (component.getComponentType().equalsIgnoreCase("ApexComponent")) {
            propertySheet = new ApexComponentPropertySheet(multiPageEditor);
        }

        return propertySheet;
    }
}
