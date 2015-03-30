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
package com.salesforce.ide.ui.views.executeanonymous;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.model.OrgModel;
import com.salesforce.ide.core.remote.apex.ExecuteAnonymousResultExt;

public class ExecuteAnonymousController extends Controller {

    public ExecuteAnonymousController() {
        super();
        model = new OrgModel();
    }

    public ExecuteAnonymousController(IProject project) {
        super();
        model = new OrgModel(project);
    }

    public ExecuteAnonymousResultExt executeExecuteAnonymous(String code) {
        return ContainerDelegate.getInstance().getServiceLocator().getApexService().executeAnonymous(code, getProject());
    }

    public List<IProject> getForceProjects() {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects();
    }

    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {}

    @Override
    public void init() {}

    @Override
    public void dispose() {}
}
