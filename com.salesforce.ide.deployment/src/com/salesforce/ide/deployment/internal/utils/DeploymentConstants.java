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
package com.salesforce.ide.deployment.internal.utils;

import com.salesforce.ide.core.internal.utils.Constants;

public interface DeploymentConstants {

    //  P L U G I N   &   C O N T R I B U T I O N   I D S
    String PLUGIN_NAME = "Force.com IDE Deployment";
    String PLUGIN_PREFIX = Constants.FORCE_PLUGIN_PREFIX + ".deployment";
    String INTERNAL_PLUGIN_PREFIX = PLUGIN_PREFIX + ".internal";
    String RESOURCE_BUNDLE_ID = INTERNAL_PLUGIN_PREFIX + ".utils.messages";

    //  P R O J E C T
    String LAST_DEPLOYMENT_USERNAME_SELECTED = "lastDeploymentUserSelected";
    String LAST_DEPLOYMENT_ENV_SELECTED = "lastDeploymentEnvSelected";
    String LAST_DEPLOYMENT_SERVER_SELECTED = "lastDeploymentServerSelected";
    String LAST_DEPLOYMENT_KEEP_ENDPOINT_SELECTED = "lastDeploymentKeepEndpointSelected";
    String LAST_DEPLOYMENT_PROTOCOL_SELECTED = "lastDeploymentProtocolSelected";
    String LAST_SOURCE_DEPLOYMENT_ARCHIVE_DIR_SELECTED = "lastSourceDeploymentArchiveDirSelected";
    String LAST_DEST_DEPLOYMENT_ARCHIVE_DIR_SELECTED = "lastDestDeploymentArchiveDirSelected";

}
