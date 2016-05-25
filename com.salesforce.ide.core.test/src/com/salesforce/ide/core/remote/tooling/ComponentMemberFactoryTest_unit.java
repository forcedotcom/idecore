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
package com.salesforce.ide.core.remote.tooling;

import junit.framework.TestCase;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.services.ToolingService;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.sforce.soap.tooling.sobject.ApexClassMember;
import com.sforce.soap.tooling.ApexCodeUnitStatus;
import com.sforce.soap.tooling.sobject.ApexComponentMember;
import com.sforce.soap.tooling.sobject.ApexPageMember;
import com.sforce.soap.tooling.sobject.ApexTriggerMember;
import com.sforce.soap.tooling.sobject.SObject;

/**
 * Tests the factory methods to ensure that we are creating the right ContainerMembers. Again, this method of testing is
 * not scalable as we have more types. When that time comes, create a test generator for the different types.
 * 
 * @author nchen
 * 
 */
public class ComponentMemberFactoryTest_unit extends TestCase {
    private static final String METADATA_CONTAINER_ID = "BOGUS_ID";

    ContainerMemberFactory cmf;
    ComponentList cmps;

    private Component apexClass;
    private Component apexTrigger;
    private Component apexPage;
    private Component apexComponent;
    private Component apexClassMeta;
    private Component apexTriggerMeta;
    private Component apexPageMeta;
    private Component apexComponentMeta;

    @Override
    public void setUp() {
        cmf = new ContainerMemberFactory(METADATA_CONTAINER_ID);

        setUpApexClass();
        setUpApexTrigger();
        setUpApexPage();
        setUpApexComponent();

        cmps = new ComponentList();
        cmps.add(apexClass);
        cmps.add(apexClassMeta);
        cmps.add(apexTrigger);
        cmps.add(apexTriggerMeta);
        cmps.add(apexPage);
        cmps.add(apexPageMeta);
        cmps.add(apexComponent);
        cmps.add(apexComponentMeta);

    }

