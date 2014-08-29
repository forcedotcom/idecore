package com.salesforce.ide.resourcebundles.internal.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.ide.resourcebundles.internal.MetaXmlHandler;
import com.salesforce.ide.resourcebundles.internal.ResourceTester;
import com.salesforce.ide.resourcebundles.test.Helper;

public class MetaXmlHandlerTest {
    
    private Helper helper;
    private MetaXmlHandler handler;
    
    @Before
    public void setUp() throws Exception {
        
        helper = new Helper();
        helper.createProject();
        handler = new MetaXmlHandler();
    }
    
    @After
    public void tearDown() throws Exception {
        
        helper.deleteProject();
    }
    
    @Test
    public void create() throws Exception {
        
        IFile meta = helper.resources.getFile("Abc" + ResourceTester.CONTENT_EXTENSION);
        assertFalse(meta.exists());
        
        handler.createOrUpdate(meta);
        assertTrue(meta.exists());
        
        helper.assertContent("ApplicationZip.resource-meta.xml", meta);
    }
    
    @Test
    public void update() throws Exception {
        
        IFile meta = helper.addBundleFile("TextPlain.resource-meta.xml");
        handler.createOrUpdate(meta);
        helper.assertContent("ApplicationZip.resource-meta.xml", meta);
    }
    
    @Test
    public void isZip() throws Exception {
        
        assertEquals(false, handler.isZip(helper.addBundleFile("TextPlain.resource-meta.xml")));
        assertEquals(false, handler.isZip(helper.addBundleFile("Junk.resource-meta.xml")));
        assertEquals(true, handler.isZip(helper.addBundleFile("ApplicationZip.resource-meta.xml")));
    }
}
