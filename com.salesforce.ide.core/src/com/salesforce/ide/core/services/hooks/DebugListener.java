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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Broadcasts the information to the interested listeners.
 * 
 * @author jwidjaja
 *
 */
public class DebugListener {

	private static final String DEBUG_EXTENSION_ID = "com.salesforce.ide.core.debugServices";
    private static Logger logger = Logger.getLogger(DebugListener.class);

    /**
     * Return whether or not the project has an active debugging session
     */
    public static boolean isDebugging(IProject project) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry.getConfigurationElementsFor(DEBUG_EXTENSION_ID);
        
        if (configurationElements.length > 1) {
        	logger.warn("Found multiple IDebugBroadcasters");
        }
        
        try {
            for (IConfigurationElement element : configurationElements) {
                Object executable = element.createExecutableExtension("class");
                if (executable instanceof IDebugBroadcaster) {
                	IDebugBroadcaster debugExecutable = (IDebugBroadcaster) executable;
                	return debugExecutable.isDebuggingActive(project);
                }
            }
        } catch (CoreException e) {
            logger.error("Error creating a contributed IDebugListener", e);
        }
        
        return false;
    }
}
