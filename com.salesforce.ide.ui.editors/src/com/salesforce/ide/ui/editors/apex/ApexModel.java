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
package com.salesforce.ide.ui.editors.apex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.XmlConstants;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.assistance.ApexObject;

/**
 * 
 * This is the old way of getting the completions. It's a misnomer to call it "ApexModel". I'm leaving this around for
 * now since there are some good test cases that rely on this. After I convert them, I will remove this.
 * 
 */
@Deprecated
public class ApexModel {

    private static final Logger logger = Logger.getLogger(ApexModel.class);

    public static final String APEX_XML_MODEL = "/config/apexModel.xml";

    private final HashMap<String, ApexObject> objects = new HashMap<>();

    private static final ApexModel INSTANCE;

    static {
    	try {
			INSTANCE = new ApexModel(ForceIdeEditorsPlugin.getFullUrlResource(ApexModel.APEX_XML_MODEL));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    public static ApexModel getInstance() {
    	return INSTANCE;
    }

    private ApexModel(URL apexModelUrl) throws IOException, SAXException {
        if (apexModelUrl == null) {
            throw new IllegalArgumentException("URL for Apex Model should not be null");
        }

        File apexModelFile = new File(apexModelUrl.getFile());
        if (!apexModelFile.exists()) {
            throw new IllegalArgumentException("File for Apex Model, '" + apexModelUrl.toExternalForm()
                    + "', should not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Loading Apex Model from " + apexModelFile.getAbsolutePath());
        }

        DOMParser parser = new DOMParser();
        try (final QuietCloseable<InputStream> c = QuietCloseable.make(apexModelUrl.openStream())) {
            final InputStream in = c.get();

            InputSource source = new InputSource(in);
            parser.parse(source);
        }

        Document doc = parser.getDocument();

        NodeList objects = doc.getChildNodes();

        objects = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < objects.getLength(); i++) {
            Node childNode = objects.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                addObject(objects.item(i));
            }
        }
    }

    private void addObject(Node node) {
        if (node != null && XmlConstants.ELEM_NAMESPACE.equals(node.getLocalName())) {
            String namespace = node.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
            NodeList children = node.getChildNodes();
            if (Utils.isEmpty(children)) {
                return;
            }

            for (int i = 0; i < children.getLength(); i++) {
                Node typeNode = children.item(i);
                if (typeNode != null && Utils.isNotEmpty(typeNode.getLocalName())
                        && XmlConstants.ELEM_TYPE.equals(typeNode.getLocalName())) {
                    String objectName =
                            typeNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue().toUpperCase();
                    String uniformName = Utils.capFirstLetterAndLetterAfterToken(objectName, ".", true);
                    if (objects.containsKey(uniformName)) {
                        ApexObject object = objects.get(uniformName);
                        object.loadObject(typeNode);
                    } else {
                        objects.put(uniformName, new ApexObject(namespace, typeNode));
                        if (uniformName.startsWith("System.")) {
                            objects.put(uniformName.substring(uniformName.indexOf(".")+1), new ApexObject(namespace, typeNode));
                        }
                    }
                }
            }
        }
    }

    public static boolean isStaticMethod(Node methodNode) {
        return methodNode != null && methodNode.hasAttributes()
                && methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_STATIC) != null
                && "true".equals(methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_STATIC).getNodeValue());
    }

    public HashMap<String, ApexObject> getApexObjects() {
        return objects;
    }

    public ApexObject getObject(String objectName) {
        String uniformName = Utils.capFirstLetterAndLetterAfterToken(objectName, ".", true);
        ApexObject object = objects.get(uniformName);
        if (object == null) {
            object = objects.get(objectName);
        }
        return object;
    }

    public Set<String> getObjectNames() {
        TreeSet<String> objNameSet = new TreeSet<>();
        if (Utils.isNotEmpty(objects)) {
            for (String objName : objects.keySet()) {
                objNameSet.add(objName);
            }
        }
        return objNameSet;
    }

    public int getCount() {
        return objects.size();
    }
}
