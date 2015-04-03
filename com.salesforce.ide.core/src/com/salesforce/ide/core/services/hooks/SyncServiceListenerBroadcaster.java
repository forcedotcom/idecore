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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;

import com.salesforce.ide.core.model.ComponentList;

/**
 * Broadcasts the information to the interested listeners.
 * 
 * @author nchen
 * 
 */
public class SyncServiceListenerBroadcaster {
    private static final String SYNC_EXTENSION_ID = "com.salesforce.ide.core.syncServices";
    private static Logger logger = Logger.getLogger(SyncServiceListenerBroadcaster.class);

    public static void broadcast(ComponentList componentList) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry.getConfigurationElementsFor(SYNC_EXTENSION_ID);
        try {
            for (IConfigurationElement element : configurationElements) {
                Object executable = element.createExecutableExtension("class");
                if (executable instanceof ISyncServiceListener) {
                    executeExtension((ISyncServiceListener) executable, componentList);
                }
            }
        } catch (CoreException e) {
            logger.error("Error creating a contributed ISyncServiceListener", e);
        }

    }

    private static void executeExtension(final ISyncServiceListener executable, final ComponentList componentList) {
        ISafeRunnable runnable = new ISafeRunnable() {

            @Override
            public void run() throws Exception {
                executable.projectSync(componentList);
            }

            @Override
            public void handleException(Throwable exception) {
                logger.error("Error executing a contributed ISyncServiceListener", exception);
            }
        };
        SafeRunnable.run(runnable);
    }
}
