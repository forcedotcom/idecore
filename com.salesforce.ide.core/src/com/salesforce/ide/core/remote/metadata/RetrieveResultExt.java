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
package com.salesforce.ide.core.remote.metadata;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.sforce.soap.metadata.RetrieveResult;

public class RetrieveResultExt implements IMetadataResultExt {
	private static final Logger logger = Logger.getLogger(RetrieveResultExt.class);

	private ProjectPackageList projectPackageList = null;
	private RetrieveResult retrieveResult = null;
	private RetrieveMessageExt messageHandler = null;
	private FileMetadataExt fileMetadataHandler = null;

	public RetrieveResultExt() {
	}

	public RetrieveResultExt(RetrieveResult retrieveResult) {
		this.retrieveResult = retrieveResult;
	}

	public void setRetrieveResult(RetrieveResult retrieveResult) {
		this.retrieveResult = retrieveResult;
	}

	public void setMessageHandler(RetrieveMessageExt messageHandler) {
		this.messageHandler = messageHandler;
	}

	public ProjectPackageList getProjectPackageList() {
		return projectPackageList;
	}

	public void setProjectPackageList(ProjectPackageList projectPackageList) {
		this.projectPackageList = projectPackageList;
	}

	public void setFileMetadataHandler(FileMetadataExt fileMetadataHandler) {
		this.fileMetadataHandler = fileMetadataHandler;
	}

    public RetrieveResult getRetrieveResult() {
		return retrieveResult;
	}

	@Override
    public RetrieveMessageExt getMessageHandler() {
		if (retrieveResult != null && messageHandler == null) {
			messageHandler = new RetrieveMessageExt(retrieveResult.getMessages());
		}
		return messageHandler;
	}

	@Override
    public int getMessageCount() {
		int count = 0;
		if (retrieveResult != null && Utils.isNotEmpty(retrieveResult.getMessages())) {
			count = retrieveResult.getMessages().length;
		}
		return count;
	}

	@Override
    public boolean hasMessages() {
		return getMessageCount() > 0;
	}

    public byte[] getZipFile() {
    	return (retrieveResult != null ? retrieveResult.getZipFile() : null);
    }

    public boolean containsFilePath(String filePath) {
    	return containsFilePath(filePath, false);
    }

    public boolean containsFilePath(String filePath, boolean ignoreCase) {
    	if (filePath == null) {
    		throw new IllegalArgumentException("Filepath cannot be null");
    	}

    	try {
			final byte[] zipFile = getZipFile();
            if (zipFile != null) {
                try (final QuietCloseable<ZipInputStream> c = QuietCloseable.make(new ZipInputStream(new ByteArrayInputStream(zipFile)))) {
                    final ZipInputStream zis = c.get();

					for (;;) {
						ZipEntry ze = zis.getNextEntry();
						if (ze == null) {
							break;
						} else if (ze.isDirectory()) {
							continue;
						} else {
							if (ignoreCase && filePath.equalsIgnoreCase(ze.getName())) {
								return true;
							} else if (filePath.equals(ze.getName())) {
								return true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to get zip file count: " + e.getMessage());
		}
		return false;
    }

    public FileMetadataExt getFileMetadataHandler() {
        if (retrieveResult != null && fileMetadataHandler == null) {
			fileMetadataHandler = new FileMetadataExt(getRetrieveResult().getFileProperties());
		}
		return fileMetadataHandler;
    }

    public int getZipFileCount() {
    	int fileCount = 0;
    	try {
			final byte[] zipFile = getZipFile();
            if (zipFile != null) {
                try (final QuietCloseable<ZipInputStream> c = QuietCloseable.make(new ZipInputStream(new ByteArrayInputStream(zipFile)))) {
                    final ZipInputStream zis = c.get();

					for (;;) {
						ZipEntry ze = zis.getNextEntry();
						if (ze == null) {
							break;
						} else if (ze.isDirectory()) {
							continue;
						} else {
							fileCount++;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to get zip file count: " + e.getMessage());
		}
		return fileCount;
    }
}
