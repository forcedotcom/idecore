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
package com.salesforce.ide.ui.editors.internal.utils;

import com.salesforce.ide.ui.internal.utils.UIConstants;

public interface EditorConstants {

    //   P L U G I N   &   C O N T R I B U T I O N   I D S
    String PLUGIN_NAME = "Force.com IDE Editors";
    String PLUGIN_PREFIX = UIConstants.PLUGIN_PREFIX + ".editors";
    String INTERNAL_PLUGIN_PREFIX = PLUGIN_PREFIX + ".internal";
    String RESOURCE_BUNDLE_ID = INTERNAL_PLUGIN_PREFIX + ".utils.messages";
    String APPLICATION_CONTEXT = "/config/editor-application-context.xml";
    // editor
    String APEX_EDITOR_ID = PLUGIN_PREFIX + ".apex";
    String HTML_EDITOR_ID = PLUGIN_PREFIX + ".html";
}
