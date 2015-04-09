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
package com.salesforce.ide.core.internal.components.reportType;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.salesforce.ide.api.metadata.types.ReportLayoutSection;
import com.salesforce.ide.api.metadata.types.ReportTypeColumn;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;
import com.sforce.soap.partner.wsc.Field;

/**
 * Encapsulates attributes for new Report Type generation.
 * 
 * @author fchang
 */
public class ReportTypeModel extends ComponentModel {
    private static final String DEFAULT_EXCLUDED_FIELD_KEY = "Default";
    private static final Logger logger = Logger.getLogger(ReportTypeModel.class);

    @Override
    public String getComponentType() {
        return Constants.REPORT_TYPE;
    }

    @Override
    public void loadAdditionalComponentAttributes() throws FactoryException, JAXBException {
        com.salesforce.ide.api.metadata.types.ReportType reportType = null;
        DescribeSObjectResult primaryObject = null;
        DescribeObjectRegistry describeObjectRegistry = null;

        try {
            reportType = (com.salesforce.ide.api.metadata.types.ReportType) component.getDefaultMetadataExtInstance();
            describeObjectRegistry = getComponentFactory().getConnectionFactory().getDescribeObjectRegistry();
            // BaseObject here is plural form save from user inpurt in Ui
            primaryObject =
                    describeObjectRegistry.getCachedDescribeByPluralLabel(getProject(), reportType.getBaseObject());
        } catch (ForceConnectionException e) {
            logger.warn("Unable to get describeObject for " + reportType.getBaseObject(), e);
        } catch (ForceRemoteException e) {
            logger.warn("Unable to get describeObject for " + reportType.getBaseObject(), e);
        }

        if (null == primaryObject) {
            logger.error("Unable to load additional component attributes for Report Type '" + reportType.getLabel()
                    + "' due to unable to locate primary object '" + reportType.getBaseObject()
                    + "' from DescribeObjectRegistry");
            return;
        }
        // load all fields & sections from primary object
        ReportLayoutSection section = new ReportLayoutSection();
        section.setMasterLabel(primaryObject.getLabelPlural()); // Master label is plural label of describeObject

        // exclude fields that describeSObject returned as extra: default + sobject specific set.
        List<String> excludeFieldList = new ArrayList<>();
        List<String> defaultExcludeFieldList =
                describeObjectRegistry.getExcludedCrtFields().get(DEFAULT_EXCLUDED_FIELD_KEY);
        excludeFieldList.addAll(defaultExcludeFieldList);
        List<String> sObjectSpecificExcludedList =
                describeObjectRegistry.getExcludedCrtFields().get(primaryObject.getName());
        if (Utils.isNotEmpty(sObjectSpecificExcludedList)) {
            excludeFieldList.addAll(sObjectSpecificExcludedList);
        }

        String tableName = primaryObject.getName(); // Table label is name of describeSObject
        Field[] fields = primaryObject.getFields();
        for (Field field : fields) {
            if (excludeFieldList.contains(field.getName()))
                continue;

            // skip Person Account related fields
            if (tableName.equalsIgnoreCase("Account")
                    && (field.getName().contains("Person") || field.getName().endsWith("__pc")))
                continue;

            String fieldName = field.getName();
            // use relationshipName instead when soapType is Id
            if (field.getType() == com.sforce.soap.partner.wsc.FieldType.reference
                    && !field.getName().equalsIgnoreCase("Id")) {
                // if relationshipName is null then use fieldName but remove trailing "Id"
                fieldName =
                        Utils.isNotEmpty(field.getRelationshipName()) ? field.getRelationshipName() : fieldName
                                .substring(0, fieldName.length() - 2);
            }
            ReportTypeColumn column = new ReportTypeColumn();
            column.setCheckedByDefault(false);
            column.setField(fieldName);
            column.setTable(tableName);
            section.getColumns().add(column);
        }
        reportType.getSections().clear(); // spring bean, reportTypeMetadata is singleton; therefore, content from
        // previous session won't be clean-up.
        reportType.getSections().add(section);
        reportType.setBaseObject(primaryObject.getName()); // replace Ui input string which is in plural label form
        // with name of describeSObject.

        // unmarshall component and init componet body
        saveMetadata(component);
        if (logger.isDebugEnabled()) {
            logger.debug("Prepared the following components for saving:\n " + componentList.toString());
        }
    }
}
