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
package com.salesforce.ide.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.Constants;

public class OnlineNature extends BaseNature {

    // This id is API, do not change without breaking compatibility with existing workspace projects
    public static final String NATURE_ID = Constants.FORCE_PLUGIN_PREFIX + ".nature.online";

    @Override
    public void configure() throws CoreException {
        configure(OnlineBuilder.BUILDER_ID);
    }

    @Override
    public void deconfigure() throws CoreException {
        deconfigure(OnlineBuilder.BUILDER_ID);
    }

    public static boolean removeNature(IProject currentProject, IProgressMonitor monitor) {
        return removeNature(currentProject, NATURE_ID, monitor);
    }

    public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {
        addNature(project, NATURE_ID, monitor);
    }
}
