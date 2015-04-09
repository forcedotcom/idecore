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
package com.salesforce.ide.core.factories;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.ToolingStubExt;

/**
 * Encapsulates functionality related to creating, storing and removing Tooling API stubs per project.
 * 
 * @see MetadataFactory
 * @author nchen
 * 
 */
public class ToolingFactory extends BaseFactory {
    private static ConcurrentMap<ForceProject, ToolingStubExt> toolingStubs = new ConcurrentHashMap<>();

    public ToolingFactory() {
        super();
    }

    public ToolingStubExt getToolingStubExtInstance() {
        return new ToolingStubExt();
    }

    private ToolingStubExt getNewToolingStubExt(ForceProject forceProject) throws ForceConnectionException,
            ForceRemoteException {
        ToolingStubExt stub = getToolingStubExtInstance();
        Connection connection = getConnectionFactory().getConnection(forceProject);
        stub.initializeToolingConnection(connection);
        return stub;
    }

    public ToolingStubExt refreshToolingStubExt(Connection connection) throws ForceRemoteException {
        ForceProject project = connection.getForceProject();
        ToolingStubExt stub = toolingStubs.get(project);
        if (stub != null) {
            stub.initializeToolingConnection(connection);
        }
        return stub;
    }

    public boolean removeToolingStubExt(ForceProject project) {
        ToolingStubExt stub = toolingStubs.remove(project);
        return stub != null ? true : false;
    }

    public ToolingStubExt getToolingStubExt(Connection connection) throws ForceConnectionException,
            ForceRemoteException {
        return getToolingStubExt(connection.getForceProject());
    }

    public ToolingStubExt getToolingStubExt(ForceProject forceProject) throws ForceConnectionException,
            ForceRemoteException {
        ToolingStubExt stub = toolingStubs.get(forceProject);

        if (stub == null) {
            stub = getNewToolingStubExt(forceProject);
            cacheToolingStubExt(forceProject, stub);
        }
        return stub;
    }

    private static void cacheToolingStubExt(ForceProject forceProject, ToolingStubExt stub) {
        toolingStubs.put(forceProject, stub);
    }

    public void clearCache() {
        toolingStubs.clear();
    }
}
