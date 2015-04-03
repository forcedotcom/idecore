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
package com.salesforce.ide.schemabrowser.ui;

import java.io.Serializable;
import java.util.Comparator;

import com.sforce.soap.partner.wsc.Field;

public class FieldComparator implements Comparator<Object>, Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /**
     * Compares two objects
     *
     * @return int
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Object obj1, Object obj2) {
        Field f1 = (Field) obj1;
        Field f2 = (Field) obj2;

        return f1.getName().compareTo(f2.getName());
    }
}
