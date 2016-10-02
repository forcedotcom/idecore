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
package com.salesforce.ide.ui.editors.internal;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.editors.properysheets.MetadataFormPage;
import com.salesforce.ide.ui.editors.properysheets.PropertySheetsFactory;

public abstract class BaseComponentMultiPageEditorPart extends BaseMultiPageEditorPart {

    private static final Logger logger = Logger.getLogger(BaseComponentMultiPageEditorPart.class);

    protected StructuredTextEditor metadataEditor = null;
    protected Component sourceComponent = null;
    protected Component metadataComponent = null;
    protected IEditorInput sourceEditorInput = null;
    protected IEditorInput metadataEditorInput = null;
    protected MetadataFormPage propertySheet = null;
    protected int sourcePageIndex = -1;
    protected int metadataPageIndex = -1;
    protected int propertyPageIndex = -1;

    // C O N S T R U C T O R S
    public BaseComponentMultiPageEditorPart() {
        super();
        setTitleImage(getImage());
    }

    // M E T H O D S
    public StructuredTextEditor getMetadataEditor() {
        return metadataEditor;
    }

    public void setMetadataEditor(StructuredTextEditor metadataEditor) {
        this.metadataEditor = metadataEditor;
    }

    public Component getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(Component sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    public Component getMetadataComponent() {
        return metadataComponent;
    }

    public void setMetadataComponent(Component metadataComponent) {
        this.metadataComponent = metadataComponent;
    }

    public IEditorInput getSourceEditorInput() {
        return sourceEditorInput;
    }

    public void setSourceEditorInput(IEditorInput sourceEditorInput) {
        this.sourceEditorInput = sourceEditorInput;
    }

    public IEditorInput getMetadataEditorInput() {
        return metadataEditorInput;
    }

    public void setMetadataEditorInput(IEditorInput metadataEditorInput) {
        this.metadataEditorInput = metadataEditorInput;
    }

    public int getSourcePageIndex() {
        return sourcePageIndex;
    }

    public int getMetadataPageIndex() {
        return metadataPageIndex;
    }

    public int getPropertyPageIndex() {
        return propertyPageIndex;
    }


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        site.getWorkbenchWindow().getPartService().addPartListener(partListener);
    }

    @Override
    public void setInput(IEditorInput input) {
        super.setInput(input);

        IFile file = getEditorInputFile();
        try {

            if (!getProjectService().getComponentFactory().isComponentMetadata(file)) {
                sourceEditorInput = input;
                sourceComponent = getProjectService().getComponentFactory().getComponentFromFile(file);
                metadataEditorInput = getCompositeEditorInput(file);
                if (logger.isDebugEnabled()) {
                    logger.debug("Set metadata editor input from source file '"
                            + (metadataEditorInput != null ? metadataEditorInput.getName() : "n/a") + "'");
                }
            } else {
                metadataComponent = getProjectService().getComponentFactory().getComponentFromFile(file);
                sourceEditorInput = getCompositeEditorInput(file);
                super.setInput(sourceEditorInput);
                metadataEditorInput = input;
                if (logger.isDebugEnabled()) {
                    logger.debug("Set source editor input from metadata file '" + file.getName() + "'");
                }
            }

            if (sourceComponent == null) {
                logger.warn("Unable to open " + getEditorName() + " for file '" + file.getName() + "'");
                Utils.openError("Unable to load " + getEditorName() + ".", "Unable to open " + getEditorName()
                        + " for file '" + file.getName() + "'");
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Opening " + sourceComponent.getFullDisplayName() + " from file '" + file.getName() + "'");
            }

        } catch (Exception e) {
            logger.warn("Unable to open " + getEditorName() + " for file '" + file.getName() + "'", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to load " + getEditorName() + ".");
        }

        setPartName(input.getName());
    }

    protected IEditorInput getCompositeEditorInput(IFile file) {
        IEditorInput editorInput = null;
        try {
            Component component = getComponentFactory().getCompositeComponentFromFile(file);
            IFile compositeComponentFile = component.getFileResource();

            if (compositeComponentFile != null && compositeComponentFile.exists()) {
                editorInput = new FileEditorInput(compositeComponentFile);
            }

        } catch (Exception e) {
            logger.error("Unable load metadata file for file '" + file.getProjectRelativePath().toPortableString()
                    + "'", e);
            Utils.openError("Metadata Not Found", "Unable to open or associated metadata file for file '"
                    + file.getProjectRelativePath().toPortableString() + "':\n\n" + e.getMessage());

        }
        return editorInput;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        WorkspaceModifyOperation saveOp = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                    InterruptedException {
                if (getSourcePageIndex() > -1) {
                    getEditor(getSourcePageIndex()).doSave(monitor);
                }

                if (getPropertyPageIndex() > -1) {
                    getEditor(getPropertyPageIndex()).doSave(monitor);
                }

                if (getMetadataPageIndex() > -1) {
                    getEditor(getMetadataPageIndex()).doSave(monitor);
                }
            }
        };

        try {
            saveOp.run(monitor);
        } catch (Exception e) {
            logger.error("Unable to open Apex Code Editor", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to open Apex Code Editor.");
        }
    }

    @Override
    public IEditorPart getActiveEditor() {
        return super.getActiveEditor();
    }

    protected void createPropertySheetPage() {
        propertySheet = PropertySheetsFactory.createPropertySheet(getSourceComponent(), this);
    }

    @Override
    protected void pageChange(int newPageIndex) {
        if (newPageIndex == propertyPageIndex) {
            if (propertySheet != null)
                propertySheet.syncFromMetadata();
        } else if (newPageIndex == metadataPageIndex) {
            if (propertySheet != null)
                propertySheet.syncToMetadata();
        }
        super.pageChange(newPageIndex);
        getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);
    }

    @Override
    public void dispose() {
        this.getEditorSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
        super.dispose();
    }

    IPartListener2 partListener = new IPartListener2() {
        @Override
        public void partActivated(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part == BaseComponentMultiPageEditorPart.this) {
                EditorUtils.removeCapabilites();
            }
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part == BaseComponentMultiPageEditorPart.this) {
                EditorUtils.removeCapabilites();
            }
        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part == BaseComponentMultiPageEditorPart.this) {
                EditorUtils.addCapabilites();
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part == BaseComponentMultiPageEditorPart.this) {
                EditorUtils.addCapabilites();
            }
        }

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part == BaseComponentMultiPageEditorPart.this) {
                EditorUtils.addCapabilites();
            }
        }

        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {

        }

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part == BaseComponentMultiPageEditorPart.this) {
                EditorUtils.removeCapabilites();
            }
        }

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {

        }
    };
}
