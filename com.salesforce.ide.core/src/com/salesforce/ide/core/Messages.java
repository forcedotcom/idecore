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
package com.salesforce.ide.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author nchen
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.salesforce.ide.core.messages"; //$NON-NLS-1$
    public static String HTTPAdapter_GenericInvokeRequestError;
    public static String HTTPAdapter_NoResponseErrorMessage;
    public static String PromiseableJob_GenericError;
    
    public static String RemoveSubscriberCode_Title;
    public static String RemoveSubscriberCode_Question;
    public static String RemoveSubscriberCode_Later;
    public static String RemoveSubscriberCode_Now;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
