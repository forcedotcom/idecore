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
package com.salesforce.ide.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.ResourceProperties;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.MarkerUtils;

/**
 * This class encapsulate attributes and operations that handle resource management such as filesystem access and
 * attribute storage and rendering.
 * 
 * @author cwall
 */
@SuppressWarnings("restriction")
public abstract class ComponentResource implements IComponent {

    private static final Logger logger = Logger.getLogger(ComponentResource.class);
    public static final long INIT_CHECKSUM = -1;

    protected String metadataFilePath = null;
    protected long bodyChecksum = INIT_CHECKSUM;
    protected long originalBodyChecksum = INIT_CHECKSUM;
    protected IFile resource = null;
    protected byte[] file = null;
    protected long fetchTime = -1;

    //   C O N S T R U C T O R S
    public ComponentResource() {
        super();
    }

    //   M E T H O D S
    // metadata filepath and filepath are the same for non-installed packages;
    // for installed packages filepath is prefixed with the package name
    @Override
    public String getMetadataFilePath() {
        return metadataFilePath;
    }

    public void setMetadataFilePath(Object filePath) {
        this.metadataFilePath = Utils.stripSourceFolder((String) filePath);
    }

    public void setFilePath(Object filePath) {
        setMetadataFilePath(filePath);
    }

    public String getResourceFilePath() {
        if (resource != null) {
            return resource.getProjectRelativePath().toPortableString();
        }
        return null;
    }

    @Override
    public long getBodyChecksum() {
        return bodyChecksum;
    }

    public void setBodyChecksum(long bodyChecksum) {
        this.bodyChecksum = bodyChecksum;
    }

    @Override
    public long getOriginalBodyChecksum() {
        return originalBodyChecksum;
    }

    public void setOriginalBodyChecksum(long originalBodyChecksum) {
        this.originalBodyChecksum = originalBodyChecksum;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    @Override
    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    @Override
    public IFile getFileResource() {
        return resource;
    }

    public IFile getFileResource(IProject project) {
        try {
            return getFileResource(project, new NullProgressMonitor());
        } catch (InterruptedException e) {
            // do nothing
            return null;
        }
    }

    public IFile getFileResource(IProject project, IProgressMonitor monitor) throws InterruptedException {
        if (resource == null && project != null) {
            resource = createFile(project, monitor);
        }
        return resource;
    }

    @Override
    public void setFileResource(IFile resource) {
        this.resource = resource;
    }

    public IProject getProject() {
        if (resource != null) {
            return resource.getProject();
        }
        return null;
    }

    protected String getId(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_ID);
    }

    protected String getFileName(IResource res) {
        String name = ResourceProperties.getProperty(res, QualifiedNames.QN_FILE_NAME);
        if (Utils.isEmpty(name)) {
            // use the filename instead
            IFile file = (IFile) res;
            String filename = file.getName();
            String extension = file.getFileExtension();
            if (Utils.isNotEmpty(extension) && Utils.isNotEmpty(filename) && filename.endsWith("." + extension)) {
                name = filename.substring(0, filename.lastIndexOf("." + extension));
            } else {
                name = filename;
            }
        }
        return name;
    }

