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

package com.salesforce.ide.core.remote;


/**
 * Common interface for all sends/receive to the server. By abstracting it at this level, we allow the possibility of
 * using different transports e.g., HTTP or Sockets.
 * 
 * @author nchen
 * 
 */
public interface IHTTPTransport<R> {
    /**
     * 
     * @return The response from the transport layer, as an object of the type parameter R.
     * @throws DebuggerConnectionException
     */
    public R receive() throws ForceConnectionException;

    /**
     * Tries to send the string representation of this object over the transport. Ensure that the data object has a
     * proper toString implementation. Ideally, we might want to enforce that the data object implements serializable
     * but that is too heavyweight for now.
     * 
     * @param data
     *            The data to send over.
     */
    public void send(Object data);

    /////////////////////////////////////
    // PERFORMANCE METRICS - IF AVAILABLE
    /////////////////////////////////////

    /**
     * Returns the total time to send and receive from the server
     */
    public long getRoundTripTime();

    /**
     * Returns a string representing the connection info. The different fields are separated by |. It's not possible to
     * specify a fixed format for different endpoints so be aware that this can be an arbitrary string.
     */
    public String getConnectionInfo();
}
