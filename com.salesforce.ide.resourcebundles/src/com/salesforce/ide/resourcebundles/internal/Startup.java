package com.salesforce.ide.resourcebundles.internal;

import org.eclipse.ui.IStartup;

import com.salesforce.ide.resourcebundles.Activator;

/**
 * Used to activate the containing plug-in on workspace startup so the resource change listener is setup.
 */
public class Startup implements IStartup {

    @Override
    public void earlyStartup() {
        Activator.getDefault();
    }
}
