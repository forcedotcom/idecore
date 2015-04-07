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
package com.salesforce.ide.ui.sync;


public class SyncException extends Exception {

    private static final long serialVersionUID = 1L;

    public SyncException(Throwable pThrowable, String pDisplayMessage) {
        super(pDisplayMessage, pThrowable);
    }

    public SyncException(Throwable pThrowable) {
        super(pThrowable);
    }

    public SyncException(String pDisplayMessage) {
        super(pDisplayMessage);
    }
}
