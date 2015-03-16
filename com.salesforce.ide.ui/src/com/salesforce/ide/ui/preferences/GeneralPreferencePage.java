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
package com.salesforce.ide.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class GeneralPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	public GeneralPreferencePage() {
	}

	public GeneralPreferencePage(String title) {
		super(title);
	}

	public GeneralPreferencePage(String title, ImageDescriptor image) {
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
        Composite colorComposite = new Composite(entryTable, 0);
        colorComposite.setLayout(new GridLayout());
        colorComposite.setLayoutData(new GridData(768));

        return entryTable;

	}

	@Override
    public void init(IWorkbench workbench) {
		setPreferenceStore(ForceIdeUIPlugin.getDefault().getPreferenceStore());
	}

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        UIUtils.setHelpContext(getControl(), this.getClass().getSimpleName());
    }
}
