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
package com.salesforce.ide.core.internal.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * A utility class for creating and extracting zip files.
 * 
 * @author cwall
 */
public class ZipUtils {
    private static final Logger logger = Logger.getLogger(ZipUtils.class);

    /**
     * A collection of statistics that is returned about the zip file generated or extracted.
     */
    public static class ZipStats {
        private int numEntries = 0;
        private int numDirectories = 0;
        private long totalBytes = 0;
        private long totalCompressed = 0;

        public int getNumEntries() {
            return this.numEntries;
        }

        public int getNumDirectories() {
            return this.numDirectories;
        }

        public long getTotalBytes() {
            return this.totalBytes;
        }

        public long getCompressed() {
            return this.totalCompressed;
        }

        public void addEntry(ZipEntry entry) {
            this.numEntries++;
            if (entry.isDirectory())
                this.numDirectories++;
            this.totalBytes += entry.getSize();
            this.totalCompressed += entry.getCompressedSize();
        }

        public void addStats(ZipStats stats) {
            this.numEntries += stats.numEntries;
            this.numDirectories += stats.numDirectories;
            this.totalBytes += stats.totalBytes;
            this.totalCompressed += stats.totalCompressed;
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
            retValue
            .append("ZipStats ( ")
            .append(super.toString())
            .append(TAB)
            .append("numEntries = ")
            .append(this.numEntries)
            .append(TAB)
            .append("numDirectories = ")
            .append(this.numDirectories)
            .append(TAB)
            .append("totalBytes = ")
            .append(this.totalBytes)
            .append(TAB)
            .append("totalCompressed = ")
            .append(this.totalCompressed)
            .append(" )");

            return retValue.toString();
        }
    }

