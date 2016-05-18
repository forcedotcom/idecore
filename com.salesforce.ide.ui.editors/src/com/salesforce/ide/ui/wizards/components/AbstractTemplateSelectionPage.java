/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 
 * Original code copied from NewXMLTemplatesWizardPage from the
 * org.eclipse.wst.xml.ui bundle.
 *******************************************************************************/
package com.salesforce.ide.ui.wizards.components;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;

/**
 * Templates page in new file wizard. Allows users to select a new file
 * template to be applied in new file.
 */
public abstract class AbstractTemplateSelectionPage extends WizardPage {

    /* Used to store dialog settings */
    private static final String KEY_LAST_TEMPLATE = "lastTemplate";
    private static final String KEY_USE_TEMPLATE = "useTemplate";

    /** Content provider for templates */
    private static class TemplateContentProvider implements IStructuredContentProvider {
        private final String contextTypeId;
        private TemplateStore templateStore;

        public TemplateContentProvider(final String contextTypeId) {
            this.contextTypeId = contextTypeId;
        }

        @Override
        public void dispose() {
            templateStore = null;
        }

        @Override
        public Object[] getElements(Object input) {
            return templateStore.getTemplates(this.contextTypeId);
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            templateStore = (TemplateStore) newInput;
        }
    }
    /** Label provider for templates. */
    private static class TemplateLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            Template template = (Template) element;

