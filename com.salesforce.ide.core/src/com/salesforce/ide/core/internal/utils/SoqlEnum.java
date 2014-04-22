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
package com.salesforce.ide.core.internal.utils;

public enum SoqlEnum {

    // A P E X   C L A S S E S
    CLASS_MTHDS_IDENTIFIER(
            "Id, ApexClassId, ParentIdentifierId, IdentifierName, IdentifierType, LineNumber, "
                    + "ColumnNumber, OptionsTestMethod, OptionsPublicIdentifier, OptionsWebService, OptionsFinalIdentifier, "
                    /*+ "OptionsSystemDefined,*/+ "OptionsPrivateIdentifier, OptionsProtectedIdentifier, OptionsAbstractIdentifier, "
                    + "OptionsMethodOverrides, OptionsStaticIdentifier, OptionsVirtualIdentifier, OptionsGlobalIdentifier"),

    // P A C K A G E S
    DEVELOPMENT_PACKAGES("SELECT Id, Name, Description, IsManaged FROM DevelopmentPackageVersion"),
    INSTALL_PACKAGES("SELECT Id, Name, Description, IsManaged, VersionName FROM InstalledPackageVersion"),

    // S O B J E C T S
    CONTACTS("Id, FirstName, LastName FROM Contact"),
    SCONTROLS("Name, DeveloperName, ContentSource FROM SControl"),
    USER("Id, Name FROM User");

    public static final String DEVELOPMENT_PACKAGE_OBJECT = "DevelopmentPackageVersion";
    public static final String INSTALL_PACKAGE_OBJECT = "InstalledPackageVersion";

    String soql;

    private SoqlEnum(String soql) {
        this.soql = soql;
    }

    public String getSoql() {
        return soql;
    }

    public static String getClassIdentifer(String id) {
        return "SELECT " + CLASS_MTHDS_IDENTIFIER.getSoql() + " " + "FROM ApexClassIdentifier WHERE ApexClassId = '"
                + id + "' " + "ORDER BY IdentifierType DESC, IdentifierName ASC ";
    }

    public static String getDevelopmentPackages() {
        return DEVELOPMENT_PACKAGES.getSoql();
    }

    public static String getUnManagedInstalledPackages() {
        return INSTALL_PACKAGES.getSoql() + " WHERE IsManaged = false";
    }

    public static String getManagedInstalledPackages() {
        return INSTALL_PACKAGES.getSoql() + " WHERE IsManaged = true";
    }

    public static String getManagedInstalledPackages(String... managedInstalledPkgNames) {
        StringBuffer soql = new StringBuffer(INSTALL_PACKAGES.getSoql());
        if (Utils.isNotEmpty(managedInstalledPkgNames)) {
            soql.append(" WHERE IsManaged = true");
            for (String installedPkgName : managedInstalledPkgNames) {
                soql.append(" AND Name = '").append(installedPkgName).append("'");
            }
        }
        return soql.toString();
    }

    public static String getSchemaInitalizationQuery() {
        return "SELECT " + USER.getSoql() + " LIMIT 10";
    }

    public static String getScontrolsByContentSource(String contentSource) {
        return "SELECT " + SCONTROLS.getSoql() + " Where ContentSource = '" + contentSource + "'";
    }
}
