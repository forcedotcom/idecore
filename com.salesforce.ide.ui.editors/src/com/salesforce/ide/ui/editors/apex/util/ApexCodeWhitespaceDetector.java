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
package com.salesforce.ide.ui.editors.apex.util;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * A java aware white space detector.
 */
public class ApexCodeWhitespaceDetector implements IWhitespaceDetector {

    /*
     * (non-Javadoc) Method declared on IWhitespaceDetector
     */
    @Override
    public boolean isWhitespace(char character) {
        return Character.isWhitespace(character);
    }
}
