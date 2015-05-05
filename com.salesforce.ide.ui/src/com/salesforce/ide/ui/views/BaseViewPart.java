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
package com.salesforce.ide.ui.views;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.ViewPart;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.services.LoggingService;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.ui.internal.ForceImages;

public abstract class BaseViewPart extends ViewPart {

    protected ServiceLocator serviceLocator = null;

    //   C O N S T R U C T O R
    public BaseViewPart() {
        super();

        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();
    }

    //   M E T H O D S
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public ProjectService getProjectService() {
        return serviceLocator.getProjectService();
    }

    public LoggingService getLoggingService() {
        return serviceLocator.getLoggingService();
    }

    protected Image getImage() {
        return ForceImages.get(ForceImages.APEX_TITLE_IMAGE);
    }
}
