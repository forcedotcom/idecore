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
package com.salesforce.ide.ui.editors.intro.preferences;

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

import com.salesforce.ide.ui.editors.intro.IntroPlugin;
import com.salesforce.ide.ui.editors.intro.Messages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class IntroPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor showStartPageOnStart;
    private BooleanFieldEditor showStartPageOnPerspectiveOpen;

    //    private BooleanFieldEditor showStartPageOnUpdate;

    public IntroPreferencePage() {
    }

    public IntroPreferencePage(String title) {
        super(title);
    }

    public IntroPreferencePage(String title, ImageDescriptor image) {
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
        startupGroup.setText(Messages.IntroPreference_ShowGroup_message);
        startupGroup.setLayout(new GridLayout());
        startupGroup.setLayoutData(new GridData(768));

        showStartPageOnStart =
                new BooleanFieldEditor(PreferenceConstants.SHOW_START_PAGE_ON_STARTUP,
                        Messages.IntroPreference_ShowOnStartup_message, startupGroup);
        showStartPageOnStart.setPage(this);
        showStartPageOnStart.setPreferenceStore(getPreferenceStore());
        showStartPageOnStart.load();

        showStartPageOnPerspectiveOpen =
                new BooleanFieldEditor(PreferenceConstants.SHOW_START_PAGE_ON_PERSPECTIVE_OPEN,
                        Messages.IntroPreference_ShowOnPerspectiveOpen_message, startupGroup);
        showStartPageOnPerspectiveOpen.setPage(this);
        showStartPageOnPerspectiveOpen.setPreferenceStore(getPreferenceStore());
        showStartPageOnPerspectiveOpen.load();

        //        showStartPageOnUpdate = new BooleanFieldEditor(PreferenceConstants.SHOW_START_PAGE_ON_UPDATE, Messages.IntroPreference_ShowOnUpdate_message, startupGroup);
        //        showStartPageOnUpdate.setPage(this);
        //        showStartPageOnUpdate.setPreferenceStore(getPreferenceStore());
        //        showStartPageOnUpdate.load();

        return entryTable;

    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(IntroPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        UIUtils.setHelpContext(getControl(), this.getClass().getSimpleName());
    }

    @Override
    protected void performDefaults() {
        showStartPageOnStart.loadDefault();
        showStartPageOnPerspectiveOpen.loadDefault();
        //		showStartPageOnUpdate.loadDefault();
    }

    @Override
    public boolean performOk() {
        showStartPageOnStart.store();
        showStartPageOnPerspectiveOpen.store();
        //		showStartPageOnUpdate.store();
        return super.performOk();
    }
}
