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
package com.salesforce.ide.core.remote.apex;

import com.sforce.soap.apex.DebuggingInfo_element;
import com.sforce.soap.apex.ExecuteAnonymousResult;


public class ExecuteAnonymousResultExt {

    private DebuggingInfo_element debugInfo = null;
    private ExecuteAnonymousResult executeAnonymousResult = null;

    public ExecuteAnonymousResultExt(ExecuteAnonymousResult results, DebuggingInfo_element debugInfo) {
        this.debugInfo = debugInfo;
        executeAnonymousResult = results;
    }

    public DebuggingInfo_element getDebugInfo() {
        return debugInfo;
    }

    public ExecuteAnonymousResult getResult() {
        return executeAnonymousResult;
    }

    public void setDebugInfo(DebuggingInfo_element dbi) {
        debugInfo = dbi;
    }

    public int getColumn() {
        return executeAnonymousResult.getColumn();
    }

    public java.lang.String getCompileProblem() {
        return executeAnonymousResult.getCompileProblem();
    }

    public boolean getCompiled() {
        return executeAnonymousResult.getCompiled();
    }

    public java.lang.String getExceptionMessage() {
        return executeAnonymousResult.getExceptionMessage();
    }

    public java.lang.String getExceptionStackTrace() {
        return executeAnonymousResult.getExceptionStackTrace();
    }

    public int getLine() {
        return executeAnonymousResult.getLine();
    }

    public boolean getSuccess() {
        return executeAnonymousResult.getSuccess();
    }

}
