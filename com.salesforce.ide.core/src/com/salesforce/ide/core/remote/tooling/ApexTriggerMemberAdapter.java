package com.salesforce.ide.core.remote.tooling;

import com.salesforce.ide.core.model.Component;
import com.sforce.soap.tooling.MetadataContainer;

/**
 * Adapts an ApexTrigger component to a ApexTriggerMember for deployment.
 * 
 * @author nchen
 * 
 */
public class ApexTriggerMemberAdapter extends ContainerMemberAdapter {

    public ApexTriggerMemberAdapter(Component component, MetadataContainer container) {
        super(component, container);
    }

}
