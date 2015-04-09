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

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSet;
import com.salesforce.ide.deployment.internal.DeploymentComponentSorter;
import com.salesforce.ide.deployment.internal.DeploymentPayload;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;

public class DeploymentPlanComposite extends BaseDeploymentComposite {
    private static final Logger logger = Logger.getLogger(DeploymentPlanComposite.class);

    public final static int ACTION_COLUMN = 0;
    public final static int NAME_COLUMN = 1;
    public final static int PACKAGE_COLUMN = 2;
    public final static int TYPE_COLUMN = 3;

    protected Label lblSummary;
    private Table tblDeployPlan;
    private Button btnSelectAll;
    private Button btnDeselectAll;
    private Button btnRefresh;
    private Label lblActionTooltip;
    protected Button btnTestRun;
    protected CheckboxTableViewer tblViewer;
    private Label lblTestDeployment;
    private Label lblByClickingNext;

    private final DeploymentPlanPage deploymentPlanPage;

    public DeploymentPlanComposite(Composite parent, int style, DeploymentPlanPage deploymentPlanPage) {
        super(parent, style);
        this.deploymentPlanPage = deploymentPlanPage;
        initialize();
    }

    class ColumnData {

        ColumnData(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        Integer sortOrder;
        boolean ascending = true;

        public void toggleOrderSwith() {
            ascending = !ascending;
        }
    }

    private void initialize() {
        setLayout(new GridLayout(5, false));

        lblSummary = new Label(this, SWT.NONE);
        lblSummary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));
        // set count value after table has been loaded
        lblSummary.setText("Found (0) deployment candidates");

