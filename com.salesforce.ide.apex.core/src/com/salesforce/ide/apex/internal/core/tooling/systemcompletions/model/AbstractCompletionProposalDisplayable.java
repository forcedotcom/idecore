/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model;

/**
 * Implementors signal that they have a way to display themselves as a proposal string.
 * 
 * @author nchen
 * 
 */
public abstract class AbstractCompletionProposalDisplayable {
    public abstract String getReplacementString();

    public abstract String getDisplayString();

    public int cursorPosition() {
        return getReplacementString().length();
    }
}
