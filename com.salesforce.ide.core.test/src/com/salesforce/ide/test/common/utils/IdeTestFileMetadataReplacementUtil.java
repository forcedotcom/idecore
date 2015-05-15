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
package com.salesforce.ide.test.common.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.project.ForceProjectException;

/**
 *
 * This utility is used for replace pre-defined token with runtime value in test file metadata before deploy to server.
 * Usage: using add method to specify token, and replace value and component type it applies. then call checkAndReplace method to perform the replacement.
 *
 * @author fchang
 */
public class IdeTestFileMetadataReplacementUtil {
    private final Map<String, Map<String, String>> replacementComponentTypeMap = new HashMap<String, Map<String, String>>();

    /**
     * Specify the token and replaced value along with component type it applies.
     * @param componentType - the component type this replacement should perform on
     * @param replacementToken - the pre-defined replacement token
     * @param replacementString - the value would like to replace with
     */
    public void add(String componentType, String replacementToken, String replacementString) {
        if (IdeTestUtil.isNotEmpty(replacementComponentTypeMap.get(replacementToken))) {
            replacementComponentTypeMap.get(componentType).put(replacementToken, replacementString);
        } else {
            HashMap<String, String> replacementTokenMap = new HashMap<String, String>();
            replacementTokenMap.put(replacementToken, replacementString);
            replacementComponentTypeMap.put(componentType, replacementTokenMap);
        }
    }

    /**
     * Perform replacement on given directory recursively.
     * @param deployRootDir - root directory to perform replacement from.
     * @throws FactoryException
     * @throws ForceProjectException
     * @throws IdeTestException
     */
    public void checkAndReplace(File deployRootDir) throws IdeTestException {
        if (deployRootDir.isDirectory()) {
            File[] listFiles = deployRootDir.listFiles();
            for (File file : listFiles) {
                checkAndReplace(file);
            }
        }else{
            for (String replacementComponentType : replacementComponentTypeMap.keySet()) {
                Component replacementComponent=null;
                try {
                    replacementComponent = IdeTestUtil.getComponentFactory().getComponentByComponentType(replacementComponentType);
                } catch (ForceProjectException e) {
                    IdeTestException.wrapAndThrowException("ForceProject Exception When Trying to getComponent from component type.",e);
                }
                if (IdeTestUtil.isNotEmpty(replacementComponent) && replacementComponent.getDefaultFolder().equals(deployRootDir.getName())) {
                    Map<String, String> replacementTokenMap = replacementComponentTypeMap.get(replacementComponentType);
                    for (String token : replacementTokenMap.keySet()) {
                        IdeTestUtil.replaceFileContent(deployRootDir, token, replacementTokenMap.get(token));
                    }
                    return;
                }
            }
        }
    }
}
