package com.salesforce.ide.resourcebundles.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.salesforce.ide.resourcebundles.Activator;

/**
 * Outer class used to share implementation detail with the inner classes.
 */
public class Operations {
    
    // Custom marker types (defined in plugin.xml) so can be added and deleted independently
    public static final String ZIP_PROBLEM_MARKER_TYPE = Activator.PLUGIN_ID + ".problemmarker.zip";
    public static final String UNZIP_PROBLEM_MARKER_TYPE = Activator.PLUGIN_ID + ".problemmarker.unzip";
    
    private static final String MACOSX_JUNK_PREFIX = "__MACOSX";
    
    private static final int ZIP_FILE_BUFFER_SIZE = 1024 * 1024;
    private static final int ZIP_ENTRY_BUFFER_SIZE = 64 * 1024;
    
    /**
     * IFile writing closes the InputStream but the ZipInputStream needs to be left open so wrap in this.
     */
    private static class NoCloseInputStream extends InputStream {

        private InputStream in;

        public NoCloseInputStream(InputStream in) {
            this.in = in;
        }

        /**
         * Has no effect.
         */
        @Override
        public void close() throws IOException {
            // Intentionally does nothing
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return in.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void mark(int readlimit) {
            in.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            in.reset();
        }
        
        @Override
        public boolean markSupported() {
            return in.markSupported();
        }
    }
    
    /**
     * Common operation interface.
     */
    public interface Runnable {
        void run(IProgressMonitor monitor);
        String getNameForProgress();
    }
    
    /**
     * Convert resource bundle folders and files into ZIP files.
     */
    public static class Zip implements Runnable {
        
        private Collection<IFolder> folders;
        private MetaXmlHandler handler = new MetaXmlHandler();
        
        public Zip(Collection<IFolder> folders) {
            this.folders = folders;
        }
        
        public String getNameForProgress() {
            return "Zipping";
        }
        
        public void run(IProgressMonitor monitor) {
            
            monitor.beginTask(MessageFormat.format("Zipping {0} resource bundles to static resource", folders.size()), folders.size());
            try {
                for (IFolder folder : folders) {
                    try {
                        createZip(folder);
                        removeErrorMarkerCarefully(folder, ZIP_PROBLEM_MARKER_TYPE);
                    } catch (Exception e) {
                        try {
                            String message = "Problem zipping:" + (e.getMessage() != null ? " " + e.getMessage() : "") + " (" + String.valueOf(e) + ")";
                            addErrorMarkerCarefully(folder, ZIP_PROBLEM_MARKER_TYPE, message);
                        } catch (Exception ee) {
                            Activator.log(ee);
                        }
                    }
                    monitor.worked(1);
                }
            } finally {
                monitor.done();
            }
        }

        private void createZip(IFolder rootFolder) throws Exception {
            
            IFile zipFile = getResourceFolder(rootFolder.getProject()).getFile(rootFolder.getName());
            ensureAllFoldersExist(zipFile);

            // Content
            ByteArrayOutputStream bos = new ByteArrayOutputStream(ZIP_FILE_BUFFER_SIZE); 
            ZipOutputStream zos = new ZipOutputStream(bos);
            try {
                addToZip(rootFolder, zos);
            } finally {
                zos.close();
            }
            if (zipFile.exists()) {
                zipFile.setContents(new ByteArrayInputStream(bos.toByteArray()), IResource.FORCE, new NullProgressMonitor());
            } else {
                zipFile.create(new ByteArrayInputStream(bos.toByteArray()), IResource.FORCE, new NullProgressMonitor());
            }
            
            // Meta
            handler.createOrUpdate(ResourceTester.getMetaFile(zipFile));
        }
        
        private void addToZip(IFolder folder, ZipOutputStream zos) throws CoreException, IOException {
            
            for (IResource resource : folder.members()) {
                if (resource.getType() == IResource.FOLDER) {
                    addToZip((IFolder) resource, zos);
                } else if (resource.getType() == IResource.FILE) {
                    IFile file = (IFile) resource;
                    ZipEntry entry = new ZipEntry(zipFileRelativePath(file));
                    zos.putNextEntry(entry); 
                    transfer(file.getContents(), zos);
                    zos.closeEntry();
                }
            }
        }
        
