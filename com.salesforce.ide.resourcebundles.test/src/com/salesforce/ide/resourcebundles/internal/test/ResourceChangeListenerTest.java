package com.salesforce.ide.resourcebundles.internal.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.resourcebundles.internal.Operations;
import com.salesforce.ide.resourcebundles.test.Helper;

public class ResourceChangeListenerTest {

    private Helper helper;
    
    @Before
    public void setUp() throws Exception {
        
        helper = new Helper();
        helper.createProject();
    }
    
    @After
    public void tearDown() throws Exception {
        
        helper.deleteProject();
    }
    
    private void setPreferences(boolean zip, boolean unzip) throws Exception {
        
         IEclipsePreferences prefs = ContainerDelegate.getInstance().getServiceLocator()
                 .getProjectService().getPreferences(helper.project);
         prefs.putBoolean(Constants.PROP_ZIP_RESOURCE_BUNDLES_AUTOMATICALLY, zip);
         prefs.putBoolean(Constants.PROP_UNZIP_STATIC_RESOURCES_AUTOMATICALLY, unzip);
         prefs.flush();
    }
    
    @Test
    public void unzippedBundleAutomaticallyCreatedOn() throws Exception {
        
        unzippedBundleAutomaticallyCreated(true);
    }
        
    @Test
    public void unzippedBundleAutomaticallyCreateOff() throws Exception {
        
        unzippedBundleAutomaticallyCreated(false);
    }

    private void unzippedBundleAutomaticallyCreated(boolean unzipPreference) throws Exception {
        
        setPreferences(true, unzipPreference);

        IFile content1 = helper.addResourceFile("SampleZip1.resource");
        IFile content2 = helper.addResourceFile("SampleZip1.resource-meta.xml");
        
        helper.addResourceFile("SampleZip2.resource");
        helper.addResourceFile("SampleZip2.resource-meta.xml");
        
        helper.waitForJobsToFinish();
        
        IFolder bundle1 = helper.bundles.getFolder("SampleZip1.resource");
        IFolder bundle2 = helper.bundles.getFolder("SampleZip2.resource");
        
        assertEquals(unzipPreference, bundle1.exists());
        assertEquals(unzipPreference, bundle2.exists());
        
        if (unzipPreference) {
            helper.assertFolderAndFileCounts(bundle1, 1 + 3, 5 + 13 + 3 + 1);
            helper.assertFolderAndFileCounts(bundle2, 1 + 3 + 1, 3 + 2 + 1);
            
            // No problems marked on ZIP files
            helper.assertMarkers(0, Operations.UNZIP_PROBLEM_MARKER_TYPE, content1);
            helper.assertMarkers(0, Operations.UNZIP_PROBLEM_MARKER_TYPE, content2);
        }
    }
    
    @Test
    public void unzippedBundleAutomaticallyDeleted() throws Exception {

        helper.addResourceFile("SampleZip1.resource");
        IFile meta = helper.addResourceFile("SampleZip1.resource-meta.xml");
        
        helper.waitForJobsToFinish();
        
        IFolder bundle = helper.bundles.getFolder("SampleZip1.resource");
        assertEquals(true, bundle.exists());
        
        meta.delete(true, null);
        
        helper.waitForJobsToFinish();
        assertEquals(false, bundle.exists());
    }
    
    @Test
    public void unzippedBundleAutomaticallyChanged() throws Exception {

        helper.addResourceFile("SampleZip1.resource");
        helper.addResourceFile("SampleZip1.resource-meta.xml");
        IFolder bundle = helper.bundles.getFolder("SampleZip1.resource");
        
        helper.waitForJobsToFinish();
        helper.assertFolderAndFileCounts(bundle, 1 + 3, 5 + 13 + 3 + 1);
        
        helper.replaceResourceFileContent("SampleZip1.resource", "SampleZip1WithLessContent.resource");
        
        helper.waitForJobsToFinish();
        helper.assertFolderAndFileCounts(bundle, 1 + 2, 2 + 2);
    }
    
    @Test
    public void zippedResourceAutomaticallyCreatedOn() throws Exception {
        
        zippedResourceAutomaticallyCreated(true);
    }
        
    @Test
    public void zippedResourceAutomaticallyCreatedOff() throws Exception {
        
        zippedResourceAutomaticallyCreated(false);
    }
            
    private void zippedResourceAutomaticallyCreated(boolean zipPreference) throws Exception {
        
        setPreferences(zipPreference, true);
        
        helper.addBundleFile(new Path("SampleExpanded.resource").append("css").append("demo_page.css"));
        helper.addBundleFile(new Path("SampleExpanded.resource").append("images").append("back_disabled.png"));
        helper.addBundleFile(new Path("SampleExpanded.resource").append("images").append("sort_desc.png"));
        helper.addBundleFile(new Path("SampleExpanded.resource").append("license-bsd.txt"));
        
        helper.waitForJobsToFinish();
        
        IFile meta = helper.resources.getFile("SampleExpanded.resource-meta.xml");
        IFile content = helper.resources.getFile("SampleExpanded.resource");
        
        assertEquals(zipPreference, meta.exists());
        assertEquals(zipPreference, content.exists());
        
        if (zipPreference) {
            helper.assertFileSize(content, 3731);
            helper.assertContent("ApplicationZip.resource-meta.xml", meta);
            
            // No problems marked on bundle folder
            helper.assertMarkers(0, Operations.ZIP_PROBLEM_MARKER_TYPE, helper.bundles.getFolder("SampleExpanded.resource"));
        }
    }
    
    @Test
    public void zippedResourceAutomaticallyDeleted() throws Exception {
        
        IFolder bundleFolder = helper.addBundleFolder("SampleExpanded.resource");
        helper.addBundleFile(new Path("SampleExpanded.resource").append("license-bsd.txt"));
        
        helper.waitForJobsToFinish();
        
        IFile content = helper.resources.getFile("SampleExpanded.resource");
        IFile meta = helper.resources.getFile("SampleExpanded.resource-meta.xml");
        assertEquals(true, content.exists());
        assertEquals(true, meta.exists());
        
        bundleFolder.delete(true, null);
        
        helper.waitForJobsToFinish();
        assertEquals(false, content.exists());
        assertEquals(false, meta.exists());
    }
    
    @Test
    public void unzipProblemMarker() throws Exception {
        
        IFile content = helper.addResourceFile("CorruptZip.resource");
        helper.addResourceFile("CorruptZip.resource-meta.xml");
        
        helper.waitForJobsToFinish();
        
        helper.assertMarkers(1, Operations.UNZIP_PROBLEM_MARKER_TYPE, content);
    }
    
    @Test
    public void zipProblemMarker() throws Exception {
        
        // Bundle will be zipped to this location
        IFile resourceFile = helper.resources.getFile("SampleExpanded.resource");
        resourceFile.create(new ByteArrayInputStream(new byte[] {}), IResource.FORCE, null);
        
        // Set to read only force an error
        ResourceAttributes attributes = resourceFile.getResourceAttributes();
        attributes.setReadOnly(true);
        resourceFile.setResourceAttributes(attributes);
        
        try {
            // Single file bundle
            IFolder bundle = (IFolder) helper.addBundleFile(new Path("SampleExpanded.resource").append("license-bsd.txt")).getParent();
            
            helper.waitForJobsToFinish();
            
            helper.assertMarkers(1, Operations.ZIP_PROBLEM_MARKER_TYPE, bundle);
        } finally {
            // So tear down can remove the project
            attributes.setReadOnly(false);
            resourceFile.setResourceAttributes(attributes);
        }
    }
}
