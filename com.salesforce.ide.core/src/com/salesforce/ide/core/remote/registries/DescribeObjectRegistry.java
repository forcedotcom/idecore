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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;

/**
 * 
 * @author cwall
 */
public class DescribeObjectRegistry extends BaseRegistry {
    private static final Logger logger = Logger.getLogger(DescribeObjectRegistry.class);

    private static ConcurrentMap<String, Hashtable<String, DescribeSObjectResult>> describeCaches =
            new ConcurrentHashMap<>();

    protected List<String> workflowableObjectNames = null;
    protected List<String> crtableObjectNames = null;
    protected Map<String, List<String>> excludedCrtFields = null;
    protected List<String> excludedTypes = null;
    protected Comparator<DescribeSObjectResult> describeSObjectResultComparator =
            new Comparator<DescribeSObjectResult>() {
                @Override
                public int compare(DescribeSObjectResult o1, DescribeSObjectResult o2) {
                    String s1 = o1.getName();
                    String s2 = o2.getName();
                    return s1.compareTo(s2);
                }
            };

    // C O N S T R U C T O R
    public DescribeObjectRegistry() {}

    // M E T H O D S
    public List<String> getWorkflowableObjectNames() {
        return workflowableObjectNames;
    }

    public void setWorkflowableObjectNames(List<String> workflowableObjectNames) {
        this.workflowableObjectNames = workflowableObjectNames;
    }

    public List<String> getCrtableObjectNames() {
        return crtableObjectNames;
    }

    public void setCrtableObjectNames(List<String> crtableObjectNames) {
        this.crtableObjectNames = crtableObjectNames;
    }

    public Map<String, List<String>> getExcludedCrtFields() {
        return excludedCrtFields;
    }

    public void setExcludedCrtFields(Map<String, List<String>> excludedCrtFields) {
        this.excludedCrtFields = excludedCrtFields;
    }

    public List<String> getExcludedTypes() {
        return excludedTypes;
    }

    public void setExcludedTypes(List<String> excludedTypes) {
        this.excludedTypes = excludedTypes;
    }

    // all types
    public SortedSet<String> getCachedGlobalDescribeTypes(IProject project) throws ForceConnectionException,
            ForceRemoteException {
        if (project == null || Utils.isEmpty(project.getName())) {
            return null;
        }

        Connection connection = connectionFactory.getConnection(project);
        return getCachedGlobalDescribeTypes(connection, project.getName());
    }

