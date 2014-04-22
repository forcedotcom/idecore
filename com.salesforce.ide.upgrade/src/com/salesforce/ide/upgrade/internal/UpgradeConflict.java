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
package com.salesforce.ide.upgrade.internal;

import com.salesforce.ide.core.model.Component;

public class UpgradeConflict {

    private Component localComponent = null;
    private Component remoteComponent = null;

    public UpgradeConflict(Component localComponent, Component remoteComponent) {
        super();
        this.localComponent = localComponent;
        this.remoteComponent = remoteComponent;
    }

    public Component getLocalComponent() {
        return localComponent;
    }

    public void setLocalComponent(Component localComponent) {
        this.localComponent = localComponent;
    }

    public Component getRemoteComponent() {
        return remoteComponent;
    }

    public void setRemoteComponent(Component remoteComponent) {
        this.remoteComponent = remoteComponent;
    }

}
