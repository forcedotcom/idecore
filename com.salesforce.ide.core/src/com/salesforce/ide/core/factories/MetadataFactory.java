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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.MetadataStubExt;

/**
 * Encapsulates functionality related to creating, storing, and removing Salesforce.com Metadata API stubs per project.
 * 
 * @author cwall
 */
public class MetadataFactory extends BaseFactory {

    private static final Logger logger = Logger.getLogger(MetadataFactory.class);

    private static ConcurrentMap<ForceProject, MetadataStubExt> metadataStubs =
            new ConcurrentHashMap<>();

    public MetadataFactory() {
        super();
    }

    /**
     * We rely on Spring to give us an instance of this MetadataFactory and provide a override for this method with a
     * lookup method injection.
     */
    public MetadataStubExt getMetadataStubExtInstance() {
        return new MetadataStubExt();
    }

    MetadataStubExt getNewMetadataStubExt(ForceProject forceProject) throws ForceConnectionException,
            ForceRemoteException {
        MetadataStubExt metadataStubExt = getMetadataStubExtInstance();
        Connection connection = getConnectionFactory().getConnection(forceProject);
        metadataStubExt.initializeMetadataConnection(connection);
        return metadataStubExt;
    }

    public MetadataStubExt refreshMetadataStubExt(Connection connection) throws ForceRemoteException {
        final ForceProject forceProject = connection.getForceProject();

        final MetadataStubExt metadataStubExt = metadataStubs.get(forceProject);
        if (metadataStubExt != null) {
            metadataStubExt.initializeMetadataConnection(connection);
        }

        return metadataStubExt;
    }

    public boolean removeMetadataStubExt(ForceProject forceProject) {
        if (forceProject == null) {
            return false;
        }

        MetadataStubExt obsoleteMetadataStubExt = metadataStubs.remove(forceProject);
        return obsoleteMetadataStubExt != null ? true : false;
    }

    public MetadataStubExt getMetadataStubExt(Connection connection) throws ForceConnectionException,
            ForceRemoteException {
        return getMetadataStubExt(connection.getForceProject());
    }

    public MetadataStubExt getMetadataStubExt(ForceProject forceProject) throws ForceConnectionException,
            ForceRemoteException {
        if (forceProject == null) {
            throw new IllegalArgumentException(" project must be specified to obtain a connection.");
        }

        if (logger.isDebugEnabled()) {
            logStoredMetadataStubExts();
            logger.debug("Checking cache for existing metadata stub for connection project:\n "
                    + forceProject.getLogDisplay() + "[" + forceProject.hashCode() + "]");
        }

        MetadataStubExt metadataStubExt = metadataStubs.get(forceProject);

        if (metadataStubExt == null) {
            metadataStubExt = getNewMetadataStubExt(forceProject);
            cacheMetadataStubExt(forceProject, metadataStubExt);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Got cached metadata stub");
            }
        }

        return metadataStubExt;
    }

    private static void cacheMetadataStubExt(ForceProject forceProject, MetadataStubExt metadataStubExt)
            throws ForceConnectionException {
        if (forceProject == null) {
            throw new ForceConnectionException(" project must be specific to get a connection.");
        }

        metadataStubs.put(forceProject, metadataStubExt);

        if (logger.isDebugEnabled()) {
            logger.debug("Add new metadata stub to cache:\n  " + metadataStubExt.getLogDisplay());
            logStoredMetadataStubExts();
        }
    }

    public void clearCache() {
        if (Utils.isEmpty(metadataStubs)) {
            logger.warn("No cached metadata stubs");
            return;
        }
        logger.warn("Clearing the following cached metadata stubs");
        logStoredMetadataStubExts();
        metadataStubs.clear();
    }

    public int getCacheSize() {
        return Utils.isEmpty(metadataStubs) ? 0 : metadataStubs.size();
    }

    public void dispose() {
        clearCache();
    }

    private static void logStoredMetadataStubExts() {
        if (metadataStubs == null || Utils.isEmpty(metadataStubs.keySet())) {
            logger.info("No cached metadata stubs");
            return;
        }

        Set<ForceProject> forceProjects = metadataStubs.keySet();
        StringBuffer strBuffer = new StringBuffer("Cached metadata stubs [" + metadataStubs.size() + "] are:");
        int metadataStubCnt = 0;
        for (ForceProject forceProject : forceProjects) {
            MetadataStubExt metadataStubExt = metadataStubs.get(forceProject);
            strBuffer.append("\n (").append(++metadataStubCnt).append(") ").append(forceProject.getLogDisplay())
                    .append(" [").append(forceProject.hashCode()).append("]");
            if (metadataStubExt != null) {
                strBuffer.append(", ").append(metadataStubExt.toStringLite());
            }
        }
        logger.info(strBuffer.toString());
    }
}
