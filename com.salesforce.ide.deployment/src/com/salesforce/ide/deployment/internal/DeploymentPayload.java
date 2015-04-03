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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;

public class DeploymentPayload {

    private static final Logger logger = Logger.getLogger(DeploymentPayload.class);

    protected IProject project;  // TODO only used by clients of DeploymentPayload; probably doesn't belong here.
    protected List<IResource> deployResources;  // TODO see below - is this ever used?
    protected DeploymentComponentSet deploymentComponents = new DeploymentComponentSet();
    protected String destinationOrgUsername;
    protected ComponentList remoteComponentList;

    public DeploymentPayload() {}

    // TODO Is this method ever called?
    public DeploymentPayload(DeploymentComponentSet deploymentComponents) {
        this.deploymentComponents = deploymentComponents;
    }

    public DeploymentPayload(IProject project, List<IResource> deployResources) {
        this.deployResources = deployResources;
        this.project = project;
    }

    // TODO Is this method ever called?  Is "deployResources" ever used?
    public List<IResource> getDeployResources() {
        return deployResources;
    }

    public void setDeployResource(List<IResource> deployResources) {
        this.deployResources = deployResources;
    }

    public String getDestinationOrgUsername() {
        return destinationOrgUsername;
    }

    public void setDestinationOrgUsername(String destinationOrgUsername) {
        this.destinationOrgUsername = destinationOrgUsername;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public ComponentList getRemoteComponentList() {
        return remoteComponentList;
    }

    public void setRemoteComponentList(ComponentList remoteComponentList) {
        this.remoteComponentList = remoteComponentList;
    }

    public DeploymentComponentSet getDeploymentComponents() {
        return deploymentComponents;
    }

    public boolean add(DeploymentComponent deploymentComponent) {
        return add(deploymentComponent, true);
    }

    public boolean add(DeploymentComponent deploymentComponent, boolean updateComposite) {
        return deploymentComponents.add(deploymentComponent, updateComposite);
    }

    public void addAll(DeploymentComponentSet deploymentComponents) {
        this.deploymentComponents.addAll(deploymentComponents);
    }

    public boolean remove(DeploymentComponent deploymentComponent) {
        boolean success = false;
        if (deploymentComponents != null && deploymentComponent != null) {
            success = deploymentComponents.remove(deploymentComponent);
        }
        return success;
    }

    public int size() {
        return (deploymentComponents != null ? deploymentComponents.size() : 0);
    }

    public boolean isEmpty() {
        return (deploymentComponents != null ? deploymentComponents.isEmpty() : true);
    }

    public DeploymentComponent getDeploymentComponentByFilename(String fileName) {
        if (Utils.isEmpty(fileName)) {
            return null;
        }

        DeploymentComponent deploymentComponent = null;
        if (!isEmpty()) {
            for (DeploymentComponent tmpDeploymentComponent : deploymentComponents) {
                if (fileName.equals(tmpDeploymentComponent.getFileName())) {
                    deploymentComponent = tmpDeploymentComponent;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found deployment component for filepath '" + fileName + "'");
                    }
                    break;
                }
            }
        }
        return deploymentComponent;
    }

    public DeploymentComponentSet getDeploySelectedComponents() {
        return getDeploySelectedComponents(false);
    }

    public DeploymentComponentSet getDeploySelectedComponents(boolean includeMetadata) {
        DeploymentComponentSet deploySelectedComponents = new DeploymentComponentSet();

        if (isEmpty()) {
            return deploySelectedComponents;
        }

        for (DeploymentComponent deploymentComponent : deploymentComponents) {
            if (deploymentComponent.isDeploy()) {
                deploySelectedComponents.add(deploymentComponent);
                Component component = deploymentComponent.getComponent();
                if (includeMetadata && component.isMetadataComposite() && !component.isMetadataInstance()) {
                    DeploymentComponent compositeDeploymentComponent =
                            deploymentComponents.getByFilePath(component.getCompositeMetadataFilePath());
                    if (compositeDeploymentComponent != null) {
                        deploySelectedComponents.add(compositeDeploymentComponent);
                    }
                }
            }
        }

        return deploySelectedComponents;
    }

    public int getDeploySelectedCount() {
        if (isEmpty()) {
            return 0;
        }

        return getDeploySelectedComponents().size();
    }

    public DeploymentComponentSet getDeploySelectedComponentsByType(String type) {
        if (Utils.isEmpty(type) || isEmpty()) {
            return null;
        }

        DeploymentComponentSet deploySelectedComponents = getDeploySelectedComponents();
        DeploymentComponentSet typedDeployComponents = new DeploymentComponentSet();
        for (DeploymentComponent deploySelectedComponent : deploySelectedComponents) {
            if (type.equals(deploySelectedComponent.getType())) {
                typedDeployComponents.add(deploySelectedComponent);
            }
        }
        return typedDeployComponents;
    }

    // get deployment artifact given name and type
    public DeploymentComponent getDeploymentComponent(String name, String packageName, String type) {
        if (Utils.isEmpty(name) || Utils.isEmpty(type) || Utils.isEmpty(deploymentComponents)) {
            return null;
        }

        for (DeploymentComponent tmpDeploymentComponent : deploymentComponents) {
            if (tmpDeploymentComponent.getFileName().startsWith(name)
                    && isSamePackage(packageName, tmpDeploymentComponent.getPackageName())
                    && tmpDeploymentComponent.getDisplayName().contains(type)) {
                return tmpDeploymentComponent;
            }
        }
        return null;
    }

    private static boolean isSamePackage(String packageName1, String packageName2) {
        return "".equals(packageName1) && Constants.DEFAULT_PACKAGED_NAME.equals(packageName2) ? true : Utils.isEqual(packageName1, packageName1);
    }

    public DeploymentComponentSet getDeploymentComponentsByType(String type) {
        if (Utils.isEmpty(type) || isEmpty()) {
            return null;
        }

        DeploymentComponentSet typedDeploymentComponentSet = new DeploymentComponentSet();
        for (DeploymentComponent deploymentComponent : deploymentComponents) {
            if (type.equals(deploymentComponent.getType())) {
                typedDeploymentComponentSet.add(deploymentComponent);
            }
        }
        return typedDeploymentComponentSet;
    }

    public void enableDeploymentAll(boolean enabled) {
        if (Utils.isNotEmpty(deploymentComponents)) {
            for (DeploymentComponent deploymentComponent : deploymentComponents) {
                if (DeploymentSummary.isDeployableAction(deploymentComponent.getDestinationSummary())) {
                    deploymentComponent.setDeploy(enabled);
                }
            }
        }
    }

    public String getDeploymentPlanSummary() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Deployment plan summary:");

        DeploymentComponentSet tmpDeploymentComponentSet =
                deploymentComponents.sort(DeploymentComponentSorter.SORT_TYPE);

        int cnt = 0;
        for (DeploymentComponent deploymentComponent : tmpDeploymentComponentSet) {
            strBuff.append("\n  (").append(++cnt).append(") ").append(deploymentComponent.getFullDisplayName()).append(
                ", evaluation=").append(deploymentComponent.getDestinationSummary().getCompareResult()).append(
                ", action=").append(deploymentComponent.getDestinationSummary().getAction()).append(", deploy=")
                    .append(deploymentComponent.isDeploy());
        }

        return strBuff.toString();
    }

}
