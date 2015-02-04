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
package com.salesforce.ide.core.services.hooks;

import com.salesforce.ide.core.model.ComponentList;

/**
 * Allows listeners to any sync activities in case any additional actions have to be performed.
 * 
 * @author nchen
 * 
 */
public interface ISyncServiceListener {
    public void projectSync(ComponentList componentList);
}
