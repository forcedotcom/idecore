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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

/**
 * Provides syncinfo with force.com
 *
 */
public class ComponentSyncInfo extends SyncInfo {

    public ComponentSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote,
            IResourceVariantComparator comparator) {
        super(local, base, remote, comparator);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.team.core.subscribers.SyncInfo#calculateKind(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected int calculateKind() throws TeamException {
        return super.calculateKind();
    }
    
    @Override
    public String getLocalContentIdentifier() {
    	if (getBase() != null && ((ComponentVariant) getBase()).getComponent() != null) {
    		return String.valueOf(((ComponentVariant) getBase()).getComponent().getBodyChecksum());
    	}
		return null;
	}
}
