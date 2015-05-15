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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;

import com.salesforce.ide.core.internal.utils.Utils;


/**
 * picks up the tests classes for execution based on the teype  of tests 
 * @author cwall
 *
 */
public class TestClassCollector {

    private static final Logger logger = Logger.getLogger(TestClassCollector.class);

    public static Class<?>[] getTestClasses(Class<?> clazz, final String allTestClassName, final String suffix,
            ClassLoader classLoader) throws Exception {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        // Get a File object for the package
        File directory = null;
        final String classSuffix = "Test" + suffix + ".class";

        String pkgname = clazz.getPackage().getName();

        logger.info("Searching for test classes in package '" + pkgname + "'");

        URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (resource == null) {
            logger.error("No resource for " + clazz.getSimpleName() + ".class");
            throw new ClassNotFoundException("No resource for " + clazz.getSimpleName() + ".class");
        }

        String path = null;
        try {
            path = FileLocator.resolve(resource).getPath();
        } catch (Exception e) {
            logger.error("Could not get path from FileLocator: " + e.getMessage());
            path = resource.getFile();
        }

        if (path.endsWith(".class")) {
            path = path.substring(0, path.lastIndexOf("/"));
        }

        if (Utils.isEmpty(path)) {
            logger.error("Unable to get root resource for package '" + path + "'");
            throw new ClassNotFoundException("Unable to get root resource for package '" + path + "'");
        }

        if (path.contains(".jar!")) {
            String jarPath = path.substring(path.indexOf(":") + 1, path.lastIndexOf("!"));
            logger.info("Inspecting jar:\n " + jarPath);
            JarInputStream jarFile = new JarInputStream(new FileInputStream(jarPath));
            JarEntry jarEntry = null;

            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }

                if (jarEntry.getName().startsWith(pkgname.replaceAll("\\.", "/"))
                        && jarEntry.getName().endsWith(classSuffix)) {
                    String className =
                            jarEntry.getName().substring(jarEntry.getName().lastIndexOf("/") + 1,
                                jarEntry.getName().lastIndexOf("."));
                    className = pkgname + "." + className;
                    try {
                        // classes.add(Class.forName(className));
                        classes.add(Class.forName(className, true, classLoader));
                        logger.info("Added test class: " + className);
                    } catch (ClassNotFoundException e) {
                        logger.info("Unable to add test class: " + className, e);
                    }
                }
            }
        } else {
            directory = new File(path);
            if (directory.exists()) {
                File[] files = directory.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(classSuffix);
                    }
                });

                if (Utils.isNotEmpty(files)) {
                    Arrays.sort(files, new Comparator<File>() {
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });

                    for (File file : files) {
                        String className = pkgname + '.' + file.getName().substring(0, file.getName().length() - 6);
                        try {
                            classes.add(Class.forName(className, true, classLoader));
                            logger.info("Added test class: " + className);
                        } catch (Exception e) {
                            logger.error("Unable to add class '" + className + "': " + e.getMessage());
                        }
                    }
                } else {
                    logger.error(pkgname + " does not appear to be a valid package - class directory '" + path
                            + "' not found");
                    throw new ClassNotFoundException(pkgname
                            + " does not appear to be a valid package - class directory '" + path + "' not found");
                }
            } else {
                logger.error(pkgname + " does not appear to be a valid package - class directory '" + path
                        + "' not found");
                throw new ClassNotFoundException(pkgname + " does not appear to be a valid package - class directory '"
                        + path + "' not found");
            }
        }

        Class<?>[] classesA = new Class<?>[classes.size()];
        classes.toArray(classesA);
        return classesA;
    }

   
}
