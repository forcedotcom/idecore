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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;


/**
 * Creates String from template resource.
 *
 * @author cwall
 */
public class FileReader {
    private static final Logger logger = Logger.getLogger(FileReader.class);

    private static FileReader _instance = new FileReader();

    private FileReader() {
        super();
    }

    public FileReader(File templateFile) {
        super();
    }

    public FileReader getInstance() {
        if (_instance == null) {
            _instance = new FileReader();
        }
        return _instance;
    }

    public static String getTemplateContent(ClassPathResource templateFile) {
        if (templateFile == null || !templateFile.exists()) {
            logger.error("Unable to load template file - file is null or does not exist");
            return Constants.EMPTY_STRING;
        }

        try {
            InputStream in = templateFile.getInputStream();
            byte[] buffer = new byte[256];
            int bytes_read;
            int tb = 0;
            String output = "";
            while ((bytes_read = in.read(buffer)) != -1) {
                output += new String(buffer);
                tb += bytes_read;
            }

            String template = output.substring(0, tb);

            if (Utils.isNotEmpty(template)) {
                template = template.trim();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Found the following template body size ["+template.length()+"] for '"
                        + templateFile.getFilename() + "'");
            }

            return template;
        } catch (IOException e) {
            logger.warn("Unable to read template file.  Returning empty string.", e);
            return Constants.EMPTY_STRING;
        }
    }

    public static String getTemplateContent(File templateFile) {
        if (templateFile == null || !templateFile.exists()) {
            logger.error("Unable to load template file - file is null or does not exist");
            return Constants.EMPTY_STRING;
        }

        try (final QuietCloseable<FileInputStream> c = QuietCloseable.make(new FileInputStream(templateFile))) {
            final InputStream in = c.get();

            byte[] buffer = new byte[256];
            int bytes_read;
            int tb = 0;
            String output = "";
            while ((bytes_read = in.read(buffer)) != -1) {
                output += new String(buffer);
                tb += bytes_read;
            }

            String template = output.substring(0, tb);

            if (Utils.isNotEmpty(template)) {
                template = template.trim();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Found the following template body size [" + template.length() + "] for '"
                        + templateFile.getName() + "'");
            }

            return template;
        } catch (IOException e) {
            logger.warn("Unable to read template file.  Returning empty string.", e);
            return Constants.EMPTY_STRING;
        }
    }

    public static String getTemplateContent(InputStream in) {
        if (in == null) {
            logger.error("Unable to load template file - stream is null");
            return Constants.EMPTY_STRING;
        }

        try {
            byte[] buffer = new byte[256];
            int bytes_read;
            int tb = 0;
            String output = "";
            while ((bytes_read = in.read(buffer)) != -1) {
                output += new String(buffer);
                tb += bytes_read;
            }

            String template = output.substring(0, tb);

            if (Utils.isNotEmpty(template)) {
                template = template.trim();
            }

            return template;
        } catch (IOException e) {
            logger.warn("Unable to read template file.  Returning empty string.", e);
            return Constants.EMPTY_STRING;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Unable to close template input stream");
                }
            }
        }
    }

}
