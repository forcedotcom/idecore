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
package com.salesforce.ide.ui.editors.properysheets.widgets;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A generic widget that shows a combo list of items to choose from.
 * 
 * @author nchen
 * 
 */
public class ComboWidget<T extends Enum<T>> {
    private T value;
    private String label;
    private ComboViewer comboViewer;
    private FormToolkit toolkit;

    public ComboWidget(FormToolkit toolkit, String label, T initialValue) {
        this.toolkit = toolkit;
        this.label = label;
        this.value = initialValue;
    }

    /*
     * Assumes that the parent composite is a two-column GridLayout
     */
    public void addTo(Composite composite) {
        toolkit.createLabel(composite, label);
        comboViewer = new ComboViewer(composite, SWT.DROP_DOWN);
        comboViewer.setContentProvider(new ComboBoxContentProvider());
        comboViewer.setLabelProvider(new ComboBoxLabelProvider());

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        comboViewer.getControl().setLayoutData(gridData);
    }

    public void setValue(T value) {
        this.value = value;
        comboViewer.setInput(this.value);
        comboViewer.setSelection(new StructuredSelection(value));
        comboViewer.refresh();
    }

    @SuppressWarnings("unchecked")
    // This cast is safe because we are putting in/taking out enums
    public T getValue() {
        StructuredSelection selection = (StructuredSelection) comboViewer.getSelection();
        return (T) selection.getFirstElement();
    }

    public ComboViewer getComboViewer() {
        return comboViewer;
    }

    private final class ComboBoxContentProvider implements IStructuredContentProvider {
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // not applicable
        }

        @Override
        public void dispose() {
            // not applicable
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement.getClass().isEnum()) {
                @SuppressWarnings("unchecked")
                // This cast is safe because of the isEnum check above
                Enum<T> enumValue = (Enum<T>) inputElement;
                T[] enumConstants = enumValue.getDeclaringClass().getEnumConstants();
                return enumConstants;
            }
            return new Object[0];
        }
    }

    private final class ComboBoxLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            return super.getText(element);
        }
    }

}
