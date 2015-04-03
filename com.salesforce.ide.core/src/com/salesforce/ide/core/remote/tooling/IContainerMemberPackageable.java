/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.remote.tooling;

/**
 * Encapsulates common behavior for components that can be packaged as members of a container member for deployment
 * through the Tooling API.
 * 
 * @author nchen
 */
public interface IContainerMemberPackageable {
    
    // The contents of this member
    public String getBody();

    // Reference to the original component (must exist on server) that we want to modify
    public String getEntityId();

    // Reference to the MetadataContainer that we are using as our deployment container
    public String getMetadataContainerId();
}