    protected String getFullName(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_FULL_NAME);
    }

    protected String getNamespacePrefix(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_NAMESPACE_PREFIX);
    }

    protected String getOriginalBodyChecksum(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_ORIGINAL_BODY_CHECKSUM);
    }

    protected String getSystemModStamp(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_SYSTEM_MODSTAMP);
    }

    protected String getLastModifiedById(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_LAST_MODIFIED_BY_ID);
    }

    protected String getLastModifiedDate(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_LAST_MODIFIED_DATE);
    }

    protected String getLastModifiedByName(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_LAST_MODIFIED_BY_NAME);
    }

    protected String getCreatedById(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_CREATED_BY_ID);
    }

    protected String getCreatedDate(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_CREATED_DATE);
    }

    protected String getCreatedByName(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_CREATED_BY_NAME);
    }

    protected String getPackageName(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_PACKAGE_NAME);
    }

    protected String getFetchTime(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_FETCH_DATE);
    }

    protected String getState(IResource res) {
        return ResourceProperties.getProperty(res, QualifiedNames.QN_STATE);
    }

    /**
     * Set the base file properties valid for all Force.com objects
     */
    protected void saveFileProperties(IFile file) {
        ResourceProperties.setProperty(file, QualifiedNames.QN_ID, getId());
        ResourceProperties.setProperty(file, QualifiedNames.QN_FILE_NAME, getFileName());
        ResourceProperties.setProperty(file, QualifiedNames.QN_FULL_NAME, getFullName());
        ResourceProperties.setProperty(file, QualifiedNames.QN_NAMESPACE_PREFIX, getNamespacePrefix());
        ResourceProperties.setProperty(file, QualifiedNames.QN_PACKAGE_NAME, getPackageName());
        ResourceProperties.setProperty(file, QualifiedNames.QN_STATE, getState());

        ResourceProperties.setProperty(file, QualifiedNames.QN_CREATED_BY_NAME, getCreatedByName());
        ResourceProperties.setProperty(file, QualifiedNames.QN_CREATED_BY_ID, getCreatedById());
        ResourceProperties.setLong(file, QualifiedNames.QN_CREATED_DATE, (getCreatedDate() != null ? getCreatedDate()
                .getTimeInMillis() : 0));

        ResourceProperties.setProperty(file, QualifiedNames.QN_LAST_MODIFIED_BY_NAME, getLastModifiedByName());
        ResourceProperties.setProperty(file, QualifiedNames.QN_LAST_MODIFIED_BY_ID, getLastModifiedById());
        ResourceProperties.setLong(file, QualifiedNames.QN_LAST_MODIFIED_DATE, (getLastModifiedDate() != null
                ? getLastModifiedDate().getTimeInMillis() : 0));

        ResourceProperties.setLong(file, QualifiedNames.QN_ORIGINAL_BODY_CHECKSUM, getOriginalBodyChecksum());

        ResourceProperties.setLong(file, QualifiedNames.QN_FETCH_DATE, fetchTime);

        saveAdditionalFileProperties(file);
    }

    /**
     * Implement if you want to define object specific properties
     * 
     * @param file
     */
    protected abstract void saveAdditionalFileProperties(IFile file);

    public IFile saveToFile(IProject project, ProjectPackage projectPackage, IProgressMonitor monitor)
            throws CoreException, InterruptedException, ForceProjectException {
        monitor.subTask(getFileName());
        setProjectPackageProperties(projectPackage);
        resource = createFile(project, monitor);
        setFileResource(resource);
        saveToFile(true, monitor);
        setFileAccess(resource);
        return resource;
    }

    private void setProjectPackageProperties(ProjectPackage projectPackage) {
        if (projectPackage != null) {
            setPackageName(projectPackage.getName());
            setInstalled(projectPackage.isInstalled());
        }
    }

    public IFile saveToFile(boolean saveProperties, IProgressMonitor monitor) throws CoreException, InterruptedException, ForceProjectException {
        monitorCheck(monitor);

        if (resource == null) {
            logger.warn("Unable to save " + getFullDisplayName() + " to file - resource is null");
            return resource;
        }

        if (getFile() == null) {
            throw new ForceProjectException("No content found for file '"
                    + resource.getProjectRelativePath().toPortableString() + "'");
        }

        // create filepath
        createParents(resource, monitor);

        InputStream stream = null;
        try {
            // save or update contents
            stream = new ByteArrayInputStream(getFile());

            if (resource.exists()) {
                resource.setContents(stream, true, true, new SubProgressMonitor(monitor, 1));
            } else {
                removeCaseSensitiveDupFile(resource);
                resource.create(stream, true, new SubProgressMonitor(monitor, 1));
            }

            // create parallel folder for folder metadata
            if (Constants.FOLDER.equals(getComponentType())) {
                createFolder(resource, new SubProgressMonitor(monitor, 1));
            }

            MarkerUtils.getInstance().clearAll(resource);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                logger.error("Unable to close stream on file '" + resource.getProjectRelativePath().toPortableString()
                        + "'");
            }

            // save associated file properties
            if (resource.exists() && saveProperties) {
                saveFileProperties(resource);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Saved " + getFullDisplayName() + " [" + getFile().length + "] to file '"
                    + resource.getProjectRelativePath().toPortableString() + "' and associated properties");
        }

        return resource;
    }

    /**
     * Borrow partial functions from checkDoesNotExist() method in File.create() to handle case insensitivity for Win32
     * and MacOs & case sensitive for Linux Instead of throwing exception complaining 'resource exists with a diff
     * case'/create separate file w/ same name , delete the local copy first.
     * http://bugforce.soma.salesforce.com/bug/bugDetail.jsp?id=100000000000rIE
     * 
     * @param file
     * @throws CoreException
     */
    private static void removeCaseSensitiveDupFile(IFile file) throws CoreException {
        Resource fileResource = (Resource) file;
        // now look for a matching case variant in the tree
        IResource variant = fileResource.findExistingResourceVariant(fileResource.getFullPath());
        if (variant == null) {
            return;
        }

        variant.delete(true, new NullProgressMonitor());
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted case insensitive instance of file '" + file.getName() + "'");
        }
    }

    public void handleReadOnlyFile() {
        if (!resource.exists()) {
            return;
        }

        // get current, existing checksum to compare incoming, remote checksum to determine if content is equal
        String content = "";
        try {
            content = getContentString(resource);
        } catch (Exception e) {
            logger.error("Unable to get content for file '" + resource.getProjectRelativePath().toPortableString()
                    + "'", e);
        }

        long currentFileChecksum = generateChecksum(content);
        if (currentFileChecksum != getBodyChecksum()) {
            MarkerUtils.getInstance().applyDirty(resource,
                Messages.getString("Markers.ReadOnlyLocalFileNotInSyncWithRemote.message"));
            ResourceProperties.setLong(resource, QualifiedNames.QN_ORIGINAL_BODY_CHECKSUM, currentFileChecksum);
            if (logger.isInfoEnabled()) {
                logger.info("Local body is not in sync w/ remote body - updating original "
                        + "checksum and dirtying file '" + resource.getProjectRelativePath().toPortableString() + "'");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Current, local file content is the same as remote instance");
            }
        }
    }

    protected final IFile createFile(IProject project, IProgressMonitor monitor) throws InterruptedException {
        if (Utils.isEmpty(getMetadataFilePath()) || project == null) {
            logger.warn("Unable to create file - filepath and/or project is null");
            return null;
        }

        if (isInstalled()) {
            createFolder(project.getFolder(Constants.REFERENCED_PACKAGE_FOLDER_NAME), monitor);
        } else {
            createFolder(project.getFolder(Constants.SOURCE_FOLDER_NAME), monitor);
        }

        return project.getFile(prependProperFilePath(metadataFilePath));
    }

    private String prependProperFilePath(String filePath) {
        if (Utils.isEmpty(filePath)) {
            return filePath;
        }

        if (isInstalled() && !filePath.startsWith(Constants.REFERENCED_PACKAGE_FOLDER_NAME)) {
            return Constants.REFERENCED_PACKAGE_FOLDER_NAME + "/" + filePath;
        } else if (!isInstalled() && !filePath.startsWith(Constants.SOURCE_FOLDER_NAME)) {
            return Constants.SOURCE_FOLDER_NAME + "/" + filePath;
        } else {
            return filePath;
        }
    }

    protected final void createParents(IResource childResource, IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        IResource parentResource = childResource.getParent();

        if (!parentResource.exists() && parentResource instanceof IFolder) {
            createParents(parentResource, monitor);
            createFolder((IFolder) parentResource, monitor);
        }
    }

    protected final IFolder createFolder(IResource resource, IProgressMonitor monitor) throws InterruptedException {
        if (resource.exists() && resource.getType() == IResource.FILE && resource.getParent() != null
                && resource.getParent().getType() == IResource.FOLDER) {
            // strip file extension
            String folderName = resource.getName();
            if (folderName.endsWith(getFileExtension())) {
                folderName = folderName.substring(0, folderName.indexOf(getFileExtension()));
            }

            IFolder parentFolder = (IFolder) resource.getParent();
            IFolder folder =
                    parentFolder.getProject().getFolder(parentFolder.getProjectRelativePath() + "/" + folderName);
            return createFolder(folder, monitor);
        } else if (resource.getType() == IResource.FOLDER) {
            return createFolder((IFolder) resource, monitor);
        } else {
            return null;
        }
    }

    protected final IFolder createFolder(IFolder folder, IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        if (folder == null || folder.exists()) {
            return folder;
        }

        try {
            folder.create(true, true, new SubProgressMonitor(monitor, 1));
            if (logger.isDebugEnabled()) {
                logger.debug("Created '" + folder.getFullPath().toOSString() + "' folder");
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to create '" + folder.getFullPath().toOSString() + "' folder: " + logMessage);
            return null;
        }
        return folder;
    }

    public Component loadFromFile(boolean includeBody) throws IOException, CoreException {
        if (getFileResource() == null) {
            throw new IllegalArgumentException("File resource cannot be null and must exist");
        }

        if (!getFileResource().exists()) {
            logger.warn("File '" + getFileResource().getProjectRelativePath().toPortableString() + "' does not exist");
            return null;
        }

        if (includeBody && (isTextContent() || isMetadataInstance())) {
            loadBodyFromFile(getFileResource());
        }

        loadProperties(getFileResource());

        setFileAccess(getFileResource());

        Component component = (Component) this;
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded the following Component from '" + getFileResource().getName() + "':\n" + "  "
                    + component.toString());
        }

        return (Component) this;
    }

    protected void loadBodyFromFile(IFile file) throws IOException, CoreException {
        String contentStr = getContentString(file);
        if (Utils.isNotEmpty(contentStr)) {
            setBody(contentStr);
            setFile(contentStr.getBytes());
        }
    }

    protected boolean isTextContentType() {
        String contentType = getContentType();
        return Utils.isNotEmpty(contentType) ? true : false;
    }

    protected String getContentType() {
        String contentTypeName = null;

        if (getFileResource() != null) {
            try {
                IContentDescription contentDescription = getFileResource().getContentDescription();
                if (contentDescription != null) {
                    IContentType contentType = contentDescription.getContentType();
                    contentTypeName = contentType.getName();
                } else {
                    logger.warn("No content type found for file '"
                            + getFileResource().getProjectRelativePath().toPortableString() + "'");
                }
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable to determine content type for file '"
                        + getFileResource().getProjectRelativePath().toPortableString() + "': " + logMessage);
            }
        }

        return contentTypeName;
    }

    protected String getContentString(IFile file) throws IOException, CoreException {
        return Utils.getContentString(file);
    }

    public void loadProperties(IFile file) {
        if (file == null) {
            logger.warn("Unable to load file properties - file is null");
            return;
        }

        if (!file.exists()) {
            logger.warn("Unable to load file properties - file '" + file.getProjectRelativePath().toPortableString()
                    + "' does not exist");
            return;
        }

        // get body checksum from current, local content of file
        updateBodyChecksum();

        // get locally stored original - value of retrieved content - checksum
        String originalBodyChecksumString = getOriginalBodyChecksum(file);
        long originalBodyChecksum = INIT_CHECKSUM;
        if (Utils.isNotEmpty(originalBodyChecksumString)) {
            originalBodyChecksum = Long.parseLong(originalBodyChecksumString);
        }

        // it could be that the original checksum was lost or not captured
        // if so, set value to current, local body checksum
        if (originalBodyChecksum > INIT_CHECKSUM) {
            setOriginalBodyChecksum(originalBodyChecksum);
        } else {
            setOriginalBodyChecksum(getBodyChecksum());
        }

        // load date last fetched
        String fetchDateStr = getFetchTime(file);
        if (Utils.isNotEmpty(fetchDateStr)) {
            long fetchDate = Long.parseLong(fetchDateStr);
            setFetchTime(fetchDate);
        }

        loadComponentProperties(file);
    }

    protected abstract void loadComponentProperties(IFile file);

    protected void setFileAccess(IFile file) {
        if (file != null && isInstalled()) {
            ResourceAttributes resourceAttributes = new ResourceAttributes();
            resourceAttributes.setReadOnly(true);
            try {
                file.setResourceAttributes(resourceAttributes);
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable to set read-only on file " + resource.getName() + ": " + logMessage);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Set read-only access on " + file.getName());
            }
        }
    }

    @Override
    public void initChecksum() {
        updateBodyChecksum();
        setOriginalBodyChecksum(bodyChecksum);

        if (logger.isDebugEnabled()) {
            logger.debug("Set body [" + getBodyChecksum() + "] and orginal body [" + getOriginalBodyChecksum()
                    + "] checksums");
        }
    }

    public void updateBodyChecksum() {
        if (isTextContent() || isMetadataInstance()) {
            long bodyChecksum = generateChecksum(getBody());
            setBodyChecksum(bodyChecksum);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Getting checksum from file size for non-text component '" + getDisplayName() + "'");
            }

            // get from file bytes - typically directly from metadata result
            if (Utils.isNotEmpty(file)) {
                long bodyChecksum = generateChecksum(file);
                setBodyChecksum(bodyChecksum);
            } else if (resource != null) {
                // if load from body, get from file resource
                try {
                    file = Utils.getBytesFromFile(resource);
                    long bodyChecksum = generateChecksum(file);
                    setBodyChecksum(bodyChecksum);
                } catch (Exception e) {
                    logger.warn("Unable to get checksum from file and/or resource - setting checksum value to "
                            + INIT_CHECKSUM + " '" + getComponentType() + "'");
                    setBodyChecksum(INIT_CHECKSUM);
                }
            } else {
                setBodyChecksum(INIT_CHECKSUM);
                logger.warn("Resource and file bytes are null - setting checksum value to " + INIT_CHECKSUM + " '"
                        + getComponentType() + "'");
            }

        }

    }

    protected final static long generateChecksum(String str) {
        long checksumValue = 0;
        if (Utils.isNotEmpty(str)) {
            CRC32 checksum = new CRC32();
            String[] lines = str.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String tmpStr = lines[i];
                checksum.update(tmpStr.getBytes(), 0, tmpStr.getBytes().length);
            }
            checksumValue = checksum.getValue();
        }
        return checksumValue;
    }

    protected final static long generateChecksum(IFile file) {
        long checksumValue = INIT_CHECKSUM;
        if (file != null) {
            try {
                byte[] fileBtyes;
                fileBtyes = Utils.getBytesFromStream(file.getContents(), 1024);
                checksumValue = generateChecksum(fileBtyes);
            } catch (Exception e) {
                logger.error("Unable to get bytes from stream");
            }
        }
        return checksumValue;
    }

    protected final static long generateChecksum(byte[] file) {
        long checksumValue = 0;
        if (Utils.isNotEmpty(file)) {
            CRC32 checksum = new CRC32();
            checksum.update(file, 0, file.length);
            checksumValue = checksum.getValue();
        }
        return checksumValue;
    }

    protected final static byte[] compress(byte[] input) {
        // Create the compressor with highest level of compression
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        // Give the compressor the data to compress
        compressor.setInput(input);
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // You cannot use an array that's the same size as the orginal because
        // there is no guarantee that the compressed data will be smaller than
        // the uncompressed data.
        try (final QuietCloseable<ByteArrayOutputStream> c = QuietCloseable.make(new ByteArrayOutputStream(input.length))) {
            final ByteArrayOutputStream bos = c.get();

            // Compress the data
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }

            // Get the compressed data
            return bos.toByteArray();
        }
    }

    protected void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new InterruptedException("Operation cancelled");
            }
        }
    }

    public boolean isFetchedAfter(long otherFetchDate) {
        if (logger.isDebugEnabled()) {
            logger.debug("Is this.fetchdate '" + (new Date(getFetchTime())).toString() + "' after other.fetchdate '"
                    + (new Date(otherFetchDate)).toString() + "'? " + (getFetchTime() > otherFetchDate));
        }

        return getFetchTime() > otherFetchDate;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     * 
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("filePath=").append(this.metadataFilePath).append(TAB).append("bodyChecksum=")
                .append(this.bodyChecksum).append(TAB).append("originalBodyChecksum=")
                .append(this.originalBodyChecksum).append(TAB).append("resource=")
                .append(this.resource != null ? this.resource.getName() : null).append(TAB).append("file size=")
                .append(this.file != null ? this.file.length : 0).append(TAB).append("fetchDate=")
                .append((new Date(getFetchTime())).toString());

        return retValue.toString();
    }

    @Override
    public String toStringLite() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("filePath=").append(this.metadataFilePath).append(TAB).append("bodyChecksum=")
                .append(this.bodyChecksum).append(TAB).append("originalBodyChecksum=")
                .append(this.originalBodyChecksum);
        return retValue.toString();
    }
}
