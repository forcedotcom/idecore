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
package com.salesforce.ide.test.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.services.ProjectService;

/**
 * Base Testing Utility class for generic utility methods
 * 
 * @author agupta
 * @deprecated move anything that's required to idetestutil
 */
public class BaseTestingUtil {

    /**
     * returns "http://"+value of server in test.properties
     * 
     * @return
     */
    public static String getSoapEndpointWithProtocol() {
        return "http://" + getSoapEndpointServer();
    }

    /**
     * returns the value for "server" in test.properties
     * 
     * @return
     */
    public static String getSoapEndpointServer() {
        return ConfigProps.getInstance().getProperty("server");
    }

    public static String getUseHttp() {
        return ConfigProps.getInstance().getProperty("default.pde.use-https");
    }

    public static Object getBean(String name) throws ForceProjectException {
        //return IdeContainerDelegate.getInstance().getBean(name);
        return ContainerDelegate.getInstance().getBean(name);
    }

    public static ProjectService getProjectService() throws ForceProjectException {
        return (ProjectService) getBean("projectService");
    }

    public static String getRandomString(int maxLength) {
        String randomString = System.currentTimeMillis() + Double.toString(Math.random());
        randomString = randomString.replaceAll("\\W", "");
        return randomString.substring(randomString.length() - maxLength);
    }

    public static String getRandomString() {
        return getRandomString(10);
    }

    public static boolean compareStringsIgnoreNewLine(String expected, String actual) {
        expected = expected.trim();
        actual = actual.trim();
        if (expected.replaceAll("[\r\n]", "").equals(actual.replaceAll("[\r\n]", "")))
            return true;

        return false;
    }

    public static boolean compareStringsIgnoreNewLine(byte[] expected, byte[] actual) {
        return compareStringsIgnoreNewLine(new String(expected), new String(actual));
    }

    public static boolean deleteDirectoryRecursively(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectoryRecursively(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static void copyFilesRecursively(File destDir, File... srcFiles) throws IOException {
        if (!destDir.exists())
            destDir.mkdir();

        File destPath;
        for (File srcPath : srcFiles) {
            if (srcPath.exists()) {
                destPath = new File(destDir, srcPath.getName());
                if (srcPath.isDirectory()) {
                    copyFilesRecursively(destPath, srcPath.listFiles());
                } else {
                    FileOutputStream fos = new FileOutputStream(destPath);
                    fos.write(Utils.getBytesFromFile(srcPath));
                    fos.close();
                }
            }
        }
    }
}
