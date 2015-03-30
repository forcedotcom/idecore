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
package com.salesforce.ide.core.internal.components.homepage.component;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.project.ForceProjectException;

public class HomePageComponentComponentController extends ComponentController {

    public HomePageComponentComponentController() throws ForceProjectException {
        super(new HomePageComponentModel());
    }

    @Override
    protected void preSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        // TODO Auto-generated method stub
        
    }
}
