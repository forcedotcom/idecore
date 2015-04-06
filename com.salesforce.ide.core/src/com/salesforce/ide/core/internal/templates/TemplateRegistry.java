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
package com.salesforce.ide.core.internal.templates;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.springframework.core.io.ClassPathResource;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.FileReader;
import com.salesforce.ide.core.internal.utils.Utils;


public class TemplateRegistry {

    private static final Logger logger = Logger.getLogger(TemplateRegistry.class);

    public static final String DEFAULT_TEMPLATE_EXTENSION = "template";
    public static final String DEFAULT_TEMPLATE_DEPOT_LOCATION = "/templates";

    private String templateExtension = DEFAULT_TEMPLATE_EXTENSION;
    private String templateDepotLocation = DEFAULT_TEMPLATE_DEPOT_LOCATION;
    private int maxCustomTemplates = 1;
    private String customPrefix = "Custom";
    private final Map<String, Map<String, String>> templateRegistryMap = new HashMap<>();

    //   C O N S T R U C T O R
    public TemplateRegistry() {}

    //   M E T H O D S
    public Map<String, Map<String, String>> getTemplateRegistry() {
        return templateRegistryMap;
    }

    public String getTemplateExtension() {
        return templateExtension;
    }

    public void setTemplateExtension(String templateExtension) {
        this.templateExtension = templateExtension;
    }

    public String getTemplateDepotLocation() {
        return templateDepotLocation;
    }

    public void setTemplateDepotLocation(String templateDepotLocation) {
        this.templateDepotLocation = templateDepotLocation;
    }

    public String getCustomPrefix() {
        return customPrefix;
    }

