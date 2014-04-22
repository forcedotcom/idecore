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

import java.io.File;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;


public class DeploymentComponent {

    private Component component = null;
    private ComponentList componentList = null;
    private DeploymentSummary destinationSummary = DeploymentSummary.NEW; // describes remote state
    private boolean deploy = true;
    private boolean remoteFound = true;

    public DeploymentComponent(Component component) {
        super();
        this.component = component;
    }

    public DeploymentComponent(ComponentList componentList) {
        super();
        this.componentList = componentList;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public ComponentList getComponentList() {
        return componentList;
    }

    public void setComponentList(ComponentList componentList) {
        this.componentList = componentList;
    }

    public String getFileName() {
        return component.getFileName();
    }

    public String getStrippedFilePath() {
        return Utils.stripSourceFolder((Utils.stripExtension(component.getMetadataFilePath())));
    }

    public String getCompositeFilePath() {
        return component.getCompositeMetadataFilePath();
    }

    public String getNameWithFolder() {
        return component.isWithinFolder() && Utils.isNotEmpty(component.getParentFolderNameIfComponentMustBeInFolder()) ? component.getParentFolderNameIfComponentMustBeInFolder() + File.separator + getComponent().getName() : getComponent().getName();
    }

    public String getPackageName() {
        return component.getPackageName();
    }

    public String getType() {
        return component.getComponentType();
    }

    public boolean isMetadataComposite() {
        return component.isMetadataComposite();
    }

    public DeploymentSummary getDestinationSummary() {
        return destinationSummary;
    }

    public void setDestinationSummary(DeploymentSummary destinationSummary) {
        this.destinationSummary = destinationSummary;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    public String getDisplayName() {
        return component.getDisplayName();
    }

    public String getFullDisplayName() {
        return component.getFullDisplayName();
    }

    public boolean isRemoteFound() {
        return remoteFound;
    }

    public void setRemoteFound(boolean remoteFound) {
        this.remoteFound = remoteFound;
    }

    public String toLog() {
        StringBuffer strBuff =
                new StringBuffer(Utils.isNotEmpty(component.getNamespacePrefix()) ? component.getNamespacePrefix()
                        + "." : "");
        strBuff.append(component.getName()).append(" | ").append(component.getComponentType()).append(" | ").append(
            destinationSummary.getAction());
        return strBuff.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        result = prime * result + ((componentList == null) ? 0 : componentList.hashCode());
        result = prime * result + (deploy ? 1231 : 1237);
        result = prime * result + ((destinationSummary == null) ? 0 : destinationSummary.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DeploymentComponent other = (DeploymentComponent) obj;
        if (component == null) {
            if (other.component != null)
                return false;
        } else if (!component.equals(other.component))
            return false;
        if (componentList == null) {
            if (other.componentList != null)
                return false;
        } else if (!componentList.equals(other.componentList))
            return false;
        if (deploy != other.deploy)
            return false;
        if (destinationSummary == null) {
            if (other.destinationSummary != null)
                return false;
        } else if (!destinationSummary.equals(other.destinationSummary))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("DeploymentComponent ( ").append(super.toString()).append(TAB).append("component = ").append(
            this.component).append(TAB).append("destinationSummary = ").append(this.destinationSummary).append(TAB)
                .append("deploy = ").append(this.deploy).append(TAB).append(" )");
        return retValue.toString();
    }

    public String getRunningUser() {
        if (!getComponent().getComponentType().equals(Constants.DASHBOARD)) {
            return null;
        }
        String body = getComponent().getBody();
        String openTokenElement = "<runningUser>";
        String closeTokenElement = "</runningUser>";
        int beginIndex = body.indexOf(openTokenElement) + openTokenElement.length();
        int endIndex = body.indexOf(closeTokenElement);
        if (endIndex < beginIndex) {
            return null;
        }
        String currentRunningUser = body.substring(beginIndex, endIndex);
        return currentRunningUser;
    }

}
