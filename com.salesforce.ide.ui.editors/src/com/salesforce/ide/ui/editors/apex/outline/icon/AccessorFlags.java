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
package com.salesforce.ide.ui.editors.apex.outline.icon;

/**
 * This class is used so that we can compute both the JVM and SWT-style accessor flags at the same time for the
 * modifiers. Apparently, both set of flags represent slightly different things but they chose to use same bit locations
 * - so, sticking them into the same bitvector will clobber one another. Thus, we have to maintain two different
 * bitvectors.
 * 
 * @author nchen
 */
public final class AccessorFlags {
    public Integer accessorFlags_JVM = 0;
    public Integer accessorFlags_JDT = 0;

    public AccessorFlags() {
        this(0, 0);
    }

    public AccessorFlags(Integer accessorFlag_JVM, Integer accessorFlag_JDT) {
        this.accessorFlags_JDT = accessorFlag_JDT;
        this.accessorFlags_JVM = accessorFlag_JVM;
    }

}
