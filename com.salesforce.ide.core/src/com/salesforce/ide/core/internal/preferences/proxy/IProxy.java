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
package com.salesforce.ide.core.internal.preferences.proxy;


public interface IProxy {

    //   M E T H O D S
    public long getLastUpdated();

    public void setLastUpdated(long lastUpdated);

    public boolean isProxiesEnabled();

    public void setProxiesEnabled(boolean proxiesEnabled);

    public String getProxyHost();

    public String getProxyPort();

    public String getProxyUser();

    public String getProxyPassword();

    public boolean hasNonProxyHost();

    public String getNonProxyHosts();

    public boolean isServerExcluded(String serverName);

}
