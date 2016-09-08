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
package com.salesforce.ide.core.model;

import java.util.Calendar;
import java.util.List;

import org.eclipse.core.resources.IFile;

public interface IComponent {

    String getBody();

    void setBody(String body);

    long getBodyChecksum();

    String getCreatedById();

    String getCreatedByName();

    Calendar getCreatedDate();

    String getDefaultFolder();

    String getFullDisplayName();

    String getDisplayName();

    String getFileExtension();

    byte[] getFile();

    String getMetadataFilePath();

    String getId();

    String getLastModifiedById();

    String getLastModifiedByName();

    Calendar getLastModifiedDate();

    String getState();

    String getFileName();

    String getFullName();

    String getNamespacePrefix();

    void setNamespacePrefix(Object namespacePrefix);

    String getPackageName();

    void setPackageName(String packageName);

    String getComponentType();

    String getComponentTypeAlias();

    boolean isTextContent();

    long getOriginalBodyChecksum();

    boolean isMetadataComposite();

    boolean isWithinFolder();

    boolean isInternal();

    boolean isPackageManifest();

    boolean isUnknown();

    String getParentFolderNameIfComponentMustBeInFolder();

    IFile getFileResource();

    void setFileResource(IFile resource);

    boolean isInstalled();

    void setInstalled(boolean managed);

    String getMetadataFileExtension();

    String getCompositeMetadataFilePath();

    boolean isMetadataInstance();

    void setMetadataInstance(boolean metadataInstance);

    boolean isRemoteDeleteable();

    List<String> getAssociatedComponentTypes();

    String toStringLite();

    boolean isWildCardSupported();
    
    // Deployment
    /////////////

    /*
     * Allows this component to transform itself before being added to ComponentList.
     * Why do we need this? Because bundle components need a way to send their top-level container instead of themselves for deployment.
     */
    Component preComponentListAddition(PackageConfiguration configuration);
    
}
