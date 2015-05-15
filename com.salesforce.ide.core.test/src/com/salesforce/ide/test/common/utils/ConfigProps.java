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
package com.salesforce.ide.test.common.utils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;

public class ConfigProps {

    private static final Logger logger = Logger.getLogger(ConfigProps.class);

    private static final Map<String, ConfigProps> _instanceMap = new HashMap<String, ConfigProps>();

    private final Properties props = new Properties();

    private ConfigProps(String propertyFile) {
        loadProperties(propertyFile);
    }

    public static ConfigProps getInstance() {
        return getInstance(null);
    }

    public static ConfigProps getInstance(String propertyFile) {
        if (null == propertyFile)
            propertyFile = IdeTestConstants.TEST_PROPS_FILE;

        ConfigProps _instance = _instanceMap.get(propertyFile);
        if (_instance == null) {
            _instance = new ConfigProps(propertyFile);
            _instanceMap.put(propertyFile, _instance);
        }
        return _instance;
    }

    private void loadProperties() {
        loadProperties(IdeTestConstants.TEST_PROPS_FILE);

    }

    private void loadProperties(String file) {
        if (!props.isEmpty()) {
            return;
        }

        try {
            logger.info("Loading test configuration properies");
            URL testConfig = IdeTestUtil.getUrlEntry(file);
            if (testConfig != null) {
                testConfig = FileLocator.toFileURL(testConfig);
                props.load(testConfig.openStream());
                logger.debug("Loaded the following props:\n   " + props);
                logger.info("Test configuration loaded");
            } else {
                logger.error("Unable to load test configuration: url for file '" + file + "' is null");
            }

        } catch (Exception e) {
            logger.error("Unable to load properties from resource " + IdeTestConstants.TEST_PROPS_FILE, e);
        }
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public String getProperty(String key, boolean prefixReleaseHome) {
        if (props == null) {
            loadProperties();
        }

        if (props == null) {
            return null;
        }

        if (prefixReleaseHome) {
            String prop = props.getProperty(key);
            URL resource = IdeTestUtil.getUrlEntry(prop);
            if (resource != null) {
                try {
                    resource = FileLocator.toFileURL(resource);
                    prop = resource.getPath();
                } catch (IOException e) {
                    logger.error("Unable to get resource for '" + prop + "'");
                }
            }

            return prop;
        }
		return props.getProperty(key);
    }
}
