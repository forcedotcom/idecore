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
package com.salesforce.ide.core.internal.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.services.ProjectService;

/**
 * This class contains convenience methods for working with package.xml files which are loaded into memory as as w3c DOM
 * trees
 * 
 * @author ataylor
 * 
 */
public class PackageManifestDocumentUtils {

    private static final Logger logger = Logger.getLogger(PackageManifestDocumentUtils.class);
    
    private static final ProjectService projectService = ContainerDelegate.getInstance().getServiceLocator()
            .getProjectService();

    public static final NamespaceContext nsc = new NamespaceContext() {

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals(Constants.PACKAGE_MANIFEST_METADATA)) {
                return Constants.PACKAGE_MANIFEST_NAMESPACE_URI;
            }

            return null;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return null;
        }
    };

    /**
     * returns all the type element nodes within the document
     * 
     * @param doc
     *            The w3c Document
     * @return the list of found nodes, returns an empty list if none found
     */
    public static List<Node> getComponentTypes(Document doc) {
        List<Node> list = new ArrayList<>();
        Node packageNode = getPackageNode(doc);

        if (packageNode != null) {
            NodeList nodeList = packageNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                if (item.getNodeName().equals(Constants.PACKAGE_MANIFEST_TYPES)) {
                    list.add(item);
                }
            }
        }

        return list;
    }

    /**
     * returns all the members element nodes for the given type element
     * 
     * @param component
     *            the w3c type node
     * @return the list of found nodes, returns an empty list if none found
     */
    public static List<Node> getComponentMembers(Node component) {
        List<Node> list = new ArrayList<>();

        if (component != null) {
            NodeList nodeList = component.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                if (item.getNodeName().equals(Constants.PACKAGE_MANIFEST_TYPE_MEMBERS)) {
                    list.add(item);
                }
            }
        }

        return list;
    }

    /**
     * returns the name for the given member element
     * 
     * @param member
     *            the w3c member node
     * @return the name
     */
    public static String getMemberName(Node member) {
    	//To guard against server returning null members 
    	//https://gus.salesforce.com/a07B0000000LEC0IAO
    	if (member.getFirstChild() == null) {
    		return "null";
    	}
        return member.getFirstChild().getNodeValue();
    }

    /**
     * returns the name for the given component element
     * 
     * @param component
     *            the w3c type node
     * @return the name
     */
    public static String getComponentName(Node component) {
        NodeList nodeList = component.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item.getNodeName().equals(Constants.PACKAGE_MANIFEST_TYPE_NAME)) {
                return item.getFirstChild().getNodeValue();
            }
        }

        return null;
    }

    /**
     * returns the package element for the given document
     * 
     * @param doc
     *            the w3c document
     * @return the package element, or null if not found
     */
    public static Node getPackageNode(Document doc) {
        return doc.getDocumentElement();
    }

    /**
     * Adds a package element to the given document
     * 
     * @param doc
     *            the w3c document
     * @return the created package element
     */
    public static Node addPackageNode(Document doc) {
        Node packageNode = getPackageNode(doc);

        if (packageNode == null) {
            packageNode =
                    doc.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI, Constants.PACKAGE_MANIFEST_PACKAGE);
            Node version =
                    doc.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI, Constants.PACKAGE_MANIFEST_VERSION);

            Text versionText = doc.createTextNode(projectService.getLastSupportedEndpointVersion());
            version.appendChild(versionText);
            packageNode.appendChild(version);
            doc.insertBefore(packageNode, doc.getLastChild());
        }

        return packageNode;
    }

    /**
     * returns the last type element in the document
     * 
     * @param doc
     *            the w3c document
     * @return the type element, or null if none found
     */
    public static Node getLastComponentNode(Document doc) {
        List<Node> nodes = getComponentTypes(doc);

        if (nodes.size() > 0) {
            return nodes.get(nodes.size() - 1);
        }
        return null;
    }

    /**
     * returns the type element for the given name
     * 
     * @param doc
     *            the w3c document
     * @param name
     *            the name of the requested type element
     * @return the type element, or null if not found
     */
    public static Node getComponentNode(Document doc, String name) {
        for (Node componentTypeNode : getComponentTypes(doc)) {
            String componentTypeName = getComponentName(componentTypeNode);
            if (name.equals(componentTypeName)) {
                return componentTypeNode;
            }
        }

        return null;
    }

    /**
     * returns the version element for the given document
     * 
     * @param doc
     *            the w3c document
     * @return the version node, or null if not found
     */
    public static Node getVersionNode(Document doc) {
        NodeList nodeList = getPackageNode(doc).getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item.getNodeName().equals(Constants.PACKAGE_MANIFEST_VERSION)) {
                return item;
            }
        }

        return null;
    }

    /**
     * returns the name node for the given type element
     * 
     * @param componentNode
     *            the type element
     * @return the name node, or null if not found
     */
    public static Node getComponentNameNode(Node componentNode) {
        NodeList nodeList = componentNode.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item.getNodeName().equals(Constants.PACKAGE_MANIFEST_TYPE_NAME)) {
                return item;
            }
        }

        return null;
    }

    /**
     * returns the member element of the given name for the given type element
     * 
     * @param parent
     *            the type element of interest
     * @param name
     *            the name of the member
     * @return the member element, or null if not found
     */
    public static Node getMemberNode(Node parent, String name) {
        List<Node> members = getComponentMembers(parent);

        for (Node member : members) {
            if (getMemberName(member).equals(name)) {
                return member;
            }
        }

        return null;
    }

    /**
     * returns the last member element for the given type element
     * 
     * @param parent
     *            the type element
     * @return the last member element, or null if none found
     */
    public static Node getLastMemberNode(Node parent) {
        List<Node> nodes = getComponentMembers(parent);

        if (nodes.size() > 0) {
            return nodes.get(nodes.size() - 1);
        }
        return null;
    }

    /**
     * removes all component elements from the document
     * 
     * @param document
     *            the w3c document
     */
    public static void removeAllComponentTypeNodes(Document document) {
        List<Node> types = getComponentTypes(document);
        for (Node type : types) {
            type.getParentNode().removeChild(type);
        }
    }

    /**
     * creates a new member element and adds it as a child of the given type element. If the member element already
     * exists will return the existing element
     * 
     * @param componentTypeNode
     *            the type element
     * @param memberName
     *            the name of the new member element
     * @return the member element which has name memberName
     */
    public static Node addMemberNode(Node componentTypeNode, String memberName) {
        Node nameNode = getComponentNameNode(componentTypeNode);
        Node wildcard = getMemberNode(componentTypeNode, Constants.PACKAGE_MANIFEST_WILDCARD);

        Node member = getMemberNode(componentTypeNode, memberName);
        if (member == null) {
            member =
                    componentTypeNode.getOwnerDocument().createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI,
                        Constants.PACKAGE_MANIFEST_TYPE_MEMBERS);
            Text memberNameNode = componentTypeNode.getOwnerDocument().createTextNode(memberName);
            member.appendChild(memberNameNode);

            if (wildcard == null) {
                componentTypeNode.insertBefore(member, nameNode);
            }

            else {
                componentTypeNode.insertBefore(member, wildcard);
            }
        }
        return member;
    }

    /**
     * creates a new wildcard member element to the given type element. If a wildcard element already exists, will
     * return the existing element
     * 
     * @param component
     *            the type element
     * @return the child member of component which has name "*"
     */
    public static Node addWildcardMember(Node component) {
        Node member = getMemberNode(component, Constants.PACKAGE_MANIFEST_WILDCARD);

        if (member == null) {
            member =
                    component.getOwnerDocument().createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI,
                        Constants.PACKAGE_MANIFEST_TYPE_MEMBERS);
            Text wildcard = component.getOwnerDocument().createTextNode(Constants.PACKAGE_MANIFEST_WILDCARD);
            member.appendChild(wildcard);
            component.insertBefore(member, component.getLastChild());
        }

        return member;
    }

    /**
     * removes the member element by the given name from the given type element
     * 
     * @param component
     *            the type element
     * @param name
     *            the name of the member to remove
     */
    public static void removeMemberNode(Node component, String name) {
        Node member = getMemberNode(component, name);

        if (member != null) {
            member.getParentNode().removeChild(member);
        }
    }

    /**
     * removes the wildcard member element from the given type element
     * 
     * @param component
     *            the type element
     */
    public static void removeWildcardNode(Node component) {
        removeMemberNode(component, Constants.PACKAGE_MANIFEST_WILDCARD);
    }

    /**
     * creates a new type element of the given name and adds it as a child of the given document. If the type element
     * already exists, will return the existing element
     * 
     * @param doc
     *            the w3c document
     * @param nodeName
     *            the name of the type element to create
     * @return the new type element
     */
    public static Node addComponentTypeNode(Document doc, String nodeName) {
        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, nodeName);

        if (typeNode == null) {
            typeNode = doc.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI, Constants.PACKAGE_MANIFEST_TYPES);

            Node name =
                    doc.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI, Constants.PACKAGE_MANIFEST_TYPE_NAME);
            typeNode.appendChild(name);

            Text typeName = doc.createTextNode(nodeName);
            name.appendChild(typeName);

            Node versionNode = PackageManifestDocumentUtils.getVersionNode(doc);
            if (versionNode != null) {
                getPackageNode(doc).insertBefore(typeNode, versionNode);
            }

            else {
                getPackageNode(doc).appendChild(typeNode);
            }
        }

        return typeNode;
    }

    /**
     * removes the type element of the given name from the document
     * 
     * @param doc
     *            the w3c document
     * @param nodeName
     *            name of the type element to remove
     */
    public static void removeComponentTypeNode(Document doc, String nodeName) {
        Node component = getComponentNode(doc, nodeName);

        if (component != null) {
            component.getParentNode().removeChild(component);
        }
    }

    private static Comparator<Node> typeComparator = new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(getComponentName(o1), getComponentName(o2));
        }
    };

    private static Comparator<Node> memberComparator = new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(getMemberName(o1), getMemberName(o2));
        }
    };

    /**
     * compares the contents of two package.xml documents
     * 
     * @param p1
     *            the first w3c document
     * @param p2
     *            the second w3c document
     * @return true if document content is equal, false otherwise
     */
    public static boolean isEqual(Document p1, Document p2) {
        List<Node> p1Types = getComponentTypes(p1);
        List<Node> p2Types = getComponentTypes(p2);

        if (p1Types.size() != p2Types.size()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Documents have different number of component types"); //$NON-NLS-1$
            }
            return false;
        }

        Collections.sort(p1Types, typeComparator);
        Collections.sort(p2Types, typeComparator);

        for (int i = 0; i < p1Types.size(); i++) {
            if (!getComponentName(p1Types.get(i)).equals(getComponentName(p2Types.get(i)))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Component types differ, expected: " + getComponentName(p1Types.get(i)) + " found: " //$NON-NLS-1$  //$NON-NLS-2$
                            + getComponentName(p2Types.get(i)));
                }
                return false;
            }

            List<Node> p1Members = getComponentMembers(p1Types.get(i));
            List<Node> p2Members = getComponentMembers(p2Types.get(i));

            if (p1Members.size() != p2Members.size()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Different number of members for " + p1Types.get(i)); //$NON-NLS-1$
                }
                return false;
            }

            Collections.sort(p1Members, memberComparator);
            Collections.sort(p2Members, memberComparator);

            for (int j = 0; j < p1Members.size(); j++) {
                if (!getMemberName(p1Members.get(j)).equals(getMemberName(p2Members.get(j)))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Member names differ, expected: " + getMemberName(p1Members.get(i)) + " found: " //$NON-NLS-1$  //$NON-NLS-2$
                                + getMemberName(p2Members.get(i)));
                    }
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * returns whether the given document has any types
     * 
     * @param document
     *            the w3c document
     * @return true if the document has any type elements, false otherwise
     */
    public static boolean hasContent(Document document) {
        if (document == null) {
            return false;
        }

        return PackageManifestDocumentUtils.getComponentTypes(document).size() > 0;
    }

    /**
     * prints the content of the given document to the log
     * 
     * @param packageManifestDocument
     *            the w3c document
     */
    public static void log(Document packageManifestDocument) {
        StringBuffer strBuff = new StringBuffer("Document contains the following content:"); //$NON-NLS-1$
        if (!hasContent(packageManifestDocument)) {
            strBuff.append(" Document empty or null"); //$NON-NLS-1$
            logger.info(strBuff.toString());
            return;
        }

        List<Node> componentTypeNodes = getComponentTypes(packageManifestDocument);

        if (componentTypeNodes.size() > 0) {
            for (int i = 0; i < componentTypeNodes.size(); i++) {
                Node componentTypeNode = componentTypeNodes.get(i);
                strBuff.append("\n (").append(i + 1).append(") ").append(getComponentName(componentTypeNode)); //$NON-NLS-1$  //$NON-NLS-2$
                List<Node> componentMembers = getComponentMembers(componentTypeNode);
                strBuff.append(" [").append(componentMembers.size()).append("]:"); //$NON-NLS-1$  //$NON-NLS-2$
                for (int k = 0; k < componentMembers.size(); k++) {
                    Node componentMember = componentMembers.get(k);
                    strBuff.append("\n   (").append(k + 1).append(") ").append(getMemberName(componentMember)); //$NON-NLS-1$  //$NON-NLS-2$
                }
            }
        } else {
            strBuff.append("n/a"); //$NON-NLS-1$
        }
        logger.info(strBuff.toString());
    }
}
