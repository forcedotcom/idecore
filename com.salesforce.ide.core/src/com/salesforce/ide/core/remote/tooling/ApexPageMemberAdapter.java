package com.salesforce.ide.core.remote.tooling;

import com.salesforce.ide.core.model.Component;
import com.sforce.soap.tooling.MetadataContainer;

/**
 * Adapts an ApexPage component to a ApexPageMember for deployment.
 * 
 * @author nchen
 * 
 */
public class ApexPageMemberAdapter extends ContainerMemberAdapter {

    public ApexPageMemberAdapter(Component component, MetadataContainer container) {
        super(component, container);
    }

}
