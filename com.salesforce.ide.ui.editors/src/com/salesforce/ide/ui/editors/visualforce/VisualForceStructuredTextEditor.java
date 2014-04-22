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
package com.salesforce.ide.ui.editors.visualforce;

import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.salesforce.ide.core.internal.utils.Constants;

public class VisualForceStructuredTextEditor extends StructuredTextEditor {

    public VisualForceStructuredTextEditor() {
        super();
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setHelpContextId(Constants.DOCUMENTATION_PLUGIN_PREFIX + "." + this.getClass().getSimpleName());
    }
}