            switch (columnIndex) {
                case 0 :
                    return template.getName();
                case 1 :
                    return template.getDescription();
                default :
                    return ""; //$NON-NLS-1$
            }
        }
    }

    private final String contextTypeId;
    /** Template store used by this wizard page */
    private final TemplateStore templateStore;

    /** The viewer displays the pattern of selected template. */
    private SourceViewer patternViewer;
    /** The table presenting the templates. */
    private TableViewer tableViewer;
    /** Checkbox for using templates. */
    private Button useTemplateButton;

    /**
     * Creates a new wizard page with the given name, title, and image.
     *
     * @param contextTypeId where the templates are registered in the template store
     * @param templateStore the source of the templates
     * @param pageName the name of the page
     * @param description the description text for this dialog page, or <code>null</code> if none
     */
    protected AbstractTemplateSelectionPage(
        final String contextTypeId,
        final TemplateStore templateStore,
        final String pageName,
        final String description
    ) {
        super(pageName, NewWizardMessages.WizardPage_0, null);
        setDescription(description);
        this.contextTypeId = contextTypeId;
        this.templateStore = templateStore;
    }

    protected abstract String getHelpContextId();

    protected abstract String getLinkText();

    protected abstract String getPreferencePageId();

    /**
     * Correctly resizes the table so no phantom columns appear
     */
    private static void configureTableResizing(final Composite parent, final Table table, final TableColumn column1, final TableColumn column2) {
        parent.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle area = parent.getClientArea();
                Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int width = area.width - 2 * table.getBorderWidth();
                if (preferredSize.y > area.height) {
                    // Subtract the scrollbar width from the total column
                    // width
                    // if a vertical scrollbar will be required
                    Point vBarSize = table.getVerticalBar().getSize();
                    width -= vBarSize.x;
                }

                Point oldSize = table.getSize();
                if (oldSize.x > width) {
                    // table is getting smaller so make the columns
                    // smaller first and then resize the table to
                    // match the client area width
                    column1.setWidth(width / 2);
                    column2.setWidth(width / 2);
                    table.setSize(width, area.height);
                }
                else {
                    // table is getting bigger so make the table
                    // bigger first and then make the columns wider
                    // to match the client area width
                    table.setSize(width, area.height);
                    column1.setWidth(width / 2);
                    column2.setWidth(width / 2);
                }
            }
        });
    }

    @Override
    public final void createControl(Composite ancestor) {
        Composite parent = new Composite(ancestor, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);

        // create checkbox for user to use Template
        useTemplateButton = new Button(parent, SWT.CHECK);
        useTemplateButton.setSelection(true); // checked by default
        useTemplateButton.setText(NewWizardMessages.WizardPage_3);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        useTemplateButton.setLayoutData(data);
        useTemplateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                enableTemplates();
            }
        });

        // create composite for Templates table
        Composite innerParent = new Composite(parent, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 2;
        innerLayout.marginHeight = 0;
        innerLayout.marginWidth = 0;
        innerParent.setLayout(innerLayout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        innerParent.setLayoutData(gd);

        Label label = new Label(innerParent, SWT.NONE);
        label.setText(NewWizardMessages.WizardPage_5);
        data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        label.setLayoutData(data);

        // create table that displays templates
        Table table = new Table(innerParent, SWT.BORDER | SWT.FULL_SELECTION);

        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(2);
        data.heightHint = convertHeightInCharsToPixels(10);
        data.horizontalSpan = 2;
        table.setLayoutData(data);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText(NewWizardMessages.WizardPage_1);

        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText(NewWizardMessages.WizardPage_2);

        tableViewer = new TableViewer(table);
        tableViewer.setLabelProvider(new TemplateLabelProvider());
        tableViewer.setContentProvider(new TemplateContentProvider(this.contextTypeId));

        tableViewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object object1, Object object2) {
                if ((object1 instanceof Template) && (object2 instanceof Template)) {
                    Template left = (Template) object1;
                    Template right = (Template) object2;
                    int result = left.getName().compareToIgnoreCase(right.getName());
                    if (result != 0) {
                        return result;
                    }
                    return left.getDescription().compareToIgnoreCase(right.getDescription());
                }
                return super.compare(viewer, object1, object2);
            }

            @Override
            public boolean isSorterProperty(Object element, String property) {
                return true;
            }
        });

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent e) {
                updateViewerInput();
            }
        });

        // create viewer that displays currently selected template's contents
        patternViewer = doCreateViewer(parent);

        tableViewer.setInput(this.templateStore);

        // Create linked text to just to templates preference page
        final String linkText = getLinkText();
        if (null != linkText) {
            Link link = new Link(parent, SWT.NONE);
            link.setText(linkText);
            data = new GridData(SWT.END, SWT.FILL, true, false, 2, 1);
            link.setLayoutData(data);
            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    linkClicked();
                }
            });
        }

        configureTableResizing(innerParent, table, column1, column2);
        loadPreviousSelections();

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, getHelpContextId());
        Dialog.applyDialogFont(parent);
        setControl(parent);
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        final IDialogSettings master = super.getDialogSettings();
        return DialogSettings.getOrCreateSection(master, this.contextTypeId);
    }

    /**
     * Creates, configures and returns a source viewer to present the template
     * pattern on the preference page. Clients may override to provide a
     * custom source viewer featuring e.g. syntax coloring.
     * 
     * @param parent
     *            the parent control
     * @return a configured source viewer
     */
    protected abstract SourceViewer createViewer(Composite parent);

    private SourceViewer doCreateViewer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(NewWizardMessages.WizardPage_4);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);

        SourceViewer viewer = createViewer(parent);
        viewer.setEditable(false);

        Control control = viewer.getControl();
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.heightHint = convertHeightInCharsToPixels(5);
        // [261274] - source viewer was growing to fit the max line width of the template
        data.widthHint = convertWidthInCharsToPixels(2);
        control.setLayoutData(data);

        return viewer;
    }

    /**
     * Enable/disable controls in page based on useTemplateButton's current
     * state.
     */
    final void enableTemplates() {
        boolean enabled = useTemplateButton.getSelection();
        final Template template = getSelectedTemplate();
        setPageComplete(!enabled || null != template);
        getDialogSettings().put(KEY_USE_TEMPLATE, enabled);
        tableViewer.getControl().setEnabled(enabled);
        patternViewer.getControl().setEnabled(enabled);
    }

    /**
     * Get the currently selected template.
     */
    private Template getSelectedTemplate() {
        Template template = null;
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

        if (selection.size() == 1) {
            template = (Template) selection.getFirstElement();
        }
        return template;
    }

    /**
     * Returns template string to insert.
     * 
     * @param context the context to use when rendering the template
     * @return String to insert or null if none is to be inserted
     */
    public final String getTemplateString(final TemplateContext context) {
        String templateString = null;

        if (useTemplateButton.getSelection()) {
            Template template = getSelectedTemplate();
            if (template != null) {
                try {
                    TemplateBuffer buffer = context.evaluate(template);
                    templateString = buffer.getString();
                } catch (BadLocationException | TemplateException e) {
                    final String msg = "Unable to create template for new component";
                    final IStatus status = new Status(IStatus.WARNING, ForceIdeEditorsPlugin.PLUGIN_ID, msg, e);
                    logger().log(status);
                }
            }
        }

        return templateString;
    }

    final void linkClicked() {
        final String id = getPreferencePageId();
        PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null).open();
        tableViewer.refresh();
    }

    /**
     * Load the last template name used in New File wizard.
     */
    private void loadPreviousSelections() {
        final IDialogSettings dialogSettings = getDialogSettings();

        // enabled by default if the value does not exist
        final String useTemplate = dialogSettings.get(KEY_USE_TEMPLATE);
        useTemplateButton.setSelection(null == useTemplate || Boolean.valueOf(useTemplate).booleanValue());

        setSelectedTemplate(dialogSettings.get(KEY_LAST_TEMPLATE));

        enableTemplates();
    }

    /**
     * Select a template in the table viewer given the template name. If
     * template name cannot be found or templateName is null, just select
     * first item in table. If no items in table select nothing.
     * 
     * @param templateName
     */
    private void setSelectedTemplate(String templateName) {
        Object template = null;

        if (templateName != null && templateName.length() > 0) {
            // pick the last used template
            template = templateStore.findTemplate(templateName, this.contextTypeId);
        }

        // no record of last used template so just pick first element
        if (template == null) {
            // just pick first element
            template = tableViewer.getElementAt(0);
        }

        if (template != null) {
            tableViewer.setSelection(new StructuredSelection(template), true);
        }
    }

    /**
     * Updates the pattern viewer.
     */
    final void updateViewerInput() {
        Template template = getSelectedTemplate();
        getDialogSettings().put(KEY_LAST_TEMPLATE, null == template ? null : template.getName());
        patternViewer.getDocument().set(null != template ? template.getPattern() : ""); //$NON-NLS-1$
    }

    // TODO: Inject the editor's logger.
    private static ILog logger() {
        return ForceIdeEditorsPlugin.getDefault().getLog();
    }

}