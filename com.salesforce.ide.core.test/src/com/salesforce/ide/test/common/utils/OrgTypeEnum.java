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

/**
 * Defines the different types of Orgs that we can test against.
 * @author ssasalatti
 *
 */
public enum OrgTypeEnum {

    Developer("DE"), //DE trial org
    Production("EE"), //EE Active? rename to ee active or something like that.
    Namespaced("DE"), //DE namespaced
    Enterprise("EE"), //EE trial org
    ALL(""), //TODO: do we really need this?
    Custom("");

    /**
     * The key is required so that production can set the right type or edition. It is hardcoded to use DE, EE etc. 
     */
    private final String OrgTypeKey;
    private OrgTypeEnum(final String orgTypeKey) {
        this.OrgTypeKey = orgTypeKey;
    }
    public String getOrgTypeKey() {
        return OrgTypeKey;
    }

}
