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

import com.salesforce.ide.core.model.Component;

public class ComponentVariantComparator implements IResourceVariantComparator {

    @Override
    public boolean compare(IResource file, IResourceVariant baseVariant) {
        final Component base = ((ComponentVariant) baseVariant).getComponent();
        return base.getOriginalBodyChecksum() == base.getBodyChecksum();
    }

    @Override
    public boolean compare(IResourceVariant baseVariant, IResourceVariant remoteVariant) {
        final Component base = ((ComponentVariant) baseVariant).getComponent();
        final Component remote = ((ComponentVariant) remoteVariant).getComponent();

        if (base == remote) return true;
        if (null == base || null == remote) return false;
        return base.getOriginalBodyChecksum() == remote.getBodyChecksum();
    }

    @Override
    public boolean isThreeWay() {
        return true;
    }

}
