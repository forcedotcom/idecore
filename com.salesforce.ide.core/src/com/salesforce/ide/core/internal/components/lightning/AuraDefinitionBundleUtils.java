/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.internal.components.lightning;

import java.util.Set;

import org.eclipse.core.resources.IFolder;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.MarkerUtils;
import com.sforce.soap.metadata.DeployMessage;

import jersey.repackaged.com.google.common.collect.Sets;

/**
 * This class centralizes anything that needs to be done to handle AuraDefinitionBundle. AuraDefinition bundle has many
 * quirks that will be improved in subsequent releases. When those releases come around, we can slowly deprecate this
 * class. But, for now, centralizing it here makes it easier to find all the special handling that we are doing.
 * 
 * @author nchen
 *         
 */
public class AuraDefinitionBundleUtils {
    /**
     * Special case handling for error messages because AuraDefinitionBundle doesn't surface the file name or location.
     * We still need to create an error marker, but since we don't know where (or, even worse, which bundle) we can only
     * do it on the src/aura folder.
     * 
     * @author nchen
     *         
     */
    public final static class DeployErrorHandler {
        // Error messages could be duplicated
        Set<String> errorMessages = Sets.newHashSet();
        
        public static boolean shouldHandle(DeployMessage deployMessage) {
            String componentType = deployMessage.getComponentType();
            return Utils.isNotEmpty(componentType) && componentType.equals(Constants.AURA_DEFINITION_BUNDLE);
        }
        
        /*
         * Because I don't have a way to find out what markers are associated with which resources/bundle, I can't just clear
         * the errors for that resource/bundle, so I have to clear it for that project on each deploy.
         */
        public static void clearAuraMarkers(IFolder srcFolder) {
            if (srcFolder != null) {
                IFolder auraFolder = srcFolder.getFolder(Constants.AURA);
                if (auraFolder.exists()) {
                    MarkerUtils.getInstance().clearAll(auraFolder);
                }
            }
        }
        
        public void applySaveErrorMarker(DeployMessage deployMessage, IFolder srcFolder) {
            String problem = deployMessage.getProblem();
            if (!errorMessages.contains(problem)) {
                errorMessages.add(problem);
                IFolder auraFolder = srcFolder.getFolder(Constants.AURA);
                if (auraFolder.exists()) {
                    MarkerUtils.getInstance().applySaveErrorMarker(auraFolder, problem);
                }
            }
        }
    }
}
