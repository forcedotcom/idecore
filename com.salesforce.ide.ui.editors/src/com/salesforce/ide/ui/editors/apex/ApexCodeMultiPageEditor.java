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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.salesforce.ide.apex.core.tooling.systemcompletions.ApexSystemCompletionsRepository;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;
import com.salesforce.ide.ui.editors.internal.utils.EditorConstants;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;

/**
 * Handles opening Apex Code content and associated metadata.
 * 
 * @author cwall
 */
public class ApexCodeMultiPageEditor extends BaseComponentMultiPageEditorPart {

    private static final Logger logger = Logger.getLogger(ApexCodeMultiPageEditor.class);

    public static final String EDITOR_NAME = "Apex Editor";

    protected ApexCodeEditor apexCodeEditor = null;

    public ApexCodeMultiPageEditor() {
        super();
    }

    // M E T H O D S
    @Override
    public String getEditorName() {
        return EDITOR_NAME;
    }

    public ApexCodeEditor getApexCodeEditor() {
        return apexCodeEditor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        setPartName(editorInput.getName());
        setProject(getEditorInputFile().getProject());
        ApexSystemCompletionsRepository.INSTANCE.getCompletionsFetchIfNecessary(getProjectService().getForceProject(
            getProject()));
//        initSObjects();
    }

    @Override
    protected void createPages() {
        try {
            preparePages();
        } catch (Exception e) {
            logger.warn("Unable to open Apex Code editor for file '" + getEditorInputFile().getName() + "'", e);
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

    private void addSourcePage() throws PartInitException {
        sourcePageIndex = addPage(apexCodeEditor, getSourceEditorInput());
        setPageText(sourcePageIndex, EditorMessages.getString("ApexEditor.SourceTab.label"));
        firePropertyChange(PROP_TITLE);
    }

    private void addPropertiesSheet() throws PartInitException {
        if (propertySheet != null) {
            propertyPageIndex = addPage(propertySheet, getMetadataEditorInput());
            setPageText(propertyPageIndex, EditorMessages.getString("ApexEditor.PropertiesTab.label"));
        }
    }

    private void addMetadataPage() throws PartInitException {
        metadataPageIndex = addPage(metadataEditor, getMetadataEditorInput());
        setPageText(metadataPageIndex,
            EditorMessages.getString("ApexEditor.MetadataTab.label", new Object[] { getEditorInput().getName() }));
        // the update's critical, to get viewer selection manager and highlighting to work
        try {
            metadataEditor.update();
        } catch (Exception e) {
            logger.error("Unable to open Apex Code Editor", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to open Apex Code Editor.");
        }
        firePropertyChange(PROP_TITLE);
    }

    /**
     * Creates page 0 of the multi-page editor, which contains a text editor.
     */
    private void createSourcePage() {
        if (apexCodeEditor == null) {
            apexCodeEditor = new ApexCodeEditor(project);
        }
    }

    private void createMetadataPage() {
        metadataEditor = new StructuredTextEditor();
        metadataEditor.setEditorPart(this);
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart
     */
    public void gotoMarker(IMarker marker) {
        setActivePage(0);
        // IDE.gotoMarker(getEditor(0), marker);
        ((IGotoMarker) apexCodeEditor.getAdapter(IGotoMarker.class)).gotoMarker(marker);
    }

    void doFirePropertyChange(int property) {
        super.firePropertyChange(property);
    }

    /**
     * Closes all project files on project close.
     */
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                    for (IWorkbenchPage element : pages) {
                        if (((FileEditorInput) apexCodeEditor.getEditorInput()).getFile().getProject()
                                .equals(event.getResource())) {
                            IEditorPart editorPart = element.findEditor(apexCodeEditor.getEditorInput());
                            element.closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected IEditorSite createSite(IEditorPart editor) {
        // Due to an eclipse bug/misfeature, the Id of a multipart editor is always "", so you can't contribute
        // to the editor without doing this in code.  This workaround allows the debugger plugin to add a RulerDoubleClick
        // event to make breakpoints work again.
        return new MultiPageEditorSite(this, editor) {
            @Override
            public String getId() {
                return EditorConstants.APEX_EDITOR_ID;
            }
        };
    }
}
