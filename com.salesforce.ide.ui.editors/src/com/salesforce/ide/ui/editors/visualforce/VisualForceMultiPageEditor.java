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
package com.salesforce.ide.ui.editors.visualforce;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;

/**
 * Handles opening Visualforce or HTML content and associated metadata.
 * 
 * @author dcarroll
 */
public class VisualForceMultiPageEditor extends BaseComponentMultiPageEditorPart implements IResourceChangeListener {

    private static final Logger logger = Logger.getLogger(VisualForceMultiPageEditor.class);

    protected VisualForceStructuredTextEditor sourceEditor = null;
    private PropertyListener sourcePropertyListener = null;
    private PropertyListener metadataPropertyListener = null;

    public VisualForceMultiPageEditor() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    // M E T H O D S
    @Override
    protected String getEditorName() {
        return "S-Control Editor";
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        if (editorInput == null) {
            logger.warn("Unable to open editor - input is null");
            return;
        }
        super.init(site, editorInput);
        setPartName(editorInput.getName());
    }

    @Override
    protected void createPages() {
        try {
            preparePages();
            updateTitle();
        } catch (Exception e) {
            logger.warn("Unable to open " + getEditorName() + " for file '" + getEditorInputFile().getName() + "'", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to load " + getEditorName() + ".");
        }
    }

    protected void preparePages() throws PartInitException {
        if (sourceEditorInput != null) {
            createSourcePage();
            addSourcePage();
        }
        if (metadataEditorInput != null) {
            // Create the metadata page first since the properties page depends on it
            createMetadataPage();
            createPropertySheetPage();

            // Add metadata page last so that is appears last
            addPropertiesSheet();
            addMetadataPage();
        }
    }

    protected void addSourcePage() throws PartInitException {
        sourcePageIndex = addPage(sourceEditor, getSourceEditorInput());
        setPageText(sourcePageIndex, EditorMessages.getString("GenericEditor.SourceTab.label"));
        // the update's critical, to get viewer selection manager and highlighting to work
        try {
            sourceEditor.update();
        } catch (Exception e) {
            logger.error("Unable to open " + getEditorName() + "", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to open " + getEditorName() + ".");
        }
        firePropertyChange(PROP_TITLE);
    }

    private void addPropertiesSheet() throws PartInitException {
        if (propertySheet != null) {
            propertyPageIndex = addPage(propertySheet, getMetadataEditorInput());
            setPageText(propertyPageIndex, EditorMessages.getString("ApexEditor.PropertiesTab.label"));
        }
    }

    protected void addMetadataPage() throws PartInitException {
        metadataPageIndex = addPage(metadataEditor, getMetadataEditorInput());
        setPageText(metadataPageIndex, EditorMessages.getString("GenericEditor.MetadataTab.label"));
        // the update's critical, to get viewer selection manager and highlighting to work
        try {
            metadataEditor.update();
        } catch (Exception e) {
            logger.error("Unable to open " + getEditorName() + "", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to open " + getEditorName() + ".");
        }
        firePropertyChange(PROP_TITLE);
    }

    /**
     * Creates page 0 of the multi-page editor, which contains a text editor.
     */
    protected void createSourcePage() {
        sourceEditor = new VisualForceStructuredTextEditor();
        sourceEditor.setEditorPart(this);

        if (sourcePropertyListener == null) {
            sourcePropertyListener = new PropertyListener();
        }
        sourceEditor.addPropertyListener(sourcePropertyListener);
    }

    protected void createMetadataPage() {
        metadataEditor = new StructuredTextEditor();
        metadataEditor.setEditorPart(this);
        if (metadataPropertyListener == null) {
            metadataPropertyListener = new PropertyListener();
        }
        metadataEditor.addPropertyListener(metadataPropertyListener);
    }

    public StructuredTextEditor getTextEditor() {
        return sourceEditor;
    }

    @Override
    protected void updateTitle() {
        IEditorInput input = getEditorInput();
        setPartName(input.getName());
        setTitleToolTip(input.getToolTipText());
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        Object adapter = super.getAdapter(key);
        if (adapter == null) {
            adapter = getSourcePage().getAdapter(key);
        }
        return adapter;
    }

    protected StructuredTextEditor getSourcePage() {
        return sourceEditor;
    }

    @Override
    public String getTitle() {
        String title = super.getTitle();
        if ((title == null) && (getEditorInput() != null)) {
            title = getEditorInput().getName();
        }
        return title;
    }

    /**
     * The <code>MultiPageEditorPart</code> implementation of this <code>IWorkbenchPart</code> method disposes all
     * nested editors. Subclasses may extend.
     */
    @Override
    public void dispose() {
        getTextEditor().getDocumentProvider().disconnect(getEditorInput());
        sourceEditor.removePropertyListener(sourcePropertyListener);
        metadataEditor.removePropertyListener(metadataPropertyListener);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    protected IEditorPart getEditor(int pageIndex) {
        return super.getEditor(pageIndex);
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart
     */
    public void gotoMarker(IMarker marker) {
        setActivePage(0);
        ((IGotoMarker) sourceEditor.getAdapter(IGotoMarker.class)).gotoMarker(marker);
    }

    @Override
    public void setFocus() {
        switch (getActivePage()) {
        case 0:
            sourceEditor.setFocus();
            break;
        case 1:
            metadataEditor.setFocus();
            break;
        }
    }

    protected void doFirePropertyChange(int property) {
        super.firePropertyChange(property);
    }

    /**
     * Closes all project files on project close.
     */
    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                    for (IWorkbenchPage element : pages) {
                        if (((FileEditorInput) sourceEditor.getEditorInput()).getFile().getProject()
                                .equals(event.getResource())) {
                            IEditorPart editorPart = element.findEditor(sourceEditor.getEditorInput());
                            element.closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

    class PropertyListener implements IPropertyListener {
        @Override
        public void propertyChanged(Object source, int propId) {
            switch (propId) {
            // had to implement input changed "listener" so that
            // StructuredTextEditor could tell it containing editor that
            // the input has change, when a 'resource moved' event is
            // found.
            case IEditorPart.PROP_INPUT:
            case IEditorPart.PROP_DIRTY: {
                if (source == getTextEditor()) {
                    if (getTextEditor().getEditorInput() != getEditorInput()) {
                        setInput(getTextEditor().getEditorInput());
                        // title should always change when input changes. create runnable for following post call
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                doFirePropertyChange(IWorkbenchPart.PROP_TITLE);
                            }
                        };
                        // Update is just to post things on the display queue (thread). We have to do this to get the
                        // dirty property to get updated after other things on the queue are executed.
                        ((Control) getTextEditor().getAdapter(Control.class)).getDisplay().asyncExec(runnable);
                    }
                }
                break;
            }
            case IWorkbenchPart.PROP_TITLE: {
                // update the input if the title is changed
                if (source == getTextEditor()) {
                    if (getTextEditor().getEditorInput() != getEditorInput()) {
                        setInput(getTextEditor().getEditorInput());
                    }
                }
                break;
            }
            default: {
                // propagate changes. Is this needed? Answer: Yes.
                if (source == getTextEditor()) {
                    doFirePropertyChange(propId);
                }
                break;
            }
            }

        }
    }

}
