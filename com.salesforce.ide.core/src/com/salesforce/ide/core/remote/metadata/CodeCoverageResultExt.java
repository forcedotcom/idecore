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

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.ICodeCoverageResultExt;
import com.salesforce.ide.core.remote.ICodeLocationExt;
import com.sforce.soap.metadata.CodeCoverageResult;

public class CodeCoverageResultExt implements ICodeCoverageResultExt {

    private CodeCoverageResult codeCoverageResult = null;

    public CodeCoverageResultExt(CodeCoverageResult codeCoverageResult) {
        this.codeCoverageResult = codeCoverageResult;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getDmlInfo()
    */
    @Override
    public ICodeLocationExt[] getDmlInfo() {
        ICodeLocationExt[] codeLocations = null;
        if (Utils.isNotEmpty(codeCoverageResult.getDmlInfo())) {
            codeLocations = new ICodeLocationExt[codeCoverageResult.getDmlInfo().length];
            for (int i = 0; i < codeCoverageResult.getDmlInfo().length; i++) {
                codeLocations[i] = new CodeLocationExt(codeCoverageResult.getDmlInfo()[i]);
            }
        }
        return codeLocations;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getDmlInfo(int)
    */
    @Override
    public ICodeLocationExt getDmlInfo(int i) {
        return new com.salesforce.ide.core.remote.metadata.CodeLocationExt(codeCoverageResult.getDmlInfo()[i]);
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getId()
    */
    @Override
    public String getId() {
        return codeCoverageResult.getId().toString();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getLocationsNotCovered()
    */
    @Override
    public ICodeLocationExt[] getLocationsNotCovered() {
        ICodeLocationExt[] codeLocations = null;
        if (Utils.isNotEmpty(codeCoverageResult.getLocationsNotCovered())) {
            codeLocations = new ICodeLocationExt[codeCoverageResult.getLocationsNotCovered().length];
            for (int i = 0; i < codeCoverageResult.getLocationsNotCovered().length; i++) {
                codeLocations[i] = new CodeLocationExt(codeCoverageResult.getLocationsNotCovered()[i]);
            }
        }
        return codeLocations;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getLocationsNotCovered(int)
    */
    @Override
    public ICodeLocationExt getLocationsNotCovered(int i) {
        return new com.salesforce.ide.core.remote.metadata.CodeLocationExt(codeCoverageResult.getLocationsNotCovered()[i]);
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getMethodInfo()
    */
    @Override
    public ICodeLocationExt[] getMethodInfo() {
        ICodeLocationExt[] codeLocations = null;
        if (Utils.isNotEmpty(codeCoverageResult.getMethodInfo())) {
            codeLocations = new ICodeLocationExt[codeCoverageResult.getMethodInfo().length];
            for (int i = 0; i < codeCoverageResult.getMethodInfo().length; i++) {
                codeLocations[i] = new CodeLocationExt(codeCoverageResult.getMethodInfo()[i]);
            }
        }
        return codeLocations;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getMethodInfo(int)
    */
    @Override
    public ICodeLocationExt getMethodInfo(int i) {
        return new CodeLocationExt(codeCoverageResult.getMethodInfo()[i]);
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getName()
    */
    @Override
    public java.lang.String getName() {
        return codeCoverageResult.getName();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getNamespace()
    */
    @Override
    public java.lang.String getNamespace() {
        return codeCoverageResult.getNamespace();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getNumLocations()
    */
    @Override
    public int getNumLocations() {
        return codeCoverageResult.getNumLocations();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getNumLocationsNotCovered()
    */
    @Override
    public int getNumLocationsNotCovered() {
        return codeCoverageResult.getNumLocationsNotCovered();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getSoqlInfo()
    */
    @Override
    public ICodeLocationExt[] getSoqlInfo() {
        ICodeLocationExt[] codeLocations = null;
        if (Utils.isNotEmpty(codeCoverageResult.getSoqlInfo())) {
            codeLocations = new ICodeLocationExt[codeCoverageResult.getSoqlInfo().length];
            for (int i = 0; i < codeCoverageResult.getSoqlInfo().length; i++) {
                codeLocations[i] = new CodeLocationExt(codeCoverageResult.getSoqlInfo()[i]);
            }
        }
        return codeLocations;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getSoqlInfo(int)
    */
    @Override
    public ICodeLocationExt getSoqlInfo(int i) {
        return new com.salesforce.ide.core.remote.metadata.CodeLocationExt(codeCoverageResult.getSoqlInfo()[i]);
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.ICodeCoverageResultExt#getType()
    */
    @Override
    public java.lang.String getType() {
        return codeCoverageResult.getType();
    }
}
