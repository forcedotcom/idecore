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

import com.salesforce.ide.core.remote.ICodeLocationExt;
import com.sforce.soap.metadata.CodeLocation;

public class CodeLocationExt implements ICodeLocationExt  {
    private CodeLocation codeLocation = null;

    public CodeLocationExt(CodeLocation codeLocation) {
        this.codeLocation = codeLocation;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeLocationExt#getColumn()
    */
    @Override
    public int getColumn() {
        return codeLocation.getColumn();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeLocationExt#getLine()
    */
    @Override
    public int getLine() {
        return codeLocation.getLine();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeLocationExt#getNumExecutions()
    */
    @Override
    public int getNumExecutions() {
        return codeLocation.getNumExecutions();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeLocationExt#getTime()
    */
    @Override
    public double getTime() {
        return codeLocation.getTime();
    }

}
