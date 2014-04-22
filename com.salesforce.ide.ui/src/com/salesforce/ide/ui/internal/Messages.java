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
package com.salesforce.ide.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages {
    private static final String BUNDLE_NAME = "com.salesforce.ide.ui.internal.Messages"; //$NON-NLS-1$

    public static String PackageManifestChangeListener_dialog_title;
    public static String PackageManifestChangeListener_dialog_message;

    public static String PackageManifestTree_columnName_component;
    public static String PackageManifestTree_columnName_wildcard;
    public static String PackageManifestTree_columnItemLabel_wildcard;
    public static String PackageManifestTree_showOnlySelected_label;

    public static String PackageManifestTree_selectAll_text;
    public static String PackageManifestTree_deselectAll_text;
    public static String PackageManifestTree_expandAll_text;
    public static String PackageManifestTree_collapseAll_text;
    public static String PackageManifestTree_refresh_text;

    public static String PackageManifestTree_checkWarning_text;
    public static String PackageManifestTree_filterWarning_text;
    public static String PackageManifest_content_Warning_text;

    public static String ResourceDeleteParticipant_remoteConfirmation_title;
    public static String ResourceDeleteParticipant_remoteConfirmation_message;
    public static String ResourceDeleteParticipant_exception_message;

    public static String ResourceDeleteParticipant_fileLocallyDeleted_message;
    public static String ResourceDeleteParticipant_remoteWorkflowConfirmation_message;
    public static String ResourceDeleteParticipant_remotePortalConfirmation_message;
    public static String ResourceDeleteParticipant_remoteSiteConfirmation_message;

    public static String DEBUG;
    public static String INFO;
    public static String WARNING;
    public static String ERROR;
    public static String TRACE;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
