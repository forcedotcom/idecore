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

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.internal.ForceImages;

/**
 * 
 * @author ataylor
 * 
 */
public class CustomObjectComponentNode extends PackageTreeNode {
    Component component = null;

    public CustomObjectComponentNode(String name) {
        super(name);
        image = ForceImages.get(ForceImages.CUSTOMOBJECT_NODE);
        retrieved = true;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
