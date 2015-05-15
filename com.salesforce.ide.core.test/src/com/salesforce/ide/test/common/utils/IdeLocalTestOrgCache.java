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

import java.util.HashMap;

/**
 * concrete cache that is used in {@link IdeLocalTestOrgFixture}. This is a singleton.
 * @author ssasalatti
 */
public class IdeLocalTestOrgCache extends IdeOrgCache{
	 /**
     * Singleton
     */
    private static IdeLocalTestOrgCache instance = new IdeLocalTestOrgCache();

    public static IdeLocalTestOrgCache getInstance() {
        return instance;
    }

    private IdeLocalTestOrgCache() {
        orgCache = new HashMap<OrgTypeEnum, OrgInfo>();
        for (OrgTypeEnum ote : OrgTypeEnum.values()) {
            orgCache.put(ote, null);
        }

    }
}
