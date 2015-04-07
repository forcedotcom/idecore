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

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

import com.salesforce.ide.core.model.Component;

class ComponentVariant implements IResourceVariant {

    private final Component component;

    ComponentVariant(Component component) {
        this.component = component;
    }

    @Override
    public String getName() {
        return component.getName();
    }

    @Override
    public byte[] asBytes() {
        return getContentIdentifier().getBytes();
    }

    @Override
    public String getContentIdentifier() {
        return Long.toString(component.getBodyChecksum());
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    Component getComponent() {
        return component;
    }

    @Override
    public IStorage getStorage(IProgressMonitor monitor) throws TeamException {
        return new ComponentStorage(getComponent());
    }
}