    private void setUpApexClass() {
        apexClass = new Component();
        apexClass.setComponentType("ApexClass");
        apexClass.setFullName("MyClass");
        apexClass.setFileExtension("cls");
        apexClass.setMetadataFileExtension("-meta.xml");
        apexClass.setFileName("MyClass.cls");
        apexClass.setMetadataFilePath("classes/MyClass.cls");
        apexClass.setBody("public with sharing class MyClass {\n" + "\n" + "}");

        apexClassMeta = new Component();
        apexClassMeta.setMetadataInstance(true);
        apexClassMeta.setComponentType("ApexClass");
        apexClassMeta.setFullName("MyClass");
        apexClassMeta.setFileExtension("cls");
        apexClassMeta.setMetadataFileExtension("-meta.xml");
        apexClassMeta.setFileName("MyClass.cls-meta.xml");
        apexClassMeta.setMetadataFilePath("classes/MyClass.cls-meta.xml");
        apexClassMeta.setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<ApexClass xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                + "    <apiVersion>30.0</apiVersion>\n" + "    <status>Active</status>\n" + "</ApexClass>\n");
    }

    private void setUpApexTrigger() {
        apexTrigger = new Component();
        apexTrigger.setComponentType("ApexTrigger");
        apexTrigger.setFullName("MyTrigger");
        apexTrigger.setFileExtension("trigger");
        apexTrigger.setMetadataFileExtension("-meta.xml");
        apexTrigger.setFileName("MyTrigger.trigger");
        apexTrigger.setMetadataFilePath("triggers/MyClass.trigger");
        apexTrigger.setBody("trigger MyTrigger on Account (before insert) {\n" + "\n" + "}");

        apexTriggerMeta = new Component();
        apexTriggerMeta.setMetadataInstance(true);
        apexTriggerMeta.setComponentType("ApexTrigger");
        apexTriggerMeta.setFullName("MyTrigger");
        apexTriggerMeta.setFileExtension("trigger");
        apexTriggerMeta.setMetadataFileExtension("-meta.xml");
        apexTriggerMeta.setFileName("MyTrigger.trigger-meta.xml");
        apexTriggerMeta.setMetadataFilePath("triggers/MyTrigger.cls-meta.xml");
        apexTriggerMeta.setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<ApexTrigger xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                + "    <apiVersion>30.0</apiVersion>\n" + "    <status>Active</status>\n" + "</ApexTrigger>\n" + "");
    }

    private void setUpApexPage() {
        apexPage = new Component();
        apexPage.setComponentType("ApexPage");
        apexPage.setFullName("MyPage");
        apexPage.setFileExtension("page");
        apexPage.setMetadataFileExtension("-meta.xml");
        apexPage.setFileName("MyPage.page");
        apexPage.setMetadataFilePath("pages/MyPage.page");
        apexPage.setBody("<apex:page >\n" + "<h1>MyPage</h1>\n" + "</apex:page>");

        apexPageMeta = new Component();
        apexPageMeta.setMetadataInstance(true);
        apexPageMeta.setComponentType("ApexPage");
        apexPageMeta.setFullName("MyPage");
        apexPageMeta.setFileExtension("page");
        apexPageMeta.setMetadataFileExtension("-meta.xml");
        apexPageMeta.setFileName("MyPage.page-meta.xml");
        apexPageMeta.setMetadataFilePath("pages/MyPage.page-meta.xml");
        apexPageMeta.setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<ApexPage xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                + "    <apiVersion>30.0</apiVersion>\n" + "    <availableInTouch>false</availableInTouch>\n"
                + "    <confirmationTokenRequired>false</confirmationTokenRequired>\n"
                + "    <description></description>\n" + "    <label>MyPage</label>\n" + "</ApexPage>");
    }

    private void setUpApexComponent() {
        apexComponent = new Component();
        apexComponent.setComponentType("ApexComponent");
        apexComponent.setFullName("MyComponent");
        apexComponent.setFileExtension("component");
        apexComponent.setMetadataFileExtension("-meta.xml");
        apexComponent.setFileName("MyComponent.component");
        apexComponent.setMetadataFilePath("components/MyComponent.component");
        apexComponent.setBody("<apex:component >\n" + "<h1>MyComponent</h1>\n" + "</apex:component>");

        apexComponentMeta = new Component();
        apexComponentMeta.setMetadataInstance(true);
        apexComponentMeta.setComponentType("ApexComponent");
        apexComponentMeta.setFullName("MyComponent");
        apexComponentMeta.setFileExtension("component");
        apexComponentMeta.setMetadataFileExtension("-meta.xml");
        apexComponentMeta.setFileName("MyComponent.component-meta.xml");
        apexComponentMeta.setMetadataFilePath("components/MyComponent.component-meta.xml");
        apexComponentMeta
        .setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<ApexComponent xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n"
                + "    <apiVersion>30.0</apiVersion>\n"
                + "    <description>This is a generic template for Visualforce Component.  With this template, you may adjust the default elements and values and add new elements and values.</description>\n"
                + "    <label>MyComponent</label>\n" + "</ApexComponent>\n");

    }

    // The following tests that the client actually sets things up properly.
    // It does *not* test that the server actually uses all the information. For that we rely on server side tests.
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void testHandleMetadataInstance() throws Exception {
        Component cmp = new Component();
        cmp.setMetadataInstance(true);
        assertNull(cmf.handleApexClass(cmp, cmps));
        assertNull(cmf.handleApexTrigger(cmp, cmps));
        assertNull(cmf.handleApexPage(cmp, cmps));
        assertNull(cmf.handleApexComponent(cmp, cmps));
    }

    public void testHandleApexClass() throws Exception {
        ToolingService toolingService = IdeTestUtil.getToolingService();
        SObject handledApexClass = toolingService.componentDelegate(apexClass, cmps, cmf);
        assertNotNull(handledApexClass);

        ApexClassMember acm = (ApexClassMember) handledApexClass;
        assertEquals(METADATA_CONTAINER_ID, acm.getMetadataContainerId());
        assertTrue(acm.getBody().contains("MyClass"));
        assertEquals(30.0, acm.getMetadata().getApiVersion());
        assertEquals(ApexCodeUnitStatus.Active, acm.getMetadata().getStatus());
    }

    public void testHandleApexTrigger() throws Exception {
        ToolingService toolingService = IdeTestUtil.getToolingService();
        SObject handledApexTrigger = toolingService.componentDelegate(apexTrigger, cmps, cmf);
        assertNotNull(handledApexTrigger);

        ApexTriggerMember atm = (ApexTriggerMember) handledApexTrigger;
        assertEquals(METADATA_CONTAINER_ID, atm.getMetadataContainerId());
        assertTrue(atm.getBody().contains("MyTrigger"));
        assertEquals(30.0, atm.getMetadata().getApiVersion());
        assertEquals(ApexCodeUnitStatus.Active, atm.getMetadata().getStatus());
    }

    public void testHandleApexPage() throws Exception {
        ToolingService toolingService = IdeTestUtil.getToolingService();
        SObject handledApexPage = toolingService.componentDelegate(apexPage, cmps, cmf);
        assertNotNull(handledApexPage);

        ApexPageMember apm = (ApexPageMember) handledApexPage;
        assertEquals(METADATA_CONTAINER_ID, apm.getMetadataContainerId());
        assertTrue(apm.getBody().contains("MyPage"));
        assertEquals(30.0, apm.getMetadata().getApiVersion());
        assertEquals("MyPage", apm.getMetadata().getLabel());

        // By default, the values are treated as false and they don't need to be included in the xml
        // However, we have to treat them as false explicitly because of the way we serialize/deserialize
        assertEquals(false, apm.getMetadata().getAvailableInTouch());
        assertEquals(false, apm.getMetadata().getConfirmationTokenRequired());

    }

    public void testHandleApexComponent() throws Exception {
        ToolingService toolingService = IdeTestUtil.getToolingService();
        SObject handledApexComponent = toolingService.componentDelegate(apexComponent, cmps, cmf);
        assertNotNull(handledApexComponent);

        ApexComponentMember acm = (ApexComponentMember) handledApexComponent;
        assertEquals(METADATA_CONTAINER_ID, acm.getMetadataContainerId());
        assertTrue(acm.getBody().contains("MyComponent"));
        assertEquals(30.0, acm.getMetadata().getApiVersion());
        assertEquals("MyComponent", acm.getMetadata().getLabel());
    }

    // Test package level utilities
    ///////////////////////////////

    public void testReplaceNamespace() throws Exception {
        String originalXml =
                "<RootElement xmlns=\"http://www.salesforce.com/testing\">\n" + "<Child>\n" + "Just some values\n"
                        + "</Child>\n" + "</RootElement>";
        // The replacer will insert the xml version header - that's fine
        String expectedXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><RootElement xmlns=\"urn:com.salesforce.com.test\">\n"
                        + "<Child>\n" + "Just some values\n" + "</Child>\n" + "</RootElement>";
        String replacedXml = cmf.replaceNamespace(originalXml, "urn:com.salesforce.com.test");
        assertEquals("Replacement of namespace has failed", stringToDocumentToString(expectedXml), stringToDocumentToString(replacedXml));
    }
    
    private String stringToDocumentToString(String input) throws Exception {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	Document doc = builder.parse(new InputSource(new StringReader(input)));
    	DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        return lsSerializer.writeToString(doc);
    }

    public void testGetCorrespondingMetadataComponent() throws Exception {
        Component meta = cmf.getCorrespondingMetaComponentIfAny(apexClass, cmps);
        assertNotNull(meta);
        assertEquals("File name doesn't match", "MyClass.cls-meta.xml", meta.getFileName());
    }
}
