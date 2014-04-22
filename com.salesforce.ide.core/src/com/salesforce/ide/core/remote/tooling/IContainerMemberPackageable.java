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