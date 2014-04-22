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
package com.salesforce.ide.ui.packagemanifest;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.core.model.Component;

/**
 * 
 * @author ataylor
 * 
 */
public class ComponentTypeNode extends PackageTreeNode {
    public ComponentTypeNode(Component comp) {
        super(comp);
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        image = sharedImages.getImage(ISharedImages.IMG_OBJ_FOLDER);
    }

    @Override
    public String getName() {
        String name = getComponent().getDefaultFolder();
        if (name == null) {
            name = getComponent().getComponentType();
        }
        return name.toLowerCase();
    }

    public Component getComponent() {
        return (Component) getValue();
    }
}
