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
package com.salesforce.ide.deployment.internal;

import com.salesforce.ide.core.internal.utils.Constants;

/**
 * 
 * @author cwall
 */
public enum DeploymentSummary {

    // the following is a collection of source and remote comparisons and what the action will be when deployed to
    // the destination instance.
    // 1.) index
    // 2.) short desc of source compare
    // 3.) long desc of source compare
    // 4.) based on destination compare, action to be taken during deployment  (shown as action to-be-applied in deployment plan)
    // 5.) long desc of action to be taken (shown as tooltip in deployment plan)
    // 6.) initial color
    // 7.) checked color

    // 0-99 are deploy-able evaluations

    NEW(
            0,
            "Deleted",
            "Project instance is new or remote instance has been deleted",
            "Add",
            "Add to destination",
            Constants.GREEN,
            Constants.GREEN),
    UPDATED(
            5,
            "Updated",
            "Home instance has been UPDATED since last sync",
            "Overwrite",
            "Overwrite destination instance",
            Constants.YELLOW,
            Constants.YELLOW),
    DELETED(
            10,
            "New",
            "Home instance has been ADDED since last sync",
            "Delete",
            "Delete destination instance",
            Constants.RED,
            Constants.RED),
    NO_CHANGE(
            15,
            "No Change",
            "Project and home instances are in sync",
            "No Action",
            "No differences detected",
            Constants.GRAY,
            Constants.GRAY),
    // W-583823
    NO_CHANGE_OVERWRITE(
            16,
            "No Change",
            "Project and home instances are in sync",
            "Overwrite",
            "No differences detected",
            Constants.GRAY,
            Constants.GRAY),
    NEW_NOT_SUPPORTED(
            20,
            "Not Supported",
            "Deploy cannot create new components of this type",
            "No Action",
            "Deploy cannot create new components of this type",
            Constants.GRAY,
            Constants.GRAY),

    // 100-N are non-deploy-able evaluations
    NOT_PERMISSIBLE(
            100,
            "Not Permissible",
            "Component type not permissible in destination organization",
            "No Action",
            "Component type not permissible in destination organization",
            Constants.GRAY,
            Constants.GRAY),
    DELETE_NOT_SUPPORTED(
            105,
            "Not Supported",
            "Deploy cannot delete components of this type",
            "No Action",
            "Deploy cannot delete components of this type",
            Constants.GRAY,
            Constants.GRAY),
    DEPENDENT_NOT_DEPLOYABLE(
            110,
            "Dependent Not Deployable",
            "Dependent component, parent or metadata, is not deployable",
            "No Action",
            "Dependent component, parent or metadata, is not deployable",
            Constants.GRAY,
            Constants.GRAY),
    RESOURCE_NOT_FOUND(
            115,
            "Resource Not Found",
            "Source or metadata resource not found in project",
            "No Action",
            "Source or metadata resource not found in project",
            Constants.GRAY,
            Constants.GRAY);

    

    private static final int NOT_DEPLOY_INDEX_START = 100;

    // see descriptions above
    private int idx;
    private String compareResult;
    private String sourceDesc;
    private String action;
    private String actionDesc;
    private String initColor;
    private String checkedColor;

    private DeploymentSummary(int idx, String compareResult, String sourceDesc, String action, String actionDesc,
            String initColor, String checkedColor) {
        this.idx = idx;
        this.compareResult = compareResult;
        this.sourceDesc = sourceDesc;
        this.action = action;
        this.actionDesc = actionDesc;
        this.initColor = initColor;
        this.checkedColor = checkedColor;
    }

    public String getSourceDescription() {
        return sourceDesc;
    }

    public void setSourceDescription(String sourceDesc) {
        this.sourceDesc = sourceDesc;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCompareResult() {
        return compareResult;
    }

    public void setCompareResult(String compareResult) {
        this.compareResult = compareResult;
    }

    public String getActionDescription() {
        return actionDesc;
    }

    public void setActionDescription(String description) {
        this.actionDesc = description;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getInitColor() {
        return initColor;
    }

    public void setInitColor(String initColor) {
        this.initColor = initColor;
    }

    public String getCheckedColor() {
        return checkedColor;
    }

    public void setCheckedColor(String checkedColor) {
        this.checkedColor = checkedColor;
    }

    public static boolean isDeployableAction(DeploymentSummary deploymentSummary) {
        return Constants.NO_ACTION.equals(deploymentSummary.getAction()) ? false : true;
    }

    public static boolean isDeployable(DeploymentSummary deploymentSummary) {
        return deploymentSummary.getIdx() < NOT_DEPLOY_INDEX_START;
    }
}