    public SortedSet<String> getCachedGlobalDescribeTypes(Connection connection, String projectName)
            throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName)) {
            return null;
        }

        SortedSet<String> types = null;
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isNotEmpty(describeCache)) {
            types = getSortedDescribeSObjectResult(describeCache.keySet());

            if (logger.isDebugEnabled()) {
                logger.debug("Got existing sobject types");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Initial fetch or refreshing sobjects");
            }
            describeCache = loadDescribeCaches(connection, projectName);
            if (Utils.isNotEmpty(describeCache)) {
                types = getSortedDescribeSObjectResult(describeCache.keySet());
            }
        }

        return types;
    }

    private static SortedSet<String> getSortedDescribeSObjectResult(Set<String> types) {
        return new TreeSet<>(types);
    }

    // custom objects
    public SortedSet<String> getCachedCustomDescribeTypes(IProject project, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null || Utils.isEmpty(project.getName())) {
            return null;
        }

        Connection connection = connectionFactory.getConnection(project);
        return getCachedCustomDescribeTypes(connection, project.getName(), refresh);
    }

    public SortedSet<String> getCachedCustomDescribeTypes(Connection connection, String projectName, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName)) {
            return null;
        }

        SortedSet<String> customTypes = new TreeSet<>();
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isEmpty(describeCache) || refresh) {
            describeCache = loadDescribeCaches(connection, projectName);
        }

        if (Utils.isEmpty(describeCache)) {
            logger.warn("No describe objects found in cache");
            return customTypes;
        }

        Collection<DescribeSObjectResult> describeObjects = describeCache.values();
        for (DescribeSObjectResult describeSObjectResult : describeObjects) {
            if (describeSObjectResult.isCustom()) {
                customTypes.add(describeSObjectResult.getName());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got [" + customTypes.size() + "] custom sobject types");
        }

        return customTypes;
    }

    // workflowable objects
    public SortedSet<String> getCachedWorkflowableDescribeTypes(IProject project, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null || Utils.isEmpty(project.getName())) {
            return null;
        }
        return getCachedWorkflowableDescribeTypes(connectionFactory.getConnection(project), project.getName(), refresh);
    }

    public SortedSet<String> getCachedWorkflowableDescribeTypes(Connection connection, String projectName,
            boolean refresh) throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName)) {
            return null;
        }

        SortedSet<String> workflowableTypes = new TreeSet<>();
        workflowableTypes.addAll(getWorkflowableObjectNames());
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isEmpty(describeCache) || refresh) {
            describeCache = loadDescribeCaches(connection, projectName);
        }

        if (Utils.isEmpty(describeCache)) {
            logger.warn("No describe objects found in cache - no workflowable types returned");
            return workflowableTypes;
        }

        Collection<DescribeSObjectResult> describeObjects = describeCache.values();
        for (DescribeSObjectResult describeSObjectResult : describeObjects) {
            final String name = describeSObjectResult.getName();
            if (describeSObjectResult.isCustom() && name.endsWith(Constants.CUSTOM_OBJECT_SUFFIX)) {
                workflowableTypes.add(name);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got [" + workflowableTypes.size() + "] workflowable sobject types");
        }

        return workflowableTypes;
    }

    protected Hashtable<String, DescribeSObjectResult> getDescribeCacheForProject(String projectName) {
        Hashtable<String, DescribeSObjectResult> describeCache = describeCaches.get(projectName);
        return describeCache;
    }

    /**
     * strip namespace for custom object due to naming convention difference between partner api and md api.
     */
    public SortedSet<String> getCachedWorkflowableDescribeTypes(IProject project, boolean refresh, String namespace)
            throws ForceConnectionException, ForceRemoteException {
        SortedSet<String> cachedWorkflowableDescribeTypes = getCachedWorkflowableDescribeTypes(project, refresh);

        if (Utils.isEmpty(namespace)) {
            return cachedWorkflowableDescribeTypes;
        }

        SortedSet<String> noNsWorkflowableDescribeTypes = new TreeSet<>();
        for (String cachedWorkflowableDescribeType : cachedWorkflowableDescribeTypes) {
            String fullNamespace = namespace + Constants.NAMESPACE_SEPARATOR;
            if (cachedWorkflowableDescribeType.startsWith(fullNamespace)) {
                String objectWithoutNamespace = Utils.stripNamespace(cachedWorkflowableDescribeType, namespace);
                noNsWorkflowableDescribeTypes.add(objectWithoutNamespace);
            } else {
                noNsWorkflowableDescribeTypes.add(cachedWorkflowableDescribeType);
            }
        }

        return noNsWorkflowableDescribeTypes;
    }

    // layout objects
    public SortedSet<String> getCachedLayoutableDescribeTypes(IProject project, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null || Utils.isEmpty(project.getName())) {
            return null;
        }

        Connection connection = connectionFactory.getConnection(project);
        return getCachedLayoutableDescribeTypes(connection, project.getName(), refresh);
    }

    public SortedSet<String> getCachedLayoutableDescribeTypes(Connection connection, String projectName, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName)) {
            return null;
        }

        SortedSet<String> layoutableTypes = new TreeSet<>();
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isEmpty(describeCache) || refresh) {
            describeCache = loadDescribeCaches(connection, projectName);
        }

        if (Utils.isEmpty(describeCache)) {
            logger.warn("No describe objects found in cache - no layoutable types returned");
            return layoutableTypes;
        }

        Collection<DescribeSObjectResult> describeObjects = describeCache.values();
        for (DescribeSObjectResult describeSObjectResult : describeObjects) {
            if (describeSObjectResult.isLayoutable()) {
                layoutableTypes.add(describeSObjectResult.getName());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got [" + layoutableTypes.size() + "] layoutable sobject types");
        }

        return layoutableTypes;
    }

    /**
     * strip namespace for custom object due to naming convention difference between partner api and md api.
     */
    public SortedSet<String> getCachedLayoutableDescribeTypes(IProject project, boolean refresh, String namespace)
            throws ForceConnectionException, ForceRemoteException {
        SortedSet<String> cachedLayoutableDescribeTypes = getCachedLayoutableDescribeTypes(project, refresh);

        if (Utils.isEmpty(namespace)) {
            return cachedLayoutableDescribeTypes;
        }

        SortedSet<String> noNsLayoutableDescribeTypes = new TreeSet<>();
        for (String cachedLayoutableDescribeType : cachedLayoutableDescribeTypes) {
            if (cachedLayoutableDescribeType.startsWith(namespace)) {
                String objectWithoutNamespace = Utils.stripNamespace(cachedLayoutableDescribeType, namespace);
                noNsLayoutableDescribeTypes.add(objectWithoutNamespace);
            } else {
                noNsLayoutableDescribeTypes.add(cachedLayoutableDescribeType);
            }
        }

        return noNsLayoutableDescribeTypes;
    }

    // placeholder for report type objects
    public SortedSet<String> getCachedReportTypePrimaryObjectPluralLabels(IProject project, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null || Utils.isEmpty(project.getName())) {
            return null;
        }

        Connection connection = connectionFactory.getConnection(project);
        return getCachedReportTypePrimaryObjectPluralLabels(connection, project.getName(), refresh);
    }

    public SortedSet<String> getCachedReportTypePrimaryObjectPluralLabels(Connection connection, String projectName,
            boolean refresh) throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName)) {
            return null;
        }

        // filter out crtableObject that describeSObject is not supported
        List<String> crtableObjectNamesInPlural =
                removeObjectNotSupportedByDescribeSObject(crtableObjectNames, connection, projectName);
        SortedSet<String> crtableTypePluralLabels = new TreeSet<>();
        crtableTypePluralLabels.addAll(crtableObjectNamesInPlural);
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isEmpty(describeCache) || refresh) {
            describeCache = loadDescribeCaches(connection, projectName);
        }

        if (Utils.isEmpty(describeCache)) {
            logger.warn("No describe objects found in cache - no crtable types returned");
            return crtableTypePluralLabels;
        }

        Collection<DescribeSObjectResult> describeObjects = describeCache.values();
        for (DescribeSObjectResult describeSObjectResult : describeObjects) {

            if (describeSObjectResult.isCustom()) {
                crtableTypePluralLabels.add(describeSObjectResult.getLabelPlural()); // add custom objects to the list
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got [" + crtableTypePluralLabels.size() + "] crtable sobject types");
        }

        return crtableTypePluralLabels;
    }

    private List<String> removeObjectNotSupportedByDescribeSObject(List<String> crtableObjectNames,
            Connection connection, String projectName) throws ForceConnectionException, ForceRemoteException {
        List<String> filteredCrtableObjectNamesPluralLabel = new ArrayList<>();
        SortedSet<String> cachedGlobalDescribeTypes = getCachedGlobalDescribeTypes(connection, projectName);
        for (String crtableObjectName : crtableObjectNames) {
            if (cachedGlobalDescribeTypes.contains(crtableObjectName)) {
                DescribeSObjectResult cachedSObject = getCachedDescribe(connection, projectName, crtableObjectName);
                filteredCrtableObjectNamesPluralLabel.add(cachedSObject.getLabelPlural());
            }
        }
        if (logger.isDebugEnabled() && Utils.isNotEmpty(filteredCrtableObjectNamesPluralLabel)) {
            logger.debug("Cross check CrtableObjectNamesList to see DescribeSObject supported. The list after filtered '"
                    + filteredCrtableObjectNamesPluralLabel + "'");
        }
        return filteredCrtableObjectNamesPluralLabel;
    }

    // trigger objects
    public SortedSet<String> getCachedTriggerableDescribeTypes(IProject project, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null || Utils.isEmpty(project.getName())) {
            return null;
        }

        Connection connection = connectionFactory.getConnection(project);
        return getCachedTriggerableDescribeTypes(connection, project.getName(), refresh);
    }

    public SortedSet<String> getCachedTriggerableDescribeTypes(Connection connection, String projectName,
            boolean refresh) throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName)) {
            return null;
        }

        TreeSet<String> triggerableTypes = new TreeSet<>();
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isEmpty(describeCache) || refresh) {
            describeCache = loadDescribeCaches(connection, projectName);
        }

        if (Utils.isEmpty(describeCache)) {
            logger.warn("No describe objects found in cache - no triggerable types returned");
            return triggerableTypes;
        }

        Collection<DescribeSObjectResult> describeObjects = describeCache.values();
        for (DescribeSObjectResult describeSObjectResult : describeObjects) {
            if (describeSObjectResult.isTriggerable()) {
                triggerableTypes.add(describeSObjectResult.getName());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got [" + triggerableTypes.size() + "] triggerable sobject types");
        }

        return triggerableTypes;
    }

    public DescribeSObjectResult getCachedDescribe(IProject project, String componentType)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null || Utils.isEmpty(componentType)) {
            return null;
        }

        Connection connection = connectionFactory.getConnection(project);
        return getCachedDescribe(connection, project.getName(), componentType);
    }

    public DescribeSObjectResult getCachedDescribe(Connection connection, String projectName, String componentType)
            throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName) || Utils.isEmpty(componentType)) {
            return null;
        }

        DescribeSObjectResult describeSObjectResult = null;
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isNotEmpty(describeCache)) {
            String type = componentType.toLowerCase();

            if (logger.isDebugEnabled()) {
                logger.debug("Got existing '" + componentType + "' sobject");
            }

            describeSObjectResult = describeCache.get(type);
            if (describeSObjectResult == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("'" + componentType + "' sobject not found.  Query Salesforce");
                }
                describeSObjectResult = connection.describeSObject(componentType);
                describeCache.put(type, describeSObjectResult);
            }
            describeSObjectResult = describeCache.get(type);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Initial fetch or refreshing sobjects");
            }
            describeCache = loadDescribeCaches(connection, projectName);
            if (Utils.isNotEmpty(describeCache)) {
                describeSObjectResult = describeCache.get(componentType);
            }
        }

        return describeSObjectResult;
    }

    public DescribeSObjectResult getCachedDescribeByPluralLabel(IProject project, String objectPluralLabel)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null || Utils.isEmpty(objectPluralLabel)) {
            return null;
        }
        Collection<DescribeSObjectResult> cachedDescribeSObjects = getCachedDescribeSObjects(project);
        for (DescribeSObjectResult cachedDescribeSObject : cachedDescribeSObjects) {
            if (cachedDescribeSObject.getLabelPlural().equalsIgnoreCase(objectPluralLabel)) {
                return cachedDescribeSObject;
            }
        }
        return null;
    }

    // This method will not load the sobjects if it has not already been loaded. Instead it can return null.
    // Useful in the case of ApexCodeScanner.java where it's better to proceed to load the editor first.
    public Collection<DescribeSObjectResult> getCachedDescribeSObjectResultsIfAny(IProject project) {
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(project.getName());
        if (Utils.isNotEmpty(describeCache)) {
            return describeCache.values();
        }
        return null;
    }

    public Collection<DescribeSObjectResult> getCachedDescribeSObjects(IProject project)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null) {
            return null;
        }

        Connection connection = connectionFactory.getConnection(project);
        return getCachedDescribeSObjects(connection, project.getName());
    }

    public Collection<DescribeSObjectResult> getCachedDescribeSObjects(Connection connection, String projectName)
            throws ForceConnectionException, ForceRemoteException {
        return getCachedDescribeSObjects(connection, projectName, false);
    }

    public Collection<DescribeSObjectResult> getCachedDescribeSObjects(Connection connection, String projectName,
            boolean refresh) throws ForceConnectionException, ForceRemoteException {
        Collection<DescribeSObjectResult> describeSObjectResults = null;
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (describeCache == null || refresh) {
            if (logger.isDebugEnabled()) {
                logger.debug("Initial fetch or refreshing sobjects");
            }
            describeCache = loadDescribeCaches(connection, projectName);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Got existing sobjects");
            }
        }

        if (Utils.isNotEmpty(describeCache)) {
            describeSObjectResults = describeCache.values();
        }
        return describeSObjectResults;
    }

    public SortedSet<String> getCachedDescribeSObjectNames(Connection connection, String projectName, boolean refresh)
            throws ForceConnectionException, ForceRemoteException {
        SortedSet<String> describeSObjectNames = new TreeSet<>();

        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (describeCache == null || refresh) {
            describeCache = loadDescribeCaches(connection, projectName);
        }

        if (Utils.isNotEmpty(describeCache)) {
            Collection<DescribeSObjectResult> describeSObjectResults = describeCache.values();
            for (DescribeSObjectResult describeSObjectResult : describeSObjectResults) {
                describeSObjectNames.add(describeSObjectResult.getName());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Got existing sobjects");
            }
        }
        return describeSObjectNames;
    }

    public DescribeSObjectResult getCachedDescribeSObjectByApiName(String projectName, String apiName) {
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isNotEmpty(describeCache)) {
            Collection<DescribeSObjectResult> describeSObjectResults = describeCache.values();
            for (DescribeSObjectResult describeSObjectResult : describeSObjectResults) {
                String temp = describeSObjectResult.getName();
                if (Utils.isNotEmpty(apiName) && temp.contains(apiName)) {
                    return describeSObjectResult;
                }

            }
            if (logger.isDebugEnabled()) {
                logger.debug("Got existing sobjects");
            }
        }
        return null;
    }

    private Hashtable<String, DescribeSObjectResult> loadDescribeCaches(IProject project)
            throws ForceConnectionException, ForceRemoteException {
        if (project == null) {
            return null;
        }
        Connection connection = connectionFactory.getConnection(project);
        return loadDescribeCaches(connection, project.getName());
    }

    protected Hashtable<String, DescribeSObjectResult> loadDescribeCaches(Connection connection, String projectName)
            throws ForceConnectionException, ForceRemoteException {
        if (Utils.isEmpty(projectName) || connection == null) {
            logger.warn("Unable to load sobjects - project name and/or connection is null or empty");
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Load sobjects for project " + projectName);
        }

        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isNotEmpty(describeCache)) {
            describeCaches.remove(projectName);
        }

        String[] types = connection.retrieveTypes();
        if (Utils.isNotEmpty(types)) {
            DescribeSObjectResult[] describeSObjectResults = connection.describeSObjects(types, false);

            if (Utils.isNotEmpty(describeSObjectResults)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got [" + describeSObjectResults.length + "] sobjects");
                }

                Arrays.sort(describeSObjectResults, describeSObjectResultComparator);

                describeCache = new Hashtable<>();
                for (DescribeSObjectResult describeSObjectResult : describeSObjectResults) {
                    if (excludedTypes.contains(describeSObjectResult.getName())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Excluding type '" + describeSObjectResult.getName() + "' from describe cache");
                        }
                        continue;
                    }
                    describeCache.put(describeSObjectResult.getName(), describeSObjectResult);
                }
                describeCaches.put(projectName, describeCache);
            }
        }

        logDescribeCache(projectName);

        return describeCache;
    }

    public Hashtable<String, DescribeSObjectResult> refresh(IProject project) throws ForceConnectionException,
            ForceRemoteException {
        return loadDescribeCaches(project);
    }

    public Hashtable<String, DescribeSObjectResult> remove(String projectName) {
        return describeCaches.remove(projectName);
    }

    public DescribeSObjectResult removeObject(String projectName, String objectName) {
        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);

        if (Utils.isNotEmpty(describeCache)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempt to remove '" + objectName + "' from '" + projectName + "' project's cache");
            }
            return describeCache.remove(objectName);
        }

        return null;
    }

    public void clearCache() {
        if (Utils.isEmpty(describeCaches)) {
            logger.warn("No cached describes");
            return;
        }
        logger.warn("Clearing cached describes");
        describeCaches.clear();
    }

    public int getCacheSize() {
        return Utils.isEmpty(describeCaches) ? 0 : describeCaches.size();
    }

    public void dispose() {
        clearCache();
    }

    private void logDescribeCache(String projectName) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        Hashtable<String, DescribeSObjectResult> describeCache = getDescribeCacheForProject(projectName);
        if (Utils.isEmpty(describeCache)) {
            logger.debug("No cached describe objects");
            return;
        }

        TreeSet<DescribeSObjectResult> describeSObjectResults =
                new TreeSet<>(describeSObjectResultComparator);
        describeSObjectResults.addAll(describeCache.values());
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("Cached describe objects [" + describeSObjectResults.size() + "] are:");
        int describeCnt = 0;
        for (DescribeSObjectResult describeSObjectResult : describeSObjectResults) {
            strBuffer.append("\n (").append(++describeCnt).append(") ").append(describeSObjectResult.getName())
                    .append(", custom object = ").append(describeSObjectResult.isCustom()).append(", triggerable = ")
                    .append(describeSObjectResult.isTriggerable()).append(", layoutable = ")
                    .append(describeSObjectResult.isLayoutable()).append(", workflowable = ").append("n/a");
        }
        logger.debug(strBuffer.toString());
    }

}
