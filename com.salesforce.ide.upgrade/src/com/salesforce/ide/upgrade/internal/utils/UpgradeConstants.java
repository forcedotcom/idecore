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
package com.salesforce.ide.upgrade.internal.utils;

import com.salesforce.ide.core.internal.utils.Constants;

public interface UpgradeConstants {

    String PLUGIN_NAME = "Force.com IDE Upgrade";
    String PLUGIN_PREFIX = Constants.FORCE_PLUGIN_PREFIX + ".upgrade";
    String INTERNAL_PLUGIN_PREFIX = PLUGIN_PREFIX + ".internal";
    String RESOURCE_BUNDLE_ID = INTERNAL_PLUGIN_PREFIX + ".utils.messages";
}
