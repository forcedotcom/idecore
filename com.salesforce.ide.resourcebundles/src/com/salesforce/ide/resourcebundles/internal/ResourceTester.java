package com.salesforce.ide.resourcebundles.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.resourcebundles.Activator;

/**
 * Used to identify specific resources.
 */
public class ResourceTester extends PropertyTester {
    
    public static final String SRC_FOLDER_NAME = "src"; //$NON-NLS-1$
    public static final String RESOURCE_FOLDER_NAME = "staticresources"; //$NON-NLS-1$
    public static final String BUNDLE_FOLDER_NAME = "resource-bundles"; //$NON-NLS-1$
    
    public static final String CONTENT_EXTENSION = ".resource"; //$NON-NLS-1$
    public static final String META_EXTENSION = ".resource-meta.xml"; //$NON-NLS-1$
    
    public static final IPath SRC_FOLDER_PATH = new Path(SRC_FOLDER_NAME);
    public static final IPath RESOURCE_FOLDER_PATH = new Path(SRC_FOLDER_NAME).append(RESOURCE_FOLDER_NAME);
    public static final IPath BUNDLE_FOLDER_PATH = new Path(BUNDLE_FOLDER_NAME);
    
    // Public for tests
    public static final String IS_SINGLE_ZIPPABLE_PROPERTY = "isSingleZippable"; //$NON-NLS-1$
    public static final String IS_SINGLE_UNZIPPABLE_PROPERTY = "isSingleUnzippable"; //$NON-NLS-1$
    public static final String IS_MULTIPLE_ZIPPABLE_PROPERTY = "isMultipleZippable"; //$NON-NLS-1$
    public static final String IS_MULTIPLE_UNZIPPABLE_PROPERTY = "isMultipleUnzippable"; //$NON-NLS-1$
    
    /**
     * Identify files to zip and folders to unzip; visit as shallowly as possible.
     */
    public static class ResourceDeltaVisitor implements IResourceDeltaVisitor {
        
        private Boolean zip;
        private Boolean unzip;
        
