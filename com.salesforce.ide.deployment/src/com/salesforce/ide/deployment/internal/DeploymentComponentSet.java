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

import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;

/**
 * Contains set of deployment candidates.
 * 
 * @author cwall
 */
public class DeploymentComponentSet extends TreeSet<DeploymentComponent> {
    private static final Logger logger = Logger.getLogger(DeploymentComponentSet.class);

    private static final long serialVersionUID = 1L;

    //   C O N S T R U C T O R S
    public DeploymentComponentSet() {
        super(DeploymentComponentSorter.getSorter(DeploymentComponentSorter.SORT_FILENAME));
    }

    public DeploymentComponentSet(Integer sort) {
        super(DeploymentComponentSorter.getSorter(sort));
    }

    //   M E T H O D S
    public boolean addAll(DeploymentComponentSet deploymentComponentSet) {
        return addAll(deploymentComponentSet, true);
    }

    public boolean addAll(DeploymentComponentSet deploymentComponentSet, boolean updateComposite) {
        boolean result = true;
        for (DeploymentComponent deploymentComponent : deploymentComponentSet) {
            if (!add(deploymentComponent, updateComposite)) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean add(DeploymentComponent deploymentComponent) {
        return add(deploymentComponent, true);
    }

    public boolean add(DeploymentComponent deploymentComponent, boolean updateComposite) {
        if (deploymentComponent.getComponent() == null) {
            logger.warn("Unable to add deployment component with null component");
            return false;
        }

        // update composite if associated has changed
        if (deploymentComponent.isMetadataComposite()) {
            DeploymentComponent compositeDeploymentComponent =
                    getByFilePath(deploymentComponent.getCompositeFilePath());
            if (compositeDeploymentComponent != null
                    && !compositeDeploymentComponent.getDestinationSummary().equals(
                        deploymentComponent.getDestinationSummary())) {

                if (!DeploymentSummary.isDeployable(deploymentComponent.getDestinationSummary())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adjusted " + compositeDeploymentComponent.getFullDisplayName() + " from "
                                + compositeDeploymentComponent.getDestinationSummary().toString() + " to "
                                + deploymentComponent.getDestinationSummary().toString());
                    }
                    compositeDeploymentComponent.setDestinationSummary(deploymentComponent.getDestinationSummary());
                    compositeDeploymentComponent.setDeploy(deploymentComponent.isDeploy());
                } else if (!DeploymentSummary.isDeployable(compositeDeploymentComponent.getDestinationSummary())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adjusted " + deploymentComponent.getFullDisplayName() + " from "
                                + deploymentComponent.getDestinationSummary().toString() + " to "
                                + compositeDeploymentComponent.getDestinationSummary().toString());
                    }
                    deploymentComponent.setDestinationSummary(compositeDeploymentComponent.getDestinationSummary());
                    deploymentComponent.setDeploy(deploymentComponent.isDeploy());
                }
            }
        }

        handleSubFolderContent(deploymentComponent);

        return super.add(deploymentComponent);
    }

    private void handleSubFolderContent(DeploymentComponent deploymentComponent) {
        // update subfolder deployment component if parent is not deployable, eg resource not found locally
        if (deploymentComponent.getComponent().isWithinFolder()
                && Utils.isNotEmpty(deploymentComponent.getComponent().getMetadataFilePath())) {
            String metadataFilePath = deploymentComponent.getComponent().getMetadataFilePath();
            String folderMetadataFilePath = metadataFilePath.substring(0, metadataFilePath.lastIndexOf("/"));
            folderMetadataFilePath += Constants.DEFAULT_METADATA_FILE_EXTENSION;

            DeploymentComponent folderDeploymentComponent = getByFilePath(folderMetadataFilePath);
            if (folderDeploymentComponent != null
                    && !DeploymentSummary.isDeployable(folderDeploymentComponent.getDestinationSummary())
                    && !folderDeploymentComponent.isRemoteFound()) {
                deploymentComponent.setDestinationSummary(DeploymentSummary.DEPENDENT_NOT_DEPLOYABLE);
                deploymentComponent.setDeploy(false);
                if (logger.isDebugEnabled()) {
                    logger.debug("Reset associated sub-folder resource " + deploymentComponent.getFullDisplayName()
                            + " - parent not deployable ");
                }
            }
        }

        // update child subfolder deployment component if folder deployment components is not deployable, eg resource not found locally
        // but, if local resource is missing, but found remotely, it's okay to deploy children
        if (Constants.FOLDER.equals(deploymentComponent.getComponent().getComponentType())
                && !DeploymentSummary.isDeployable(deploymentComponent.getDestinationSummary())
                && Utils.isNotEmpty(deploymentComponent.getComponent().getSecondaryComponentType())
                && (DeploymentSummary.RESOURCE_NOT_FOUND.equals(deploymentComponent.getDestinationSummary()) && !deploymentComponent
                        .isRemoteFound())) {
            DeploymentComponentSet deploymentComponentSet = getSubFolderDeploymentComponents(deploymentComponent);
            if (Utils.isNotEmpty(deploymentComponentSet)) {
                for (DeploymentComponent tmpDeploymentComponent : this) {
                    tmpDeploymentComponent.setDestinationSummary(DeploymentSummary.DEPENDENT_NOT_DEPLOYABLE);
                    tmpDeploymentComponent.setDeploy(false);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reset associated sub-folder resource "
                                + tmpDeploymentComponent.getFullDisplayName() + " - parent not deployable ");
                    }
                }
            }
        }
    }

    public DeploymentComponentSet getSubFolderDeploymentComponents(DeploymentComponent folderDeploymentComponent) {
        if (isEmpty()) {
            return null;
        }

        DeploymentComponentSet deploymentComponentSet = new DeploymentComponentSet();

        Component folderComponent = folderDeploymentComponent.getComponent();
        for (DeploymentComponent tmpDeploymentComponent : this) {
            Component tmpComponent = tmpDeploymentComponent.getComponent();
            if (tmpComponent.isWithinFolder()
                    && folderComponent.getSecondaryComponentType().equals(tmpComponent.getComponentType())
                    && Utils.isNotEmpty(tmpComponent.getParentFolderNameIfComponentMustBeInFolder())
                    && folderComponent.getName().equals(tmpComponent.getParentFolderNameIfComponentMustBeInFolder())) {
                deploymentComponentSet.add(tmpDeploymentComponent);
            }
        }

        return deploymentComponentSet;
    }

    public DeploymentComponent get(DeploymentComponent deploymentComponent) {
        if (isEmpty()) {
            return null;
        }

        DeploymentComponent returnDeploymentComponent = null;
        for (DeploymentComponent tmpDeploymentComponent : this) {
            if (deploymentComponent.equals(tmpDeploymentComponent)) {
                returnDeploymentComponent = tmpDeploymentComponent;
            }
        }
        return returnDeploymentComponent;
    }

    public DeploymentComponentSet getDeploymentComponentSetWithoutMetadata() {
        if (isEmpty()) {
            return null;
        }

        DeploymentComponentSet tmpDeploymentComponentSet = new DeploymentComponentSet();
        for (DeploymentComponent deploymentComponent : this) {
            Component component = deploymentComponent.getComponent();
            if (component.isMetadataInstance() || component.isPackageManifest()) {
                continue;
            }
            tmpDeploymentComponentSet.add(deploymentComponent);
        }
        return tmpDeploymentComponentSet;
    }

    public ComponentList getComponents() {
        return getComponents(true);
    }

    public ComponentList getComponents(boolean includeMetadata) {
        if (isEmpty()) {
            return null;
        }

        ComponentList componentList = new ComponentList();
        for (DeploymentComponent deploymentComponent : this) {
            Component component = deploymentComponent.getComponent();
            componentList.add(deploymentComponent.getComponent());

            if (includeMetadata && component.isMetadataComposite() && !component.isMetadataInstance()) {
                Component compositeComponent = getComponentByFilePath(component.getCompositeMetadataFilePath());
                if (compositeComponent != null) {
                    componentList.add(compositeComponent);
                }
            }
        }

        return componentList;
    }

    public Component getComponentByFilePath(String filepath) {
        if (isEmpty() || Utils.isEmpty(filepath)) {
            return null;
        }

        for (DeploymentComponent deploymentComponent : this) {
            Component component = deploymentComponent.getComponent();
            if (!component.isCaseSensitive() && filepath.equalsIgnoreCase(component.getMetadataFilePath())) {
                return component;
            } else if (filepath.equals(component.getMetadataFilePath())) {
                return component;
            }
        }

        return null;
    }

    public DeploymentComponent getByFilePath(String filepath) {
        if (isEmpty() || Utils.isEmpty(filepath)) {
            return null;
        }

        for (DeploymentComponent deploymentComponent : this) {
            Component component = deploymentComponent.getComponent();
            if (!component.isCaseSensitive() && filepath.equalsIgnoreCase(component.getMetadataFilePath())) {
                return deploymentComponent;
            } else if (filepath.equals(component.getMetadataFilePath())) {
                return deploymentComponent;
            }
        }

        return null;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public void setDeploy(DeploymentComponent deploymentComponent, boolean deploy, boolean includeMetadata) {
        if (isEmpty()) {
            return;
        }

        DeploymentComponent tmpDeploymentComponent = get(deploymentComponent);
        if (tmpDeploymentComponent == null) {
            logger.warn("Unable to set deploy to '" + deploy + "' on " + deploymentComponent.getFullDisplayName()
                    + " - not found in deployment candidate set");
            return;
        }
        tmpDeploymentComponent.setDeploy(deploy);

        if (includeMetadata && tmpDeploymentComponent.getComponent() != null
                && tmpDeploymentComponent.getComponent().isMetadataComposite()) {
            String metadataFileName = tmpDeploymentComponent.getComponent().getCompositeFileName();
            for (DeploymentComponent tmpTmpdeploymentComponent : this) {
                if (!tmpDeploymentComponent.getComponent().isCaseSensitive()
                        && metadataFileName.equalsIgnoreCase(tmpTmpdeploymentComponent.getFileName())) {
                    tmpTmpdeploymentComponent.setDeploy(deploy);
                    return;
                } else if (metadataFileName.equals(tmpTmpdeploymentComponent.getFileName())) {
                    tmpTmpdeploymentComponent.setDeploy(deploy);
                    return;
                }
            }

            logger.warn("Unable to set deploy to '" + deploy + "' on metadata composite " + metadataFileName + " for "
                    + deploymentComponent.getFullDisplayName() + " - component not found in deployment candidate set");
            return;
        }
    }

    public DeploymentComponentSet sort(Integer sort) {
        DeploymentComponentSet tmpDeploymentComponentSet = new DeploymentComponentSet(sort);
        tmpDeploymentComponentSet.addAll(this);
        return tmpDeploymentComponentSet;
    }

    public String getLogContent() {
        String logContent = "Deployment components are ";
        for (DeploymentComponent deploymentComponent : this) {
            logContent += deploymentComponent.getComponent().getFullName() + ",";
        }
        return logContent.substring(0, logContent.length() - 1);
    }

}
