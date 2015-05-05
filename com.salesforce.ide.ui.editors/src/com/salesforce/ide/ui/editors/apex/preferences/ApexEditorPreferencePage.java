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
package com.salesforce.ide.ui.editors.apex.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class ApexEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private final class BooleanFieldEditorExtension extends BooleanFieldEditor {
        private BooleanFieldEditorExtension(String name, String label, Composite parent) {
            super(name, label, parent);
        }

        @Override
        public Button getChangeControl(Composite parent) {
            // I want to be able to set the value of the checkbox programmatically
            return super.getChangeControl(parent);
        }
    }

    BooleanFieldEditor enableNewParser;
    // This has a dependency with the new parser so toggling that one should also toggle this one
    BooleanFieldEditorExtension enableAutoCompletion;
    Group compilerGroup;

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

        compilerGroup = new Group(entryTable, SWT.NONE);
        compilerGroup.setText(EditorMessages.getString("ApexEditorPreferencePage.ApexParserGroup")); //$NON-NLS-1$
        compilerGroup.setLayout(new GridLayout());
        compilerGroup.setLayoutData(new GridData(768));

        enableNewParser = instantiateApexCompilerBooleanField(compilerGroup);
        enableNewParser.setPage(this);
        enableNewParser.setPreferenceStore(getPreferenceStore());
        enableNewParser.load();

        enableAutoCompletion = instantiateAutoCompletionBooleanField(compilerGroup);
        enableAutoCompletion.setPage(this);
        enableAutoCompletion.setPreferenceStore(getPreferenceStore());
        enableAutoCompletion.load();

        enableNewParser.setPropertyChangeListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                Boolean newValue = (Boolean) event.getNewValue();

                if (newValue) {
                    enableAutoCompletion.setEnabled(true, compilerGroup);
                } else {
                    enableAutoCompletion.setEnabled(false, compilerGroup);
                    Button autoCompletionButton = enableAutoCompletion.getChangeControl(compilerGroup);
                    autoCompletionButton.setSelection(false);
                }

            }
        });

        return entryTable;
    }

    protected BooleanFieldEditor instantiateApexCompilerBooleanField(Composite parent) {
        return new BooleanFieldEditor(PreferenceConstants.EDITOR_PARSE_WITH_NEW_COMPILER,
                EditorMessages.getString("ApexEditorPreferencePage.EnableNewParserOption"), parent); //$NON-NLS-1$
    }

    protected BooleanFieldEditorExtension instantiateAutoCompletionBooleanField(Composite parent) {
        return new BooleanFieldEditorExtension(PreferenceConstants.EDITOR_AUTOCOMPLETION, "Enable auto-completion",
                parent);
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
        enableAutoCompletion.loadDefault();
    }

    @Override
    public boolean performOk() {
        enableNewParser.store();
        enableAutoCompletion.store();
        return super.performOk();
    }
}
