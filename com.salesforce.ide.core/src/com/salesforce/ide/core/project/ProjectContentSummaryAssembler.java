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
package com.salesforce.ide.core.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.sforce.soap.metadata.FileProperties;

public class ProjectContentSummaryAssembler {
    private static final Logger logger = Logger.getLogger(ProjectContentSummaryAssembler.class);
    private static final String SUMMARY_INDENTION = "     ";
    private final FactoryLocator factoryLocator;
    private final ComponentValidator customObjectComponentValidator;
    private final ComponentValidator standardObjectComponentValidator;

    public ProjectContentSummaryAssembler(FactoryLocator factorLocator) {
        customObjectComponentValidator = new ComponentValidator() {
            @Override
            public boolean isValidMember(String componentName) {
                return CustomObjectNameResolver.getCheckerForCustomObject().check(componentName,
                    Constants.CUSTOM_OBJECT);
            }
        };

        standardObjectComponentValidator = new ComponentValidator() {
            @Override
            public boolean isValidMember(String componentName) {
                return CustomObjectNameResolver.getCheckerForStandardObject().check(componentName,
                    Constants.STANDARD_OBJECT);
            }
        };

        this.factoryLocator = factorLocator;

    }

    public ComponentValidator getStandardObjectComponentValidator() {
        return standardObjectComponentValidator;
    }

    public ProjectContentSummaryAssembler() {
        this(null);
    }

    public List<String> generateSummaryText(PackageManifestModel packageManifestModel) {
        return generateSummaryText(packageManifestModel, false);
    }

