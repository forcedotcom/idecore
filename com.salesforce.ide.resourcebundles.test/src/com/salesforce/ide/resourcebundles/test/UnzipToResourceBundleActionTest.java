package com.salesforce.ide.resourcebundles.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.ide.resourcebundles.UnzipToResourceBundleAction;

public class UnzipToResourceBundleActionTest {

    private Helper helper;
    private UnzipToResourceBundleAction action;
    private IFile content;
    private IFile meta;
    
    @Before
    public void setUp() throws Exception {
        
        helper = new Helper();
        helper.createProject();
        
        action = new UnzipToResourceBundleAction();
        content = helper.addResourceFile("SampleZip1.resource");
        meta = helper.addResourceFile("SampleZip1.resource-meta.xml");
    }
    
    @After
    public void tearDown() throws Exception {
        
        helper.deleteProject();
    }
    
    @Test
    public void unzipContentSelected() throws Exception {
        
        unzip(content);
    }
        
    @Test
    public void unzipMetaSelected() throws Exception {
        
        unzip(meta);
    }
        
    private void unzip(IFile file) throws Exception {
        
        IAction a = null;
        action.selectionChanged(a, new StructuredSelection(file));
        action.run(a);
        
        helper.waitForJobsToFinish();
        
        helper.assertFolderAndFileCounts(helper.bundles.getFolder("SampleZip1.resource"), 3 + 1, 5 + 13 + 3 + 1);
    }
}
