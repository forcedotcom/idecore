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

import com.salesforce.ide.core.model.Component;
import com.sforce.soap.tooling.sobject.MetadataContainer;

/**
 * Abstract class that represents common functionality among the different container member adapters.
 * 
 * @author nchen
 * 
 */
public abstract class ContainerMemberAdapter implements IContainerMemberPackageable {
    private Component component;
    private MetadataContainer container;

    public ContainerMemberAdapter(Component component, MetadataContainer container) {
        this.component = component;
        this.container = container;
    }

    @Override
    public String getBody() {
        return component.getBody();
    }

    @Override
    public String getEntityId() {
        return component.getId();
    }

    @Override
    public String getMetadataContainerId() {
        return container.getId();
    }

}
