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

import org.eclipse.core.runtime.Status;

/**
 * A listener which is notified when the validation status of the tree has changed
 */
public interface IStatusChangedListener {
    /**
     * Notifies that validation status has changed
     * 
     * @param status
     *            New tree status
     */
    public void statusChanged(Status status);
}
