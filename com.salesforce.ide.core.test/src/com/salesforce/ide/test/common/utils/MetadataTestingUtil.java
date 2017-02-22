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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sforce.soap.metadata.ApexClass;
import com.sforce.soap.metadata.ApexPage;
import com.sforce.soap.metadata.ApexTrigger;
import com.sforce.soap.metadata.CustomApplication;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.CustomTab;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeploymentStatus;
import com.sforce.soap.metadata.Document;
import com.sforce.soap.metadata.Encoding;
import com.sforce.soap.metadata.EncryptedFieldMaskChar;
import com.sforce.soap.metadata.EncryptedFieldMaskType;
import com.sforce.soap.metadata.FieldType;
import com.sforce.soap.metadata.Folder;
import com.sforce.soap.metadata.FolderAccessTypes;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.PicklistValue;
import com.sforce.soap.metadata.Profile;
import com.sforce.soap.metadata.RecordType;
import com.sforce.soap.metadata.SControlContentSource;
import com.sforce.soap.metadata.Scontrol;
import com.sforce.soap.metadata.SharingModel;
import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.PartnerConnection;

/**
 * Testing Utility for using metadata api
 * @author agupta
 */
public class MetadataTestingUtil extends BaseTestingUtil {

    private PartnerConnection pConnStub;

    public static enum MetadataType {
        ApexPage(ApexPage.class.getSimpleName()),
        ApexClass(ApexClass.class.getSimpleName()),
        CustomApplication(CustomApplication.class.getSimpleName()),
        CustomField(CustomField.class.getSimpleName()),
        CustomObject(CustomObject.class.getSimpleName()),
        CustomTab(CustomTab.class.getSimpleName()),
        Document(Document.class.getSimpleName()),
        Folder(Folder.class.getSimpleName()),
        Package(Package.class.getSimpleName()),
        PicklistValue(PicklistValue.class.getSimpleName()),
        Profile(Profile.class.getSimpleName()),
        RecordType(RecordType.class.getSimpleName()),
        Scontrol(Scontrol.class.getSimpleName()),
        ApexTrigger(ApexTrigger.class.getSimpleName());

        private final String typeValue;

        private static class TypeComparator implements Comparator<String> {

            private static final List<String> orderList;
            static {
                orderList = Collections.unmodifiableList(Arrays.asList(Package.toString(), ApexPage.toString(),
                    ApexTrigger.toString(), ApexClass.toString(), CustomApplication.toString(), CustomTab.toString(),
                    Scontrol.toString(), RecordType.toString(), PicklistValue.toString(), CustomField.toString(),
                    CustomObject.toString(), Document.toString(), Folder.toString(), Profile.toString()));
            }

            public int compare(String o1, String o2) {
                if (orderList.indexOf(o1) < orderList.indexOf(o2))
                    return -1;
                else if (orderList.indexOf(o1) > orderList.indexOf(o2))
                    return 1;
                return 0;
            }
        }

        private static Map<String, MetadataType> typesMap = new TreeMap<String, MetadataType>(new TypeComparator());

        static {
            typesMap.put(ApexPage.toString(), ApexPage);
            typesMap.put(ApexClass.toString(), ApexClass);
            typesMap.put(CustomApplication.toString(), CustomApplication);
            typesMap.put(CustomField.toString(), CustomField);
            typesMap.put(CustomObject.toString(), CustomObject);
            typesMap.put(CustomTab.toString(), CustomTab);
            typesMap.put(Document.toString(), Document);
            typesMap.put(Folder.toString(), Folder);
            typesMap.put(Package.toString(), Package);
            typesMap.put(PicklistValue.toString(), PicklistValue);
            typesMap.put(Profile.toString(), Profile);
            typesMap.put(RecordType.toString(), RecordType);
            typesMap.put(Scontrol.toString(), Scontrol);
            typesMap.put(ApexTrigger.toString(), ApexTrigger);
        }

        private MetadataType(String type) {
            typeValue = type;
        }

        public static MetadataType fromValue(String xmlType) throws java.lang.IllegalStateException {
            MetadataType type = typesMap.get(xmlType);
            if (null == type)
                throw new java.lang.IllegalStateException();
            return type;
        }

        @Override
        public String toString() {
            return typeValue;
        }

        public static MetadataType[] getAllTypes() {
            return typesMap.values().toArray(new MetadataType[0]);
        }
    }

