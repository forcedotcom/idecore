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
package com.salesforce.ide.deployment.ui.wizards;

import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.ui.internal.composite.BaseComposite;

public class BaseDeploymentComposite extends BaseComposite {

    public BaseDeploymentComposite(Composite parent, int style) {
        super(parent, style);
    }

	@Override
	public void validateUserInput() {
		
	}
}
