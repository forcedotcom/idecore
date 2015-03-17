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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.core.services.RetrieveException;
import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.startup.ForceStartup;

public class RefreshResourceAction extends BaseAction {
    private static final Logger logger = Logger.getLogger(RefreshResourceAction.class);

    public RefreshResourceAction() {
        super();
        actionController = new RefreshResourceActionController();
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***  R E F R E S H   F R O M   S E R V E R   ***");
            logger.debug("Refreshing [" + (Utils.isNotEmpty(selectedResources) ? selectedResources.size() : 0)
                + "] resources");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Force.com IDE: '" + ForceIdeUIPlugin.getPluginId() + "' plugin, version "
                    + ForceIdeUIPlugin.getBundleVersion());
        }
    }

    @Override
    public void execute(IAction action) {
        ForceStartup.removePackageManifestChangeListener();
        fetchRemoteComponents();
        ForceStartup.addPackageManifestChangeListener();
    }

    protected void fetchRemoteComponents() {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(true, true, new WorkspaceModifyOperation() {
                @Override
                protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                InterruptedException {
                    if(monitor==null){
                        monitor= new NullProgressMonitor();
                    }
                    monitor.beginTask("", 12);
                    try {
                        boolean success =
                                ((RefreshResourceActionController) actionController).refreshResources(monitor);
                        if (!success) {
                            throw new RetrieveException(
                                    "Error(s) occurred while refreshing resource(s).  See Problems View for details.");
                        }
                        monitorWork(monitor);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InterruptedException e) {
            logger.warn("Operation canceled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance()
                        .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
            } else if (cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to refresh resource(s): " + ForceExceptionUtils.getRootCauseMessage(cause));
                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(cause));
            } else {
                logger.error("Unable to refresh resource(s)", ForceExceptionUtils.getRootCause(cause));
                StringBuffer strBuff = new StringBuffer();
                strBuff.append("Unable to refresh resource '" + getSelectedResource().getName() + "':\n\n").append(
                    ForceExceptionUtils.getStrippedRootCauseMessage(e)).append("\n\n ");
                Utils.openError("Refresh Error", strBuff.toString());
            }
        }
    }
}
