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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.graphics.Image;

import com.salesforce.ide.ui.widgets.MultiCheckboxButton;

/**
 * 
 * @author ataylor
 * 
 */
public class PackageTreeNode extends TreeNode {
    List<PackageTreeNode> children = new ArrayList<>();
    Image image;
    boolean retrieved;
    int state = MultiCheckboxButton.ENABLED;
    boolean wildcardSelected;
    boolean filtered;

    public PackageTreeNode(Object value) {
        super(value);
    }

    @Override
    public boolean hasChildren() {
        return children.size() > 0;
    }

    @Override
    public TreeNode[] getChildren() {
        return children.toArray(new TreeNode[children.size()]);
    }

    public List<PackageTreeNode> getChildList() {
        return children;
    }

    public void addChild(PackageTreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    @Override
    public void setChildren(final TreeNode[] children) {
        this.children.clear();

        for (TreeNode child : children) {
            this.children.add((PackageTreeNode) child);
        }
    }

    public void setChildren(final List<PackageTreeNode> children) {
        this.children = children;
    }

    public String getName() {
        return value.toString();
    }

    public Image getImage() {
        return image;
    }

    public boolean hasBeenRetrieved() {
        return retrieved;
    }

    public void setRetrieved(boolean retrieved) {
        this.retrieved = retrieved;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isWildcardSelected() {
        return wildcardSelected;
    }

    public void setWildcardSelected(boolean wildcardSelected) {
        this.wildcardSelected = wildcardSelected;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PackageTreeNode other = (PackageTreeNode) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.getValue())) {
            return false;
        } else {
            final TreeNode parent = getParent();
            while (parent != null) {
                if (!parent.equals(other.getParent())) {
                    return false;
                }
				break;
            }
        }
        return true;
    }

}
