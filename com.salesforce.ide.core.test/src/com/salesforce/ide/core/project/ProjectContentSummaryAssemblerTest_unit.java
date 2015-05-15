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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.project.ProjectContentSummaryAssembler.ComponentValidator;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;

public class ProjectContentSummaryAssemblerTest_unit extends TestCase {
    private ProjectContentSummaryAssembler assembler;

    public void testHasWildCard() throws Exception {
        ProjectContentSummaryAssembler assembler = new ProjectContentSummaryAssembler();
        assertFalse(assembler.hasWildcard(null));

        List<String> members = new ArrayList<String>();
        members.add("foo");
        assertFalse(assembler.hasWildcard(members));
        members.add("*");
        assertTrue(assembler.hasWildcard(members));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assembler = new ProjectContentSummaryAssembler();
    }

    @SuppressWarnings("unchecked")
    public void testReplaceWildCardWithNames() throws Exception {
        Set<String> members = new TreeSet<String>();
        PackageTypeMembers typeStanza = mock(PackageTypeMembers.class);
        List<String> memberList = Lists.newArrayList("foo", "*", "bar");
        when(typeStanza.getMembers()).thenReturn(memberList);
        FileMetadataExt fileMetadataExt = mock(FileMetadataExt.class);
        when(fileMetadataExt.hasFileProperties()).thenReturn(true);
        when(fileMetadataExt.getComponentNamesByComponentType(anyString())).thenReturn(null,
            Lists.newArrayList("panda"));
        assembler.resolveWildcardToNames(members, typeStanza, fileMetadataExt);
        assertTrue(members.isEmpty());
        assembler.resolveWildcardToNames(members, typeStanza, fileMetadataExt);
        assertFalse(members.isEmpty());
        assertEquals(1, members.size());
        verify(fileMetadataExt, times(2)).getComponentNamesByComponentType(anyString());
    }

    public void testCustomObjectContentValidator() throws Exception {
        ComponentValidator validator = assembler.getCustomObjectComponentValidator();
        assertTrue(validator.isValidMember("foo__c"));
        assertTrue(validator.isValidMember("foo__kav"));
        assertFalse(validator.isValidMember("Account"));

        assertFalse(validator.isValidChildOfMember("foo", "foobar"));
        assertTrue(validator.isValidChildOfMember("foo__c", "foo__c.bar"));
        assertEquals("bar", validator.getChildDisplayName("foo__c", "foo__c.bar"));
    }

    public void testStandardObjectContentValidator() throws Exception {
        ComponentValidator validator = assembler.getStandardObjectComponentValidator();
        assertFalse(validator.isValidMember("foo__c"));
        assertFalse(validator.isValidMember("foo__kav"));
        assertFalse(validator.isValidMember("*"));
        assertTrue(validator.isValidMember("Account"));

        assertTrue(validator.isValidChildOfMember("foo", "foobar"));
        assertFalse(validator.isValidChildOfMember("foo__c", "foo__c.bar"));
        assertEquals("bar", validator.getChildDisplayName("foo", "foo.bar"));
    }

    public void testgetParentCustomObjectNames() throws Exception {
        Component component = mock(Component.class);
        when(component.getComponentType()).thenReturn(Constants.CUSTOM_OBJECT);
        PackageTypeMembers  typeStanza = mock(PackageTypeMembers.class);
        when(typeStanza.getMembers()).thenReturn(Lists.newArrayList("foo","*"));
        FileMetadataExt fileMetadatExt = mock(FileMetadataExt.class);
        when(fileMetadatExt.getComponentNamesByComponentType(anyString())).thenReturn(Lists.newArrayList("foo"));
        
        List<String> parentCustomObjectNames = assembler.getParentCustomObjectNames(component, typeStanza, fileMetadatExt);
        verify(fileMetadatExt,times(1)).getComponentNamesByComponentType(anyString());
        assertEquals(1, parentCustomObjectNames.size());
        assertEquals("foo", parentCustomObjectNames.get(0));
    }
    public void testgetParentCustomObjectNames_WhenNoFileMetadataExt() throws Exception {
        Component component = mock(Component.class);
        when(component.getComponentType()).thenReturn(Constants.CUSTOM_OBJECT);
        PackageTypeMembers  typeStanza = mock(PackageTypeMembers.class);
        when(typeStanza.getMembers()).thenReturn(Lists.newArrayList("foo","*"));
        FileMetadataExt fileMetadatExt = null;
        List<String> parentCustomObjectNames = assembler.getParentCustomObjectNames(component, typeStanza, fileMetadatExt);
        assertEquals(1, parentCustomObjectNames.size());
        assertEquals("foo", parentCustomObjectNames.get(0));
    }

    public void testGenerateObjectChildrenSummary_ForCustomObj_PrependsObjectTypeName() throws Exception {
        ProjectContentSummaryAssembler assembler = spy(new ProjectContentSummaryAssembler());
        Component component = mock(Component.class);
        when(component.getComponentType()).thenReturn(Constants.CUSTOM_OBJECT);
        PackageTypeMembers typeStanza=mock(PackageTypeMembers.class);
        when(typeStanza.getMembers()).thenReturn(Lists.newArrayList("*","cobj__c"));
        FileMetadataExt fileMetadatExt = mock(FileMetadataExt.class);
        final List<String> emptyList = Collections.emptyList();
        final Map<String, List<String>> emptyMap = new HashMap<String, List<String>>();
        doReturn(emptyList).when(assembler).getParentCustomObjectNames(component, typeStanza,fileMetadatExt);
        PackageManifestModel packageManifestModel = mock(PackageManifestModel.class);
        when(packageManifestModel.getFileMetadatExt()).thenReturn(fileMetadatExt);
        Package packageManifest = mock(Package.class);
        doReturn(emptyMap).when(assembler).getComponentTypeMembers(eq(component), eq(packageManifestModel), eq(packageManifest ), eq(emptyList));
        final ComponentValidator validator = assembler.getCustomObjectComponentValidator();
        doReturn("foo").when(assembler).generateChildrenSummary(eq(component), eq(emptyList), eq(packageManifestModel), eq(emptyMap), eq(validator));
        
        final String summary = assembler.generateObjectChildrenSummary(component, typeStanza, packageManifestModel, packageManifest, validator);
        assertTrue(summary.startsWith("Objects - Custom (Subscribed to"));
        assertFalse(summary.contains("Objects - Standard"));
    }
}
