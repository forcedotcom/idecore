package com.salesforce.ide.core.remote.tooling;

import com.salesforce.ide.core.model.Component;
import com.sforce.soap.tooling.MetadataContainer;

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
