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

import junit.framework.TestCase;

public class PackageTreeNodeTest_unit extends TestCase {
    
    public void testPackageNodeEquality(){
        assertTrue(new PackageTreeNode("foo").equals(new PackageTreeNode("foo")));
        assertFalse(new PackageTreeNode("bar").equals(new PackageTreeNode("foo")));
        assertFalse(new PackageTreeNode("bar").equals(new PackageTreeNode(null)));
        assertTrue(new PackageTreeNode(null).equals(new PackageTreeNode(null)));
        assertTrue(new PackageTreeNode("").equals(new PackageTreeNode("")));
    }
    
    public void testPackageNodeEqualityWithHierarchy(){
        final PackageTreeNode parent = new PackageTreeNode("foo");
        final PackageTreeNode child = new PackageTreeNode("bar");
        final PackageTreeNode grandchild = new PackageTreeNode("crap");
        child.addChild(grandchild);
        parent.addChild(child);
        
        final PackageTreeNode anotherParent = new PackageTreeNode("foo");
        final PackageTreeNode anotherChild = new PackageTreeNode("bar");
        final PackageTreeNode anotherGrandchild = new PackageTreeNode("crap");
        anotherChild.addChild(anotherGrandchild);
        anotherParent.addChild(anotherChild);
        
        assertTrue(grandchild.equals(anotherGrandchild));
        assertTrue(child.equals(anotherChild));
        
        assertFalse(grandchild.equals(anotherChild));
    }
}
