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
package com.salesforce.ide.ui.internal.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.core.internal.utils.ZipUtils.ZipStats;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;

/**
 *
 * Component archive utility class.
 *
 */
public class ComponentArchiver {

    private static final Logger logger = Logger.getLogger(ComponentArchiver.class);

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static void generateArchive(String zipName, File zipPath, ComponentList componentList) throws IOException {
        if(!validInput(zipName, zipPath, componentList)) return;

        if (Utils.isEmpty(componentList)) {
            return;
        }

        File zipFile = new File(zipPath + File.separator + zipName + ".zip");
        if (zipFile.exists()) {
            boolean success = zipFile.delete();
            if (logger.isInfoEnabled() && success) {
                logger.info("Deleted zip '" + zipPath + File.separator + zipName + ".zip" + "'");
            }
        }

        ZipStats stats = new ZipStats();

        try (final ZipOutputStream zipFileStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (Component component: componentList) {
                if (component.getFileResource() == null && Utils.isEmpty(component.getBody())) {
                    logger.warn("File and body for component " + component.getFullDisplayName() + " is null");
                    continue;
                }

                ZipStats tmpStats = null;
                String filePath = Utils.stripSourceFolder(component.getMetadataFilePath());
                if (component.getFileResource() != null) {
                    File file = component.getFileResource().getRawLocation().toFile();
                    if (!file.exists()) {
                        logger.warn("File '" + file.getAbsolutePath() + "' does not exist");
                        continue;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Zipping content from component's file '"
                                + component.getFileResource().getProjectRelativePath().toPortableString());
                    }

                    // get zip and add to zip stats
                    tmpStats = ZipUtils.zipFile(filePath, file, zipFileStream, Integer.MAX_VALUE);
                } else if (Utils.isNotEmpty(component.getBody())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Zipping content from component's body");
                    }

                    // get zip and add to zip stats
                    tmpStats = ZipUtils.zipFile(filePath, component.getBody(), zipFileStream, Integer.MAX_VALUE);
                }

                stats.addStats(tmpStats);

                if (logger.isDebugEnabled()) {
                    logger.debug("Updated zip stats:\n" + stats.toString());
                }
            }
        }
    }

    private static boolean validInput(String zip_name, File zip_path, ComponentList components) {
        if (Utils.isEmpty(zip_path) || !zip_path.exists()) {
            throw new IllegalArgumentException("Zip path '" + zip_path.getAbsolutePath() + "' does not exist");
        }

        if (Utils.isEmpty(zip_name)) {
            throw new IllegalArgumentException("Zip name not provided");
        }

        if (Utils.isEmpty(components)) {
            logger.warn("Component list is null or empty.  Nothing to zip.");
            return false;
        }
        return true;
    }
}
