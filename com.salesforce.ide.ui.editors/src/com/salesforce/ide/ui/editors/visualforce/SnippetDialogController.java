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
package com.salesforce.ide.ui.editors.visualforce;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.List;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;
import com.salesforce.ide.core.remote.registries.MergeFieldsRegistry;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;
import com.sforce.soap.partner.wsc.Field;

/**
 *
 * @author cwall
 */
public class SnippetDialogController extends Controller {
    private static final Logger logger = Logger.getLogger(SnippetDialogController.class);

    private MergeFieldsRegistry mergeFieldsRegistry = null;
    private DescribeObjectRegistry describeObjectRegistry = null;
    private final Hashtable<String, Hashtable<String, String>> componentTypes =
            new Hashtable<>();
    private Hashtable<String, String> snippets = new Hashtable<>();
    private IProject project = null;
    private String selectedField = null;
    private String selectedSnippet = null;

    //   C O N S T R U C T O R S
    public SnippetDialogController() {
        super();
    }

    //   M E T H O D S
    public MergeFieldsRegistry getMergeFieldsRegistry() {
        if (mergeFieldsRegistry == null) {
            mergeFieldsRegistry = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getMergeFieldsRegistry();
        }
        return mergeFieldsRegistry;
    }

    public DescribeObjectRegistry getDescribeObjectRegistry() {
        if (describeObjectRegistry == null) {
            describeObjectRegistry = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry();
        }

        return describeObjectRegistry;
    }

    public Hashtable<String, Hashtable<String, String>> getComponentTypes() {
        return componentTypes;
    }

    public Hashtable<String, String> getSnippets() {
        return snippets;
    }

    public String getSnippet(String snippetName) {
        return snippets.get(snippetName);
    }

