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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ManageableState;

public class UtilsTest_unit extends TestCase {

    public void testUtils_removeServiceLevelFromPluginVersion() throws Exception {
        String clientId = Utils.removeServiceLevelFromPluginVersion("13.0.0.qualifier");
        assertEquals("13.0.qualifier", clientId);
    }

    public void teststripNonAlphaNumericChars(){
    	assertEquals(Utils.stripNonAlphaNumericChars("__a__b__c__"), "a_b_c");
    	assertEquals(Utils.stripNonAlphaNumericChars("__a__b__c"), "a_b_c");
    	assertEquals(Utils.stripNonAlphaNumericChars("a__b__c__"), "a_b_c");
    	assertEquals(Utils.stripNonAlphaNumericChars("a__b__c"), "a_b_c");
    	assertEquals(Utils.stripNonAlphaNumericChars("ab c"), "ab c");
    	assertEquals(Utils.stripNonAlphaNumericChars("__a__#$%^* b__c__"), "a_ b_c");
    	assertEquals(Utils.stripNonAlphaNumericChars("#$%"), "");
    }

    public void testStringIsEqual() throws Exception {
        assertFalse(Utils.isEqual(null, null, true));
        assertFalse(Utils.isEqual(null, "", true));
        assertFalse(Utils.isEqual("", null, true));
        assertFalse(Utils.isEqual(null, "something", true));
        assertFalse(Utils.isEqual("something", null, true));
        assertFalse(Utils.isEqual("", "", true));
        assertFalse(Utils.isEqual("something", "nothing", true));
        assertTrue(Utils.isEqual("equal", "equal", true));
        assertTrue(Utils.isEqual("equal", "EqUaL", false));

        assertTrue(Utils.isNotEqual("equal", "not_equal"));
    }
    public void testObjectEmptiness(){
        //Strings
        String nullString = null;
        String emptyString = "";
        String nonEmptyString = "asdf";
        assertTrue("null String empty check should've returned true.", Utils.isEmpty(nullString));
        assertFalse("null String non-empty check should've returned false.", Utils.isNotEmpty(nullString));
        assertTrue("Empty String empty check should've returned true.", Utils.isEmpty(emptyString));
        assertFalse("Empty String non-empty check should've returned false.", Utils.isNotEmpty(emptyString));
        assertFalse("Non Empty String empty check should've returned false.", Utils.isEmpty(nonEmptyString));
        assertTrue("Non Empty String non-empty check should've returned true.", Utils.isNotEmpty(nonEmptyString));

        //Objects
        Object nullObject =  null;
        Object nonNullObject = emptyString;
        assertTrue("null Object empty check should've returned true.", Utils.isEmpty(nullObject));
        assertFalse("null Object non-empty check should've returned true.", Utils.isNotEmpty(nullObject));
        assertFalse("non null Object empty check should've returned false.", Utils.isEmpty(nonNullObject));
        assertTrue("non null Object non-empty check should've returned true.", Utils.isNotEmpty(nonNullObject));

        //object Array
        Object[] nullObjectArray = null;
        Object[] emptyObjectArray = new Object[0];
        Object[] nonEmptyObjectArray = new Object[1];
        assertTrue("nullObjectArray empty check should've returned true.", Utils.isEmpty(nullObjectArray));
        assertFalse("nullObjectArray non-empty check should've returned false.", Utils.isNotEmpty(nullObjectArray));
        assertTrue("emptyObjectArray empty check should've returned true.", Utils.isEmpty(emptyObjectArray));
        assertFalse("emptyObjectArray non-empty check should've returned false.", Utils.isNotEmpty(emptyObjectArray));
        assertFalse("nonEmptyObjectArray empty check should've returned false.", Utils.isEmpty(nonEmptyObjectArray));
        assertTrue("nonEmptyObjectArray non-empty check should've returned true.", Utils.isNotEmpty(nonEmptyObjectArray));
        //byte array
        byte[] nullByteArray = null;
        byte[] emptyByteArray = new byte[0];
        byte[] nonEmptyByteArray = new byte[1];
        assertTrue("nullbyteArray empty check should've returned true.", Utils.isEmpty(nullByteArray));
        assertFalse("nullbyteArray non-empty check should've returned false.", Utils.isNotEmpty(nullByteArray));
        assertFalse("nullbyteArray non-empty check should've returned false.", Utils.isNotEmpty(emptyByteArray));
        assertTrue("emptybyte array empty check should've returned true.", Utils.isEmpty(emptyByteArray));
        assertFalse("non emptybyte array empty check should've returned false.", Utils.isEmpty(nonEmptyByteArray));
        assertTrue("non emptybyte array non-empty check should've returned true.", Utils.isNotEmpty(nonEmptyByteArray));

        //collection
        Collection<String> nullCollection= null;
        Collection<String> emptyCollection= new ArrayList<String>();
        Collection<String> nonEmptyCollection= new ArrayList<String>();
        nonEmptyCollection.add("asdf");
        assertTrue("nullCollection empty check should've returned true.", Utils.isEmpty(nullCollection));
        assertFalse("nullCollection non-empty check should've returned false.", Utils.isNotEmpty(nullCollection));
        assertTrue("emptyCollection empty check should've returned true.", Utils.isEmpty(emptyCollection));
        assertFalse("emptyCollection non-empty check should've returned false.", Utils.isNotEmpty(emptyCollection));
        assertTrue("nonEmptyCollection non-empty check should've returned true.", Utils.isNotEmpty(nonEmptyCollection));
        assertFalse("nonEmptyCollection empty check should've returned false.", Utils.isEmpty(nonEmptyCollection));

        //list
        List<String> nullList= null;
        List<String> emptyList= new ArrayList<String>();
        List<String> nonEmptyList= new ArrayList<String>();
        nonEmptyList.add("asdf");
        assertTrue("nullList empty check should've returned true.", Utils.isEmpty(nullList));
        assertFalse("nullList non-empty check should've returned false.", Utils.isNotEmpty(nullList));
        assertTrue("emptyList empty check should've returned true.", Utils.isEmpty(emptyList));
        assertFalse("emptyList non-empty check should've returned false.", Utils.isNotEmpty(emptyList));
        assertTrue("nonEmptyList non-empty check should've returned true.", Utils.isNotEmpty(nonEmptyList));
        assertFalse("nonEmptyList empty check should've returned false.", Utils.isEmpty(nonEmptyList));

        //map
        Map<String, String> nullMap= null;
        Map<String, String> emptyMap= new HashMap<String, String>();
        Map<String, String> nonEmptyMap= new HashMap<String, String>();
        nonEmptyMap.put("key", "value");
        assertTrue("nullMap empty check should've returned true.", Utils.isEmpty(nullMap));
        assertFalse("nullMap non-empty check should've returned false.", Utils.isNotEmpty(nullMap));
        assertTrue("emptyMap empty check should've returned true.", Utils.isEmpty(emptyMap));
        assertFalse("emptyMap non-empty check should've returned false.", Utils.isNotEmpty(emptyMap));
        assertTrue("nonEmptyMap non-empty check should've returned true.", Utils.isNotEmpty(nonEmptyMap));
        assertFalse("nonEmptyMap empty check should've returned false.", Utils.isEmpty(nonEmptyMap));

    }

