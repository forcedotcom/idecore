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
package com.salesforce.ide.upgrade.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.project.BaseNature;
import com.salesforce.ide.core.project.InactiveNature;
import com.salesforce.ide.core.project.OnlineNature;

/**
 * Handles applying upgrade nature
 * 
 * @author chris
 */
public class UpgradeNature extends BaseNature {

    // This id is API, do not change without breaking compatibility with existing workspace projects
    public static final String NATURE_ID = Constants.FORCE_PLUGIN_PREFIX + ".nature.upgrade";

    @Override
    public void configure() throws CoreException {
        configure(UpgradeBuilder.BUILDER_ID);
    }

    @Override
    public void deconfigure() throws CoreException {
        deconfigure(UpgradeBuilder.BUILDER_ID);
    }

    public static boolean removeNature(IProject project, IProgressMonitor monitor) throws CoreException {
        removeNature(project, new String[] { NATURE_ID, InactiveNature.NATURE_ID }, monitor);
        OnlineNature.addNature(project, monitor);
        return true;
    }

    public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {
        OnlineNature.removeNature(project, monitor);
        addNature(project, new String[] { NATURE_ID, InactiveNature.NATURE_ID }, monitor);
    }
}
