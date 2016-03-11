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
package com.salesforce.ide.apex.internal.core.builder;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Lists;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;

/**
 * Traverses the resources in the current project and collects Apex files (classes and triggers).
 * 
 * @author nchen
 * 
 */
class ApexResourcesVisitor implements IResourceProxyVisitor {
    List<IFile> files = Lists.newArrayList();
    private final String classExtension;
    private final String triggerExtension;
    
    ApexResourcesVisitor() {
        ComponentFactory componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
        classExtension = componentFactory.getComponentByComponentType("ApexClass").getFileExtension();
        triggerExtension = componentFactory.getComponentByComponentType("ApexTrigger").getFileExtension();
    }
    
    @Override
    public boolean visit(IResourceProxy proxy) throws CoreException {
        switch (proxy.getType()) {
            case IResource.FILE:
                if (proxy.getName().endsWith(classExtension) 
                	|| proxy.getName().endsWith(triggerExtension)) {
                    files.add((IFile) proxy.requestResource());
                }
                return false;
            case IResource.FOLDER:
                // Only traverse resources that are not inside the Referenced Packages folder
                return !proxy.getName().equals(Constants.REFERENCED_PACKAGE_FOLDER_NAME);
            case IResource.PROJECT:
                return true;
            default:
                return false;
        }
    }
    
    public List<IFile> getFiles() {
        return files;
    }
}