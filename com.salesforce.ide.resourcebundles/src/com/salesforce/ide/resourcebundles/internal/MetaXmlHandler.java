package com.salesforce.ide.resourcebundles.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * Access and modify the resource meta file.
 */
public class MetaXmlHandler {

    private static final int META_BUFFER_SIZE = 1024;
    private static final String CACHE_CONTROL = "Public"; //$NON-NLS-1$
    private static final String ZIP_CONTENT_TYPE = "application/zip"; //$NON-NLS-1$
    private static final String NS = "http://soap.sforce.com/2006/04/metadata"; //$NON-NLS-1$
    
    // Access these via getters
    private DocumentBuilder builder;
    private Transformer transformer;
    
    /**
     * Check if a resource meta file has ZIP content type.
     */
    public boolean isZip(IFile meta) throws Exception {
    	if (!meta.exists()) {
    		return false;
    	}
        try {
            Document doc = getBuilder().parse(meta.getContents());
            NodeList nl = doc.getElementsByTagName("contentType"); //$NON-NLS-1$
            if (nl.getLength() == 1) {
                Node n = nl.item(0);
                return ZIP_CONTENT_TYPE.equals(n.getTextContent());
            } else {
                return false;
            }
        } catch (SAXParseException e) {
            // Invalid content
            return false;
        }
    }
    
    /**
     * If the resource meta file doesn't exist create it or if it does exists but isn't of ZIP content type update it.
     */
    public void createOrUpdate(IFile meta) throws Exception {
        if (meta.exists()) {
            Document doc = getBuilder().parse(meta.getContents());
            NodeList nl = doc.getElementsByTagName("contentType"); //$NON-NLS-1$
            if (nl.getLength() == 1) {
                Node n = nl.item(0);
                if (ZIP_CONTENT_TYPE.equals(n.getTextContent())) {
                    // Good already
                    return;
                } else {
                    // Change just this element
                    n.setTextContent(ZIP_CONTENT_TYPE);
                    write(meta, doc);
                }
            } else {
                // Create new one
                write(meta, createDocument());
            }
        } else {
            write(meta, createDocument());
        }
    }
    
    private void write(IFile meta, Document doc) throws Exception {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(META_BUFFER_SIZE);
        getTransformer().transform(new DOMSource(doc), new StreamResult(bos));
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        
        if (meta.exists()) {
            meta.setContents(bis, IResource.FORCE, null);
        } else {
            meta.create(bis, IResource.FORCE, null);
        }
    }
    
    private Document createDocument() throws Exception {

        Document doc = getBuilder().newDocument();
        Element root = doc.createElementNS(NS, "StaticResource"); //$NON-NLS-1$
        doc.appendChild(root);
        Element cacheControl = doc.createElementNS(NS, "cacheControl"); //$NON-NLS-1$
        root.appendChild(cacheControl);
        Element contentType = doc.createElementNS(NS, "contentType"); //$NON-NLS-1$
        root.appendChild(contentType);
    
        cacheControl.setTextContent(CACHE_CONTROL);
        contentType.setTextContent(ZIP_CONTENT_TYPE);
        
        return doc;
    }
    
    private DocumentBuilder getBuilder() throws Exception {
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        return builder;
    }
    
    private Transformer getTransformer() throws Exception {
        if (transformer == null) {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return transformer;
    }
}