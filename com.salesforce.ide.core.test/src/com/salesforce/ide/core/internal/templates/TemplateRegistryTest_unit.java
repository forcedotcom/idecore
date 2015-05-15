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
package com.salesforce.ide.core.internal.templates;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.test.common.IdeSetupTest;
import com.salesforce.ide.test.common.IdeTestCase;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Tests the retrieve of templates from the Template Registry.
 * An external contributor improved our templating system so we no longer
 * use components.xml. This unit test still has value to make sure we do not
 * regress.
 * 
 * @see https://github.com/forcedotcom/idecore/pull/98/files
 * 
 * @author ssasalatti
 * 
 */
@IdeSetupTest
public class TemplateRegistryTest_unit extends IdeTestCase {

    private final int NUM_OF_TEMPLATES = 1;
    private final int NUM_OF_APEX_CLASS_TEMPLATES = 0;
    private final int NUM_OF_APEX_COMPONENT_TEMPLATES = 0;
    private final int NUM_OF_APEX_PAGE_TEMPLATES = 0;
    private final int NUM_OF_APEX_TRIGGER_TEMPLATES = 0;
    private final int NUM_OF_SCONTROL_TEMPLATES = 1;

    /**
     * Is the template registry instantiation successful.
     * 
     * @throws IdeTestException
     * @throws ForceProjectException
     */
    public void testTemplateRegistryBean() throws IdeTestException, ForceProjectException {

        // Get an instance of the template registry.
        final ContainerDelegate containerDelegate = ContainerDelegate.getInstance();
        assertNotNull("ContainerDelegate should not be null", containerDelegate);
        final TemplateRegistry templateRegistry = (TemplateRegistry) containerDelegate.getBean("templateRegistry");
        assertNotNull("TemplateRegistry should not be null", templateRegistry);

        assertTrue("Prefix should be 'Custom'", "Custom".equals(templateRegistry.getCustomPrefix()));

        // There should be 1 template in total.
        assertEquals("Template count should be 1 not " + templateRegistry.count(), NUM_OF_TEMPLATES, templateRegistry
                .count());
    }

    /**
     * Test for apex class template
     */
    public void testApexClassTemplates() {
        final TemplateRegistry templateRegistry = IdeTestUtil.getTemplateRegistry();
        // There should be 0 apex class template.
        assertEquals(Constants.APEX_CLASS + " template count should be " + NUM_OF_APEX_CLASS_TEMPLATES,
            NUM_OF_APEX_CLASS_TEMPLATES, templateRegistry.componentTemplateCount(Constants.APEX_CLASS));

        final String apexClassTemplate = templateRegistry.getDefaultTemplate(Constants.APEX_CLASS);
        assertTrue(Constants.APEX_CLASS + " default template should be empty", IdeTestUtil.isEmpty(apexClassTemplate));
    }

    /**
     * Test for apex component template
     */
    public void testApexComponentTemplate() {
        final TemplateRegistry templateRegistry = IdeTestUtil.getTemplateRegistry();
        // There should be 0 apex component template.
        assertEquals(Constants.APEX_COMPONENT + " template count should be " + NUM_OF_APEX_COMPONENT_TEMPLATES,
            NUM_OF_APEX_COMPONENT_TEMPLATES, templateRegistry.componentTemplateCount(Constants.APEX_COMPONENT));

        final String apexComponentTemplate = templateRegistry.getDefaultTemplate(Constants.APEX_COMPONENT);
        assertTrue(Constants.APEX_COMPONENT + " default template should be empty", IdeTestUtil.isEmpty(apexComponentTemplate));
    }

    /**
     * Test for apex page templates
     */
    public void testApexPageTemplate() {
        final TemplateRegistry templateRegistry = IdeTestUtil.getTemplateRegistry();
        // There should be 0 apex page template.
        assertEquals(Constants.APEX_PAGE + " template count should be " + NUM_OF_APEX_PAGE_TEMPLATES,
            NUM_OF_APEX_PAGE_TEMPLATES, templateRegistry.componentTemplateCount(Constants.APEX_PAGE));

        final String apexPageTemplate = templateRegistry.getDefaultTemplate(Constants.APEX_PAGE);
        assertTrue(Constants.APEX_PAGE + " default template should be empty", IdeTestUtil.isEmpty(apexPageTemplate));
    }
    
    /**
     * Test for all the apex trigger templates
     */
    public void testApexTriggerTemplate() {
        final TemplateRegistry templateRegistry = IdeTestUtil.getTemplateRegistry();
        // There should be 0 apex trigger template.
        assertEquals(Constants.APEX_TRIGGER + " template count should be " + NUM_OF_APEX_TRIGGER_TEMPLATES,
            NUM_OF_APEX_TRIGGER_TEMPLATES, templateRegistry.componentTemplateCount(Constants.APEX_TRIGGER));
        
        final String apexTriggerTemplate = templateRegistry.getDefaultTemplate(Constants.APEX_TRIGGER);
        assertTrue(Constants.APEX_TRIGGER + " default template should end with ", IdeTestUtil.isEmpty(apexTriggerTemplate));
    }
    
    /**
     * Test for all the apex scontrol templates. starting prefix. number of scontrol templates etc.
     */
    public void testSControlTemplate() {
        final TemplateRegistry templateRegistry = IdeTestUtil.getTemplateRegistry();
        // There should be 1 scontrol templates.
        assertEquals(Constants.SCONTROL+ " template count should be " + NUM_OF_SCONTROL_TEMPLATES,
            NUM_OF_SCONTROL_TEMPLATES, templateRegistry.componentTemplateCount(Constants.SCONTROL));

        final String sControlStartsWith = "<html>";
        final String sControlTemplate = templateRegistry.getDefaultTemplate(Constants.SCONTROL);
        assertTrue(Constants.SCONTROL + " default template should start with " + sControlStartsWith, IdeTestUtil
                .isNotEmpty(sControlTemplate)
                && sControlTemplate.substring(sControlTemplate.indexOf("\n")+1).startsWith(sControlStartsWith));

        final String sControlEndsWith = "</html>";
        assertTrue(Constants.SCONTROL + " default template should end with " + sControlEndsWith, IdeTestUtil
                .isNotEmpty(sControlTemplate)
                && sControlTemplate.endsWith(sControlEndsWith));

    }
}
