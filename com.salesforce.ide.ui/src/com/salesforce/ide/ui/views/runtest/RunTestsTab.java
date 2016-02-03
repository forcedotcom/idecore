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

package com.salesforce.ide.ui.views.runtest;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.google.common.annotations.VisibleForTesting;
import com.salesforce.ide.apex.internal.core.ApexSourceUtils;
import com.salesforce.ide.core.internal.utils.Utils;

public abstract class RunTestsTab extends AbstractLaunchConfigurationTab {
	
	@VisibleForTesting
	public Color colorBlack;
	@VisibleForTesting
	public Color colorGray;
	
	protected final ApexSourceUtils sourceLookup = ApexSourceUtils.INSTANCE;
	
	public abstract void saveSiblingTab(RunTestsTab tab);
	public abstract boolean validatePage();
	
	@VisibleForTesting
	public Composite createComposite(Composite parent, int style) {
    	return new Composite(parent, style);
    }
	
	@VisibleForTesting
	public Group createGroup(Composite parent, int style) {
    	return new Group(parent, style);
    }
	
	/**
     * Enable or disable a button based the text.
     * @return True if text is not null/empty and does not equal
     *   to default strings (all classes) (all methods). False otherwise.
     */
	@VisibleForTesting
	public boolean shouldEnableBasedOnText(String text) {
    	return StringUtils.isNotBlank(text) && !text.equals(Messages.Tab_AllClasses)
    			&& !text.equals(Messages.Tab_AllMethods);
    }
    
    /**
     * Create a button with specified text and enabled value
     */
	@VisibleForTesting
	public Button makeDefaultButton(Group parent, String defaultText, boolean enabled) {
    	Button button = new Button(parent, SWT.PUSH);
    	button.setText(defaultText);
    	button.setLayoutData(new GridData());
    	button.setEnabled(enabled);
    	return button;
    }
    
    /**
     * Create a label with specified text.
     */
	@VisibleForTesting
	public Label makeDefaultLabel(Group parent, String defaultText) {
    	Label label = new Label(parent, SWT.SINGLE);
    	label.setText(defaultText);
        return label;
    }
	
	/**
     * Create a checkbox with specified text and enabled value
     */
	@VisibleForTesting
	public Button makeDefaultCheckbox(Composite parent, String defaultText, boolean enabled, boolean selected) {
    	Button button = new Button(parent, SWT.CHECK);
    	button.setText(defaultText);
    	button.setEnabled(enabled);
    	button.setSelection(selected);
        return button;
    }
    
	/**
	 * Create a multi check table
	 */
	@VisibleForTesting
	public Table makeDefaultMultiCheckTable(Composite parent, String... columnNames) {
    	Table table = new Table(parent, SWT.MULTI | SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    	table.setHeaderVisible(true);
    	table.setLinesVisible(true);
    	GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
    	gridData.heightHint = 10 * table.getItemHeight();
    	gridData.widthHint = 3 * gridData.heightHint; 
    	table.setLayoutData(gridData);
    	
    	for (String columnName : columnNames) {
    		TableColumn tc = new TableColumn(table, SWT.NONE);
    		tc.setText(columnName);
    	}
    	
    	return table;
    }
    
    /**
     * Create a text field with specified text and foreground color
     */
    @VisibleForTesting
	public Text makeDefaultText(Group parent, String defaultText, Color defaultColor) {
    	Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
    	return setTextProperties(text, defaultText, defaultColor);
    }
    
    /**
     * Set defaults for a Text widget. The text field is not editable.
     */
    @VisibleForTesting
	public Text setTextProperties(Text text, String defaultText, Color defaultColor) {
    	if (Utils.isEmpty(text)) return null;
    	
    	text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	// Do not let the user edit this value except through the browse button to minimize errors
    	text.setEditable(false);
    	text.setText(defaultText);
    	Color textColor = shouldEnableBasedOnText(defaultText) ? defaultColor : colorGray;
    	text.setForeground(textColor);
    	return text;
    }
}