    public static String getRandomString() {
        String randomString = System.currentTimeMillis() + Double.toString(Math.random());
        return randomString.replaceAll("\\W", "");
    }
    private static Map<MetadataType, String> queries = new HashMap<MetadataType, String>();
    static {
        queries.put(MetadataType.ApexPage, "select id from ApexPage");
        queries.put(MetadataType.CustomObject, "select id from CustomEntityDefinition");
        queries.put(MetadataType.CustomTab, "select id from CustomTabDefinition");
        queries.put(MetadataType.Document, "select id from Document");
        queries.put(MetadataType.Scontrol, "select id from SControl");
        queries.put(MetadataType.Package, "select id from Project");
        queries.put(MetadataType.ApexTrigger, "select id from ApexTrigger");
    }

    public void deleteAllCustomMetadata(MetadataType t) throws Exception {
        if (t != MetadataType.ApexClass && t != MetadataType.ApexTrigger && t != MetadataType.Profile) {
            SObject[] so = pConnStub.query(queries.get(t)).getRecords();
            deleteSObjects(so);
        }
    }

    public void deleteMetadataObject(MetadataType t, String fullName) throws Exception {
        Metadata mObject;
        mObject = (Metadata) Class.forName(Metadata.class.getPackage().getName() + "." + t.toString()).newInstance();
        mObject.setFullName(fullName);
        Thread.sleep(4000);
    }

    private void deleteSObjects(SObject[] so) throws Exception {
        if (so != null && so.length != 0) {
            List<String> ids = new ArrayList<String>(so.length);
            for (SObject s : so) {
                ids.add(s.getId());
            }
            pConnStub.delete(ids.toArray(new String[ids.size()]));
        }
    }

    public static CustomObject fillDummyCustomObject(FieldType nameFieldType) {
        CustomObject co = new CustomObject();
        String name = "FTEST" + getRandomString();
        co.setFullName(name + "__c");
        co.setDeploymentStatus(DeploymentStatus.Deployed);
        co.setDescription("created by the Metadata API");
        co.setEnableActivities(true);
        co.setLabel(name);
        co.setPluralLabel(co.getLabel() + "s");
        co.setSharingModel(SharingModel.ReadWrite);

        CustomField nf = new CustomField();
        nf.setType(nameFieldType);
        nf.setLabel("Name");
        if (nameFieldType == FieldType.AutoNumber) {
            nf.setDisplayFormat("AN-{0000}");
        }
        co.setNameField(nf);
        return co;
    }

    public static CustomObject fillDummyCustomObject() {
        return fillDummyCustomObject(FieldType.Text);
    }

    public static CustomField fillDummyCustomField(String parentEntityName) {
        return fillDummyCustomField(parentEntityName, FieldType.Text);
    }

    public static CustomField fillDummyCustomField(String parentEntityName, FieldType ft) {
        return fillDummyCustomField(parentEntityName, ft, false);
    }

    public static CustomField fillDummyCustomField(String parentEntityName, FieldType ft, boolean isFormula) {
        CustomField f = new CustomField();
        final String name = "FTestCF" + getRandomString();
        f.setFullName(parentEntityName + "." + name + "__c");
        f.setLabel(name);
        f.setType(ft);
        if (ft == FieldType.AutoNumber) {
            f.setDisplayFormat("AF-{0000}");
        } else if (ft == FieldType.Lookup) {
            f.setReferenceTo("Account");
            f.setRelationshipName("CustomNoParentLookups");
        } else if (ft == FieldType.MasterDetail) {
            f.setReferenceTo("Case");
            f.setRelationshipName("CaseMasterDetail");
        } else if (ft == FieldType.Checkbox) {
            f.setDefaultValue("false");
        } else if (ft == FieldType.Currency || ft == FieldType.Number || ft == FieldType.Percent) {
            f.setScale(1);
            f.setPrecision(4);
        } else if (ft == FieldType.Text && !isFormula) {
            f.setLength(100);
        } else if (ft == FieldType.LongTextArea) {
            f.setVisibleLines(5);
            f.setLength(5000);
        } else if (ft == FieldType.EncryptedText) {
            f.setLength(100);
            f.setMaskType(EncryptedFieldMaskType.lastFour);
            f.setMaskChar(EncryptedFieldMaskChar.asterisk);
        }
        return f;
    }