    public void setSnippets(Hashtable<String, String> snippets) {
        this.snippets = snippets;
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    public String getSelectedField() {
        return selectedField;
    }

    public void setSelectedField(String selectedField) {
        this.selectedField = selectedField;
    }

    public String getSelectedSnippet() {
        return selectedSnippet;
    }

    public void setSelectedSnippet(String selectedSnippet) {
        this.selectedSnippet = selectedSnippet;
    }

    public static Logger getLogger() {
        return logger;
    }

    public void initializeLists(List listObjects, List listSnippets) {
        if (project == null) {
            logger.warn("Unable to initialize controller - project is null");
            return;
        }

        loadObjectsAndFields();
        if (Utils.isNotEmpty(componentTypes)) {
            Set<String> componentTypeKeys = componentTypes.keySet();
            TreeSet<String> sortedComponentTypeKeys = new TreeSet<>();
            sortedComponentTypeKeys.addAll(componentTypeKeys);
            for (String sortedComponentTypeKey : sortedComponentTypeKeys) {
                listObjects.add(sortedComponentTypeKey);
            }
        }

        if (Utils.isNotEmpty(snippets)) {
            Set<String> snippetKeys = snippets.keySet();
            TreeSet<String> sortedSnippetKeys = new TreeSet<>();
            sortedSnippetKeys.addAll(snippetKeys);
            for (String sortedSnippetKey : sortedSnippetKeys) {
                listSnippets.add(sortedSnippetKey);
            }
        }
    }

    public void loadFieldList(List listFields, String selection) throws ForceConnectionException, ForceRemoteException {
        if (project == null) {
            logger.warn("Unable to initialize controller - project is null");
            return;
        }

        listFields.removeAll();
        Hashtable<String, String> mergeFieldObjects = componentTypes.get(selection);
        if (Utils.isEmpty(mergeFieldObjects)) {
            DescribeSObjectResult describeSObjectResult =
                    getDescribeObjectRegistry().getCachedDescribe(project, selection);
            mergeFieldObjects = new Hashtable<>(describeSObjectResult.getFields().length);

            Field[] fields = describeSObjectResult.getFields();
            if (Utils.isNotEmpty(fields)) {
                Arrays.sort(fields, new Comparator<Field>() {
                    @Override
                    public int compare(Field o1, Field o2) {
                        String name1 = (o1).getLabel();
                        String name2 = (o2).getLabel();
                        return name1.compareTo(name2);
                    }
                });
            }

            for (Field field : fields) {
                mergeFieldObjects.put(field.getLabel(), field.getName());
            }
            componentTypes.put(selection, mergeFieldObjects);
        }

        Set<String> mergeFieldObjectKeys = mergeFieldObjects.keySet();
        TreeSet<String> sortedMergeFieldObjectKeys = new TreeSet<>();
        sortedMergeFieldObjectKeys.addAll(mergeFieldObjectKeys);
        for (String sortedMergeFieldObjectKey : sortedMergeFieldObjectKeys) {
            listFields.add(sortedMergeFieldObjectKey);
            listFields.setData(sortedMergeFieldObjectKey, mergeFieldObjects.get(sortedMergeFieldObjectKey));
        }
    }

    public void loadObjectsAndFields() {
        if (project == null) {
            logger.warn("Unable to initialize controller - project is null");
            return;
        }

        try {
            Hashtable<String, String> objectActions = getMergeFieldsRegistry().getAllObjectActions(project);
            if (Utils.isNotEmpty(objectActions)) {
                componentTypes.put("$Actions", objectActions);
            }
        } catch (ForceRemoteException e) {
            logger.warn("Unable to load objects and fields", e);
        }

        try {
            Hashtable<String, String> objectTyes = getMergeFieldsRegistry().getAllObjectTypes(project);
            if (Utils.isNotEmpty(objectTyes)) {
                componentTypes.put("$ComponentType", objectTyes);
            }
        } catch (ForceRemoteException e) {
            logger.warn("Unable to load objects and fields", e);
        }

        Hashtable<String, String> orgTypes = getMergeFieldsRegistry().getOrganizationTypes();
        if (Utils.isNotEmpty(orgTypes)) {
            componentTypes.put("$Organization", orgTypes);
        }

        Hashtable<String, String> profileTypes = getMergeFieldsRegistry().getProfileTypes();
        if (Utils.isNotEmpty(profileTypes)) {
            componentTypes.put("$Profile", profileTypes);
        }

        try {
            Hashtable<String, String> scontrols = getMergeFieldsRegistry().getSControlList(project);
            if (Utils.isNotEmpty(scontrols)) {
                componentTypes.put("$SControl", scontrols);
            }
        } catch (ForceRemoteException e) {
            logger.warn("Unable to load objects and fields", e);
        }

        Hashtable<String, String> userTypes = getMergeFieldsRegistry().getUserTypes();
        if (Utils.isNotEmpty(userTypes)) {
            componentTypes.put("$User", getMergeFieldsRegistry().getUserTypes());
        }
        Hashtable<String, String> roleTypes = getMergeFieldsRegistry().getUserRoleTypes();
        if (Utils.isNotEmpty(roleTypes)) {
            componentTypes.put("$UserRole", getMergeFieldsRegistry().getUserRoleTypes());
        }
        componentTypes.put("Account", getEmtpyMergeFieldHashtable());
        componentTypes.put("Activity", getEmtpyMergeFieldHashtable());
        componentTypes.put("Asset", getEmtpyMergeFieldHashtable());
        componentTypes.put("Campaign", getEmtpyMergeFieldHashtable());
        componentTypes.put("Case", getEmtpyMergeFieldHashtable());
        componentTypes.put("Contact", getEmtpyMergeFieldHashtable());
        componentTypes.put("Contract", getEmtpyMergeFieldHashtable());
        componentTypes.put("Event", getEmtpyMergeFieldHashtable());
        componentTypes.put("Lead", getEmtpyMergeFieldHashtable());
        componentTypes.put("Opportunity", getEmtpyMergeFieldHashtable());
        componentTypes.put("OpportunityLineItem", getEmtpyMergeFieldHashtable());
        componentTypes.put("Product2", getEmtpyMergeFieldHashtable());
        componentTypes.put("Solution", getEmtpyMergeFieldHashtable());
        componentTypes.put("Task", getEmtpyMergeFieldHashtable());

        try {
            Set<String> gtypes = getDescribeObjectRegistry().getCachedGlobalDescribeTypes(project);
            if (Utils.isNotEmpty(gtypes)) {
                TreeSet<String> sortedTypes = new TreeSet<>();
                sortedTypes.addAll(gtypes);
                for (String sortedType : sortedTypes) {
                    if (sortedType.endsWith(Constants.CUSTOM_OBJECT_SUFFIX)) {
                        componentTypes.put(sortedType, getEmtpyMergeFieldHashtable());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to load objects and fields", e);
        }

        try {
            snippets = getMergeFieldsRegistry().getSnippets(project);
        } catch (ForceRemoteException e) {
            logger.error("Unable to load objects and fields", e);
            Utils.openError(e, true, "Unable to load objects and fields\n\n" + e.getStrippedExceptionMessage());
        }
    }

    private static Hashtable<String, String> getEmtpyMergeFieldHashtable() {
        return new Hashtable<>();
    }

    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, IOException {

    }

    @Override
    public void init() {

    }

    public void clean() {

    }

    @Override
    public void dispose() {

    }
}
