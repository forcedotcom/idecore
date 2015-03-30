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

import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.api.metadata.types.PackageVersion;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;

/**
 * An editable table-based widget for Package Versions ({@link PackageVersion}. You cannot add/remove rows of the table.
 * You can only modify the version column of the table. This is just how Package Versions work currently. They are
 * added/removed automatically when you reference/dereference something in a managed package.
 * 
 * @author nchen
 * 
 */
public class PackageVersionWidget {
    private static final int TABLE_MIN_HEIGHT = 100;
    private static final int NAMESPACE_COL_WIDTH = 300;
    private static final int VERSION_COL_WIDTH = 100;

    private List<PackageVersion> packageVersions;
    private TableViewer tableViewer;
    private TableViewerColumn namespaceColumn;
    private TableViewerColumn versionColumn;
    private VersionColumnEditingSupport versionColumnEditingSupport;

    /*
     * Assumes that the parent composite is a two-column GridLayout
     */
    public void addTo(Composite composite) {
        tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        tableViewer.getTable().setHeaderVisible(true);

        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.minimumHeight = TABLE_MIN_HEIGHT;
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        tableViewer.getControl().setLayoutData(gridData);

        namespaceColumn = new TableViewerColumn(tableViewer, SWT.BORDER);
        namespaceColumn.getColumn().setWidth(NAMESPACE_COL_WIDTH);
        namespaceColumn.getColumn().setText(EditorMessages.getString("PackageVersionWidget.NamespaceColumn")); //$NON-NLS-1$
        namespaceColumn.setLabelProvider(new NamespaceLabelProvider());

        versionColumn = new TableViewerColumn(tableViewer, SWT.BORDER);
        versionColumn.getColumn().setWidth(VERSION_COL_WIDTH);
        versionColumn.getColumn().setText(EditorMessages.getString("PackageVersionWidget.VersionColumn")); //$NON-NLS-1$
        versionColumn.setLabelProvider(new VersionLabelProvider());
        versionColumnEditingSupport = new VersionColumnEditingSupport(tableViewer);
        versionColumn.setEditingSupport(versionColumnEditingSupport);
    }

    public List<PackageVersion> getPackageVersions() {
        return packageVersions;
    }

    public void addListener(ICellEditorListener listener) {
        versionColumnEditingSupport.cellEditor.addListener(listener);
    }

    public void removeListener(ICellEditorListener listener) {
        versionColumnEditingSupport.cellEditor.removeListener(listener);
    }

    public void setPackageVersions(List<PackageVersion> packageVersions) {
        this.packageVersions = packageVersions;
        tableViewer.setInput(this.packageVersions);
        tableViewer.refresh();
    }

    // This is non-final and non-private so we can unit test it.
    static class VersionColumnEditingSupport extends EditingSupport {
        private TextCellEditor cellEditor;
        private TableViewer viewer;
        static Pattern packageFormatWithDecimal = Pattern.compile("(\\d+)(.\\d+)?"); //$NON-NLS-1$

        private VersionColumnEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
            cellEditor = new TextCellEditor(viewer.getTable());
        }

        @Override
        protected void setValue(Object element, Object value) {
            PackageVersion pv = (PackageVersion) element;
            updateValueIfPossible(pv, value);
            viewer.update(element, null);
        }

        protected PackageVersion updateValueIfPossible(PackageVersion pv, Object value) {
            String number = (String) value;
            Matcher matcher = packageFormatWithDecimal.matcher(number);

            if (matcher.matches()) {
                // Reset the values first (especially in case there is no decimal part, we want to default to 0)
                pv.setMajorNumber(0);
                pv.setMinorNumber(0);

                try (final Scanner scanner = new Scanner(number)) {
                    // Whole part - must exist for it to pass the matcher
                    pv.setMajorNumber(scanner.useDelimiter("\\.").nextInt()); //$NON-NLS-1$

                    if (scanner.hasNextInt()) { // Decimal part, user might just not put it, then default to 0
                        pv.setMinorNumber(scanner.nextInt());
                    }
                }
            }

            return pv;
        }

        @Override
        protected Object getValue(Object element) {
            PackageVersion pv = (PackageVersion) element;
            return String.format("%d.%d", pv.getMajorNumber(), pv.getMinorNumber()); //$NON-NLS-1$
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return cellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    private static final class NamespaceLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object element) {
            PackageVersion pv = (PackageVersion) element;
            return pv.getNamespace();
        }
    }

    private static final class VersionLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object element) {
            PackageVersion pv = (PackageVersion) element;
            return String.format("%d.%d", pv.getMajorNumber(), pv.getMinorNumber()); //$NON-NLS-1$
        }
    }

}
