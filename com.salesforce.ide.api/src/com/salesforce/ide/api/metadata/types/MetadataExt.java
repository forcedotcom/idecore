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
package com.salesforce.ide.api.metadata.types;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.salesforce.ide.api.ForceIdeAPIPlugin;
import com.salesforce.ide.api.internal.utils.ApiConstants;

public class MetadataExt extends com.salesforce.ide.api.metadata.types.Metadata {

    private static final Logger logger = Logger.getLogger(MetadataExt.class);

    private static Schema metadataSchema = null;

    static {
        URL schemaFile = ForceIdeAPIPlugin.getFullUrlResource(ApiConstants.SCHEMA_LOCATION);
        try {
            metadataSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile);
        } catch (Exception e) {
            logger.warn("Unable to get metadata schema from '" + ApiConstants.SCHEMA_LOCATION + "': " + e.getMessage());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String getXMLString() throws JAXBException {
        // prepare body and save component
        Marshaller marshaller = JAXBContext.newInstance(getClass()).createMarshaller();

        if (marshaller == null) {
            logger.error("Unable to get marshaller for class '" + getClass().getName() + "'");
            return null;
        }

        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        QName qname = new QName("http://soap.sforce.com/2006/04/metadata", this.getClass().getSimpleName());
        marshaller.marshal(new JAXBElement(qname, this.getClass(), this), stringWriter);

        if (logger.isDebugEnabled()) {
            logger.debug("Generated the following XML string for object '" + getClass().getName() + "':\n"
                    + stringWriter.toString());
        }

        return stringWriter.toString();
    }

    @SuppressWarnings("unchecked")
    public MetadataExt getComponentFromXML(String xmlString) throws JAXBException {
        if (isEmpty(xmlString)) {
            logger.warn("Unable to unmarshaller class '" + getClass().getName() + "' - no XML string provided");
            return null;
        }

        // prepare body and save component
        Unmarshaller unmarshaller = JAXBContext.newInstance(getClass()).createUnmarshaller();

        if (unmarshaller == null) {
            logger.error("Unable to get unmarshaller for class '" + getClass().getName() + "'");
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalling the following content for class '" + getClass().getName() + "':\n" + xmlString);
        }

        JAXBElement<MetadataExt> metadataExt =
                (JAXBElement<MetadataExt>) unmarshaller.unmarshal(new StreamSource(new StringReader(xmlString)),
                    getClass());
        return metadataExt.getValue();
    }

    @SuppressWarnings("unchecked")
    public MetadataExt getComponentFromXML(String xmlString, boolean validate,
            ValidationEventHandler validationEventHandler) throws JAXBException {
        if (isEmpty(xmlString)) {
            logger.warn("Unable to unmarshaller class '" + getClass().getName() + "' - no XML string provided");
            return null;
        }

        // prepare body and save component
        Unmarshaller unmarshaller = JAXBContext.newInstance(getClass()).createUnmarshaller();

        if (unmarshaller == null) {
            logger.error("Unable to get unmarshaller for class '" + getClass().getName() + "'");
            return null;
        }

        if (validate && metadataSchema != null) {
            unmarshaller.setSchema(metadataSchema);
        }

        unmarshaller.setEventHandler(validationEventHandler);

        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalling the following content for class '" + getClass().getName() + "':\n" + xmlString);
        }

        JAXBElement<MetadataExt> metadataExt =
                (JAXBElement<MetadataExt>) unmarshaller.unmarshal(new StreamSource(new StringReader(xmlString)),
                    getClass());

        return metadataExt.getValue();
    }

    @SuppressWarnings("unchecked")
    public MetadataExt getComponentFromNode(Node node) throws JAXBException {
        if (node == null) {
            logger.warn("Unable to unmarshaller class '" + getClass().getName() + "' - no node provided");
            return null;
        }

        // prepare body and save component
        Unmarshaller unmarshaller = JAXBContext.newInstance(getClass()).createUnmarshaller();

        if (unmarshaller == null) {
            logger.error("Unable to get unmarshaller for class '" + getClass().getName() + "'");
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalling the following content for class '" + getClass().getName() + "':\n"
                    + node.getNodeName());
        }

        JAXBElement<MetadataExt> metadataExt = (JAXBElement<MetadataExt>) unmarshaller.unmarshal(node, getClass());
        return metadataExt.getValue();
    }

    public byte[] getBytes() {
        String xmlStr = null;
        byte[] byteArray = null;
        try {
            xmlStr = getXMLString();
            if (isNotEmpty(xmlStr)) {
                byteArray = xmlStr.getBytes();
            }
        } catch (JAXBException e) {
            logger.error("Unable to get XML string", e);
        }

        return byteArray;
    }

    protected boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    protected boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
