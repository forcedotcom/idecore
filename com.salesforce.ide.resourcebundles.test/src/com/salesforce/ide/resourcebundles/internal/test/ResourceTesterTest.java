package com.salesforce.ide.resourcebundles.internal.test;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.ide.resourcebundles.internal.ResourceTester;
import com.salesforce.ide.resourcebundles.test.Helper;

public class ResourceTesterTest {

    private Helper helper;
    private ResourceTester tester;
    
    @Before
    public void setUp() throws Exception {
        
        helper = new Helper();
        helper.createProject();
        tester = new ResourceTester();
    }
    
    @After
    public void tearDown() throws Exception {
        
        helper.deleteProject();
    }

    @Test
    public void none() throws Exception {
        
        assertEquals(false, tester.test(helper.resources, ResourceTester.IS_SINGLE_ZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(helper.resources, ResourceTester.IS_MULTIPLE_ZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(helper.bundles, ResourceTester.IS_SINGLE_UNZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(helper.bundles, ResourceTester.IS_MULTIPLE_UNZIPPABLE_PROPERTY, null, null));
    }
    
    @Test
    public void bundles() throws Exception {
        
        // First
        IFolder folder = helper.addBundleFolder("abc" + ResourceTester.CONTENT_EXTENSION);
        
        assertEquals(true, tester.test(folder, ResourceTester.IS_SINGLE_ZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(folder, ResourceTester.IS_MULTIPLE_ZIPPABLE_PROPERTY, null, null));
        
        assertEquals(true, tester.test(helper.bundles, ResourceTester.IS_SINGLE_ZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(helper.bundles, ResourceTester.IS_MULTIPLE_ZIPPABLE_PROPERTY, null, null));
        
        // Second
        helper.addBundleFolder("def" + ResourceTester.CONTENT_EXTENSION);
        
        assertEquals(false, tester.test(helper.bundles, ResourceTester.IS_SINGLE_ZIPPABLE_PROPERTY, null, null));
        assertEquals(true, tester.test(helper.bundles, ResourceTester.IS_MULTIPLE_ZIPPABLE_PROPERTY, null, null));
    }
    
    @Test
    public void resources() throws Exception {
        
        // First
        IFile content = helper.addResourceFile("SampleZip1.resource");
        IFile meta = helper.addResourceFile("SampleZip1.resource-meta.xml");
        
        assertEquals(true, tester.test(content, ResourceTester.IS_SINGLE_UNZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(content, ResourceTester.IS_MULTIPLE_UNZIPPABLE_PROPERTY, null, null));
        
        assertEquals(true, tester.test(meta, ResourceTester.IS_SINGLE_UNZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(meta, ResourceTester.IS_MULTIPLE_UNZIPPABLE_PROPERTY, null, null));

        assertEquals(true, tester.test(helper.resources, ResourceTester.IS_SINGLE_UNZIPPABLE_PROPERTY, null, null));
        assertEquals(false, tester.test(helper.resources, ResourceTester.IS_MULTIPLE_UNZIPPABLE_PROPERTY, null, null));
        
        // Second
        helper.addResourceFile("SampleZip2.resource");
        helper.addResourceFile("SampleZip2.resource-meta.xml");
        
        assertEquals(false, tester.test(helper.resources, ResourceTester.IS_SINGLE_UNZIPPABLE_PROPERTY, null, null));
        assertEquals(true, tester.test(helper.resources, ResourceTester.IS_MULTIPLE_UNZIPPABLE_PROPERTY, null, null));
    }
}