        public Changes changes = new Changes();
        
        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            if (isForceProject(resource)) {
                if (zip == null || unzip == null) {
                    ForceProject fp = ContainerDelegate.getInstance().getServiceLocator()
                            .getProjectService().getForceProject(resource.getProject());
                    zip = fp.getZipResourceBundlesAutomatically();
                    unzip = fp.getUnzipStaticResourcesAutomatically();
                }
                if ((zip || unzip) && resource instanceof IProject) {
                    // Go deeper
                    return true;
                } else if (unzip && (isSrcFolder(resource) || isResourceFolder(resource))) {
                    // Go deeper
                    return true;
                } else if (zip && isBundleFolder(resource)) {
                    // Go deeper
                    return true;
                } else if (isResourceFolderChildFile(resource)) {
                    if (delta.getKind() == IResourceDelta.REMOVED) {
                        changes.addDeletedFileToUnzip((IFile) resource);
                    } else {
                        changes.addFileToUnzip((IFile) resource);
                    }
                } else if (isBundleFolderChildFolder(resource)) {
                    if (delta.getKind() == IResourceDelta.REMOVED) {
                        changes.addDeletedFolderToZip((IFolder) resource);
                    } else {
                        changes.addFolderToZip((IFolder) resource);
                    }
                }
                // All done - no need to go deeper into the tree
                return false;
            } else {
                // Go deeper into the tree
                return true;
            }
        }
    };
    
    private MetaXmlHandler handler = new MetaXmlHandler();

    /**
     * Used in plugin.xml to decide whether to show menu items or not.
     */
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        try {
            if (receiver instanceof IResource) {
                IResource resource = (IResource) receiver;
                if (isForceProject(resource)) {
                    if (property.equals(IS_SINGLE_ZIPPABLE_PROPERTY)) {
                        return zippableCount(resource) == 1;
                    } else if (property.equals(IS_MULTIPLE_ZIPPABLE_PROPERTY)) {
                        return zippableCount(resource) > 1;
                    } else if (property.equals(IS_SINGLE_UNZIPPABLE_PROPERTY)) {
                        return unzippableCount(resource) == 1;
                    } else if (property.equals(IS_MULTIPLE_UNZIPPABLE_PROPERTY)) {
                        return unzippableCount(resource) > 1;
                    }
                }
            }
        } catch (Exception e) {
            Activator.log(e);
        }
        return false;
    }
    
    /**
     * Return the content file corresponding to either a content or meta file.
     */
    public static IFile getContentFile(IFile contentOrMeta) {
        if (contentOrMeta.getName().endsWith(CONTENT_EXTENSION)) {
            return contentOrMeta;
        } else {
            return contentOrMeta.getParent().getFile(new Path(contentOrMeta.getName().replace(META_EXTENSION, CONTENT_EXTENSION)));
        }
    }
    
    /**
     * Return the meta file corresponding to either a content or meta file.
     */
    public static IFile getMetaFile(IFile contentOrMeta) {
        if (contentOrMeta.getName().endsWith(META_EXTENSION)) {
            return contentOrMeta;
        } else {
            return contentOrMeta.getParent().getFile(new Path(contentOrMeta.getName().replace(CONTENT_EXTENSION, META_EXTENSION)));
        }
    }
    
    /**
     * Find anything to unzip in a selection.
     */
    public static List<IFile> getFilesToUnzip(IStructuredSelection selection) throws Exception {
        List<IFile> files = new ArrayList<IFile>();
        for (Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
            Object o = iter.next();
            if (o instanceof IResource) {
                IResource resource = (IResource) o;
                if (isForceProject(resource)) {
                    if (isResourceFolderChildFile(resource)) {
                        files.add((IFile) resource);
                    } else if (isResourceFolder(resource)) {
                        for (IResource child : ((IFolder) resource).members()) {
                            if (isResourceFolderChildFile(child)) {
                                files.add((IFile) child);
                            }
                        }
                    }
                }
            }
        }
        return files;
    }
    
    /**
     * Find anything to zip in a selection.
     */
    public static List<IFolder> getFoldersToZip(IStructuredSelection selection) throws Exception {
        List<IFolder> folders = new ArrayList<IFolder>();
        for (Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
            Object o = iter.next();
            if (o instanceof IResource) {
                IResource resource = (IResource) o;
                if (isForceProject(resource)) {
                    if (isBundleFolderChildFolder(resource)) {
                        folders.add((IFolder) resource);
                    } else if (isBundleFolder(resource)) {
                        // Folder - immediate child files
                        for (IResource child : ((IFolder) resource).members()) {
                            if (isBundleFolderChildFolder(child)) {
                                folders.add((IFolder) child);
                            }
                        }
                    }
                }
            }
        }
        return folders;
    }
    
    // Returns 0, 1 or 2
    private int zippableCount(IResource resource) throws Exception {
        if (isBundleFolderChildFolder(resource)) {
            return 1;
        } else if (isBundleFolder(resource)) {
            int count = 0;
            for (IResource child : ((IFolder) resource).members()) {
                if (child instanceof IFolder) {
                    count++;
                }
                if (count == 2) {
                    break;
                }
            }
            return count;
        }
        return 0;
    }
    
    // Returns 0, 1 or 2
    private int unzippableCount(IResource resource) throws Exception {
        if (isResourceFolderChildFile(resource)) {
            IFile meta = getMetaFile((IFile) resource);
            return meta.exists() && handler.isZip(meta) ? 1 : 0;
        } else if (isResourceFolder(resource)) {
            int count = 0;
            for (IResource child : ((IFolder) resource).members()) {
                if (child instanceof IFile && child.getName().endsWith(META_EXTENSION)) {
                    if (handler.isZip((IFile) child)) {
                        count++;
                    }
                    if (count == 2) {
                        break;
                    }
                }
            }
            return count;
        } else {
            return 0;
        }
    }
    
    private static boolean isForceProject(IResource resource) throws CoreException {
        IProject project = resource.getProject();
        return project != null && project.exists() && project.isOpen() && project.hasNature(DefaultNature.NATURE_ID);
    }
    
    private static boolean isSrcFolder(IResource resource) {
        return resource instanceof IFolder && SRC_FOLDER_PATH.equals(resource.getProjectRelativePath());
    }
    
    private static boolean isResourceFolder(IResource resource) {
        return resource instanceof IFolder && RESOURCE_FOLDER_PATH.equals(resource.getProjectRelativePath());
    }
    
    private static boolean isResourceFolderChildFile(IResource resource) {
        return resource instanceof IFile
                && RESOURCE_FOLDER_PATH.equals(resource.getParent().getProjectRelativePath())
                && (resource.getName().endsWith(CONTENT_EXTENSION) || resource.getName().endsWith(META_EXTENSION))
                ;
    }
    
    private static boolean isBundleFolder(IResource resource) {
        return resource instanceof IFolder && BUNDLE_FOLDER_PATH.equals(resource.getProjectRelativePath());
    }
    
    private static boolean isBundleFolderChildFolder(IResource resource) {
        return resource instanceof IFolder
                && BUNDLE_FOLDER_PATH.equals(resource.getParent().getProjectRelativePath())
                && resource.getName().endsWith(CONTENT_EXTENSION)
                ;
    }
}