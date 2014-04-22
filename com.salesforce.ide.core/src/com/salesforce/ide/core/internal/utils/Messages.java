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
package com.salesforce.ide.core.internal.utils;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.salesforce.ide.core.internal.utils.Constants;

public class Messages {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Constants.RESOURCE_BUNDLE_ID);

    private Messages() {}

    public static ResourceBundle getResourceBundle() {
    	return RESOURCE_BUNDLE;
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getString(String key, Object[] values) {
        try {
        	return MessageFormat.format(getString(key), values);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