    public List<String> generateSummaryText(PackageManifestModel packageManifestModel, boolean augmentWithCache) {
        List<String> summaries = new ArrayList<>();
        if (packageManifestModel == null || packageManifestModel.getManifestDocument() == null) {
            summaries.add(Messages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.NoContent.message"));
            return summaries;
        }

        Package packageManifest = packageManifestModel.getPackageManifest();
        try {
            PackageManifestFactory packageManifestFactory = getFactoryLocator().getPackageManifestFactory();
            packageManifest = packageManifestFactory.createPackageManifest(packageManifestModel.getManifestDocument());
            packageManifestFactory.sort(packageManifest);
        } catch (JAXBException e) {
            logger.warn("Unable to create package manifest instance from document", e);
            summaries.add(Messages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.NoContent.message"));
            return summaries;
        }

        if (augmentWithCache) {
            augmentWildcardWithCache(packageManifest, packageManifestModel.getManifestCache());
        }

        List<PackageTypeMembers> types = packageManifest.getTypes();
        if (Utils.isEmpty(types)) {
            summaries.add(Messages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.NoContent.message"));
            return summaries;
        }

        // exclude custom/standard object sub component types, eg CustomFields,
        // which are placed on their parents
        // TODO: handle other subtypes, Workflow-WorkflowAlert
        List<String> childComponentTypes = factoryLocator.getComponentFactory().getSubComponentTypes(Constants.CUSTOM_OBJECT);

        // for each type, loop thru constructing summary stanza
        factoryLocator.getPackageManifestFactory().sort(types);
        // REVIEW ME: instead of fire listMetadata call per type, maybe we can
        // aggregate the calls into one - fchang
        for (PackageTypeMembers typeStanza : types) {
            // get component for display name purposes
            Component component = null;
            if (factoryLocator.getComponentFactory().isRegisteredComponentType(typeStanza.getName())) {
                component = factoryLocator.getComponentFactory().getComponentByComponentType(typeStanza.getName());
            }

            // if display name not available, use type name
            String name = null;
            if (component == null) {
                name = typeStanza.getName();
                logger.warn("Unable to get display name for component type '" + name + "'");
            } else {
                name = component.getDisplayName();
            }

            // handle parents with children - placing children within the parent
            // stanza except
            // when subtype parent isn't explicit list in manifest. this this
            // case we list component
            // separately under it's component types
            if (Utils.isNotEmpty(childComponentTypes) && childComponentTypes.contains(typeStanza.getName())) {
                handleChildTypeSummary(summaries, packageManifest, typeStanza, name);
            } else if (component != null && Constants.CUSTOM_OBJECT.equals(component.getComponentType())) {
                generateObjectsSummary(summaries, packageManifestModel, packageManifest, typeStanza, component);
            } else {
                // resulting summary text
                StringBuffer summary = new StringBuffer();

                // no parent-child relationship, handle normally
                Set<String> members = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                members.addAll(typeStanza.getMembers());
                // if members contain "*", add org members to display all
                // content to be retrieved
                resolveWildcardToNames(members, typeStanza, packageManifestModel.getFileMetadatExt());

                summary.append(generateSummaryTextWork(name, members));
                summary.append(Constants.NEW_LINE);

                if (logger.isDebugEnabled()) {
                    logger.info("Generated the following content summary for '" + name + "':" + Constants.NEW_LINE
                            + summary.toString());
                }

                summaries.add(summary.toString());
            }
        }

        return summaries;
    }

    public FactoryLocator getFactoryLocator() {
        return factoryLocator;
    }

    private Package augmentWildcardWithCache(Package packageManifest, Document manifestDocument) {
        if (Utils.isEmpty(packageManifest.getTypes()) || manifestDocument == null || !manifestDocument.hasChildNodes()) {
            return packageManifest;
        }

        // get package manifest object to make it easier to work with
        Package cachePackageManifest = null;
        try {
            cachePackageManifest = factoryLocator.getPackageManifestFactory().createPackageManifest(manifestDocument);
        } catch (JAXBException e) {
            logger.warn("Unable to create package manifest instance from document", e);
            return packageManifest;
        }

        if (cachePackageManifest == null || Utils.isEmpty(cachePackageManifest.getTypes())) {
            logger.warn("Unable to add cache to project's package manifest: cache is null or empty");
            return packageManifest;
        }

        // for each type stanza, check for wildcard and if found add cache names
        // to type
        for (PackageTypeMembers projectPackageComponentType : packageManifest.getTypes()) {
            if (projectPackageComponentType.getMembers().contains(Constants.PACKAGE_MANIFEST_WILDCARD)) {
                PackageTypeMembers cacheManifestComponentType =
                        getPackageTypeMembers(projectPackageComponentType, cachePackageManifest);
                if (cacheManifestComponentType != null) {
                    // augment project manifest content for type w/ cache
                    // content
                    Set<String> tmpProjectManifestComponentMembers = new HashSet<>();
                    tmpProjectManifestComponentMembers.addAll(projectPackageComponentType.getMembers());
                    tmpProjectManifestComponentMembers.addAll(cacheManifestComponentType.getMembers());
                    projectPackageComponentType.getMembers().clear();
                    projectPackageComponentType.getMembers().addAll(tmpProjectManifestComponentMembers);
                }
            }
        }

        return packageManifest;
    }

    private static PackageTypeMembers getPackageTypeMembers(PackageTypeMembers projectPackageComponentType,
            Package cachePackageManifest) {
        for (PackageTypeMembers cachePackageComponentType : cachePackageManifest.getTypes()) {
            if (projectPackageComponentType.getName().equals(cachePackageComponentType.getName())) {
                return cachePackageComponentType;
            }
        }
        return null;

    }

    protected void resolveWildcardToNames(Set<String> members, PackageTypeMembers typeStanza,
            FileMetadataExt fileMetadataExt) {
        if (hasWildcard(typeStanza.getMembers()) && fileMetadataExt != null && fileMetadataExt.hasFileProperties()) {
            List<String> componentNames = fileMetadataExt.getComponentNamesByComponentType(typeStanza.getName());
            if (Utils.isNotEmpty(componentNames)) {
                members.addAll(componentNames);
            }
        }
    }

    protected boolean hasWildcard(List<String> members) {
        if (Utils.isEmpty(members)) {
            return false;
        }
        for (String member : members) {
            if (Constants.SUBSCRIBE_TO_ALL.equals(member)) {
                return true;
            }
        }

        return false;
    }

    private void generateObjectsSummary(List<String> summaries, PackageManifestModel packageManifestModel,
            Package packageManifest, PackageTypeMembers typeStanza, Component component) {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating summary for type '" + component.getComponentType() + "' with ["
                    + typeStanza.getMembers().size() + "] components");
        }

        Set<String> members = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        members.addAll(typeStanza.getMembers());

        // custom objects require additional validation to differentiate between standard v. custom objects
        String customObjectSummary =
                generateObjectChildrenSummary(component, typeStanza, packageManifestModel, packageManifest,
                    getCustomObjectComponentValidator());

        if (customObjectSummary.length() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Generated the following content summary:" + Constants.NEW_LINE + customObjectSummary);
            }
            summaries.add(customObjectSummary + Constants.NEW_LINE);
        }

        Component standardObjectComponent =
                factoryLocator.getComponentFactory().getComponentByComponentType(Constants.STANDARD_OBJECT);
        if (standardObjectComponent != null) {
            String standardObjectSummary =
                    generateObjectChildrenSummary(standardObjectComponent, typeStanza, packageManifestModel,
                        packageManifest, getStandardObjectComponentValidator());

            // append standard object summary to just after custom object
            if (standardObjectSummary.length() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Generated the following content summary:" + Constants.NEW_LINE
                            + standardObjectSummary);
                }
                summaries.add(standardObjectSummary + Constants.NEW_LINE);
            }
        }

    }

    // handle child types (eg, custom fields) that do not have a parent
    // explicitly list. eg, Account.MyDate__c
    // where Account is not a member of CustomObject
    private void handleChildTypeSummary(List<String> summaries, Package packageManifest, PackageTypeMembers typeStanza,
            String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating summary for child type '" + name + "' with [" + typeStanza.getMembers().size()
                    + "] potential components");
        }

        List<String> typeMembers = typeStanza.getMembers();
        if (Utils.isEmpty(typeMembers)) {
            return;
        }

        Set<String> members = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String typeMember : typeMembers) {
            if (!hasParentInManifest(typeMember, packageManifest)) {
                members.add(typeMember);
            }
        }

        if (Utils.isNotEmpty(members)) {
            StringBuffer summary = new StringBuffer();
            summary.append(generateSummaryTextWork(name, members));
            summary.append(Constants.NEW_LINE);
            if (logger.isDebugEnabled()) {
                logger.info("Generated the following content summary for '" + name + "':" + Constants.NEW_LINE
                        + summary.toString());
            }
            summaries.add(summary.toString());
        }
    }

    private boolean hasParentInManifest(String typeMember, Package packageManifest) {
        if (!typeMember.contains(".")) {
            return false;
        }

        String parentComponentName = typeMember.substring(0, typeMember.indexOf(Constants.DOT));

        List<String> parentComponentTypes = factoryLocator.getComponentFactory().getParentTypesWithSubComponentTypes();
        if (Utils.isEmpty(parentComponentTypes)) {
            return false;
        }

        for (String parentComponentType : parentComponentTypes) {
            PackageTypeMembers parentTypeStanza = getPackageTypeMembers(packageManifest, parentComponentType);
            if (parentTypeStanza != null && Utils.isNotEmpty(parentTypeStanza.getMembers())) {
                for (String member : parentTypeStanza.getMembers()) {
                    if (parentComponentName.equals(member)) {
                        // parent explicit list in package.xml
                        return true;
                    }
                }
            }
        }

        // looked through all types - parents not found
        return false;
    }

    /**
     * Generate summary content - content in organization to be retrieved and added to project - for a given component
     * types.
     *
     * @param fileMetadata
     * @param componentTypes
     *            Include "StandardObject" type StandardObject objects are desired. "CustomObjects" types includes only
     *            CustomObjects objects.
     * @param subscribe
     * @return
     */
    public List<String> generateSummaryText(FileMetadataExt fileMetadata, String[] componentTypes, boolean subscribe) {
        List<String> summaries = new ArrayList<>();

        if (componentTypes == null && fileMetadata != null) {
            Set<String> componentTypesSet = fileMetadata.getComponentTypes();
            if (Utils.isNotEmpty(componentTypesSet)) {
                componentTypes = componentTypesSet.toArray(new String[componentTypesSet.size()]);
            }
        }

        if (fileMetadata == null || !fileMetadata.hasFileProperties() || null == componentTypes || 0 == componentTypes.length) {
            return summaries;
        }

        // exclude custom/standard object sub component types, eg CustomFields,
        // which are placed on their parents
        List<String> childComponentTypes = null;
        childComponentTypes = factoryLocator.getComponentFactory().getSubComponentTypes(Constants.CUSTOM_OBJECT);

        // go... assemble tree string for each given type
        Arrays.sort(componentTypes);
        for (String componentType : componentTypes) {
            // skip custom/standard object sub component types, eg CustomFields,
            // which are placed on their parents
            if (Utils.isNotEmpty(childComponentTypes) && childComponentTypes.contains(componentType)) {
                continue;
            }

            Component component = null;
            if (factoryLocator.getComponentFactory().isRegisteredComponentType(componentType)) {
                component = factoryLocator.getComponentFactory().getComponentByComponentType(componentType);
            }

            Set<String> members = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            String name = componentType;
            if (component == null) {
                name = componentType;
                logger.warn("Unable to get display name for component type '" + name + "'");

                // add subscribe if selected
                if (subscribe) {
                    members.add(Constants.SUBSCRIBE_TO_ALL);
                }
            } else {
                name = component.getDisplayName();

                // add subscribe if selected
                if (subscribe && component.isWildCardSupported()) {
                    members.add(Constants.SUBSCRIBE_TO_ALL);
                }
            }

            FileProperties[] filePropertiesArray =
                    fileMetadata.getFilePropertiesByComponentTypes(new String[] { componentType });

            // handle parents with children - placing children within the parent
            // stanza
            // TODO: handle other subtypes, Workflow-WorkflowAlert

            if (logger.isDebugEnabled()) {
                logger.debug("Generating summary for type '" + componentType + "' with [" + filePropertiesArray.length
                        + "] components");
            }

            // add members to summary list
            if (Utils.isNotEmpty(filePropertiesArray)) {
                for (FileProperties fileProperties : filePropertiesArray) {
                    members.add(fileProperties.getFullName());
                }
            }

            // resulting summary text
            StringBuffer summary = new StringBuffer((generateSummaryTextWork(name, members)));
            summary.append(Constants.NEW_LINE);

            if (logger.isDebugEnabled()) {
                logger.debug("Generated the following content summary:" + Constants.NEW_LINE + summary.toString());
            }

            summaries.add(summary.toString());
        }

        return summaries;
    }

    /*
     * Method generates the following structure <component>(-es/s) (Subscribe to
     * new <component>(-es/s)) <name> <name>
     */
    protected String generateSummaryTextWork(String typeName, Set<String> members) {
        StringBuffer summary = new StringBuffer(typeName);
        if (Utils.isNotEmpty(members)) {
            for (String member : members) {
                if (Constants.SUBSCRIBE_TO_ALL.equals(member)) {
                    summary.append(" ").append(
                        Messages.getString(
                            "ProjectCreateWizard.ProjectContent.ContentSummary.Subscribe.message",
                            new String[] { Utils.sentenceCase(Utils.getPlural(typeName)) }));
                } else {
                    summary.append(Constants.NEW_LINE).append(SUMMARY_INDENTION).append(member);
                }
            }
        }
        return summary.toString();
    }
    
    /*
     * Method generates the following structure: Objects - Custom candidate__c
     * (Partial) Custom Fields City Country jobapplication__c Custom Fields
     * <list all custom fields> Validation Rules <list all validation rules>
     */
    protected String generateObjectChildrenSummary(Component component, PackageTypeMembers typeStanza,
            PackageManifestModel packageManifestModel, Package packageManifest, ComponentValidator componentvalidator) {

        final List<String> members = typeStanza.getMembers();
        if (Utils.isEmpty(members)) {
            return Constants.EMPTY_STRING;
        }
        List<String> parentComponentNames =
                getParentCustomObjectNames(component, typeStanza, packageManifestModel.getFileMetadatExt());

        Map<String, List<String>> componentTypeMembers =
                getComponentTypeMembers(component, packageManifestModel, packageManifest, parentComponentNames);

        String displayName;
        if (Constants.CUSTOM_OBJECT.equals(component.getComponentType())) {
            displayName = getDisplayName(component, members.contains(Constants.SUBSCRIBE_TO_ALL));
        } else {
            displayName = getDisplayName(component, false);
        }

        final String childrenSummary = generateChildrenSummary(component,
            parentComponentNames, packageManifestModel, componentTypeMembers, componentvalidator);
        return (Utils.isNotEmpty(childrenSummary))?(new StringBuilder(displayName).append(childrenSummary)).toString():childrenSummary;
    }

    protected Map<String, List<String>> getComponentTypeMembers(Component component,
            PackageManifestModel packageManifestModel, Package packageManifest, List<String> parentComponentNames) {
        // list of sub or child types which we'll inspect and display
        List<String> subComponentTypes = component.getSubComponentTypes();
        Collections.sort(subComponentTypes);

        // pre-fetch the package stanza for each sub type to reduce looping thru
        // types superfluously
        Map<String, List<String>> componentTypeMemberMap = new HashMap<>(subComponentTypes.size());
        for (String subComponentType : subComponentTypes) {
            if (parentComponentNames.contains(Constants.SUBSCRIBE_TO_ALL)
                    && packageManifestModel.getFileMetadatExt() != null) {
                List<String> subComponentNames =
                        packageManifestModel.getFileMetadatExt().getComponentNamesByComponentType(subComponentType);
                if (Utils.isNotEmpty(subComponentNames)) {
                    Collections.sort(subComponentNames);
                    componentTypeMemberMap.put(subComponentType, subComponentNames);
                }
            } else {
                PackageTypeMembers subType = getPackageTypeMembers(packageManifest, subComponentType);
                if (subType != null && Utils.isNotEmpty(subType.getMembers())) {
                    List<String> members = subType.getMembers();
                    Collections.sort(members);
                    componentTypeMemberMap.put(subComponentType, members);
                }
            }
        }
        return componentTypeMemberMap;
    }

    protected List<String> getParentCustomObjectNames(Component component, PackageTypeMembers typeStanza,
            FileMetadataExt fileMetadatExt) {
        final List<String> members = typeStanza.getMembers();
        List<String> parentComponentNames = new ArrayList<>(members);


        if (parentComponentNames.contains(Constants.SUBSCRIBE_TO_ALL)
                && Constants.CUSTOM_OBJECT.equals(component.getComponentType())) {
            List<String> componentNames =
                    (fileMetadatExt != null) ? fileMetadatExt.getComponentNamesByComponentType(component
                            .getComponentType()) : null;
            if (Utils.isNotEmpty(componentNames)) {
                parentComponentNames = componentNames;
            } else {
                parentComponentNames.remove(Constants.SUBSCRIBE_TO_ALL);
            }
        }
        Collections.sort(parentComponentNames);
        return parentComponentNames;
    }

    protected String generateChildrenSummary(Component component, List<String> parentComponentNames,
            PackageManifestModel packageManifestModel, Map<String, List<String>> componentTypeMemberMap,
            ComponentValidator validator) {

        // some objects have specific display names other than the default, eg
        // custom/standard objects
        StringBuffer summary = new StringBuffer();
        StringBuffer strBuffSubType = new StringBuffer();
        // for each member in the parent stanza, loop display name and
        // generating child type stanzas
        for (String member : parentComponentNames) {
            if (!validator.isValidMember(member)) {
                continue;
            }

            strBuffSubType.append(Constants.NEW_LINE).append(SUMMARY_INDENTION).append(member);

            if (Utils.isNotEmpty(componentTypeMemberMap)) {
                // for each child type, display name
                Set<String> subTypeNames = componentTypeMemberMap.keySet();
                for (String subTypeName : subTypeNames) {
                    List<String> subComponents = componentTypeMemberMap.get(subTypeName);
                    // if child has members, inspect
                    if (Utils.isNotEmpty(subComponents)) {
                        StringBuffer strBuffMembers = new StringBuffer();
                        // member will be prefixed with parent name
                        for (String subMember : subComponents) {
                            if (validator.isValidChildOfMember(member, subMember)) {
                                if (subMember.startsWith(member)) {
                                    strBuffMembers.append(Constants.NEW_LINE).append(getSummaryIndention(3)).append(
                                        validator.getChildDisplayName(member, subMember));
                                } else {
                                    strBuffMembers.append(Constants.NEW_LINE).append(getSummaryIndention(3)).append(
                                        subMember);
                                }
                            }
                        }

                        // if no members of the parent were found, don't display
                        // anything
                        if (strBuffMembers.length() > 0) {
                            String name = Utils.camelCaseToSpaces(subTypeName);
                            name = Utils.getPlural(subTypeName);
                            strBuffSubType.append(Constants.NEW_LINE).append(getSummaryIndention(2)).append(name)
                                    .append(strBuffMembers.toString());
                        }
                    } else {
                        strBuffSubType.append(Constants.NEW_LINE).append(getSummaryIndention(3)).append("n/a");
                    }
                }
            }
        }

        if (strBuffSubType.length() > 0) {
            summary.append(strBuffSubType.toString());
        }

        return summary.toString();
    }

    private static String getDisplayName(Component component, boolean appendSubscribeText) {
        String displayName = null;
        final String type = component.getComponentType();
        if (Constants.CUSTOM_OBJECT.equals(type) || Constants.STANDARD_OBJECT.equals(type)) {
            displayName = "Objects - " + type.substring(0, type.indexOf("Object"));
        } else {
            displayName = component.getDisplayName();
        }
        
        if (appendSubscribeText) {
            displayName += " " + Messages.getString(
                "ProjectCreateWizard.ProjectContent.ContentSummary.Subscribe.message",
                new String[] { Utils.getPlural(component.getDisplayName()) });
        }
        
        return displayName;
    }
    
    private static PackageTypeMembers getPackageTypeMembers(Package packageManifest, String typeName) {
        List<PackageTypeMembers> types = packageManifest.getTypes();
        for (PackageTypeMembers type : types) {
            if (typeName.equals(type.getName())) {
                return type;
            }
        }

        return null;
    }

    private static String getSummaryIndention(int num) {
        StringBuffer summary = new StringBuffer(SUMMARY_INDENTION);
        if (num > 1) {
            for (int i = 1; i < num; i++) {
                summary.append(SUMMARY_INDENTION);
            }
        }
        return summary.toString();
    }

    public ComponentValidator getCustomObjectComponentValidator() {
        return customObjectComponentValidator;
    }

    protected abstract class ComponentValidator {
        public abstract boolean isValidMember(String componentName);

        public boolean isValidChildOfMember(String parentComponentName, String childComponentName) {
            return isValidMember(parentComponentName) && childComponentName.startsWith(parentComponentName);
        }

        public String getChildDisplayName(String parentComponentName, String childComponentName) {
            return childComponentName.substring(parentComponentName.length() + 1);
        }
    }
}
