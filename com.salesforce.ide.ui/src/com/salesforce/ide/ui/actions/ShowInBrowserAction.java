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
package com.salesforce.ide.ui.actions;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.ui.internal.utils.UIMessages;

/**
 *
 * 
 * @author cwall
 */
public class ShowInBrowserAction extends BaseAction implements IShowInTarget {
    private static final Logger logger = Logger.getLogger(ShowInBrowserAction.class);

    protected String frontDoorUrlPart = null;
    protected String retUrlPart = null;
    protected String setupPart = null;
    protected String packagesPart = null;
    protected String installedPackagesPart = null;
    protected String developPart = null;
    protected String createPart = null;
    protected String customizePart = null;
    protected String folderPart = null;
    protected URL finalUrl = null;

    // C O N S T R U C T O R
    public ShowInBrowserAction() throws ForceProjectException {
        super();
        if (logger.isInfoEnabled()) {
            logger.info(getClass().getSimpleName()
                    + " should not be instantiated directly.  Get instance from bean container.");
        }
    }

    // M E T H O D S
    public String getFolderPart() {
        return folderPart;
    }

    public void setFolderPart(String folderPart) {
        this.folderPart = folderPart;
    }

    public String getRetUrlPart() {
        return retUrlPart;
    }

    public void setRetUrlPart(String retUrlPart) {
        this.retUrlPart = retUrlPart;
    }

    public String getFrontDoorUrlPart() {
        return frontDoorUrlPart;
    }

    public void setFrontDoorUrlPart(String frontDoorUrlPart) {
        this.frontDoorUrlPart = frontDoorUrlPart;
    }

    public String getSetupPart() {
        return setupPart;
    }

    public void setSetupPart(String setupPart) {
        this.setupPart = setupPart;
    }

    public String getPackagesPart() {
        return packagesPart;
    }

    public void setPackagesPart(String packagesPart) {
        this.packagesPart = packagesPart;
    }

    public String getInstalledPackagesPart() {
        return installedPackagesPart;
    }

    public void setInstalledPackagesPart(String installedPackagesPart) {
        this.installedPackagesPart = installedPackagesPart;
    }

    public String getDevelopPart() {
        return developPart;
    }

    public void setDevelopPart(String developPart) {
        this.developPart = developPart;
    }

    public String getCreatePart() {
        return createPart;
    }

    public void setCreatePart(String createPart) {
        this.createPart = createPart;
    }

    public String getCustomizePart() {
        return customizePart;
    }

    public void setCustomizePart(String customizePart) {
        this.customizePart = customizePart;
    }

    public URL getFinalUrl() {
        return finalUrl;
    }

    @Override
    public boolean show(ShowInContext context) {
        if (context == null) {
            return false;
        }

        ISelection sel = context.getSelection();
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) sel;
            Object firstElement = ss.getFirstElement();
            if (firstElement != null && firstElement instanceof IResource) {
                addSelectedResource((IResource) firstElement);
                execute(null);
                return true;
            }
        }
        return false;
    }

    @Override
    public void init() {}

    @Override
    public void execute(IAction action) {
        if (getSelectedResource() == null) {
            Utils.openError("File Unknown", "Unable to open browser for given file or project .");
            return;
        }

        try {
            if (getSelectedResource() instanceof IFile) {
                handleFile((IFile) getSelectedResource());
            } else if (getSelectedResource() instanceof IFolder) {
                handleFolder((IFolder) getSelectedResource());
            } else if (getSelectedResource() instanceof IProject) {
                handleSetupLanding((IProject) getSelectedResource());
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

    private void handleFile(IFile file) throws ForceConnectionException, FactoryException, PartInitException,
            MalformedURLException, InsufficientPermissionsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Show file '" + file.getProjectRelativePath().toPortableString() + "' in Salesforce.com");
        }

        Connection connection = getConnectionFactory().getConnection(project);
        Component component = getComponentFactory().getComponentFromFile(file, false);

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

    private void handleFolder(IFolder folder) throws ForceConnectionException, PartInitException,
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

    private void handleSourceFolder(IFolder folder) throws ForceConnectionException, PartInitException,
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
        strBuff.append(encode(this.getDevelopPart()));

        if (logger.isDebugEnabled()) {
            logger.debug("Opening browser to '" + strBuff.toString() + "' for folder '"
                    + folder.getProjectRelativePath().toPortableString());
        }

        openBrowser(strBuff.toString());
    }

    private void handlePackageFolder(IFolder folder) throws ForceConnectionException, PartInitException,
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

    private void handleSubComponentFolder(IFolder folder) throws ForceConnectionException, FactoryException,
            PartInitException, MalformedURLException, InsufficientPermissionsException {
        Component component = getComponentFactory().getComponentFromSubFolder(folder, false);

        if (component == null) {
            handleSetupLanding(folder.getProject());
            return;
        }

        handleSubComponentFolder(folder.getProject(), component);
    }

    private void handleSubComponentFolder(IProject project, Component component) throws ForceConnectionException,
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

    private void handleComponentFolder(IFolder folder) throws ForceConnectionException, FactoryException,
            PartInitException, MalformedURLException, InsufficientPermissionsException {
        Component component = getComponentFactory().getComponentByFolderName(folder.getName());

        if (component == null) {
            handleSetupLanding(folder.getProject());
            return;
        }

        handleComponentFolder(folder.getProject(), component);
    }

    private void handleComponentFolder(IProject project, Component component) throws ForceConnectionException,
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

    private void handleSetupLanding(IProject project) throws ForceConnectionException, PartInitException,
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

    private void openBrowser(String urlStr) throws MalformedURLException, PartInitException {
        // Open a browser window
        finalUrl = new URL(urlStr);
        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(finalUrl);
    }

    private String getUrl(Connection connection, IProject project) throws ForceConnectionException {
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
