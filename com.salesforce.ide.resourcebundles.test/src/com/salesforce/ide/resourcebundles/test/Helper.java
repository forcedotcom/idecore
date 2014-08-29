package com.salesforce.ide.resourcebundles.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;

import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.resourcebundles.Activator;
import com.salesforce.ide.resourcebundles.internal.ResourceTester;
import com.salesforce.ide.resourcebundles.internal.Scheduler;

/**
 * Methods used by tests for setup and assertion.
 */
public class Helper {
    
    private static final IPath TEST_DATA_ROOT = new Path("test-data");
    
    public IProject project;
    public IFolder src;
    public IFolder resources;
    public IFolder bundles;

    /**
     * Creates a Force.com project (via nature).
     */
    public IProject createProject() throws Exception {
        
        project = ResourcesPlugin.getWorkspace().getRoot().getProject("project-" + System.currentTimeMillis());
        project.create(null);
        project.open(null);
        
        // Force.com nature
        IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] {DefaultNature.NATURE_ID});
        project.setDescription(description, null);
        
        src = project.getFolder(ResourceTester.SRC_FOLDER_NAME);
        src.create(true, true, null);
        
        resources = src.getFolder(ResourceTester.RESOURCE_FOLDER_NAME);
        resources.create(true, true, null);
        
        bundles = project.getFolder(ResourceTester.BUNDLE_FOLDER_NAME);
        bundles.create(true, true, null);
        
        return project;
    }
    
    /**
     * For cleanup.
     */
    public Helper deleteProject() throws Exception {
        
        project.delete(true,  null);
        
        return this;
    }
    
    /**
     * Because the work is done through jobs, if this isn't called to wait for the jobs to finish,
     * bad things such as the project being deleted and then the job running will happen.
     * So ALWAYS call this.
     */
    public void waitForJobsToFinish() throws OperationCanceledException, InterruptedException {
        
        Job.getJobManager().join(Scheduler.JOB_FAMILY, null);
    }
    
    /**
     * Create named folder in project resourcebundles folder.
     */
    public IFolder addBundleFolder(String name) throws Exception {
        
        IFolder folder = bundles.getFolder(name);
        folder.create(true, true, null);
        
        return folder;
    }
    
    /**
     * Copy named file from test data to project src/staticresources folder.
     */
    public IFile addBundleFile(String testDataName) throws Exception {
        
        return addBundleFile(new Path(testDataName));
    }
    
    /**
     * Copy file identified by path from test data to project resourcebundles folder
     * with same path creating folders as needed.
     */
    public IFile addBundleFile(IPath testDataPath) throws Exception {
        
        InputStream is = open(testDataPath);
        
        IFolder parent = bundles;
        for (int i = 0; i < testDataPath.segmentCount() - 1; i++) {
            parent = parent.getFolder(testDataPath.segment(i));
            if (!parent.exists()) {
                parent.create(true, true, null);
            }
        }
        
        IFile file = parent.getFile(testDataPath.lastSegment());
        file.create(is,  true,  null);
        return file;
    }
    
    /**
     * Copy named file from test data to project src/staticresources folder.
     */
    public IFile addResourceFile(String testDataName) throws Exception {
        
        InputStream is = open(new Path(testDataName));
        IFile file = resources.getFile(testDataName);
        file.create(is,  true,  null);
        
        return file;
    }
    
    public IFile replaceResourceFileContent(String existingResourceFileName, String replacementDataName) throws Exception {
        
        InputStream is = open(new Path(replacementDataName));
        IFile file = resources.getFile(existingResourceFileName);
        file.setContents(is, IResource.FORCE, null);
        
        return file;
    }
    
    public void assertContent(String testDataName, IFile actualFile) throws Exception {
        
        String fileString = readString(actualFile.getContents());
        String testDataString = readString(open(new Path(testDataName)));
        assertEquals(testDataString, fileString);
    }
    
    public void assertFolderAndFileCounts(IFolder folder, int expectedFolderCount, int expectedFileCount) throws Exception {
        
        class CountingVisitor implements IResourceVisitor {
           
            int folderCount = 0;
            int fileCount = 0;

            @Override
            public boolean visit(IResource resource) throws CoreException {
                if (resource instanceof IFile) {
                    fileCount++;
                } else if (resource instanceof IFolder) {
                    folderCount++;
                }
                return true;
            } 
        };
        
        CountingVisitor v = new CountingVisitor();
        folder.accept(v);
        
        assertEquals(expectedFolderCount, v.folderCount);
        assertEquals(expectedFileCount, v.fileCount);
    }
    
    public void assertFileSize(IFile actualFile, int expectedSize) throws Exception {
        
        assertEquals(expectedSize, readBytes(actualFile.getContents()).length);
    }
    
    public void assertMarkers(int expectedMarkers, String type, IResource resource) throws Exception {
        
        assertEquals(expectedMarkers, resource.findMarkers(type, false, 0).length);
    }
    
    private String readString(InputStream is) throws Exception {
        
        return new String(readBytes(is), Charset.forName("UTF-8"));    
    }
    
    private byte[] readBytes(InputStream is) throws Exception {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[8192];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.flush();
        return bos.toByteArray();   
    }
    
    private InputStream open(IPath testDataPath) throws Exception {
        
        return FileLocator.openStream(Activator.getDefault().getBundle(), TEST_DATA_ROOT.append(testDataPath), false);
    }
}