        tblDeployPlan = new Table(this, SWT.BORDER | SWT.CHECK | SWT.MULTI | SWT.FULL_SELECTION);
        tblDeployPlan.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 15));
        tblDeployPlan.setLinesVisible(true);
        tblDeployPlan.setHeaderVisible(true);

        tblViewer = new CheckboxTableViewer(tblDeployPlan);
        tblViewer.setContentProvider(new DeploymentPlanContentProvider());
        tblViewer.setLabelProvider(new DeploymentPlanLabelProvider());
        tblViewer.addCheckStateListener(new DeploymentPlanCheckStateListener(deploymentPlanPage));

        Listener sortListener = new DeploymentPlanColumnSortListener(this);

        final TableColumn colActionSelect = new TableColumn(tblDeployPlan, SWT.NONE);
        colActionSelect.setWidth(100);
        colActionSelect.setText("Apply Action?");
        colActionSelect.setAlignment(SWT.CENTER);
        colActionSelect.setData(new ColumnData(DeploymentComponentSorter.SORT_ACTION));
        colActionSelect.addListener(SWT.Selection, sortListener);

        final TableColumn colComponentName = new TableColumn(tblDeployPlan, SWT.NONE);
        colComponentName.setWidth(150);
        colComponentName.setText("Name");
        colComponentName.setData(new ColumnData(DeploymentComponentSorter.SORT_NAME));
        colComponentName.addListener(SWT.Selection, sortListener);

        final TableColumn colPackageName = new TableColumn(tblDeployPlan, SWT.NONE);
        colPackageName.setWidth(100);
        colPackageName.setText("Package");
        colPackageName.setData(new ColumnData(DeploymentComponentSorter.SORT_PACKAGE_NAME));
        colPackageName.addListener(SWT.Selection, sortListener);

        final TableColumn colComponentType = new TableColumn(tblDeployPlan, SWT.NONE);
        colComponentType.setWidth(75);
        colComponentType.setText("Type");
        colComponentType.setData(new ColumnData(DeploymentComponentSorter.SORT_TYPE));
        colComponentType.addListener(SWT.Selection, sortListener);

        btnSelectAll = new Button(this, SWT.NONE);
        btnSelectAll.setText("Select All");
        btnSelectAll.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
        btnSelectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tblViewer.setAllChecked(true);
                btnTestRun.setEnabled(true);
                deploymentPlanPage.setDeploySelectionForAll(true);
                deploymentPlanPage.setEnableNext(true);
            }
        });

        btnDeselectAll = new Button(this, SWT.NONE);
        btnDeselectAll.setText("Deselect All");
        btnDeselectAll.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
        btnDeselectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tblViewer.setAllChecked(false);
                btnTestRun.setEnabled(false);
                deploymentPlanPage.setDeploySelectionForAll(false);
                deploymentPlanPage.setEnableNext(false);
            }
        });

        btnRefresh = new Button(this, SWT.NONE);
        btnRefresh.setText("Refresh Plan");
        btnRefresh.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
        btnRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deploymentPlanPage.reloadDeploymentView();
            }
        });

        lblActionTooltip = new Label(this, SWT.WRAP);
        lblActionTooltip.setText("Click on row for description");
        lblActionTooltip.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, 1, 1));

        Label filler1 = new Label(this, SWT.NONE);
        filler1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));

        Label separator = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));

        lblTestDeployment = new Label(this, SWT.WRAP);
        lblTestDeployment.setText(DeploymentMessages
                .getString("DeploymentWizard..DeploymentPlanComposite.Validate.message"));
        lblTestDeployment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));

        btnTestRun = new Button(this, SWT.NONE);
        btnTestRun.setText("Validate Deployment");
        btnTestRun.setLayoutData(new GridData(SWT.BEGINNING, 0, true, false, 5, 0));
        btnTestRun.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deploymentPlanPage.testDeployment();
            }
        });

        Label filler16 = new Label(this, SWT.NONE);
        filler16.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));
        Label filler161 = new Label(this, SWT.NONE);
        filler161.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));

        lblByClickingNext = new Label(this, SWT.NONE);
        lblByClickingNext.setText(DeploymentMessages.getString("DeploymentWizard.ReviewComposite.Next.label"));
        lblByClickingNext.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, 5, 0));
    }

    public Table getTblDeployPlan() {
        return tblDeployPlan;
    }

    public DeploymentPlanPage getDeploymentPlanPage() {
        return deploymentPlanPage;
    }
 
    public void setButtonEnablement(boolean enable) {
        btnDeselectAll.setEnabled(enable);
        btnSelectAll.setEnabled(enable);
        btnRefresh.setEnabled(enable);
        btnTestRun.setEnabled(enable);
    }

    public Button getBtnDeselectAll() {
        return btnDeselectAll;
    }

    public void setBtnDeselectAll(Button btnDeselectAll) {
        this.btnDeselectAll = btnDeselectAll;
    }

    public Button getBtnSelectAll() {
        return btnSelectAll;
    }

    public void setBtnSelectAll(Button btnSelectAll) {
        this.btnSelectAll = btnSelectAll;
    }

    public Button getBtnRefresh() {
        return btnRefresh;
    }

    public Button getBtnTestRun() {
        return btnTestRun;
    }

    public void setBtnTestRun(Button btnTestRun) {
        this.btnTestRun = btnTestRun;
    }

    public Label getLblActionTooltip() {
        return lblActionTooltip;
    }

    public void setLblActionTooltip(Label lblActionTooltip) {
        this.lblActionTooltip = lblActionTooltip;
    }

    public void resetLblActionTooltip(String txt) {
        setLblActionTooltipText(txt);
        lblActionTooltip.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, 1, 2));
        layout();
    }

    public void setLblActionTooltipText(String txt) {
        this.lblActionTooltip.setText(txt);
    }

    public Table getTable() {
        return tblDeployPlan;
    }

    public void setTable(Table table) {
        this.tblDeployPlan = table;
    }

    public void updateSummaryLabel(int candidateCnt, String orgUserName) {
        if (lblSummary != null) {
            lblSummary.setText("Found " + candidateCnt + " deployment candidates");
            FontRegistry registry = new FontRegistry();
            Font boldFont = registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
            lblSummary.setFont(boldFont);
            lblSummary.update();
        }
    }

    public void clearSummaryLabel() {
        if (lblSummary != null) {
            lblSummary.setText("");
            lblSummary.update();
        }
    }

    class DeploymentPlanContentProvider implements IStructuredContentProvider {

        private Integer sortOrder = DeploymentComponentSorter.SORT_ACTION;

        public DeploymentPlanContentProvider() {}

        public DeploymentPlanContentProvider(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            DeploymentPayload tmpDeploymentPayload = (DeploymentPayload) inputElement;

            DeploymentComponentSet deploymentComponentSet = tmpDeploymentPayload.getDeploymentComponents();
            if (Utils.isEmpty(deploymentComponentSet)) {
                logger.warn("Unable to provide table content -deployment candidates are null or empty");
                return null;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Preparing deployment payload for table");
            }
            deploymentComponentSet = deploymentComponentSet.getDeploymentComponentSetWithoutMetadata();
            updateSummaryLabel(deploymentComponentSet.size(), tmpDeploymentPayload.getDestinationOrgUsername());

            if (Utils.isEmpty(deploymentComponentSet)) {
                return null;
            }

            deploymentComponentSet = deploymentComponentSet.sort(sortOrder);
            return deploymentComponentSet.toArray();
        }

        @Override
        public void dispose() {
        // not implemented
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // not implemented
        }
    }

    class DeploymentPlanLabelProvider implements ITableLabelProvider {
        List<ILabelProviderListener> listeners = new ArrayList<>();

        // TODO: when we get action images
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            DeploymentComponent deploymentComponent = (DeploymentComponent) element;
            switch (columnIndex) {
            case ACTION_COLUMN:
                return deploymentComponent.getDestinationSummary().getAction();
            case NAME_COLUMN:
                return deploymentComponent.getNameWithFolder();
            case PACKAGE_COLUMN:
                String packageName = deploymentComponent.getComponent().getPackageName();
                // show "" for unpackaged content
                if (Utils.isNotEmpty(packageName) && Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
                    packageName = Constants.EMPTY_STRING;
                }
                return packageName;
            case TYPE_COLUMN:
                return deploymentComponent.getComponent().getDisplayName();
            }
            return "";
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            listeners.add(listener);
        }

        @Override
        public void dispose() {
        // not implemented
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            // not implemented
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            listeners.remove(listener);
        }
    }

    public CheckboxTableViewer getTblViewer() {
        return tblViewer;
    }

}
