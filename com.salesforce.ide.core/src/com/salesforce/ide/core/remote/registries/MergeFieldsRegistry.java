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
package com.salesforce.ide.core.remote.registries;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.SoqlEnum;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.sforce.soap.partner.sobject.wsc.SObject;

/**
 *
 *
 * @author cwall
 */
public class MergeFieldsRegistry extends BaseRegistry {
    private static final Logger logger = Logger.getLogger(MergeFieldsRegistry.class);

    private static final String SNIPPET_LIST = "snippetList";
    private static final String SCONTROL_LIST = "scontrolList";
    private static final String CUSTOM_OBJECT_TYPES = "customObjectTypes";
    private static final String CUSTOM_OBJECT_ACTIONS = "customObjectActions";

    private DescribeObjectRegistry describeObjectRegistry = null;
    private Hashtable<String, Hashtable<String, String>> standardMergeFields =
            new Hashtable<>();
    private Hashtable<String, Hashtable<String, Hashtable<String, String>>> projectMergeFields =
            new Hashtable<>();

    private String userRoleKey = "userRole";
    private String profileKey = "profile";
    private String standardObjectActionsKey = "standardObjectActions";
    private String standardObjectTypesKey = "standardObjectTypes";
    private String organizationKey = "organization";
    private String userKey = "user";

    //   C O N S T R U C T O R S
    public MergeFieldsRegistry() {}

    //   M E T H O D S
    public void setStandardMergeFields(Hashtable<String, Hashtable<String, String>> standardMergeFields) {
        this.standardMergeFields = standardMergeFields;
    }

    public Hashtable<String, Hashtable<String, String>> getStandardMergeFields() {
        return standardMergeFields;
    }

    public DescribeObjectRegistry getDescribeObjectRegistry() {
        return describeObjectRegistry;
    }

    public void setDescribeObjectRegistry(DescribeObjectRegistry describeObjectRegistry) {
        this.describeObjectRegistry = describeObjectRegistry;
    }

    public Hashtable<String, Hashtable<String, Hashtable<String, String>>> getProjectMergeFields() {
        return projectMergeFields;
    }

    public void setProjectMergeFields(Hashtable<String, Hashtable<String, Hashtable<String, String>>> projectMergeFields) {
        this.projectMergeFields = projectMergeFields;
    }

    public String getUserRoleKey() {
        return userRoleKey;
    }

