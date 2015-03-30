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
/**
 * 
 */
package com.salesforce.ide.deployment.ui.wizards;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSorter;
import com.salesforce.ide.deployment.ui.wizards.DeploymentPlanComposite.ColumnData;

final class DeploymentPlanColumnSortListener implements Listener {
    private final DeploymentPlanComposite deploymentPlanComposite;

    public DeploymentPlanColumnSortListener(DeploymentPlanComposite deploymentPlanComposite) {
        this.deploymentPlanComposite = deploymentPlanComposite;
    }

    @Override
    public void handleEvent(Event e) {
        TableItem[] items = deploymentPlanComposite.getTblDeployPlan().getItems();
        TableColumn column = (TableColumn) e.widget;
        ColumnData columnData = (ColumnData) column.getData();
        Comparator<DeploymentComponent> comparator = DeploymentComponentSorter.getSorter(columnData.sortOrder);

        for (int i = 1; i < items.length; i++) {
            DeploymentComponent deploymentComponent1 = (DeploymentComponent) items[i].getData();
            for (int j = 0; j < i; j++) {
                DeploymentComponent deploymentComponent2 = (DeploymentComponent) items[j].getData();
                boolean compareResult =
                        (columnData.ascending ? comparator.compare(deploymentComponent1, deploymentComponent2) < 0
                                : comparator.compare(deploymentComponent1, deploymentComponent2) > 0);
                if (compareResult) {
                    String action = items[i].getText(DeploymentPlanComposite.ACTION_COLUMN);
                    String name = items[i].getText(DeploymentPlanComposite.NAME_COLUMN);
                    String packageName = items[i].getText(DeploymentPlanComposite.PACKAGE_COLUMN);
                    String type = items[i].getText(DeploymentPlanComposite.TYPE_COLUMN);
                    boolean checked = items[i].getChecked();
                    boolean grayed = items[i].getGrayed();
                    items[i].dispose();

                    TableItem item = new TableItem(deploymentPlanComposite.getTblDeployPlan(), SWT.NONE, j);
                    item.setText(DeploymentPlanComposite.ACTION_COLUMN, action);

                    deploymentPlanComposite.getDeploymentPlanPage().setTableEsthetics(deploymentComponent1, item);

                    //item.setImage(ACTION_COLUMN, DeploymentSummary.getImage(action));
                    item.setText(DeploymentPlanComposite.NAME_COLUMN, name);
                    item.setText(DeploymentPlanComposite.PACKAGE_COLUMN, packageName);
                    item.setText(DeploymentPlanComposite.TYPE_COLUMN, type);
                    item.setChecked(checked);
                    item.setGrayed(grayed);
                    item.setData(deploymentComponent1);
                    items = deploymentPlanComposite.getTblDeployPlan().getItems();
                    break;
                }
            }
        }
        columnData.toggleOrderSwith();
        deploymentPlanComposite.getTblDeployPlan().setSortColumn(column);
        deploymentPlanComposite.getTblDeployPlan().update();
    }
}
