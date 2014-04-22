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

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

import com.salesforce.ide.ui.packagemanifest.PackageManifestTree.PackageManifestTreeViewer;
import com.salesforce.ide.ui.widgets.MultiCheckboxButton;

/**
 * 
 * @author ataylor
 * 
 */
public class ManifestTreeFilter extends PatternFilter {
    @Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
        String labelText = ((PackageTreeNode) element).getName();

        if (labelText == null) {
            return false;
        }
        boolean visible = wordMatches(labelText);

        if (!visible) {
            return isChildMatch(viewer, element);
        }

        return visible;
    }

    @Override
    public boolean isElementVisible(Viewer viewer, Object element) {
        boolean visible = true;
        if (element instanceof ComponentTypeNode || element instanceof CustomObjectFolderNode) {
            visible = isParentMatch(viewer, element);
        }

        else {
            visible = isParentMatch(viewer, element) || isLeafMatch(viewer, element);
        }

        PackageTreeNode node = ((PackageTreeNode) element);
        node.setFiltered(!visible);

        List<PackageTreeNode> list = ((PackageManifestTreeViewer) viewer).filteredList;
        if (visible) {
            list.remove(node);
        }

        else {
            if (!list.contains(node)) {
                list.add(node);
            }
        }

        ((PackageManifestTreeViewer) viewer).checkedAndFiltered |=
                (!visible && !MultiCheckboxButton.isUnChecked(node.getState()));
        return visible;
    }

    public boolean isChildMatch(Viewer viewer, Object element) {
        PackageTreeNode parent = (PackageTreeNode) ((PackageTreeNode) element).getParent();
        boolean match = false;
        while (parent.getValue() != null) {
            if (!(parent instanceof ComponentTypeNode) && !(parent instanceof CustomObjectFolderNode)) {
                match = wordMatches(parent.getName());
            }

            if (!match) {
                parent = (PackageTreeNode) parent.getParent();
            } else {
                break;
            }
        }

        return match;
    }
}
