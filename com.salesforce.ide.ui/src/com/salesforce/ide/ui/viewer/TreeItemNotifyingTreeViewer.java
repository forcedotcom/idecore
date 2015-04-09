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
package com.salesforce.ide.ui.viewer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public class TreeItemNotifyingTreeViewer extends CheckboxTreeViewer {
	Set<ITreeItemCreatedListener> itemCreationListeners = new HashSet<>();

	public TreeItemNotifyingTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	public TreeItemNotifyingTreeViewer(Composite parent) {
		super(parent);
	}

	public TreeItemNotifyingTreeViewer(Tree tree) {
		super(tree);
	}

	@Override
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		super.doUpdateItem(item, element, fullMap);
		for (ITreeItemCreatedListener listener : itemCreationListeners) {
			listener.treeItemCreated((TreeItem) item, element);
		}
	}

	public void addTreeItemCreationListener(ITreeItemCreatedListener listener) {
		itemCreationListeners.add(listener);
	}

	public void removeTreeItemCreationListener(ITreeItemCreatedListener listener) {
		itemCreationListeners.remove(listener);
	}
}
