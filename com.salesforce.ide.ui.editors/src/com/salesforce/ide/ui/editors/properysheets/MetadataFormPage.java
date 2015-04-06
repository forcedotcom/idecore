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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;

/**
 * Base-class for all editors that has a corresponding composite metadata file, e.g., Apex {Class, Trigger, Page,
 * Component}.
 * 
 * @author nchen
 * 
 */
public abstract class MetadataFormPage extends FormPage implements IMetadataSyncable {

    private static final Logger logger = Logger.getLogger(MetadataFormPage.class);

    public FormToolkit toolkit;

    public ScrolledForm form;

    protected BaseComponentMultiPageEditorPart multiPageEditor;

    protected boolean isPageModified;

    protected abstract void setFormTitle();

    protected abstract void createContent();

    protected Listener modificationListener;

    protected ICellEditorListener cellEditorListener;

    public MetadataFormPage(BaseComponentMultiPageEditorPart multiPageEditor) {
        super("", "");
        this.multiPageEditor = multiPageEditor;
        modificationListener = new Listener() {

            @Override
            public void handleEvent(Event event) {
                fireDirtyAsNecessary();
            }
        };
        cellEditorListener = new ICellEditorListener() {

            @Override
            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                // not applicable
            }

            @Override
            public void cancelEditor() {
                //not applicable
            }

            @Override
            public void applyEditorValue() {
                fireDirtyAsNecessary();
            }
        };
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (isPageModified) {
            syncToMetadata();
        } else {
            syncFromMetadata();
        }
        isPageModified = false;
        super.doSave(monitor);
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
        // Not allowed so do nothing
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        setSite(site);
        setInput(input);
    }

    @Override
    public boolean isDirty() {
        return isPageModified || super.isDirty();
    }

    @Override
    public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        form = toolkit.createScrolledForm(parent);
        setFormTitle();
        createContent();
    }

    @Override
    public void setFocus() {
        // Not applicable, no one particular UI that we prefer to set initial focus to
    }

    @Override
    public void dispose() {
        toolkit.dispose();
    }

    protected String getTextFromMetadataEditor() {
        StructuredTextEditor metadataEditor = multiPageEditor.getMetadataEditor();
        return metadataEditor.getDocumentProvider().getDocument(getEditorInput()).get();
    }

    protected void setTextOfMetadataEditor(String value) {
        AbstractDecoratedTextEditor metadataEditor = multiPageEditor.getMetadataEditor();
        IDocument document = metadataEditor.getDocumentProvider().getDocument(getEditorInput());
        if (!value.contentEquals(document.get().trim())) { // If the content is the same, don't bother setting
            document.set(value);
        }
    }

    private void fireDirtyAsNecessary() {
        boolean wasDirty = isDirty();
        isPageModified = true;
        if (!wasDirty) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public static <T> T unmarshall(String rawXML, Class<T> type) throws JAXBException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        JAXBContext context = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        XMLStreamReader createXMLStreamReader = factory.createXMLStreamReader(new StringReader(rawXML));
        return unmarshaller.unmarshal(createXMLStreamReader, type).getValue();
    }

    public static <T> String marshall(T object) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(object.getClass()).createMarshaller();

        if (marshaller == null) {
            logger.error("Unable to get marshaller for class '" + object.getClass().getName() + "'");
            return null;
        }

        // This removes the <?xml version....> 
        // We don't want this because JAXB adds the standalone="yes" fragment as well and it is hard to remove it
        // See http://stackoverflow.com/questions/277996/jaxb-remove-standalone-yes-from-generated-xml
        // So we don't add that header and just append it ourselves through the StringWriter
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        stringWriter.append(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n"));

        QName qname = new QName("http://soap.sforce.com/2006/04/metadata", object.getClass().getSimpleName());

        @SuppressWarnings("unchecked")
        // This cast is save since we are using the object's type
        Class<T> type = (Class<T>) object.getClass();
        marshaller.marshal(new JAXBElement<>(qname, type, object), stringWriter);

        return stringWriter.toString();
    }
}