    public static ApexPage fillDummyApexPage(String namePrefix) {

        ApexPage page = new ApexPage();
        String label = namePrefix + getRandomString();
        page.setContent("<apex:page></apex:page>".getBytes());
        page.setFullName(label + "__c");
        page.setLabel(label);

        return page;
    }

    public static ApexPage fillDummyApexPage() {
        return fillDummyApexPage("FTestAP");
    }

    public static Scontrol fillDummyScontrol(String namePrefix, SControlContentSource contentSource, String content,
        Encoding encodingKey, boolean supportsCaching) {

        Scontrol scontrol = new Scontrol();
        String label = namePrefix + getRandomString();
        scontrol.setContent(content.getBytes());
        scontrol.setContentSource(contentSource);
        scontrol.setDescription("A test scontrol");
        scontrol.setEncodingKey(encodingKey);
        scontrol.setName(label);
        scontrol.setFullName(label + "__c");
        scontrol.setSupportsCaching(supportsCaching);

        return scontrol;
    }

    public static Scontrol fillDummyScontrol(String namePrefix, SControlContentSource contentSource, String content) {
        return fillDummyScontrol(namePrefix, contentSource, content, Encoding.UTF_8, true);
    }

    public static Scontrol fillDummyScontrol(String namePrefix) {
        return fillDummyScontrol(namePrefix, SControlContentSource.HTML,
            "<html><body>This is a Ftest Scontrol</body></html>");
    }

    public static Scontrol fillDummyScontrol() {
        return fillDummyScontrol("FTestSC");
    }

    public static CustomTab fillDummyURLCustomTab(String namePrefix, String url) {

        CustomTab ctab = new CustomTab();
        String label = namePrefix + getRandomString();
        ctab.setFullName(label);
        ctab.setDescription("A Ftest URL type Custom Tab.");
        ctab.setMotif("Custom1: Heart");
        ctab.setUrlEncodingKey(Encoding.UTF_8);
        ctab.setUrl(url);
        ctab.setFrameHeight(602);
        ctab.setCustomObject(false);

        return ctab;
    }

    public static CustomTab fillDummyURLCustomTab() {
        return fillDummyURLCustomTab("FTestCT", "http://www.salesforce.com");
    }

    public static CustomTab fillDummyCustomScontrolTab(String namePrefix, String scontrolName) {

        CustomTab ctab = new CustomTab();
        String label = namePrefix + getRandomString();
        ctab.setFullName(label);
        ctab.setDescription("A Ftest Custom Scontrol Tab.");
        ctab.setMotif("Custom1: Heart");
        ctab.setScontrol(scontrolName);
        ctab.setFrameHeight(602);
        ctab.setCustomObject(false);

        return ctab;
    }

    public static CustomTab fillDummyCustomScontrolTab(String scontrolName) {
        return fillDummyCustomScontrolTab("FTestCT", scontrolName);
    }

    public static CustomTab fillDummyCustomObjectTab(String customObject) {

        CustomTab ctab = new CustomTab();
        ctab.setFullName(customObject);
        ctab.setDescription("A Ftest Custom Scontrol Tab.");
        ctab.setMotif("Custom1: Heart");
        ctab.setCustomObject(true);

        return ctab;
    }

    public static Folder fillDummyFolder() {
        return fillDummyFolder(FolderAccessTypes.Public);
    }

    public static Folder fillDummyFolder(FolderAccessTypes accessType) {
        return fillDummyFolder("FTestFolder", accessType);
    }

    public static Folder fillDummyFolder(String namePrefix, FolderAccessTypes accessType) {
        Folder folder = new Folder();
        folder.setAccessType(accessType);
        folder.setFullName(namePrefix + getRandomString());
        return folder;
    }

    public static Document fillDummyDocument() {
        return fillDummyDocument(true);
    }

    public static Document fillDummyDocument(boolean isPublic) {
        return fillDummyDocument("FTestDocument", isPublic);
    }

    public static Document fillDummyDocument(String namePrefix, boolean isPublic) {
        Document document = new Document();
        document.setPublic(isPublic);
        document.setInternalUseOnly(true);
        document.setContent("This is Ftest document. I can do crazy things...:D".getBytes());
        document.setFullName(namePrefix + getRandomString());
        return document;
    }

    public static DeployOptions createDefaultDeployOptions() {
        DeployOptions de = new DeployOptions();
        de.setRollbackOnError(true);
        de.setSinglePackage(true);
        return de;
    }
}
