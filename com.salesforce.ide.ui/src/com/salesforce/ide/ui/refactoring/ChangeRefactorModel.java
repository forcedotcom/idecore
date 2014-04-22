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
package com.salesforce.ide.ui.refactoring;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Encapsulates data about a refactoring change - move, copy, rename.
 * 
 * @author cwall
 */
public class ChangeRefactorModel extends RefactorModel {

    private IResource destinationResource = null;
    private boolean resourceUpdated = false;

    public ChangeRefactorModel() {
        super();
    }

    public IResource getDestinationResource() {
        return destinationResource;
    }

    public void setDestinationResource(IResource resource) {
        this.destinationResource = resource;
    }

    public void setDestinationUpdated(boolean resourceUpdated) {
        this.resourceUpdated = resourceUpdated;
    }

    public boolean isDestinationUpdated() {
        return resourceUpdated;
    }

    public String getDestinationName() {
        return destinationResource != null ? destinationResource.getName() : "n/a";
    }

    public String getDestinationPath() {
        return destinationResource != null ? destinationResource.getProjectRelativePath().toPortableString() : "n/a";
    }

    public IProject getDestinationProject() {
        return destinationResource != null ? destinationResource.getProject() : null;
    }

    public String getDestinationProjectName() {
        return destinationResource.getProject() != null ? destinationResource.getProject().getName() : "n/a";
    }
}
