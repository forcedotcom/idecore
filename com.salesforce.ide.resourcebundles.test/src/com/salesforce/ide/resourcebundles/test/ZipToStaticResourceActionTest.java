package com.salesforce.ide.resourcebundles.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.ide.resourcebundles.ZipToStaticResourceAction;

public class ZipToStaticResourceActionTest {

    private Helper helper;
    private ZipToStaticResourceAction action;
    private IFolder bundle;
    
    @Before
    public void setUp() throws Exception {
        
        helper = new Helper();
        helper.createProject();
        
        action = new ZipToStaticResourceAction();
        
        bundle = helper.addBundleFolder("SampleExpanded.resource");
        
        helper.addBundleFile(new Path("SampleExpanded.resource").append("css").append("demo_page.css"));
        helper.addBundleFile(new Path("SampleExpanded.resource").append("images").append("back_disabled.png"));
        helper.addBundleFile(new Path("SampleExpanded.resource").append("images").append("sort_desc.png"));
        helper.addBundleFile(new Path("SampleExpanded.resource").append("license-bsd.txt"));
    }
    
    @After
    public void tearDown() throws Exception {
        
        helper.deleteProject();
    }
    
    @Test
    public void zip() throws Exception {
        
        IAction a = null;
        action.selectionChanged(a, new StructuredSelection(bundle));
        action.run(a);
        
        helper.waitForJobsToFinish();
        
        IFile meta = helper.resources.getFile("SampleExpanded.resource-meta.xml");
        IFile content = helper.resources.getFile("SampleExpanded.resource");
        
        helper.assertContent("ApplicationZip.resource-meta.xml", meta);
        helper.assertFileSize(content, 3731);
    }
}
