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

import org.eclipse.core.resources.IFile;

import com.salesforce.ide.core.internal.utils.Utils;


/**
 * @author cwall
 */
public class ApexCodeLocation {

    private IFile file = null;
    private Integer line = null;
    private Integer col = null;
    private String name = null;

    public ApexCodeLocation(IFile file, Integer line, Integer col) {
        this.file = file;
        this.line = line;
        this.col = col;
        this.name = Utils.isNotEmpty(file) ? file.getName() : "";
    }

    public ApexCodeLocation(String name, String line, String col) {
        try {
            this.line = Utils.isNotEmpty(line) ? Integer.valueOf(line) : Integer.valueOf(1);
        } catch (NumberFormatException e) {
            this.line = Integer.valueOf(1);
        }
        try {
            this.col = Utils.isNotEmpty(col) ? Integer.valueOf(col) : Integer.valueOf(1);
        } catch (NumberFormatException e) {
            this.col = Integer.valueOf(1);
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return col;
    }

    public IFile getFile() {
        return file;
    }

    public void setFile(IFile file) {
        this.file = file;
    }
}
