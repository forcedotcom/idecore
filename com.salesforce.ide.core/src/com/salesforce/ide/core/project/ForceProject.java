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
package com.salesforce.ide.core.project;

import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.google.common.collect.Sets;
import com.salesforce.ide.core.factories.MetadataFactory;
import com.salesforce.ide.core.factories.ToolingFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Org;
import com.salesforce.ide.core.remote.MetadataStubExt;

/**
 * A data container for a project's connection information. This is persisted in a project scoped preference. This is
 * actually a value object. You must also be careful to handle its hashCode and equals properly since it is used as a
 * key in {@link MetadataFactory} and {@link ToolingFactory}.
 * 
 * To materialize a ForceProject from an IProject, use {@link
 * com.salesforce.ide.core.services.ProjectService.getForceProject(IProject)}
 * 
 * @author cwall
 */
public class ForceProject extends Org {

    private String endpointEnvironment = null;
    private int readTimeoutSecs = Constants.READ_TIMEOUT_IN_SECONDS_DEFAULT; // stored in seconds
    private String packageName = null;
    private IProject project = null;
    private String ideVersion = null;
    private String projectIdentifier = null;
    private boolean preferToolingDeployment = true;
    private String[] enabledComponentTypes;

    //   C O N S T R U C T O R S
    public ForceProject() {
        super();
    }

    public ForceProject(IProject project) {
        super();
        this.project = project;
    }

    public ForceProject(String username, String password, String token, String sessionId, String endpointServer) {
        super(username, password, token, sessionId, endpointServer);
    }

    //   M E T H O D S
    public boolean isTimeOutChanged(ForceProject oldCi) {
        return oldCi.getReadTimeoutSecs() != getReadTimeoutSecs();
    }

    public int getReadTimeoutSecs() {
        return readTimeoutSecs;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutSecs * Constants.SECONDS_TO_MILISECONDS;
    }

    public void setReadTimeoutSecs(int readTimeoutSecs) {
        this.readTimeoutSecs = (readTimeoutSecs < 0 ? Constants.READ_TIMEOUT_IN_SECONDS_MIN : readTimeoutSecs);
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutSecs =
                (readTimeoutMillis < 0 ? Constants.READ_TIMEOUT_IN_SECONDS_MIN : readTimeoutMillis / 1000);
    }

    public String getEndpointEnvironment() {
        return endpointEnvironment;
    }

    public void setEndpointEnvironment(String endpointEnvironment) {
        this.endpointEnvironment = endpointEnvironment;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getIdeVersion() {
        return ideVersion;
    }

    public void setIdeVersion(String ideVersion) {
        this.ideVersion = ideVersion;
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }
    
    public boolean getPreferToolingDeployment() {
        return preferToolingDeployment;
    }

    public void setPreferToolingDeployment(boolean isPreferred) {
        preferToolingDeployment = isPreferred;
    }

    @Override
    public String getFullLogDisplay() {
        StringBuffer strBuff = new StringBuffer(super.getFullLogDisplay());
        strBuff.append("', project = '").append(project != null ? project.getName() : "n/a").append("', package = '")
                .append(getPackageName()).append("'");
        return strBuff.toString();
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((endpointEnvironment == null) ? 0 : endpointEnvironment
						.hashCode());
		result = prime * result
				+ ((ideVersion == null) ? 0 : ideVersion.hashCode());
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + (preferToolingDeployment ? 1231 : 1237);
		result = prime
				* result
				+ ((projectIdentifier == null) ? 0 : projectIdentifier
						.hashCode());
		result = prime * result + readTimeoutSecs;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForceProject other = (ForceProject) obj;
		if (endpointEnvironment == null) {
			if (other.endpointEnvironment != null)
				return false;
		} else if (!endpointEnvironment.equals(other.endpointEnvironment))
			return false;
		if (ideVersion == null) {
			if (other.ideVersion != null)
				return false;
		} else if (!ideVersion.equals(other.ideVersion))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (preferToolingDeployment != other.preferToolingDeployment)
			return false;
		if (projectIdentifier == null) {
			if (other.projectIdentifier != null)
				return false;
		} else if (!projectIdentifier.equals(other.projectIdentifier))
			return false;
		if (readTimeoutSecs != other.readTimeoutSecs)
			return false;
		return true;
	}

	private void initializeEnabledComponentTypes() {
        Set<String> types = Sets.newHashSet(Constants.DEV_CODE_COMPONENT_TYPES);
        MetadataStubExt metadataStubExt;
        try {
            metadataStubExt =
                    ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory().getMetadataStubExt(this);
            Set<String> supportedComponents = metadataStubExt.getSupportedMetadataComponents();
            types.retainAll(supportedComponents);
        } catch (Throwable e) {
            // not ideal, but since we're lazy (initialized, that is) we don't want to throw a caught exception
            throw new RuntimeException(e);
        }
        enabledComponentTypes = types.toArray(new String[0]);
    }

    public String[] getEnabledComponentTypes() {
        if (enabledComponentTypes == null) {
            initializeEnabledComponentTypes();
        }
        return enabledComponentTypes;
    }
}
