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

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

public class UpgradeStartup implements IStartup {

    private static final Logger logger = Logger.getLogger(UpgradeStartup.class);

    @Override
    public void earlyStartup() {
        if (logger.isDebugEnabled()) {
            logger.debug("Started upgrade plugin to inspect open Force.com projects");
        }

    }

}