    public void testGetExtensionFromFilePath() throws Exception {
        assertEquals("cls", Utils.getExtensionFromFilePath("/admin@431223414741922.com/src/classes/AccountMerge.cls"));
        assertEquals("cls-meta.xml", Utils.getExtensionFromFilePath("/admin@431223414741922.com/src/classes/AccountMerge.cls-meta.xml"));
        assertEquals("scf", Utils.getExtensionFromFilePath("/admin@431223414741922.com/src/scontrols/AddAccountToHousehold.scf"));
        assertEquals("scf-meta.xml", Utils.getExtensionFromFilePath("/admin@431223414741922.com/src/scontrols/AddAccountToHousehold.scf-meta.xml"));
        assertEquals("txt", Utils.getExtensionFromFilePath("/fchang_demo@blitz01.de/src/documents/ideTestDocumentSubFolder/testDocument.txt"));
        assertEquals("txt-meta.xml", Utils.getExtensionFromFilePath("/fchang_demo@blitz01.de/src/documents/ideTestDocumentSubFolder/testDocument.txt-meta.xml"));
        assertNull("null should return for filePath w/o file extension",Utils.getExtensionFromFilePath("/fchang_demo@blitz01_de/src/documents/ideTestDocumentSubFolder-meta"));

        assertNotNull(Utils.getExtensionFromFilePath("/fchang_demo@blitz01.de/src/documents/ideTestDocumentSubFolder-meta.xml"));
        assertNotNull(Utils.getExtensionFromFilePath("/fchang_demo@blitz01.de/src/documents/ideTestDocumentSubFolder-meta"));
    }

