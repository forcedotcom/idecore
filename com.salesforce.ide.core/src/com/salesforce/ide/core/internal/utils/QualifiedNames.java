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

import org.eclipse.core.runtime.QualifiedName;


public class QualifiedNames {

    //   O B J E C T   T Y P E   P R O P E R T I E S
	public static final QualifiedName QN_ID = new QualifiedName(Constants.FILE_PROP_PREFIX_ID, Constants.ID);
    public static final QualifiedName QN_FILE_NAME =
    	new QualifiedName(Constants.FILE_PROP_PREFIX_ID, Constants.FILE_NAME);
    public static final QualifiedName QN_FULL_NAME =
            new QualifiedName(Constants.FILE_PROP_PREFIX_ID, Constants.FULL_NAME);
    public static final QualifiedName QN_CREATED_BY_ID =
        new QualifiedName(Constants.FILE_PROP_PREFIX_ID, Constants.CREATED_BY_ID);
    public static final QualifiedName QN_CREATED_BY_NAME =
        new QualifiedName(Constants.FILE_PROP_PREFIX_ID, Constants.CREATED_BY_NAME);
    public static final QualifiedName QN_CREATED_DATE =
        new QualifiedName(Constants.FILE_PROP_PREFIX_ID, Constants.CREATED_DATE);
    public static final QualifiedName QN_LAST_MODIFIED_BY_ID =
        new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
            Constants.LAST_MODIFIED_BY_ID);
    public static final QualifiedName QN_LAST_MODIFIED_DATE = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
            Constants.LAST_MODIFIED_DATE);
    public static final QualifiedName QN_SYSTEM_MODSTAMP =
    	new QualifiedName(Constants.FILE_PROP_PREFIX_ID, Constants.SYSTEM_MODSTAMP);
    public static final QualifiedName QN_NAMESPACE_PREFIX = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.NAMESPACE_PREFIX);
    public static final QualifiedName QN_LAST_MODIFIED_BY_NAME = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.LAST_MODIFIED_BY_NAME);
    public static final QualifiedName QN_PACKAGE_NAME = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.PACKAGE_NAME);
    public static final QualifiedName QN_STATE = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.STATE);

    //   S C O N T R O L
    public static final QualifiedName QN_SCONTROL_FILENAME = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.FILENAME);
    public static final QualifiedName QN_SCONTROL_DESCRIPTION = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.DESCRIPTION);
    public static final QualifiedName QN_SCONTROL_CONTENT_SOURCE = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.CONTENT_SOURCE);
    public static final QualifiedName QN_SCONTROL_DEVELOPER_NAME = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
    		Constants.DEVELOPER_NAME);

    //   P R O J E C T   P R O P E R T I E S
    public static final QualifiedName QN_IS_NEW_PROJECT =
    	new QualifiedName(Constants.FILE_PROP_PREFIX_ID, "IsNewProject");
    public static final QualifiedName QN_SKIP_BUILDER =
    	new QualifiedName(Constants.FILE_PROP_PREFIX_ID, "SkipBuilder");

}
