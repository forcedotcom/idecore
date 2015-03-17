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
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.startup.ForceStartup;

public class SaveToServerAction extends BaseAction {
    private static final Logger logger = Logger.getLogger(SaveToServerAction.class);

    public SaveToServerAction() {
        super();
        actionController = new SaveToServerActionController();
    }

    protected SaveToServerActionController getSaveToServerActionController() {
        return (SaveToServerActionController) actionController;
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***  S A V E   T O   S E R V E R   ***");
            logger.debug("Saving [" + (Utils.isNotEmpty(selectedResources) ? selectedResources.size() : 0)
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
        saveToServer();
        ForceStartup.addPackageManifestChangeListener();
    }

    protected void saveToServer() {
        WorkspaceModifyOperation saveToServerRunnable = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
                try {
                    monitor.beginTask("", 4);
                    boolean success = getSaveToServerActionController().saveResourcesToServer(monitor);
                    if (success) {
                        getProjectService().flagSkipBuilder(getSelectedResource().getProject());
                    }
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    if (monitor != null) {
                        monitor.subTask("Done");
                    }
                }
            }
        };

        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(true, true, saveToServerRunnable);
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance()
                        .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
            } else if (cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to save resource(s): " + ForceExceptionUtils.getRootCauseMessage(cause));
                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(cause));
            } else {
                logger.error("Unable to save resource(s)", ForceExceptionUtils.getRootCause(e));
                Utils.openError(e, true, "Unable to save resource '" + getSelectedResource().getName()
                    + "' to server: " + ForceExceptionUtils.getRootCauseMessage(e));
            }
        }
    }
}
