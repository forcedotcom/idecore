package com.salesforce.ide.resourcebundles.internal;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Collection of changes that need to be processed.
 */
public class Changes {

    private Map<IProject, Set<IFolder>> foldersToZip = new HashMap<IProject, Set<IFolder>>();
    private Map<IProject, Set<IFolder>> deletedFoldersToZip = new HashMap<IProject, Set<IFolder>>();
    
    private Map<IProject, Set<IFile>> filesToUnzip = new HashMap<IProject, Set<IFile>>();
    private Map<IProject, Set<IFile>> deletedFilesToUnzip = new HashMap<IProject, Set<IFile>>();
    
    public void addFolderToZip(IFolder folder) {
        addByProject(foldersToZip, folder);
    }
    
    public void addDeletedFolderToZip(IFolder folder) {
        addByProject(deletedFoldersToZip, folder);
    }
    
    public void addFileToUnzip(IFile file) {
        addByProject(filesToUnzip, file);
    }
    
    public void addDeletedFileToUnzip(IFile file) {
        addByProject(deletedFilesToUnzip, file);
    }
    
    public Map<IProject, Set<IFolder>> getFoldersToZipByProject() {
        return foldersToZip;
    }
    
    public Map<IProject, Set<IFolder>> getDeletedFoldersToZipByProject() {
        return deletedFoldersToZip;
    }
    
    public Map<IProject, Set<IFile>> getFilesToUnzipByProject() {
        return filesToUnzip;
    }
    
    public Map<IProject, Set<IFile>> getDeletedFilesToUnzipByProject() {
        return deletedFilesToUnzip;
    }
    
    private <T extends IResource> void addByProject(Map<IProject, Set<T>> map, T r) {
        Set<T> list = map.get(r.getProject());
        if (list == null) {
            list = new LinkedHashSet<T>();
            map.put(r.getProject(), list);
        }
        list.add(r);
    }
    
    /**
     * For debugging.
     */
    public String toString() {
        
        StringBuilder sb = new StringBuilder(1024);
        
        String lf = System.getProperty("line.separator");
        for (IProject project : getFilesToUnzipByProject().keySet()) {
            sb.append("project=" + project + " filesToUnzip=" + getFilesToUnzipByProject().get(project) + lf);
        }
        for (IProject project : getFoldersToZipByProject().keySet()) {
            sb.append("project=" + project + " foldersToZip=" + getFoldersToZipByProject().get(project) + lf);
        }
        for (IProject project : getDeletedFilesToUnzipByProject().keySet()) {
            sb.append("project=" + project + " deletedFilesToUnzip=" + getDeletedFilesToUnzipByProject().get(project) + lf);
        }
        for (IProject project : getDeletedFoldersToZipByProject().keySet()) {
            sb.append("project=" + project + " deltedFoldersToZip=" + getDeletedFoldersToZipByProject().get(project) + lf);
        }
        
        return sb.toString();
    }
}