    public void testCapFirstLetterAndLetterAfterToken() throws Exception {
        assertEquals("Apex.Stack.Emptystackexception", Utils.capFirstLetterAndLetterAfterToken("APEX.STACK.EMPTYSTACKEXCEPTION", ".", true));
        assertEquals("Apex.Stack.Emptystackexception", Utils.capFirstLetterAndLetterAfterToken("apex.stack.emptystackexception", ".", true));
        assertEquals("Apex.Stack", Utils.capFirstLetterAndLetterAfterToken("APEX.STACK", ".", true));
        assertEquals("Apex.Stack", Utils.capFirstLetterAndLetterAfterToken("aPEX.sTACk", ".", true));
        assertEquals("Apex", Utils.capFirstLetterAndLetterAfterToken("aPeX", ".", true));
        assertEquals("Apex", Utils.capFirstLetterAndLetterAfterToken("ApEx", ".", true));
        assertEquals("ApEx.stack.empTystackexcepTion", Utils.capFirstLetterAndLetterAfterToken("apex.stack.emptystackexception", "p", false));
        assertEquals("ApEx.stack", Utils.capFirstLetterAndLetterAfterToken("aPEX.sTACk", "p", false));
        assertEquals("ApEx", Utils.capFirstLetterAndLetterAfterToken("aPeX", "p", false));
    }

    public void testRemovePackagedFiles() throws Exception {
		// if input FileProperties is null, skip remove package file. return same file properties.
    	assertNull(Utils.removePackagedFiles(null, null));

		// if input FileProperties is empty, skip remove package file. return same file properties.
		FileProperties[] props = new FileProperties[0];
    	assertEquals(props, Utils.removePackagedFiles(props, null));

    	// if FileProperty is installed component, it should be removed
    	FileProperties prop1 = new FileProperties();
    	prop1.setManageableState(ManageableState.installed);
    	assertEquals(0, Utils.removePackagedFiles(new FileProperties[]{prop1}, null).length);

    	// if FileProperty is not installed component and have no namespace, it should  not be removed
    	prop1.setManageableState(ManageableState.unmanaged);
    	assertEquals(1, Utils.removePackagedFiles(new FileProperties[]{prop1}, null).length);

    	// if FileProperty is not installed component and file namespace is equal to org namesapce, it should  not be removed
    	String testNameSpace = "testNameSpace";
    	prop1.setManageableState(ManageableState.unmanaged);
    	prop1.setNamespacePrefix(testNameSpace);
    	assertEquals(1, Utils.removePackagedFiles(new FileProperties[]{prop1}, testNameSpace).length);
    }
    public void testInvalidChars() throws Exception {
        assertNotNull(Utils.getInvalidChars());

    }
}
