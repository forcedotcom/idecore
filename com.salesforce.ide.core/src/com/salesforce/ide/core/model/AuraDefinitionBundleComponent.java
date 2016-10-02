/*******************************************************************************
* Copyright (c) 2016 Salesforce.com, inc..
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors:
*     Salesforce.com, inc. - initial API and implementation
*******************************************************************************/
package com.salesforce.ide.core.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.google.common.annotations.VisibleForTesting;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.services.ProjectService;

/**
 * This is a way to encapsulate functionality for dealing with aura definition bundles, which is a weird kind of
 * metadata type. Bundles have items under them and these items need to be transported as a unit during
 * retrieve()/deploy() operations. So the gist is that we any time we add an individual bundle item, we need to
 * transform those items to a "faux" top-level component that represents the whole bundle.
 * 
 * @author nchen
 */
public class AuraDefinitionBundleComponent extends Component {
    
    private ComponentFactory componentFactory;
    private ProjectService projectService;
    
    public AuraDefinitionBundleComponent() {
        // Empty constructor for Spring
    }
    
    @VisibleForTesting
    public AuraDefinitionBundleComponent(ComponentFactory componentFactory, ProjectService projectService) {
        this.componentFactory = componentFactory;
        this.projectService = projectService;
    }

    @Override
    public Component preComponentListAddition(PackageConfiguration config) {
        componentFactory = getComponentFactory();
        projectService = getProjectService();
        
        if (needsConversion(this) || config.replaceComponent) {
            return replaceComponent(componentFactory, projectService);
        }
        return this;
    }
    
    @VisibleForTesting
    public ProjectService getProjectService() {
        if (projectService == null) {
            projectService = ContainerDelegate.getInstance().getServiceLocator().getProjectService();
        }
        return projectService;
    }
    
    @VisibleForTesting
    public ComponentFactory getComponentFactory() {
        if (componentFactory == null) {
            componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
        }
        return componentFactory;
    }
    
    public Component replaceComponent(ComponentFactory componentFactory, ProjectService projectService) {
        Component replacement = componentFactory.getComponentByComponentType(this.getComponentType());
        
        IPath relativeTo = new Path(getMetadataFilePath());
        String type = Constants.AURA;
        String bundleName = relativeTo.segment(1);
        String filePath = type + Path.SEPARATOR + bundleName;
        
        replacement.setFullName(bundleName);
        replacement.setName(bundleName);
        replacement.setFilePath(filePath);
        replacement.setPackageName(this.getPackageName());
        if (getProject() != null) {
            // getProject is null when we materialize a project remotely for deployment purposes
            IFolder sourceFolder = projectService.getSourceFolder(this.getProject());
            replacement.setBundleFolder(sourceFolder.getFolder(filePath));
        }
        replacement.setBundle(true);
        
        return replacement;
    }
    
    // If a bundle conversion has an associated file resource, it means that it is still tied to the individual item.
    // We need to convert it to the top-level representation, which is a folder.
    private boolean needsConversion(Component component) {
        return getFileResource() != null;
    }
    
    @Override
    public boolean hasRemoteBundleChanged(ProjectPackage pkg, IProgressMonitor monitor) throws InterruptedException {
        // The deploy path for AuraDefinitionBundle is so different that giving an accurate account of what has changed is
        // difficult. So, instead of providing a false negative/false positive, just turn off checking for now.
        return false;
    }
}