        protected String zipFileRelativePath(IFile file) {
            
            // First segment is bundle folder and second segment is folder whole name becomes the zip resource name
            return file.getProjectRelativePath().removeFirstSegments(2).toString();
        }
    }
    
    /**
     * Extract from ZIP files into resource bundle folders and files.
     */
    public static class Unzip implements Runnable {
        
        private Collection<IFile> candidates;
        private MetaXmlHandler handler = new MetaXmlHandler();
        
        /**
         * Files expected to be either resource files or resource meta files but not necessarily ZIP files.
         */
        public Unzip(Collection<IFile> candidates) {
            this.candidates = candidates;
        }
        
        public String getNameForProgress() {
            return "Unzipping";
        }
        
        public void run(IProgressMonitor monitor) {

            Collection<IFile> filtered = zipContentFilesOnly(candidates);
            if (filtered.size() > 0) {
                processZipFiles(filtered, monitor);
            }
        }
        
        private Collection<IFile> zipContentFilesOnly(Collection<IFile> files) {

            Set<IFile> filtered = new LinkedHashSet<IFile>();
            for (IFile file : files) {
                
                IFile content = ResourceTester.getContentFile(file);
                IFile meta = ResourceTester.getMetaFile(file);
                try {
                    if (handler.isZip(meta)) {
                        filtered.add(content);
                    }
                } catch (Exception e) {
                    Activator.log(e);
                }
            }
            
            return filtered;
        }
        
        private void processZipFiles(Collection<IFile> files, IProgressMonitor monitor) {
            
            monitor.beginTask(MessageFormat.format("Unzipping {0} static resource files to resource bundles", files.size()), files.size());
            try {
                for (IFile file : files) {
                    try {
                        expandZip(file);
                        removeErrorMarkerCarefully(file, UNZIP_PROBLEM_MARKER_TYPE);
                    } catch (Exception e) {
                        try {
                            String message = "Problem unzipping:" + (e.getMessage() != null ? " " + e.getMessage() : "") + " (" +String.valueOf(e) + ")";
                            addErrorMarkerCarefully(file, UNZIP_PROBLEM_MARKER_TYPE, message);
                        } catch (Exception ee) {
                            Activator.log(ee);
                        }
                    }
                    monitor.worked(1);
                }
            } finally {
                monitor.done();
            }
        }
        
        private void expandZip(IFile zipFile) throws CoreException, IOException {
            
            IFolder destinationFolder = getBundleFolder(zipFile.getProject()).getFolder(zipFile.getName());
            ensureAllFoldersExist(destinationFolder);
            
            final Set<IResource> existingResources = new HashSet<IResource>();
            IResourceVisitor v = new IResourceVisitor() {
                @Override
                public boolean visit(IResource resource) throws CoreException {
                    existingResources.add(resource);
                    return true;
                }
            };
            destinationFolder.accept(v);
            
            final Set<IResource> requiredResources = new HashSet<IResource>();
            requiredResources.add(destinationFolder);
            ZipInputStream zis = new ZipInputStream(zipFile.getContents());
            try {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (keep(entry)) {
                        if (entry.isDirectory()) {
                            IFolder folder = destinationFolder.getFolder(new Path(entry.getName()));
                            ensureAllFoldersExist(folder);
                            requiredResources.add(folder);
                        } else {
                            IFile file = destinationFolder.getFile(new Path(entry.getName()));
                            ensureAllFoldersExist(file);
                            if (file.exists()) {
                                file.setContents(new NoCloseInputStream(zis), IResource.FORCE, new NullProgressMonitor());
                            } else {
                                file.create(new NoCloseInputStream(zis), true, new NullProgressMonitor());
                            }
                            requiredResources.add(file);
                            if (file.getParent().getType() == IResource.FOLDER) {
                                requiredResources.add(file.getParent());
                            }
                        }
                    }
                }
            } finally {
                zis.close();
            }
            
            // If ZIP now has less content some bundle files will need to be deleted
            existingResources.removeAll(requiredResources);
            for (IResource resource : existingResources) {
                if (resource.exists()) {
                    resource.delete(true, new NullProgressMonitor());
                }
            }
        }
    }
    
    /**
     * Delete ZIP files corresponding to deleted resource bundles.
     */
    public static class DeleteZipped implements Runnable {
        
        private Collection<IFolder> deletedResourceBundles;
        
        public DeleteZipped(Collection<IFolder> deletedResourceBundles) {
            this.deletedResourceBundles = deletedResourceBundles;
        }
        
