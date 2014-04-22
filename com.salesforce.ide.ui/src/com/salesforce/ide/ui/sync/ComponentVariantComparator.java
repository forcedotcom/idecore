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
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

public class ComponentVariantComparator implements IResourceVariantComparator {

    protected SyncController syncController = null;

    public ComponentVariantComparator(SyncController syncController) {
        super();
        this.syncController = syncController;
    }

    public boolean compare(IResource file, IResourceVariant baseVariant) {
        return syncController.compare(file, baseVariant);
    }

    public boolean compare(IResourceVariant baseVariant, IResourceVariant remoteVariant) {
        return syncController.compare(baseVariant, remoteVariant);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.eclipse.team.core.variants.IResourceVariantComparator#isThreeWay()
     */
    public boolean isThreeWay() {
        return true;
    }
}
