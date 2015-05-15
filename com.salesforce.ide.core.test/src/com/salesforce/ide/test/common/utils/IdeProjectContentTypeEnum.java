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

public enum IdeProjectContentTypeEnum {
    /**
     * All would get stuff based on a default package manifest.
     */
    ALL,
    /**
     * Nothing.
     */
    NONE,
    /**
     * Single Package.
     */
    SINGLE_PACKAGE,
    /**
     * Specific Components
     */
    SPECIFIC_COMPONENTS,
    /**
     * Use only when you're adding data from filemetadata. It will have a package.xml, that will be used to create the
     * project too, so that you get only what you uploaded.
     */
    ONLY_WHAT_IS_BEING_UPLOADED;
}
