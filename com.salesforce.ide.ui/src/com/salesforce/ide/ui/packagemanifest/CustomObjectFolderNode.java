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

/**
 * 
 * @author ataylor
 * 
 */
public class CustomObjectFolderNode extends PackageTreeNode {
    public CustomObjectFolderNode(String name) {
        super(name);
        image = null;
    }

    @Override
    public String getName() {
        return super.getName().toLowerCase();
    }
}
