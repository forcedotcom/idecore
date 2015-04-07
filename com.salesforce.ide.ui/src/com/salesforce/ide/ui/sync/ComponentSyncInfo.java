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
package com.salesforce.ide.ui.sync;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

import com.salesforce.ide.core.model.Component;

/**
 * Provides syncinfo with force.com
 */
class ComponentSyncInfo extends SyncInfo {

    ComponentSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote,
            IResourceVariantComparator comparator) {
        super(local, base, remote, comparator);
    }
    
    @Override
    public String getLocalContentIdentifier() {
    	final ComponentVariant base = (ComponentVariant) getBase();
        if (null != base) {
            final Component component = base.getComponent();
    		return null == component ? null : String.valueOf(component.getBodyChecksum());
    	}
		return null;
	}
}