    public void setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
    }

    public int getMaxCustomTemplates() {
        return maxCustomTemplates;
    }

    public void setMaxCustomTemplates(int maxCustomTemplates) {
        this.maxCustomTemplates = maxCustomTemplates;
    }

    public int count() {
        if (Utils.isEmpty(templateRegistryMap)) {
            return 0;
        }

        int count = 0;
        Collection<Map<String, String>> componentTemplateMaps = templateRegistryMap.values();
        for (Map<String, String> componentTemplateMap : componentTemplateMaps) {
            count += componentTemplateMap.size();
        }

        return count;
    }

    public int componentTemplateCount(String componentType) {
        Map<String, String> templates = getTemplatesByComponentType(componentType);
        return Utils.isNotEmpty(templates) ? templates.size() : 0;
    }

    // init
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing template registry");
            ForceIdeCorePlugin.getStopWatch().start("TemplateRegistry.init");
        }

        File templateDirectory = getTemplateDirectory(templateDepotLocation);
        File[] templates = getTemplates(templateDirectory);
        cacheTemplates(templates);

        if (logger.isDebugEnabled()) {
            ForceIdeCorePlugin.getStopWatch().stop("TemplateRegistry.init");
        }
    }

    public File getTemplateDirectory(String templateDepotLocation) {
        if (Utils.isEmpty(templateDepotLocation)) {
            return null;
        }

        String path = templateDepotLocation;
        URL resource = null;
        try {
            resource = (new ClassPathResource(path)).getURL();
            if (resource == null) {
                logger.warn("Unable to get classpath resource for path '" + path + "'");
                return null;
            }

            if (logger.isDebugEnabled()) {
                // testing will not have an initiated FileLocator
                logger.debug("Template resource path is " + resource.getPath());
            }

            try {
                path = FileLocator.toFileURL(resource).getPath();

                if (logger.isDebugEnabled()) {
                    // testing will not have an initiated FileLocator
                    logger.debug("Got path '" + path + "' from FileLocator");
                }

            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    // testing will not have an initiated FileLocator
                    logger.info("Could not get path from FileLocator");
                }
                path = resource.getFile();
            }

        } catch (IOException e) {
            logger.warn(e);
        }

        if (Utils.isEmpty(path)) {
            logger.warn("Unable to get root template resource");
            return null;
        }

        return new File(path);
    }

    public void cacheTemplates(File[] templates) {
        if (Utils.isEmpty(templates)) {
            if (logger.isInfoEnabled()) {
                logger.info("No templates found");
            }
            return;
        }

        for (File file : templates) {
            if (!file.getName().endsWith("." + templateExtension)) {
                continue;
            }

            // get component type from filename
            String componentType = null;
            if (file.getName().contains("-")) {
                componentType = file.getName().substring(0, file.getName().indexOf("-"));
            } else {
                componentType = file.getName().substring(0, file.getName().lastIndexOf("." + templateExtension));
            }

            if (Utils.isEmpty(componentType)) {
                logger.warn("Unable to add " + file.getAbsolutePath()
                    + " - unable to determine template's component type");
                continue;
            }

            // get template name from filename
            String templateName = null;
            if (file.getName().contains("-")) {
                templateName =
                    file.getName().substring(file.getName().indexOf("-") + 1,
                        file.getName().lastIndexOf("." + templateExtension));
            } else {
                // up to maxCustomTemplates chances to save custom template
                String tmpTemplateName = null;
                String tmpTemplate = null;
                for (int i = 0; i < maxCustomTemplates; i++) {
                    tmpTemplateName = customPrefix + (i == 0 ? "" : i);
                    tmpTemplate = getTemplate(componentType, tmpTemplateName);
                    if (Utils.isEmpty(tmpTemplate)) {
                        break;
                    }
                }

                if (Utils.isNotEmpty(tmpTemplate)) {
                    templateName = tmpTemplateName;
                }
            }

            if (null == templateName || 0 == templateName.length()) {
                logger.warn("Unable to add " + file.getAbsolutePath()
                    + " - unable to determine template name and/or max custom templates reached");
                continue;
            } else if (templateName.contains("_")) {
                templateName = templateName.replaceAll("_", " ");
            }

            String templateContent = FileReader.getTemplateContent(file);
            if (Utils.isNotEmpty(componentType) && Utils.isNotEmpty(templateName)
                    && Utils.isNotEmpty(templateContent)) {
                addTemplate(componentType, templateName, templateContent);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Added [" + count() + "] templates");
        }
    }

    public String getDefaultTemplate(String componentType) {
        return getTemplate(componentType, Constants.DEFAULT_TEMPLATE_NAME);
    }

    public String getTemplate(String componentType, String templateName) {
        if (Utils.isEmpty(templateRegistryMap) || Utils.isEmpty(componentType)
                || Utils.isEmpty(templateName)) {
            if (logger.isInfoEnabled()) {
                logger.info("No templates found");
            }
            return Constants.EMPTY_STRING;
        }

        Map<String, String> componentTemplates = templateRegistryMap.get(componentType);
        if (Utils.isEmpty(componentTemplates)) {
            if (logger.isInfoEnabled()) {
                logger.info("No templates found for component type '" + componentType + "'");
            }
            return Constants.EMPTY_STRING;
        }
		return componentTemplates.get(templateName);
    }

    public Map<String, String> getTemplatesByComponentType(String componentType) {
        if (Utils.isEmpty(templateRegistryMap) || Utils.isEmpty(componentType)) {
            if (logger.isDebugEnabled()) {
                logger.debug("No templates found");
            }
            return null;
        }

        return templateRegistryMap.get(componentType);
    }

    public boolean addTemplate(String componentType, String templateName, String templateContent) {
        if (Utils.isEmpty(componentType) || Utils.isEmpty(templateName)) {
            if (logger.isInfoEnabled()) {
                logger.info("No templates added - component type and/or template name not provided");
            }
            return false;
        }

        Map<String, String> componentTemplates = getTemplatesByComponentType(componentType);
        if (Utils.isEmpty(componentTemplates)) {
            componentTemplates = new HashMap<>();
            templateRegistryMap.put(componentType, componentTemplates);
        }

        componentTemplates.put(templateName, templateContent);

        if (logger.isDebugEnabled()) {
            logger.debug("Added new template, '" + templateName + "' [" + templateContent.length()
                + "], to component type '" + componentType + "'");
        }

        return true;
    }

    public File[] getTemplates(File directory) {
        if (directory == null) {
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Getting templates from directory " + directory.toString());
        }

        File[] templates = null;
        if (directory.exists()) {
            templates = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return Utils.isNotEmpty(name) && name.endsWith("." + templateExtension);
                }
            });
        } else {
            logger.warn("Directory '" + directory.getAbsolutePath() + "' does not exist");
        }

        return templates;
    }
}
