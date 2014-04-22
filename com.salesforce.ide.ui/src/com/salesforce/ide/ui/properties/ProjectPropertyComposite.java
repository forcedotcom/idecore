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
package com.salesforce.ide.ui.properties;

import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.ui.internal.composite.BaseProjectComposite;

/**
 * Captures project and organization settings.
 * 
 * @author cwall
 */
public class ProjectPropertyComposite extends BaseProjectComposite {

    public ProjectPropertyComposite(Composite parent, int style, ProjectPropertyPage projectPropertyPage) {
        super(parent, style, projectPropertyPage, projectPropertyPage.getSalesforceEndpoints());
    }

    // Monitors user input and reports messages.
    @Override
    public void validateUserInput() {
        ((ProjectPropertyPage) dialogPage).validateUserInput();
    }
}
