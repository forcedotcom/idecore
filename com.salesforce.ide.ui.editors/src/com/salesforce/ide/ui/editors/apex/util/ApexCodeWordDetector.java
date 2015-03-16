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

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A Java aware word detector.
 */
public class ApexCodeWordDetector implements IWordDetector {

    /*
     * (non-Javadoc) Method declared on IWordDetector.
     */
    @Override
    public boolean isWordPart(char character) {
        return Character.isJavaIdentifierPart(character);
    }

    /*
     * (non-Javadoc) Method declared on IWordDetector.
     */
    @Override
    public boolean isWordStart(char character) {
        return Character.isJavaIdentifierStart(character);
    }
}
