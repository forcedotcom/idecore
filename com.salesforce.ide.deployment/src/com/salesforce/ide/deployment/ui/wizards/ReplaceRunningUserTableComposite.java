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
package com.salesforce.ide.deployment.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.ui.internal.composite.BaseComposite;

/**
 *
 * The composite for ReplaceRunningUserViewShell.
 *
 * @author fchang
 */
public class ReplaceRunningUserTableComposite extends BaseComposite {
    public static final int DASHBOARD_NAME_COLUMN = 0;
    public static final int RUNNING_USER_NAME_COLUMN = 1;
    protected Label lblSummary = null;
    protected Table tblReplaceDashboard = null;
    protected TableViewer tblViewer;
    protected Label lblErrorMessage = null;
    protected Label lblReplaceRunningUser = null;
    protected Text txtReplaceRunningUser = null;

    public ReplaceRunningUserTableComposite(Composite parent, int style) {
        super(parent, style);
        init();
    }

    private void init() {
        setLayout(new GridLayout(1, false));

        // summary lable
        lblSummary = new Label(this, SWT.NONE | SWT.WRAP);
        lblSummary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        lblSummary.setText("Following running user(s) is not available in destination org. Please replace with valid running user before deployment. (The replacement will NOT affect dashboards in original org.)");

        //dashboard replacement info table
        tblReplaceDashboard = new Table(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        tblReplaceDashboard.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 15));
        tblReplaceDashboard.setLinesVisible(true);
        tblReplaceDashboard.setHeaderVisible(true);

        tblViewer = new TableViewer(tblReplaceDashboard);
        tblViewer.setLabelProvider(new ReplaceDashboardLabelProvider());
        tblViewer.setContentProvider(new ArrayContentProvider());

        final TableColumn colDashboardName = new TableColumn(tblReplaceDashboard, SWT.LEFT);
        colDashboardName.setWidth(150);
        colDashboardName.setText("Dashboard Name");

        final TableColumn colRunningUserName = new TableColumn(tblReplaceDashboard, SWT.LEFT);
        colRunningUserName.setWidth(150);
        colRunningUserName.setText("Running User");

        // error message
        lblErrorMessage = new Label(this, SWT.NONE);
        lblErrorMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        lblErrorMessage.setText("The specified runningUser is not available in destination org.");
        lblErrorMessage.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
        lblErrorMessage.setVisible(false);

        // replace running user lable
        lblReplaceRunningUser = new Label(this, SWT.NONE);
        lblReplaceRunningUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        lblReplaceRunningUser.setText("Replace ALL running user in above dashboard(s) with: ");

        // user input text field
        txtReplaceRunningUser = new Text(this, SWT.BORDER);
        txtReplaceRunningUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        addValidateModifyListener(txtReplaceRunningUser);

    }

    @Override
    public void validateUserInput() {
    }

    public Text getTxtReplaceRunningUser() {
        return txtReplaceRunningUser;
    }

    public String getTxtReplaceRunningUserString() {
        return getText(txtReplaceRunningUser);
    }

    public void enableErrorMessage() {
        lblErrorMessage.setVisible(true);
    }

    public TableViewer getTblViewer() {
        return tblViewer;
    }

    class ReplaceDashboardLabelProvider implements ITableLabelProvider {
        List<ILabelProviderListener> listeners = new ArrayList<>();
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            DeploymentComponent deploymentComponent = (DeploymentComponent)element;
            switch(columnIndex) {
                case DASHBOARD_NAME_COLUMN:
                    return deploymentComponent.getComponent().getName() + "." + deploymentComponent.getComponent().getFileExtension();
                case RUNNING_USER_NAME_COLUMN:
                    return deploymentComponent.getRunningUser();
            }
            return "";
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            listeners.add(listener);
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            listeners.remove(listener);
        }
    }
}