    public static List<String> getFilePaths(byte[] zipFile) throws IOException {
        if (zipFile == null) {
            throw new IllegalArgumentException("File zip cannot be null");
        }

        List<String> filepaths = new ArrayList<>();

        try (final QuietCloseable<ZipInputStream> c = QuietCloseable.make(new ZipInputStream(new ByteArrayInputStream(zipFile)))) {
            final ZipInputStream zis = c.get();

            for (;;) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) {
                    break;
                }
                String name = ze.getName();
                if (ze.isDirectory()) {
                    continue;
                }

                filepaths.add(name);
                if (logger.isDebugEnabled()) {
                    logger.debug("Found filepath '" + name + "' in zip");
                }
            }
        }
        return filepaths;
    }

    /**
     * Unzip the given input stream into the given path. Returns a set of statistics about the zip file.
     */
    public static ZipStats unzip(File path, ZipInputStream is) throws IOException {
        return unzip(path, is, false);
    }

    /**
     * Unzip the given input stream into the given path. Returns a set of statistics about the zip file.
     * 
     * @param checkMarkers
     *            True if the unzipping should check for success or failure markers added by the marking methods above.
     */
    public static ZipStats unzip(File path, ZipInputStream is, boolean checkMarkers) throws IOException {
        ZipStats stats = new ZipStats();
        boolean success = !checkMarkers;
        for (ZipEntry entry = is.getNextEntry(); entry != null; entry = is.getNextEntry()) {
            if (entry.isDirectory()) {
                File dir = new File(path, entry.getName());
                boolean mkdir = dir.mkdir();
                if (logger.isDebugEnabled() && mkdir) {
                    logger.debug("Made directory");
                }
            } else {
                String entryName = entry.getName();
                File dir = path;
                while (entryName.indexOf("/") >= 0) {
                    dir = new File(dir, entryName.substring(0, entryName.indexOf("/")));
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    entryName = entryName.substring(entryName.indexOf("/") + 1);
                }
                try (final QuietCloseable<FileOutputStream> c = QuietCloseable.make(new FileOutputStream(new File(path, entry.getName())))) {
                    final FileOutputStream os = c.get();

                    ReadableByteChannel src = Channels.newChannel(is);
                    FileChannel dest = os.getChannel();
                    copy(src, dest);
                }
            }
            stats.addEntry(entry);
        }
        if (!success) {
            throw new IOException("Zip for " + path.getAbsolutePath() + " did not contain success marker.");
        }
        return stats;
    }

    /**
     * Zip the files with the given relative path ("" for root of the zip file) into the given output stream. Returns a
     * set of statistics about the zip file generated that can be used to calculate the overall compression rate.
     */
    public static ZipStats zipFiles(String relPath, File[] files, ZipOutputStream os) throws IOException {
        return zipFiles(relPath, files, os, Integer.MAX_VALUE);
    }

    /**
     * Zip the files with the given relative path ("" for root of the zip file) into the given output stream. Returns a
     * set of statistics about the zip file generated that can be used to calculate the overall compression rate.
     * 
     * @param includeDirs
     *            True if the zip should include subdirectories.
     */
    public static ZipStats zipFiles(String relPath, File[] files, ZipOutputStream os, int maxDepth) throws IOException {
        ZipStats stats = new ZipStats();
        String pathPrefix = Utils.isNotEmpty(relPath) ? relPath + "/" : "";
        for (File file : files) {
            stats.addStats(zipFile(pathPrefix + file.getName(), file, os, maxDepth));
        }
        return stats;
    }

    /**
     * Zip the file with the given relative path ("" for root of the zip file) into the given output stream. Returns a
     * set of statistics about the zip file generated that can be used to calculate the overall compression rate.
     */
    public static ZipStats zipFile(String relPath, File file, ZipOutputStream os) throws IOException {
        return zipFile(relPath, file, os, Integer.MAX_VALUE);
    }

    /**
     * Zip the file with the given relative path ("" for root of the zip file) into the given output stream. Returns a
     * set of statistics about the zip file generated that can be used to calculate the overall compression rate.
     * 
     * @param includeDirs
     *            True if the zip should include subdirectories.
     */
    public static ZipStats zipFile(String relPath, File file, ZipOutputStream os, int maxDepth) throws IOException {
        ZipStats stats = new ZipStats();
        String filePath = relPath;
        if (file.isDirectory()) {
            ZipEntry dir = new ZipEntry(filePath + "/");
            dir.setTime(file.lastModified());

            try {
                os.putNextEntry(dir);
                os.closeEntry();
                stats.addEntry(dir);
                if (maxDepth > 0) {
                    zipFiles(filePath, file.listFiles(), os, maxDepth - 1);
                }
            } catch (IOException e) {
                handleDuplicateEntryException(dir, e);
            }
        } else {
            ZipEntry entry = addFile(filePath, file, os);
            if (entry != null) {
                stats.addEntry(entry);
            }
        }
        return stats;
    }

    protected static void handleDuplicateEntryException(ZipEntry zipEntry, IOException e) throws IOException {
        if (null == e) return;

        if (e instanceof ZipException) {
            if (Utils.isNotEmpty(e.getMessage()) && e.getMessage().contains("duplicate entry")) {
                logger.warn("Zip already contains '" + zipEntry.getName() + "' - skipping duplicate");
            }
        } else {
            throw e;
        }
    }

    public static ZipStats zipFile(String filePath, byte[] file, ZipOutputStream zos, int maxValue) throws IOException {
        ZipStats stats = new ZipStats();

        ZipEntry entry = addBytes(filePath, file, zos);
        if (entry != null) {
            stats.addEntry(entry);
        }

        return stats;
    }

    public static ZipStats zipFile(String relPath, String body, ZipOutputStream os, int maxDepth) throws IOException {
        ZipStats stats = new ZipStats();
        String filePath = relPath;
        ZipEntry entry = addString(filePath, body, os);
        if (entry != null) {
            stats.addEntry(entry);
        }
        return stats;
    }

    /**
     * Helper method to zip a specific file with the given relative path (in the zip file) into the provided output
     * stream. Returns the entry that was created for this file in the zip file.
     */
    private static ZipEntry addFile(String filename, File file, ZipOutputStream os) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        entry.setTime(file.lastModified());
        entry.setSize(file.length());

        try {
            os.putNextEntry(entry);
        } catch (IOException e) {
            handleDuplicateEntryException(entry, e);
            return null;
        }

        FileInputStream is = new FileInputStream(file);
        try {
            FileChannel src = is.getChannel();
            WritableByteChannel dest = Channels.newChannel(os);
            copy(src, dest);
            os.closeEntry();
            return entry;
        } finally {
            is.close();
        }
    }

    private static ZipEntry addBytes(String filename, byte[] file, ZipOutputStream os) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        entry.setTime((new Date()).getTime());
        entry.setSize(file.length);

        try {
            os.putNextEntry(entry);
        } catch (IOException e) {
            handleDuplicateEntryException(entry, e);
            return null;
        }

        os.write(file, 0, file.length);
        os.closeEntry();
        return entry;
    }

    private static ZipEntry addString(String filename, String body, ZipOutputStream os) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        entry.setTime(Calendar.getInstance().getTimeInMillis());
        entry.setSize(body.length());
        os.putNextEntry(entry);
        os.write(body.getBytes());
        os.closeEntry();
        return entry;
    }

    /**
     * Helper method to copy from a readable channel to a writable channel, using an in-memory buffer.
     */
    private static void copy(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        // use an in-memory byte buffer
        ByteBuffer buffer = ByteBuffer.allocate(8092);
        while (src.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                dest.write(buffer);
            }
            buffer.clear();
        }
    }

    public static void writeZipToTempDir(byte[] zipFile, String zipFileName) {
        if (Utils.isEmpty(zipFile) || Utils.isEmpty(zipFileName)) {
            logger.warn("Unable to write zip bytes to file - zip fie and/or zip name is null or empty");
        }

        File tmpZipFile = null;
        String tempDir = System.getProperty(Constants.SYS_SETTING_TEMP_DIR);
        if (Utils.isEmpty(tempDir)) {
            logger.info("Set system property '" + Constants.SYS_SETTING_TEMP_DIR + "' to write zip to filesystem");
            return;
        }

        tmpZipFile = new File(tempDir + File.separator + zipFileName);
        if (tmpZipFile.exists()) {
            boolean success = tmpZipFile.delete();
            if (logger.isDebugEnabled() && success) {
                logger.debug("Deleted zip file");
            }
        }
        try (final QuietCloseable<FileOutputStream> c = QuietCloseable.make(new FileOutputStream(tmpZipFile))) {
            final FileOutputStream fos = c.get();

            fos.write(zipFile);
        } catch (FileNotFoundException e) {
            logger.error("Unable to write zip", e);
        } catch (IOException e) {
            logger.error("Unable to write zip", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Wrote '" + tmpZipFile.getAbsolutePath() + "' to file system");
        }
    }

    public static void writeRetrieveZipToTempDir(byte[] zipFile) {
        writeZipToTempDir(zipFile, "tmp-force-com-force-retrieve.zip");
    }

    public static void writeDeployZipToTempDir(byte[] zipFile) {
        writeZipToTempDir(zipFile, "tmp-force-com-force-deploy.zip");
    }
}
