/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc.. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.apex.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class ApexEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor enableNewParser;

    public ApexEditorPreferencePage() {}

    public ApexEditorPreferencePage(String title) {
        super(title);
    }

    public ApexEditorPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite entryTable = new Composite(parent, 0);
        GridData data = new GridData(768);
        data.grabExcessHorizontalSpace = true;
        entryTable.setLayoutData(data);
        GridLayout layout = new GridLayout();
        entryTable.setLayout(layout);

        Group startupGroup = new Group(entryTable, SWT.NONE);
        startupGroup.setText(EditorMessages.getString("ApexEditorPreferencePage.ApexParserGroup")); //$NON-NLS-1$
        startupGroup.setLayout(new GridLayout());
        startupGroup.setLayoutData(new GridData(768));

        enableNewParser =
                new BooleanFieldEditor(PreferenceConstants.EDITOR_PARSE_WITH_NEW_COMPILER,
                        EditorMessages.getString("ApexEditorPreferencePage.EnableNewParserOption"), startupGroup); //$NON-NLS-1$
        enableNewParser.setPage(this);
        enableNewParser.setPreferenceStore(getPreferenceStore());
        enableNewParser.load();

        return entryTable;
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(ForceIdeEditorsPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        UIUtils.setHelpContext(getControl(), this.getClass().getSimpleName());
    }

    @Override
    protected void performDefaults() {
        enableNewParser.loadDefault();
    }

    @Override
    public boolean performOk() {
        enableNewParser.store();
        return super.performOk();
    }
}
