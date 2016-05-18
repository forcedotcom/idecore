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
import org.eclipse.core.runtime.Path;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.ProjectService;

/**
 * This is a way to encapsulate functionality for dealing with aura definition bundles, which is a weird kind of metadata type. Bundles
 * have items under them and these items need to be transported as a unit during retrieve()/deploy() operations. So the
 * gist is that we any time we add an individual bundle item, we need to transform those items to a "faux" top-level
 * component that represents the whole bundle.
 * 
 * @author nchen
 */
public class AuraDefinitionBundleComponent extends Component {
    
    @Override
    public Component preComponentListAddition() {
        ComponentFactory componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
        ProjectService projectService = ContainerDelegate.getInstance().getServiceLocator().getProjectService();

        if (needsConversion(this)) {
            Component replacement = componentFactory.getComponentByComponentType(this.getComponentType());
            
            IFolder sourceFolder = projectService.getSourceFolder(this.getProject());
            IPath sourcePath = sourceFolder.getLocation();
            IPath fileResourcePath = this.getFileResource().getLocation();
            IPath relativeTo = fileResourcePath.makeRelativeTo(sourcePath);

            String type = relativeTo.segment(0);
            String bundleName = relativeTo.segment(1);
            String filePath = type + Path.SEPARATOR + bundleName;
            
            replacement.setFullName(bundleName);
            replacement.setName(bundleName);
            replacement.setFilePath(filePath);
            replacement.setPackageName(this.getPackageName());
            replacement.setBundleFolder(sourceFolder.getFolder(filePath));
            
            return replacement;
        }
        return this;
    }
    
    // If a bundle conversion has an associated file resource, it means that it is still tied to the individual item.
    // We need to convert it to the top-level representation, which is a folder.
    private boolean needsConversion(Component component) {
        return getFileResource() != null;
    }
    
}