    public void setUserRoleKey(String userRoleKey) {
        this.userRoleKey = userRoleKey;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public String getStandardObjectActionsKey() {
        return standardObjectActionsKey;
    }

    public void setStandardObjectActionsKey(String standardObjectActionsKey) {
        this.standardObjectActionsKey = standardObjectActionsKey;
    }

    public String getStandardObjectTypesKey() {
        return standardObjectTypesKey;
    }

    public void setStandardObjectTypesKey(String standardObjectTypesKey) {
        this.standardObjectTypesKey = standardObjectTypesKey;
    }

    public String getOrganizationKey() {
        return organizationKey;
    }

    public void setOrganizationKey(String organizationKey) {
        this.organizationKey = organizationKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public Hashtable<String, String> getStandardMergeFieldsByType(String type) {
        if (Utils.isEmpty(type)) {
            throw new IllegalArgumentException("Merge field standard type cannot be null");
        }

        return standardMergeFields.get(type);
    }

    // standard object types
    public Hashtable<String, String> getOrganizationTypes() {
        return standardMergeFields.get(getOrganizationKey());
    }

    public Hashtable<String, String> getUserTypes() {
        return standardMergeFields.get(getUserKey());
    }

    public Hashtable<String, String> getProfileTypes() {
        return standardMergeFields.get(getProfileKey());
    }

    public Hashtable<String, String> getUserRoleTypes() {
        return standardMergeFields.get(getUserRoleKey());
    }

    public Hashtable<String, String> getStandardObjectTypes() {
        return standardMergeFields.get(getStandardObjectTypesKey());
    }

    public Hashtable<String, String> getStandardObjectActions() {
        return standardMergeFields.get(getStandardObjectActionsKey());
    }

    // custom objects types
    public Hashtable<String, String> getAllObjectTypes(IProject project) throws ForceRemoteException {
        Hashtable<String, String> tempTable = new Hashtable<>();
        Hashtable<String, String> standardObjectTypes = getStandardObjectTypes();
        Hashtable<String, String> customObjectTypes = getCustomObjectTypes(project);

        if (Utils.isNotEmpty(standardObjectTypes)) {
            tempTable.putAll(standardObjectTypes);
        }

        if (Utils.isNotEmpty(customObjectTypes)) {
            tempTable.putAll(customObjectTypes);
        }
        return tempTable;
    }

    public Hashtable<String, String> getCustomObjectTypes(IProject project) throws ForceRemoteException {
        Hashtable<String, String> customObjectTypes = getCustomObjects(project, CUSTOM_OBJECT_TYPES);
        if (Utils.isEmpty(customObjectTypes)) {
            try {
                customObjectTypes = loadCustomObjectTypes(project);
            } catch (ForceConnectionException e) {
                logger.warn("Unable to load custom object types: " + e.getMessage());
                return null;
            }

            if (Utils.isNotEmpty(customObjectTypes)) {
                Hashtable<String, Hashtable<String, String>> tempTable =
                        new Hashtable<>();
                tempTable.put(CUSTOM_OBJECT_TYPES, customObjectTypes);
                projectMergeFields.put(project.getName(), tempTable);
            } else {
                logger.warn("No custom objects found for project '" + project.getName() + "'");
            }
        }
        return customObjectTypes;
    }

    private Hashtable<String, String> loadCustomObjectTypes(IProject project) throws ForceConnectionException,
            ForceRemoteException {
        if (project == null) {
            logger.warn("Unable to load custom objects - project is null");
            return null;
        }

        Set<String> objectTypes = describeObjectRegistry.getCachedCustomDescribeTypes(project, false);
        if (Utils.isEmpty(objectTypes)) {
            logger.warn("Unable to load custom objects - types is null or empty for project " + project.getName());
            return null;
        }

        Hashtable<String, String> customObjectTypes = new Hashtable<>(objectTypes.size());
        for (String objectType : objectTypes) {
            customObjectTypes.put(objectType, "$ObjectType." + objectType);
        }

        return customObjectTypes;
    }

    public void setCustomObjectTypes(IProject project, Hashtable<String, String> customObjectTypes) {
        setCustomObjects(project, CUSTOM_OBJECT_TYPES, customObjectTypes);
    }

    // custom object actions
    public Hashtable<String, String> getAllObjectActions(IProject project) throws ForceRemoteException {
        Hashtable<String, String> tempTable = new Hashtable<>();
        Hashtable<String, String> standardObjectActions = standardMergeFields.get(getStandardObjectActionsKey());
        Hashtable<String, String> customObjectActions = getCustomObjectActions(project);

        if (Utils.isNotEmpty(standardObjectActions)) {
            tempTable.putAll(standardObjectActions);
        }

        if (Utils.isNotEmpty(customObjectActions)) {
            tempTable.putAll(customObjectActions);
        }
        return tempTable;
    }

    public Hashtable<String, String> getCustomObjectActions(IProject project) throws ForceRemoteException {
        Hashtable<String, String> customObjectActions = getCustomObjects(project, CUSTOM_OBJECT_ACTIONS);
        if (Utils.isEmpty(customObjectActions)) {
            try {
                customObjectActions = loadCustomObjectActions(project);
            } catch (ForceConnectionException e) {
                logger.error("Unable to load custom object actions: " + e.getMessage());
                return null;
            }

            if (Utils.isNotEmpty(customObjectActions)) {
                Hashtable<String, Hashtable<String, String>> tempTable =
                        new Hashtable<>();
                tempTable.put(CUSTOM_OBJECT_ACTIONS, customObjectActions);
                projectMergeFields.put(project.getName(), tempTable);
            } else {
                logger.warn("No custom object actions found for project '" + project.getName() + "'");
            }
        }
        return customObjectActions;
    }

    private Hashtable<String, String> loadCustomObjectActions(IProject project) throws ForceConnectionException,
            ForceRemoteException {
        if (project == null) {
            logger.warn("Unable to load custom actions - project is null");
            return null;
        }

        Set<String> objectTypes = describeObjectRegistry.getCachedGlobalDescribeTypes(project);
        if (Utils.isEmpty(objectTypes)) {
            logger.warn("Unable to load custom objects - types is null or empty for project " + project.getName());
            return null;
        }

        Hashtable<String, String> customObjectActions = new Hashtable<>(objectTypes.size());
        for (String objectType : objectTypes) {
            if (objectType.endsWith(Constants.CUSTOM_OBJECT_SUFFIX)) {
                customObjectActions.put(objectType + ".ChangeOwner", "$Action." + objectType + ".ChangeOwner");
                customObjectActions.put(objectType + ".Clone", "$Action." + objectType + ".Clone");
                customObjectActions.put(objectType + ".Delete", "$Action." + objectType + ".Delete");
                customObjectActions.put(objectType + ".Edit", "$Action." + objectType + ".Edit");
                customObjectActions.put(objectType + ".New", "$Action." + objectType + ".New");
                customObjectActions.put(objectType + ".Share", "$Action." + objectType + ".Share");
                customObjectActions.put(objectType + ".View", "$Action." + objectType + ".View");
            }
        }

        return customObjectActions;
    }

    public void setCustomObjectActions(IProject project, Hashtable<String, String> customObjectActions) {
        setCustomObjects(project, CUSTOM_OBJECT_ACTIONS, customObjectActions);
    }

    // scontrols
    public Hashtable<String, String> getSControlList(IProject project) throws ForceRemoteException {
        Hashtable<String, String> scontrols = getCustomObjects(project, SCONTROL_LIST);
        if (Utils.isEmpty(scontrols)) {
            try {
                scontrols = loadScontrols(project);
            } catch (ForceConnectionException e) {
                logger.error("Unable to load s-controls: " + e.getMessage());
                return null;
            }

            if (Utils.isNotEmpty(scontrols)) {
                Hashtable<String, Hashtable<String, String>> tempTable =
                        new Hashtable<>();
                tempTable.put(SCONTROL_LIST, scontrols != null ? scontrols : new Hashtable<String, String>());
                projectMergeFields.put(project.getName(), tempTable);
            } else {
                logger.warn("No scontrols found for project '" + project.getName() + "'");
            }
        }
        return scontrols;
    }

    private Hashtable<String, String> loadScontrols(IProject project) throws ForceConnectionException,
            ForceRemoteException {
        if (project == null) {
            logger.warn("Unable to load s-controls - project is null");
            return null;
        }

        Connection connection = getConnectionFactory().getConnection(project);
        SObject[] sobjects = connection.query(SoqlEnum.getScontrolsByContentSource("HTML")).getRecords();

        if (Utils.isEmpty(sobjects)) {
            logger.warn("Unable to load s-controls - returned s-controls of type 'HTML' is null or empty");
            return null;
        }

        Arrays.sort(sobjects, new Comparator<SObject>() {
            @Override
            public int compare(SObject o1, SObject o2) {
                String name1 = (String) (o1).getField("Name");
                String name2 = (String) (o2).getField("Name");
                return name1.compareTo(name2);
            }
        });

        Hashtable<String, String> scontrols = new Hashtable<>(sobjects.length);
        for (SObject sobject : sobjects) {
            String name = (String) sobject.getField("Name");
            String dName = (String) sobject.getField("DeveloperName");
            scontrols.put(name + " [" + dName + "]", "$SControl." + dName);
        }

        return scontrols;
    }

    public void setSControlList(IProject project, Hashtable<String, String> scontrolList) {
        setCustomObjects(project, SCONTROL_LIST, scontrolList);
    }

    // snippets list
    public Hashtable<String, String> getSnippets(IProject project) throws ForceRemoteException {
        Hashtable<String, String> snippets = getCustomObjects(project, SNIPPET_LIST);
        if (Utils.isEmpty(snippets)) {
            try {
                snippets = loadSnippets(project);
            } catch (ForceConnectionException e) {
                logger.error("Unable to load snippets: " + e.getMessage());
                return null;
            }

            if (Utils.isNotEmpty(snippets)) {
                Hashtable<String, Hashtable<String, String>> tempTable =
                        new Hashtable<>();
                tempTable.put(SNIPPET_LIST, snippets != null ? snippets : new Hashtable<String, String>());
                projectMergeFields.put(project.getName(), tempTable);
            } else {
                logger.warn("No snippets found for project '" + project.getName() + "'");
            }
        }
        return snippets;
    }

    private Hashtable<String, String> loadSnippets(IProject project) throws ForceConnectionException,
            ForceRemoteException {
        if (project == null) {
            logger.warn("Unable to load snippets - project is null");
            return null;
        }

        Connection connection = getConnectionFactory().getConnection(project);
        SObject[] scontrols = connection.query(SoqlEnum.getScontrolsByContentSource("Snippet")).getRecords();

        if (Utils.isEmpty(scontrols)) {
            logger.warn("Unable to load snippets - returned scontrols is null or empty");
            return null;
        }

        Arrays.sort(scontrols, new Comparator<SObject>() {
            @Override
            public int compare(SObject o1, SObject o2) {
                String name1 = (String) (o1).getField("Name");
                String name2 = (String) (o2).getField("Name");
                return name1.compareTo(name2);
            }
        });

        Hashtable<String, String> snippets = new Hashtable<>(scontrols.length);
        for (SObject element : scontrols) {
            if (((String) element.getField("ContentSource")).equals("Snippet")) {
                String name = (String) element.getField("Name");
                String dName = (String) element.getField("DeveloperName");
                snippets.put(name + " [" + dName + "]", "$SControl." + dName);
            }
        }

        return snippets;
    }

    public void setSnippets(IProject project, Hashtable<String, String> scontrolList) {
        setCustomObjects(project, SNIPPET_LIST, scontrolList);
    }

    // helpers
    private Hashtable<String, String> getCustomObjects(IProject project, String cacheType) {
        if (project == null || Utils.isEmpty(cacheType)) {
            logger.warn("Unable to get custom objects - project and/or type is null");
            return null;
        }
        Hashtable<String, String> objectTypes = null;
        Hashtable<String, Hashtable<String, String>> projectMergeFieldsCache =
                projectMergeFields.get(project.getName());
        if (projectMergeFieldsCache != null) {
            objectTypes = projectMergeFieldsCache.get(cacheType);
            if (logger.isDebugEnabled()) {
                logger.debug("Found " + cacheType + " merge field cache for project '" + project.getName());
            }
        }
        return objectTypes;
    }

    private void setCustomObjects(IProject project, String type, Hashtable<String, String> customObjects) {
        if (project == null || Utils.isEmpty(type) || Utils.isEmpty(customObjects)) {
            logger.warn("Unable to set custom objects - project, type, and/or objects is null");
            return;
        }

        Hashtable<String, Hashtable<String, String>> projectMergeFieldsCache =
                projectMergeFields.get(project.getName());
        if (Utils.isEmpty(projectMergeFieldsCache)) {
            projectMergeFields.put(project.getName(), new Hashtable<String, Hashtable<String, String>>());
            if (logger.isDebugEnabled()) {
                logger.debug("Added empty " + type + " to project '" + project.getName() + "' merge field cache");
            }
        } else {
            projectMergeFieldsCache.put(type, customObjects);
            if (logger.isDebugEnabled()) {
                logger.debug("Added " + type + " to project '" + project.getName() + "' merge field cache");
            }
        }
    }
}
