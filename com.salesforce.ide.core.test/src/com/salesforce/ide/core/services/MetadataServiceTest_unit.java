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
package com.salesforce.ide.core.services;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.sforce.soap.metadata.FileProperties;

public class MetadataServiceTest_unit extends TestCase {

    private MetadataService metadataService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        metadataService = new MetadataService();
    }

    public void testfilterStandardObjectsFromFileProperties() throws Exception {
        FileProperties fileProps1 = new FileProperties();
        fileProps1.setType(Constants.CUSTOM_OBJECT);
        final String someCustomObjectName = "foo__c";
        fileProps1.setFullName(someCustomObjectName);

        FileProperties fileProps2 = new FileProperties();
        fileProps2.setType(Constants.STANDARD_OBJECT);
        fileProps2.setFullName("Account");

        assertEquals(1, metadataService.filterStandardObjectsFromFileProperties(new FileMetadataExt(fileProps1))
                .getFileProperties().length);

        assertEquals(0, metadataService.filterStandardObjectsFromFileProperties(new FileMetadataExt(fileProps2))
                .getFileProperties().length);

        final FileProperties[] resultantFileProperties =
                metadataService.filterStandardObjectsFromFileProperties(new FileMetadataExt(fileProps1, fileProps2))
                        .getFileProperties();
        assertEquals(1, resultantFileProperties.length);
        assertEquals(someCustomObjectName, resultantFileProperties[0].getFullName());
    }

    public void testFilterComponentTypesFromFileProperties() throws Exception {
        FileProperties customObject = new FileProperties();
        customObject.setType(Constants.CUSTOM_OBJECT);
        customObject.setFullName("foo__c");
        customObject.setFileName("foo__c");

        FileProperties standardObjectWithCObjType = new FileProperties();
        standardObjectWithCObjType.setType(Constants.CUSTOM_OBJECT);
        standardObjectWithCObjType.setFullName("Case");
        standardObjectWithCObjType.setFileName("Case");

        FileProperties standardObject = new FileProperties();
        standardObject.setType(Constants.STANDARD_OBJECT);
        standardObject.setFullName("Account");
        standardObject.setFileName("Account");

        FileProperties otherType = new FileProperties();
        otherType.setType("foo");
        otherType.setFullName("fooname");
        otherType.setFileName("fooname");

        assertEquals(0, metadataService.filterComponentTypesFromFileProperties(new FileMetadataExt(customObject),Constants.CUSTOM_OBJECT).getFilePropertiesCount());
        assertEquals(0, metadataService.filterComponentTypesFromFileProperties(new FileMetadataExt(customObject,standardObjectWithCObjType),Constants.CUSTOM_OBJECT,Constants.STANDARD_OBJECT).getFilePropertiesCount());
        assertEquals(0, metadataService.filterComponentTypesFromFileProperties(new FileMetadataExt(standardObject),Constants.STANDARD_OBJECT).getFilePropertiesCount());
        assertEquals(0, metadataService.filterComponentTypesFromFileProperties(new FileMetadataExt(otherType), "foo").getFilePropertiesCount());
        assertEquals(2, metadataService.filterComponentTypesFromFileProperties(new FileMetadataExt(customObject, standardObject), "foo").getFilePropertiesCount());


        final FileMetadataExt fileMetadataExt = new FileMetadataExt(customObject, standardObject, otherType);
        FileProperties[] filteredResult =
                metadataService.filterComponentTypesFromFileProperties(fileMetadataExt, Constants.CUSTOM_OBJECT)
                        .getFileProperties();
        final Collection<String> names =
                Collections2.transform(Arrays.asList(filteredResult), new Function<FileProperties, String>() {

                    public String apply(FileProperties arg0) {
                        return arg0.getFullName();
                    }
                });
        assertEquals(2, names.size());
        assertFalse(names.contains("foo__c"));
    }

    public void testGetObjectFileProperties() throws Exception {
        final Connection connection = mock(Connection.class);
        final IProgressMonitor monitor = new NullProgressMonitor();
        final MetadataService metadataService2 = spy(new MetadataService());
        final Component component = mock(Component.class);
        final ComponentFactory componentFactory = mock(ComponentFactory.class);
        when(componentFactory.getComponentByComponentType(anyString())).thenReturn(component);
        doReturn(componentFactory).when(metadataService2).getComponentFactory();

        FileProperties fileProps = new FileProperties();
        fileProps.setType(Constants.CUSTOM_OBJECT);
        fileProps.setFullName("foo__kav");
        FileProperties fileProps2 = new FileProperties();
        fileProps2.setType(Constants.STANDARD_OBJECT);
        fileProps2.setFullName("foo");
        doReturn(new FileMetadataExt(fileProps, fileProps2)).when(metadataService2).listMetadata(eq(connection),
            eq(component), eq(monitor));
        FileProperties[] resultantFileProperties =
                metadataService2.getObjectFileProperties(connection, monitor,
                    CustomObjectNameResolver.getCheckerForCustomObject()).getFileProperties();
        assertEquals(1, resultantFileProperties.length);
        assertEquals("foo__kav", resultantFileProperties[0].getFullName());

        resultantFileProperties =
                metadataService2.getObjectFileProperties(connection, monitor,
                    CustomObjectNameResolver.getCheckerForStandardObject()).getFileProperties();
        assertEquals(1, resultantFileProperties.length);
        assertEquals("foo", resultantFileProperties[0].getFullName());
    }

}
