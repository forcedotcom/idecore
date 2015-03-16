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
package com.salesforce.ide.ui.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

import com.salesforce.ide.core.internal.utils.LoggingLevel;
import com.salesforce.ide.ui.internal.composite.BaseComposite;

public class RunTestLoggingPropertyComposite extends BaseComposite {

	protected Scale scale = null;
	protected CLabel lblLevel = null;
	protected ApexCodePropertyPage page;

	public RunTestLoggingPropertyComposite(Composite parent, int style, ApexCodePropertyPage page) {
		super(parent, style);
		this.page = page;
		initialize();
	}

	public int getLoggingLevel() {
		return scale.getSelection();
	}

	public void setLoggingLevel(LoggingLevel level) {
		scale.setSelection(level.getLevel());
		lblLevel.setText(level.getLevelText());
	}

	private void initialize() {
		setLayout(new GridLayout(1, true));
		createRunTestGroup();
		setSize(new Point(467, 200));
	}

	private void createRunTestGroup() {
		Group grpRunTests = new Group(this, SWT.NONE);
		grpRunTests.setText("Run Tests Logging Setting");
		grpRunTests.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		grpRunTests.setLayout(new GridLayout(4, true));
		Label label = new Label(grpRunTests, SWT.NONE);
		label.setText("Logging Level");
		scale = new Scale(grpRunTests, SWT.NONE);
		scale.setMaximum(3);
		scale.setPageIncrement(1);
		scale.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int level = scale.getSelection();
                lblLevel.setText(LoggingLevel.getLevelText(level));
            }
        });
		lblLevel = new CLabel(grpRunTests, SWT.NONE);
		lblLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 4, 1));
	}
	
	@Override
	public void validateUserInput() {
		
	}

}
