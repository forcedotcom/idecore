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
 * Concrete cache for remote orgs. This Cache is cleaned out for each test that uses orgs on a remote server. So,
 * technically its useless in terms of caching. However, each fixture contains its own singleton cache. Hence to
 * maintain generality and future enhancements, this exists. FWIW, the cache entry is set after the org is created(
 * rather retrieved in this remote org case ) and then this cache entry is used to set the orginfo for current test in
 * the idetestCase. This is a singleton.
 * 
 * @author ssasalatti
 */
public class IdeRemoteOrgCache extends IdeOrgCache {
    /**
     * Singleton
     */
    private static IdeRemoteOrgCache instance = new IdeRemoteOrgCache();

    public static IdeRemoteOrgCache getInstance() {
        return instance;
    }

    private IdeRemoteOrgCache() {
        orgCache = new HashMap<OrgTypeEnum, OrgInfo>();
        //using Developer org type as its the default option in the annotation. but it could really be any org type.
        orgCache.put(OrgTypeEnum.Developer, null);

    }
}
