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
package com.salesforce.ide.ui.editors.properysheets;


/**
 * Interface signifying the ability for this component to be able to sync to/from the metadat XML representation.
 * 
 * @author nchen
 * 
 */
public interface IMetadataSyncable {

    public abstract void syncToMetadata();

    public abstract void syncFromMetadata();

}
