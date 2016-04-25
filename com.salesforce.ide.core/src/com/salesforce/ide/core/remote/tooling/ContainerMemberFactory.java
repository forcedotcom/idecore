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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.services.ToolingService.ComponentHandler;
import com.sforce.soap.tooling.sobject.ApexClassMember;
import com.sforce.soap.tooling.sobject.ApexComponentMember;
import com.sforce.soap.tooling.sobject.ApexPageMember;
import com.sforce.soap.tooling.sobject.ApexTriggerMember;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;

/**
 * <p>
 * Handles the creation of the corresponding MetadataInstances from Force.com IDE Components. This method, obviously,
 * not scalable. As we add more types to the ToolingAPI, we should create some generator.
 * </p>
 * <p>
 * However, because it is not obvious how future components will interact, I decided not to overengineer first and just
 * do the simplest thing possible.
 * </p>
 * 
 * @author nchen
 * 
 */
public final class ContainerMemberFactory implements ComponentHandler<SObject> {
    private static final Logger logger = Logger.getLogger(ContainerMemberFactory.class);

    private final String containerId;

    public ContainerMemberFactory(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public SObject handleApexClass(Component cmp, ComponentList cmps) {
        return createContainerMemberIfPossible(cmp, cmps, ApexClassMember.class);
    }

    @Override
    public SObject handleApexTrigger(Component cmp, ComponentList cmps) {
        return createContainerMemberIfPossible(cmp, cmps, ApexTriggerMember.class);
    }

    @Override
    public SObject handleApexPage(Component cmp, ComponentList cmps) {
        return createContainerMemberIfPossible(cmp, cmps, ApexPageMember.class);
    }

    @Override
    public SObject handleApexComponent(Component cmp, ComponentList cmps) {
        return createContainerMemberIfPossible(cmp, cmps, ApexComponentMember.class);
    }

    @Override
    public SObject handleUnknownCase(Component cmp, ComponentList cmps) {
        return null;
    }

    private static XmlInputStream createCorrespondingXmlInputStream(Component metadata) throws Exception, PullParserException {
        String body = replaceNamespace(metadata.getBody(), "urn:metadata.tooling.soap.sforce.com");
        XmlInputStream xis = new XmlInputStream();
        xis.setInput(new ByteArrayInputStream(body.getBytes()), "UTF-8");
        return xis;
    }

    // Package level utilities so we can test
    //////////////////////////////////////////

    /**
     * Reflectively constructs the container member. Done using reflection because the container members are generated
     * through the WSDL and don't share a common interface/superclass.
     * 
     * @param cmp
     *            The component that we want to create a container member for
     * @param cmps
     *            The list of components that are part of this save, so we can inspect and find the corresponding
     *            metadata, if any.
     * @param containerMemberType
     *            The type that we want to instantiate
     * @return The container member
     */
    SObject createContainerMemberIfPossible(Component cmp, ComponentList cmps, Class<?> containerMemberType) {
        if (cmp.isMetadataInstance()) {
            return null;
        } else {
            try {
                Object containerMember = containerMemberType.newInstance();

                Method setBodyMethod = containerMemberType.getMethod("setBody", String.class);
                setBodyMethod.invoke(containerMember, cmp.getBody());

                // setMetadataContainerID
                Method setMetadataContainerIdMethod =
                        containerMemberType.getMethod("setMetadataContainerId", String.class);
                setMetadataContainerIdMethod.invoke(containerMember, containerId);

                // setContentEntityId
                Method setContentEntityIdMethod = containerMemberType.getMethod("setContentEntityId", String.class);
                setContentEntityIdMethod.invoke(containerMember, cmp.getId());

                Component metadata = getCorrespondingMetaComponentIfAny(cmp, cmps);
                if (metadata != null) {
                    try {
                        setMetadata(containerMember, metadata);
                    } catch (Exception e) {
                        logger.warn(
                            "Error trying to set the metadata, will proceed without saving metadata for " + cmp, e);
                    }
                }

                return (SObject) containerMember;

            } catch (Exception e) {
                logger.error("Error trying to construct the metadata container member, the component won't be saved", e);
                return null;
            }
        }
    }

    /**
     * Sets the metadata on the containerMember reflectively. We do it this way because the containerMembers are
     * generated from the WSDL and do not implement a common interface/hierarchy. If we don't use reflection, we end up
     * with a lot of boilerplate code. The downside of reflection is that it can be slower - though this is not in a
     * critical code path.
     * 
     * @param containerMember
     *            This object must have a setMetadata method on it since it will be called reflectively.
     * @param metadataComponent
     *            The component that contains the metadata as its body
     * @throws Exception
     */
    static void setMetadata(Object containerMember, Component metadataComponent) throws Exception {
        Method[] methods = containerMember.getClass().getMethods();
        Method setMetadataMethod = null;

        for (Method method : methods) {
            if (method.getName().equals("setMetadata")) {
                setMetadataMethod = method;
                break;
            }
        }

        if (setMetadataMethod != null) {
            Class<?> metadataType = (setMetadataMethod.getParameterTypes())[0]; // There is only one parameter
            Object metadataInstance = metadataType.newInstance();
            XmlInputStream xis = createCorrespondingXmlInputStream(metadataComponent);
            Method loadMethod = metadataType.getMethod("load", XmlInputStream.class, TypeMapper.class);
            loadMethod.invoke(metadataInstance, xis, new TypeMapper());
            setMetadataMethod.invoke(containerMember, metadataInstance);
        }
    }

    static String replaceNamespace(String originalContents, String newTopLevelNamespace) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(originalContents)));

        doc.getDocumentElement().removeAttribute("xmlns");
        doc.getDocumentElement().setAttribute("xmlns", newTopLevelNamespace);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    static Component getCorrespondingMetaComponentIfAny(Component cmp, ComponentList cmps) {
        String fileName = String.format("%s.%s", cmp.getFullName(), cmp.getMetadataFileExtension());
        return cmps.getComponentByFileName(fileName);
    }
}
