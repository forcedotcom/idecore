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
package com.salesforce.ide.ui.editors.apex;

import com.salesforce.ide.ui.editors.visualforce.VisualForceMultiPageEditor;

/**
*
* Handles opening Visualforce Page content and associated metadata.
*
* @author cwall
*/
public class ApexPageMultiPageEditor extends VisualForceMultiPageEditor {

    public ApexPageMultiPageEditor() {
        super();
    }

    //   M E T H O D S
    @Override
    protected String getEditorName() {
        return "Visualforce Pages Editor";
    }
}
