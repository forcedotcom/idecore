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

import java.util.Date;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

import com.salesforce.ide.core.model.Component;

public class ComponentVariant implements IResourceVariant {

    private final Component component;
    private boolean remote = false;

    public ComponentVariant(Component component) {
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
        return (new Date(component.getFetchTime())).toString();
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    public Component getComponent() {
        return component;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    @Override
    public IStorage getStorage(IProgressMonitor monitor) throws TeamException {
        return new ComponentStorage(getComponent());
    }
}
