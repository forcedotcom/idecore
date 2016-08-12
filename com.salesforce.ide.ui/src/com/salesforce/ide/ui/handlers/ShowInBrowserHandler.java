/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.handlers;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public final class ShowInBrowserHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(ShowInBrowserHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        final IStructuredSelection selection = getStructuredSelection(event);
        execute(workbench, selection);
        return null;
    }

    public static final void execute(final IWorkbench workbench, final IStructuredSelection selection) throws IllegalArgumentException {
        if (null == workbench) throw new IllegalArgumentException("The workbench argument cannot be null");
        if (null == selection) throw new IllegalArgumentException("The selection argument cannot be null");

        final List<IResource> filteredResources = getFilteredResources(selection);
        if (filteredResources.isEmpty()) return;

        final IResource firstResource = filteredResources.get(0);
        try {
            if (firstResource instanceof IFile) {
                handleFile((IFile) firstResource);
            } else if (firstResource instanceof IFolder) {
                handleFolder((IFolder) firstResource);
            } else if (firstResource instanceof IProject) {
                handleSetupLanding((IProject) firstResource);
            }
        } catch (ForceConnectionException e) {
            logger.warn("Unable to show in Salesforce.com", e);
            Utils.openWarn("", UIMessages.getString("ShowInForceAction.UnableToShow.error",
                new String[] { ForceExceptionUtils.getRootCauseMessage(e) }));
            return;
        } catch (InsufficientPermissionsException e) {
            logger.warn("Unable to show in Salesforce.com", e);
            Utils.openWarn("", UIMessages.getString("ShowInForceAction.UnableToShow.error",
                new String[] { ForceExceptionUtils.getRootCauseMessage(e) }));
            return;
        } catch (Exception e) {
            logger.warn("Unable to show in Salesforce.com", e);
            Utils.openError(new InvocationTargetException(e), true, UIMessages.getString(
                "ShowInForceAction.UnableToShow.error", new String[] { ForceExceptionUtils.getRootCauseMessage(e) }));
            return;
        }
    }

    private static void handleFile(IFile file) throws ForceConnectionException, FactoryException, PartInitException,
            MalformedURLException, InsufficientPermissionsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Show file '" + file.getProjectRelativePath().toPortableString() + "' in Salesforce.com");
        }

        Connection connection = getConnectionFactory().getConnection(file.getProject());
        Component component = getComponentFactory().getComponentFromFile(file);

        if (component == null) {
            handleSetupLanding(file.getProject());
            return;
        }

        if (Utils.isEmpty(component.getId())) {
            handleComponentFolder(file.getProject(), component);
            return;
        }

        String url = getUrl(connection, file.getProject());
        if (Utils.isEmpty(url)) {
            logger.error(UIMessages.getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            Utils.openError("Unable to Show In Salesforce.com", UIMessages
                    .getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            return;
        }

        StringBuffer strBuff = new StringBuffer();
        String componentId = component.getId();
        strBuff.append(url).append("&").append(getRetUrlPart());
        if (Utils.isNotEmpty(component.getWebComponentUrlPart())) {
            strBuff.append(encode(component.getWebComponentUrlPart()));
        }

        if (component.getComponentType().equals(Constants.LAYOUT)
                || component.getComponentType().equals(Constants.WORKFLOW)) {
        } else if (CustomObjectNameResolver.getCheckerForStandardObject().check(component.getName(), component.getComponentType())) {
            strBuff.append(component.getName());
        } else {
            strBuff.append(componentId);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Opening browser to '" + strBuff.toString() + "' for file '"
                    + file.getProjectRelativePath().toPortableString());
        }

        // open!
        openBrowser(strBuff.toString());
    }

    private static void handleFolder(IFolder folder) throws ForceConnectionException, PartInitException,
            MalformedURLException, FactoryException, InsufficientPermissionsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Show folder '" + folder.getProjectRelativePath().toPortableString() + "' in Salesforce.com");
        }

        if (getProjectService().isComponentFolder(folder)) {
            handleComponentFolder(folder);
        } else if (getProjectService().isSourceFolder(folder)) {
            handleSourceFolder(folder);
        } else if (getProjectService().isReferencedPackagesFolder(folder)) {
            handlePackageFolder(folder);
        } else if (getProjectService().isSubComponentFolder(folder)) {
            handleSubComponentFolder(folder);
        } else {
            handleSetupLanding(folder.getProject());
        }
    }

    private static void handleSourceFolder(IFolder folder) throws ForceConnectionException, PartInitException,
            MalformedURLException, InsufficientPermissionsException {
        if (!Constants.SOURCE_FOLDER_NAME.equals(folder.getName())) {
            if (logger.isInfoEnabled()) {
                logger.info("Found folder '" + Constants.SOURCE_FOLDER_NAME + "' - defaulting to project landing part");
            }
            handleSetupLanding(folder.getProject());
            return;
        }

        Connection connection = getConnectionFactory().getConnection(folder.getProject());

        String url = getUrl(connection, folder.getProject());
        if (Utils.isEmpty(url)) {
            logger.error(UIMessages.getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            Utils.openError("Unable to Show In Salesforce.com", UIMessages
                    .getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            return;
        }

        StringBuffer strBuff = new StringBuffer(url);
        strBuff.append("&").append(getRetUrlPart());
        strBuff.append(encode(getDevelopPart()));

        if (logger.isDebugEnabled()) {
            logger.debug("Opening browser to '" + strBuff.toString() + "' for folder '"
                    + folder.getProjectRelativePath().toPortableString());
        }

        openBrowser(strBuff.toString());
    }

    private static void handlePackageFolder(IFolder folder) throws ForceConnectionException, PartInitException,
            MalformedURLException, InsufficientPermissionsException {
        if (Constants.DEFAULT_PACKAGED_NAME.equals(folder.getName())) {
            if (logger.isInfoEnabled()) {
                logger.info("Found package '" + Constants.DEFAULT_PACKAGED_NAME
                        + "' - defaulting to project landing part");
            }
            handleSetupLanding(folder.getProject());
            return;
        }

        Connection connection = getConnectionFactory().getConnection(folder.getProject());

        String url = getUrl(connection, folder.getProject());
        if (Utils.isEmpty(url)) {
            logger.error(UIMessages.getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            Utils.openError("Unable to Show In Salesforce.com", UIMessages
                    .getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            return;
        }

        StringBuffer strBuff = new StringBuffer(url);
        strBuff.append("&").append(getRetUrlPart());
        if (getProjectService().isSourceFolder(folder)) {
            strBuff.append(encode(getPackagesPart()));
        } else if (getProjectService().isReferencedPackagesFolder(folder)
                || getProjectService().isReferencedPackagesFolder(folder)) {
            strBuff.append(encode(getInstalledPackagesPart()));
        } else {
            strBuff.append(encode(getPackagesPart()));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Opening browser to '" + strBuff.toString() + "' for folder '"
                    + folder.getProjectRelativePath().toPortableString());
        }

        openBrowser(strBuff.toString());
    }

    private static void handleSubComponentFolder(IFolder folder) throws ForceConnectionException, FactoryException,
            PartInitException, MalformedURLException, InsufficientPermissionsException {
        Component component = getComponentFactory().getComponentFromSubFolder(folder, false);

        if (component == null) {
            handleSetupLanding(folder.getProject());
            return;
        }

        handleSubComponentFolder(folder.getProject(), component);
    }

    private static void handleSubComponentFolder(IProject project, Component component) throws ForceConnectionException,
            PartInitException, MalformedURLException, InsufficientPermissionsException {
        Connection connection = getConnectionFactory().getConnection(project);

        String url = getUrl(connection, project);
        if (Utils.isEmpty(url)) {
            logger.error(UIMessages.getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            Utils.openError("Unable to Show In Salesforce.com", UIMessages
                    .getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            return;
        }

        StringBuffer tmpStrBuff = new StringBuffer(component.getWebComponentTypeUrlPart());
        if (Utils.isNotEmpty(component.getId())) {
            tmpStrBuff.append("?").append(getFolderPart()).append(component.getId());
        }

        StringBuffer strBuff = new StringBuffer(url);
        strBuff.append("&").append(getRetUrlPart()).append(encode(tmpStrBuff.toString()));

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Opening browser to '" + strBuff.toString() + "' for component '"
                            + component.getDisplayName());
        }

        openBrowser(strBuff.toString());
    }

    private static void handleComponentFolder(IFolder folder) throws ForceConnectionException, PartInitException, MalformedURLException, InsufficientPermissionsException {
        Component component = getComponentFactory().getComponentByFolderName(folder.getName());

        if (component == null) {
            handleSetupLanding(folder.getProject());
            return;
        }

        handleComponentFolder(folder.getProject(), component);
    }

    private static void handleComponentFolder(IProject project, Component component) throws ForceConnectionException,
            PartInitException, MalformedURLException, InsufficientPermissionsException {
        Connection connection = getConnectionFactory().getConnection(project);

        String url = getUrl(connection, project);
        if (Utils.isEmpty(url)) {
            logger.error(UIMessages.getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            Utils.openError("Unable to Show In Salesforce.com", UIMessages
                    .getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            return;
        }

        StringBuffer strBuff = new StringBuffer(url);
        strBuff.append("&").append(getRetUrlPart()).append(encode(component.getWebComponentTypeUrlPart()));

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Opening browser to '" + strBuff.toString() + "' for component '"
                            + component.getDisplayName());
        }

        openBrowser(strBuff.toString());
    }

    private static void handleSetupLanding(IProject project) throws ForceConnectionException, PartInitException,
            MalformedURLException, InsufficientPermissionsException {
        Connection connection = getConnectionFactory().getConnection(project);

        String url = getUrl(connection, project);
        if (Utils.isEmpty(url)) {
            logger.error(UIMessages.getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            Utils.openError("Unable to Show In Salesforce.com", UIMessages
                    .getString("ShowInForceAction.UnableToShow.error")
                    + ".  Frontdoor string, session id and/or component id not available.");
            return;
        }

        StringBuffer strBuff = new StringBuffer(url);
        strBuff.append("&").append(getRetUrlPart()).append(encode(getSetupPart()));

        if (logger.isDebugEnabled()) {
            logger.debug("Opening browser to '" + strBuff.toString() + "' for project '" + project.getName());
        }

        openBrowser(strBuff.toString());
    }

    private static void openBrowser(String urlStr) throws MalformedURLException, PartInitException {
        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(urlStr));
    }

    private static String getUrl(Connection connection, IProject project) throws ForceConnectionException {
        // Build a url from the file and the project connection properties
        StringBuffer strBuff = new StringBuffer(connection.getWebUrlRoot());
        String sessionId = null;
        try {
            sessionId = connection.getSessionId(true);
        } catch (ForceRemoteException e) {
            logger.warn("No session id found");
        }

        if (Utils.isEmpty(sessionId)) {
            return null;
        }

        strBuff.append("/").append(getFrontDoorUrlPart()).append(sessionId);

        if (logger.isDebugEnabled()) {
            logger.debug("Frontdoor url: " + strBuff.toString());
        }

        return strBuff.toString();
    }

    private static String getDevelopPart() {
        return "ui/setup/Setup?setupid=DevToolsIntegrate";
    }

    private static Object getFolderPart() {
        return "fcf=";
    }

    private static Object getFrontDoorUrlPart() {
        return "secur/frontdoor.jsp?sid=";
    }

    private static String getInstalledPackagesPart() {
        return "0A3?setupid=ImportedPackage&inst=1";
    }

    private static String getPackagesPart() {
        return "033?setupid=Package";
    }

    private static Object getRetUrlPart() {
        return "retURL=/";
    }

    private static String getSetupPart() {
        return "ui/setup/Setup";
    }

    private static String encode(String str) {
        if (Utils.isEmpty(str)) {
            return str;
        }

        // ... and do a full file name escape:
        try {
            String encodedStr = URLEncoder.encode(str, Constants.FORCE_DEFAULT_ENCODING_CHARSET);
            return encodedStr;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

}
