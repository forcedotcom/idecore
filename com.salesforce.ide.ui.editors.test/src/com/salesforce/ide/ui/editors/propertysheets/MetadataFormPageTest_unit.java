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

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import com.salesforce.ide.api.metadata.types.ApexClass;
import com.salesforce.ide.api.metadata.types.ApexComponent;
import com.salesforce.ide.api.metadata.types.ApexPage;
import com.salesforce.ide.api.metadata.types.ApexTrigger;
import com.salesforce.ide.ui.editors.properysheets.MetadataFormPage;

/**
 * Tests various functionality for serializing/deserializing metadata.
 * 
 * @author nchen
 *
 */
public class MetadataFormPageTest_unit extends TestCase {

    public void testSyncingApexClass() throws JAXBException, XMLStreamException {
        String apexClassXML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<ApexClass xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                        + "    <apiVersion>31.0</apiVersion>\n" + "    <packageVersions>\n"
                        + "        <majorNumber>1</majorNumber>\n" + "        <minorNumber>0</minorNumber>\n"
                        + "        <namespace>bogus</namespace>\n" + "    </packageVersions>\n"
                        + "    <status>Active</status>\n" + "</ApexClass>";
        ApexClass unmarshalled = MetadataFormPage.unmarshall(apexClassXML, ApexClass.class);
        String marshalled = MetadataFormPage.marshall(unmarshalled);
        assertEquals("Unmarshalled XML is not the same as original", apexClassXML, marshalled);
    }

    // The following tests feel redundant since we are relying on JAXB and if it works for ApexClass 
    // then it should work for all, but let's put them here just for checks

    public void testSyncingApexTrigger() throws JAXBException, XMLStreamException {
        String apexTriggerXML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<ApexTrigger xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                        + "    <apiVersion>31.0</apiVersion>\n" + "    <packageVersions>\n"
                        + "        <majorNumber>1</majorNumber>\n" + "        <minorNumber>0</minorNumber>\n"
                        + "        <namespace>bogus</namespace>\n" + "    </packageVersions>\n"
                        + "    <status>Active</status>\n" + "</ApexTrigger>";
        ApexTrigger unmarshalled = MetadataFormPage.unmarshall(apexTriggerXML, ApexTrigger.class);
        String marshalled = MetadataFormPage.marshall(unmarshalled);
        assertEquals("Unmarshalled XML is not the same as original", apexTriggerXML, marshalled);
    }

    public void testSyncingApexPage() throws JAXBException, XMLStreamException {
        String apexPageXML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<ApexPage xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                        + "    <apiVersion>31.0</apiVersion>\n" + "    <availableInTouch>true</availableInTouch>\n"
                        + "    <confirmationTokenRequired>true</confirmationTokenRequired>\n"
                        + "    <description>Simple description</description>\n" + "    <label>Simple label</label>\n"
                        + "    <packageVersions>\n" + "        <majorNumber>1</majorNumber>\n"
                        + "        <minorNumber>0</minorNumber>\n" + "        <namespace>bogus</namespace>\n"
                        + "    </packageVersions>\n" + "    <packageVersions>\n"
                        + "        <majorNumber>1</majorNumber>\n" + "        <minorNumber>0</minorNumber>\n"
                        + "        <namespace>nchen</namespace>\n" + "    </packageVersions>\n" + "</ApexPage>";
        ApexPage unmarshalled = MetadataFormPage.unmarshall(apexPageXML, ApexPage.class);
        String marshalled = MetadataFormPage.marshall(unmarshalled);
        assertEquals("Unmarshalled XML is not the same as original", apexPageXML, marshalled);
    }

    public void testSyncingApexComponent() throws JAXBException, XMLStreamException {
        String apexComponentXML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<ApexComponent xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                        + "    <apiVersion>31.0</apiVersion>\n" + "    <description>Simple Description</description>\n"
                        + "    <label>Simple Label</label>\n" + "    <packageVersions>\n"
                        + "        <majorNumber>1</majorNumber>\n" + "        <minorNumber>0</minorNumber>\n"
                        + "        <namespace>bogus</namespace>\n" + "    </packageVersions>\n"
                        + "    <packageVersions>\n" + "        <majorNumber>1</majorNumber>\n"
                        + "        <minorNumber>0</minorNumber>\n" + "        <namespace>nchen</namespace>\n"
                        + "    </packageVersions>\n" + "</ApexComponent>";
        ApexComponent unmarshalled = MetadataFormPage.unmarshall(apexComponentXML, ApexComponent.class);
        String marshalled = MetadataFormPage.marshall(unmarshalled);
        assertEquals("Unmarshalled XML is not the same as original", apexComponentXML, marshalled);
    }
}