        @Override
        public String getNameForProgress() {
            return "Delete zipped";
        }

        @Override
        public void run(IProgressMonitor monitor) {
            monitor.beginTask(MessageFormat.format("Deleting {0} static recources", deletedResourceBundles.size()), deletedResourceBundles.size());
            try {
                for (IFolder deletedResourceBundle : deletedResourceBundles) {
                    IFile zipFile = getResourceFolder(deletedResourceBundle.getProject()).getFile(deletedResourceBundle.getName());
                    try {
                        if (zipFile.exists()) {
                            zipFile.delete(true, new NullProgressMonitor());
                        }
                        IFile metaFile = ResourceTester.getMetaFile(zipFile);
                        if (metaFile.exists()) {
                            metaFile.delete(true, new NullProgressMonitor());
                        }
                        monitor.worked(1);
                    } catch (CoreException e) {
                        Activator.log(e);
                    }
                }
            } finally {
                monitor.done();
            }
        }
    }
    
    /**
     * Delete resource bundle folders corresponding to ZIP files.
     */
    public static class DeleteUnzipped implements Runnable {
        
        private Collection<IFile> deletedZips;
        
        public DeleteUnzipped(Collection<IFile> deletedZips) {
            this.deletedZips = deletedZips;
        }
        
        @Override
        public String getNameForProgress() {
            return "Delete unzipped";
        }

        @Override
        public void run(IProgressMonitor monitor) {
            monitor.beginTask(MessageFormat.format("Deleting {0} resource bundles", deletedZips.size()), deletedZips.size());
            try {
                for (IFile deletedZip : deletedZips) {
                    // Naming based on content not meta file
                    IFile f = ResourceTester.getContentFile(deletedZip);
                    IFolder folder = getBundleFolder(f.getProject()).getFolder(f.getName());
                    try {
                        if (folder.exists()) {
                            folder.delete(true, monitor);
                        }
                        monitor.worked(1);
                    } catch (CoreException e) {
                        Activator.log(e);
                    }
                }
            } finally {
                monitor.done();
            }
        }
    }
    
    private static boolean keep(ZipEntry entry) {
        
        return !entry.getName().startsWith(MACOSX_JUNK_PREFIX);
    }
    
    private static void transfer(InputStream is, OutputStream os) throws IOException {
        
        byte[] buffer = new byte[ZIP_ENTRY_BUFFER_SIZE];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    }
    
    private static IFolder getBundleFolder(IProject project) {
        return project.getFolder(ResourceTester.BUNDLE_FOLDER_PATH);
    }
    
    private static IFolder getResourceFolder(IProject project) {
        return project.getFolder(ResourceTester.RESOURCE_FOLDER_PATH);
    }
    
    private static void ensureAllFoldersExist(IFile file) throws CoreException {
        if (file.getParent().getType() == IResource.FOLDER) {
            ensureAllFoldersExist((IFolder) file.getParent());
        }
    }
    
    private static void ensureAllFoldersExist(IFolder folder) throws CoreException {
        
        List<IFolder> folders = new ArrayList<IFolder>();
        for (IContainer container = folder; container.getType() == IResource.FOLDER; container = container.getParent()) {
            folders.add((IFolder) container);
        }
        Collections.reverse(folders);
        
        for (IFolder f : folders) {
            if (!f.exists()) {
                f.create(true, true, new NullProgressMonitor());
            }
        }
    }
    
    // Trying to avoid builder cycles caused by unnecessary marker changes
    private static void addErrorMarkerCarefully(IResource resource, String markerType, String message) throws CoreException {
        
        boolean alreadyThere = false;
        for (IMarker marker : resource.findMarkers(markerType, false, 0)) {
            if (message.equals(marker.getAttribute(IMarker.MESSAGE, null))) {
                alreadyThere = true;
                break;
            }
        }
        
        if (!alreadyThere) {
            resource.deleteMarkers(markerType, false, 0);

            IMarker marker = resource.createMarker(markerType);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        }
    }
    
    // Trying to avoid builder cycles caused by unnecessary marker changes
    private static void removeErrorMarkerCarefully(IResource resource, String markerType) throws CoreException {
        
        if (resource.findMarkers(markerType, false, 0).length > 0) {
            resource.deleteMarkers(markerType, false, 0);
        }
    }
}
