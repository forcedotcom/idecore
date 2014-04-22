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
package com.salesforce.ide.ui.internal.utils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Common access methods to Ifile/Iresource persistent properties
 * 
 */
public class ResourceProperties {

    private static final Logger logger = Logger.getLogger(ResourceProperties.class);

    private ResourceProperties() {
    // only statics
    }

    public static String getProperty(IResource res, QualifiedName propertyKey) {
        String property = null;
        try {
            property = res.getPersistentProperty(propertyKey);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.info("Unable to get property '" + propertyKey + "' value from resource '" + res.getName() + "': "
                    + logMessage);
        }
        return property;
    }

    public static void setProperty(IResource res, QualifiedName propertyKey, String value) {
        try {
            res.setPersistentProperty(propertyKey, value);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to set property '" + propertyKey + "' value from resource '" + res.getName() + "': "
                    + logMessage);
        }
    }

    public static String getBoolean(IResource res, QualifiedName propertyKey) {
        return Boolean.valueOf(ResourceProperties.getProperty(res, propertyKey)).toString();
    }

    public static void setBoolean(IResource res, QualifiedName qname, Boolean bool) {
        ResourceProperties.setProperty(res, qname, bool.toString());
    }

    public static void setLong(IResource res, QualifiedName qname, long l) {
        ResourceProperties.setProperty(res, qname, String.valueOf(l));
    }

}
