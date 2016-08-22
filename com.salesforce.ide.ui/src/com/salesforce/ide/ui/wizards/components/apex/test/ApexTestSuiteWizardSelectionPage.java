/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.wizards.components.apex.test;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * A page in the ApexTestSuite wizard that allows selecting tests
 * to add to the suite component.
 * 
 * @author jwidjaja
 */
public class ApexTestSuiteWizardSelectionPage extends WizardPage {
		
	private boolean hasFetchedTestClasses = false;
	
	private Composite container;
	private Label noTests;
	private Table table;
	private Composite buttonsArea;
	private Button selectAll, deselectAll;

	protected ApexTestSuiteWizardSelectionPage() {
		super(Messages.ApexTestSuiteSelectionPage_Title);
		setTitle(Messages.ApexTestSuiteSelectionPage_Title);
		setDescription(Messages.ApexTestSuiteSelectionPage_Description);
	}

	@Override
	public void createControl(Composite ancestor) {
		final Composite parent = new Composite(ancestor, SWT.NONE);
		parent.setLayout(new GridLayout());
		
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		container.setLayout(new GridLayout(1, true));
		
		noTests = new Label(container, SWT.SINGLE);
		noTests.setText(Messages.ApexTestSuiteSelectionPage_NoTests);
		noTests.setVisible(false);
        
        table = new Table(container, SWT.MULTI | SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        gridData.heightHint = 5 * table.getItemHeight();
        gridData.widthHint = 3 * gridData.heightHint; 
    	table.setLayoutData(gridData);
    	TableColumn column = new TableColumn(table, SWT.NONE);
    	column.setText(Messages.ApexTestSuiteSelectionPage_TableColumnTitle);
    	
    	buttonsArea = new Composite(parent, SWT.NONE);
    	buttonsArea.setLayout(new GridLayout(2, false));
    	
    	selectAll = makeButton(buttonsArea, Messages.ApexTestSuiteSelectionPage_SelectAll);
    	selectAll.addSelectionListener(new SelectionAdapter() {
    		@Override
            public void widgetSelected(SelectionEvent e) {
    			setTableItemsCheckStatus(true);
    		}
    	});
    	
    	deselectAll = makeButton(buttonsArea, Messages.ApexTestSuiteSelectionPage_DeselectAll);
    	deselectAll.addSelectionListener(new SelectionAdapter() {
    		@Override
            public void widgetSelected(SelectionEvent e) {
    			setTableItemsCheckStatus(false);
    		}
    	});
		
		setControl(parent);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (!hasFetchedTestClasses && visible) {
			// If this is the first time this page is visible,
			// fetch test classes and populate the tables.
			ApexTestSuiteWizard wizard = (ApexTestSuiteWizard) getWizard();
			populateTestClassTables(wizard.getTestClasses());
			
			hasFetchedTestClasses = true;
		}
		
		super.setVisible(visible);
	}
	
	/**
	 * Get names of selected tests.
	 */
	public List<String> getSelectedTests() {
		List<String> tests = Lists.newArrayList();
		
		for (TableItem ti : table.getItems()) {
			if (Utils.isNotEmpty(ti) && ti.getChecked()) {
				tests.add(ti.getText());
			}
		}
		
		return tests;
	}
	
	private Button makeButton(Composite parent, String defaultText) {
    	Button button = new Button(parent, SWT.PUSH);
    	button.setText(defaultText);
    	button.setLayoutData(new GridData());
    	button.setEnabled(true);
    	return button;
    }
	
	/**
	 * Fill the tables with test classes found in the org.
	 */
	private void populateTestClassTables(List<String> testClasses) {
		if (testClasses == null || testClasses.isEmpty()) {
			table.dispose();
			selectAll.dispose();
			deselectAll.dispose();
			buttonsArea.dispose();
			noTests.setVisible(true);
			return;
		}
		
		for (String tc : testClasses) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(tc);
        	item.setGrayed(false);
        	item.setChecked(false);
		}
		
		for (TableColumn col : table.getColumns()) {
			col.pack();
		}
	}
	
	/**
	 * Check or uncheck all rows in the table.
	 */
	private void setTableItemsCheckStatus(boolean checked) {
		for (TableItem ti : table.getItems()) {
			ti.setChecked(checked);
		}
	}
}
