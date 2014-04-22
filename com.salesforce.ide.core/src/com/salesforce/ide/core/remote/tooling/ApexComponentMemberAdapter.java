package com.salesforce.ide.core.remote.tooling;

import com.salesforce.ide.core.model.Component;
import com.sforce.soap.tooling.MetadataContainer;

/**
 * Adapts an ApexComponent component to a ApexComponentMember for deployment.
 * 
 * @author nchen
 * 
 */
public class ApexComponentMemberAdapter extends ContainerMemberAdapter {

    public ApexComponentMemberAdapter(Component component, MetadataContainer container) {
        super(component, container);
    }

}
