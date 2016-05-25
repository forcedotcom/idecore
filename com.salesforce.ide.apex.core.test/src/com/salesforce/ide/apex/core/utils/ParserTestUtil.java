/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.apex.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Some shared utils for initializing the parser.
 * 
 * @author nchen
 * 
 */
public class ParserTestUtil {
    public static String readFromFile(String path) throws IOException {
    	String filePath = IdeTestUtil.getFullUrlEntry(path).getFile().replaceFirst("^/(.:/)", "$1");
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
