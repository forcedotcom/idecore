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

import org.eclipse.swt.widgets.TreeItem;

/** 
 * A listener which is notified when a TreeItem is created
 */
public interface ITreeItemCreatedListener {
	/**
     * Notifies that a TreeItem has been created
     *
     * @param item TreeItem which was created
     * @param element Object associated with this item
     */
    void treeItemCreated(TreeItem item, Object element);
}

