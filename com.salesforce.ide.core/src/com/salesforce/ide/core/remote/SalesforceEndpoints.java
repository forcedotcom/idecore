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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Manages Salesforce API endpoints.
 *
 * @author cwall
 *
 */
public class SalesforceEndpoints {

    private static final Logger logger = Logger.getLogger(SalesforceEndpoints.class);

    private String defaultEndpointLabel = null;
    private Map<String, String> defaultEndpointServers = null;
    private final TreeSet<String> userEndpointServers = new TreeSet<>();
    private String userEndpointsFilePath = null;
    private String endpointUrlSuffix = null;
    private String defaultApiVersion = null;
    private String defaultProtocol = null;

    //   C O N S T R U C T O R S
    public SalesforceEndpoints() {}

    public void init() {
        loadUserEndpointServers();
    }

    //   M E T H O D S
    public String getDefaultEndpointLabel() {
        return defaultEndpointLabel;
    }

    public void setDefaultEndpointLabel(String defaultEndpointLabel) {
        this.defaultEndpointLabel = defaultEndpointLabel;
    }

    public String getDefaultApiVersion() {
        return Utils.isNotEmpty(Utils.getDefaultSystemApiVersion()) ? Utils.getDefaultSystemApiVersion() : defaultApiVersion;
    }

    public void setDefaultApiVersion(String defaultApiVersion) {
        this.defaultApiVersion = defaultApiVersion;
    }

    public String getEndpointUrlSuffix() {
        return endpointUrlSuffix;
    }

    public void setEndpointUrlSuffix(String endpointUrlSuffix) {
        this.endpointUrlSuffix = endpointUrlSuffix;
    }

    public Map<String, String> getDefaultEndpointServers() {
        return defaultEndpointServers;
    }

    public void setDefaultEndpointServers(Map<String, String> defaultEndpointServers) {
        this.defaultEndpointServers = defaultEndpointServers;
    }

    public boolean isValidEndpointLabel(String endpointLabel) {
        return Utils.isEmpty(defaultEndpointServers) ? false : defaultEndpointServers.containsKey(endpointLabel);
    }

    public TreeSet<String> getUserEndpointServers() {
        return userEndpointServers;
    }

    public String getUserEndpointsFilePath() {
        return userEndpointsFilePath;
    }

    public void setUserEndpointsFilePath(String userEndpointsFilePath) {
        this.userEndpointsFilePath = System.getProperty("user.home") + File.separator + userEndpointsFilePath;
    }

    public String getDefaultProtocol() {
        return defaultProtocol;
    }

    public void setDefaultProtocol(String defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    public String getFullEndpointUrl(String endpointServer, boolean https) {
        String protocol = (https ? Constants.HTTPS : Constants.HTTP);
        return (new StringBuffer(protocol)).append("://").append(endpointServer).append("/").append(endpointUrlSuffix)
                .append("/").append(getDefaultApiVersion()).toString();
    }

    public String getEndpointServerForLabel(String label) {
        return Utils.isEmpty(label) || !defaultEndpointServers.containsKey(label) ? defaultEndpointServers.get(defaultEndpointLabel) : defaultEndpointServers.get(label);
    }

    public TreeSet<String> getAllEndpointServers() {
        TreeSet<String> endpointServers = new TreeSet<>();
        if (Utils.isNotEmpty(defaultEndpointServers)) {
            for (Object endpoint : defaultEndpointServers.values()) {
                endpointServers.add((String) endpoint);
            }
        }

        loadUserEndpointServers();

        if (Utils.isNotEmpty(userEndpointServers)) {
            for (String endpoint : userEndpointServers) {
                endpointServers.add(endpoint);
            }
        }
        return endpointServers;
    }

    public Set<String> getDefaultEndpointLabels() {
        return defaultEndpointServers.keySet();
    }

    public void loadUserEndpointServers() {
        try {
            File temp = new File(userEndpointsFilePath);
            if (temp.exists()) {
                try (final BufferedReader buffReader = new BufferedReader(new FileReader(userEndpointsFilePath))) {
                    loadEndpoints(buffReader);
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("Unable to load user endpoints from file '" + userEndpointsFilePath + "'", e);
        } catch (IOException e) {
            logger.warn("Unable to load user endpoints from file '" + userEndpointsFilePath + "'", e);
        }
    }

    private void loadEndpoints(BufferedReader buffReader) throws IOException {
        userEndpointServers.clear();

        String line = null;
        while ((line = buffReader.readLine()) != null) {
            if (Utils.isNotEmpty(line) && !line.startsWith("#")) {
                line = line.trim();
                if (logger.isDebugEnabled()) {
                    logger.debug("Got '" + line.trim() + "' as endpoint option");
                }
                userEndpointServers.add(line);
            }
        }
    }

    public void addUserEndpoint(String endpoint) {
        if (Utils.isEmpty(endpoint)) {
            return;
        }

        if (defaultEndpointServers.containsValue(endpoint)) {
            if (logger.isInfoEnabled()) {
                logger.info("Endpoint not added to user-provided list - default server found");
            }
            return;
        }

        try {
            boolean success = userEndpointServers.add(endpoint);
            if (!success) {
                return;
            }

            // write to file
            FileWriter writer = getWriter();
            if (writer == null) {
                logger.warn("Unable to write endpoint '" + endpoint + "' to file - user endpoint file not found");
                return;
            }

            try (final BufferedWriter out = new BufferedWriter(writer)) {
                out.write(endpoint + System.getProperty("line.separator"));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Added new user defined endpoint '" + endpoint + "' to file '" + userEndpointsFilePath
                        + "'");
            }
        } catch (Exception e) {
            logger.warn("Unable to add user defined endpoints to file '" + userEndpointsFilePath + "'");
        }
    }

    private FileWriter getWriter() {
        FileWriter writer = null;
        File tempUserEndpointFile = new File(userEndpointsFilePath);
        try {
            if (!tempUserEndpointFile.exists()) {
                if (!tempUserEndpointFile.getParentFile().exists()) {
                    tempUserEndpointFile.getParentFile().mkdirs();
                }
                boolean success = tempUserEndpointFile.createNewFile();
                if (logger.isInfoEnabled() && success) {
                    logger.info("Create new file '" + userEndpointsFilePath + "'");
                }
                writer = new FileWriter(userEndpointsFilePath);
            } else {
                writer = new FileWriter(userEndpointsFilePath, true);
            }
        } catch (Exception e) {
            logger.warn("Unable to get writer for '" + userEndpointsFilePath + "'");
        }
        return writer;
    }
}
