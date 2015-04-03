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

import com.salesforce.ide.core.remote.ICodeCoverageWarningExt;
import com.sforce.soap.metadata.CodeCoverageWarning;

public class CodeCoverageWarningExt implements ICodeCoverageWarningExt {

    protected CodeCoverageWarning codeCoverageWarning = null;

    public CodeCoverageWarningExt(CodeCoverageWarning codeCoverageWarning) {
        this.codeCoverageWarning = codeCoverageWarning;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageWarningExt#getId()
    */
    @Override
    public String getId() {
        return codeCoverageWarning.getId().toString();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageWarningExt#getMessage()
    */
    @Override
    public java.lang.String getMessage() {
        return codeCoverageWarning.getMessage();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageWarningExt#getName()
    */
    @Override
    public java.lang.String getName() {
        return codeCoverageWarning.getName();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageWarningExt#getNamespace()
    */
    @Override
    public java.lang.String getNamespace() {
        return codeCoverageWarning.getNamespace();
    }
}
