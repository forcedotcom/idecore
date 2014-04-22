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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.sync.ComponentSyncParticipant;

public class SyncAction extends BaseAction {
    private static final Logger logger = Logger.getLogger(SyncAction.class);

    protected static ComponentSyncParticipant syncParticipant = null;

    //   C O N S T R U C T O R S
    public SyncAction() throws ForceProjectException {
        super();
    }

    //   M E T H O D S
    public static ComponentSyncParticipant getSyncParticipant() {
        return syncParticipant;
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***   S Y N C H R O N I Z E   R E S O U R C E   ***'");
            logger.debug("Synchronizing [" + (Utils.isNotEmpty(selectedResources) ? selectedResources.size() : 0)
                + "] resources");
        }

        if (!getProjectService().isInManagedProject(getSelectedResource())) {
            Utils.openWarn("No Resource Selected", "Please select a " + Constants.PRODUCT_NAME
                + " resource to synchronize with remote server instance.");
            return;
        }
    }

    @Override
    public void execute(IAction action) {
        if (Utils.isEmpty(selectedResources) || project == null) {
            logger.warn("Unable to perform sync - resource and/or project is null");
            Utils.openWarn("No Resource Selected", "Please select a " + Constants.PRODUCT_NAME
                + " resource to synchronize with remote server instance.");
            return;
        }

        try {
            prepareSyncParticipant();

            ISynchronizeManager manager = TeamUI.getSynchronizeManager();
            manager.addSynchronizeParticipants(new ISynchronizeParticipant[] { syncParticipant });
            ISynchronizeView view = manager.showSynchronizeViewInActivePage();
            view.display(syncParticipant);
            syncParticipant.reset();

        } catch (InterruptedException e) {
            logger.warn("Operation canceled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();

            if (cause instanceof TeamException && ((TeamException) cause).getStatus() != null
                    && ((TeamException) cause).getStatus().getException() != null) {
                cause = ((TeamException) cause).getStatus().getException();
            }

            if (cause instanceof InsufficientPermissionsException) {
                // log failure; option enabling occurs downstream
                logger.warn("Unable to synchronize resource '" + getSelectedResource().getName() + "' to server: "
                        + cause.getMessage());

                InsufficientPermissionsException ex = (InsufficientPermissionsException) e.getTargetException();
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(true);

                // show dialog
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(ex);
            } else if (cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to synchronize resource '" + getSelectedResource().getName() + "' to server: "
                        + cause.getMessage());

                InvalidLoginException ex = (InvalidLoginException) cause;
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(true);

                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ex.getMessage(), null, true);
            } else {
                logger.error("Unable to synchronize resource '" + getSelectedResource().getName() + "' to server",
                    ForceExceptionUtils.getRootCause(e));
                Utils.openError(e, true, "Unable to synchronize resource '" + getSelectedResource().getName()
                    + "' to server: " + ForceExceptionUtils.getRootCauseMessage(e));
            }
        }
    }

    private void prepareSyncParticipant() throws InvocationTargetException, InterruptedException {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(true, true, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Preparing and evaluating Synchronize results", 3);
                try {
                    if (syncParticipant == null) {
                        syncParticipant = new ComponentSyncParticipant(project, selectedResources);
                    } else {
                        syncParticipant.clear();
                        syncParticipant.setProject(project);
                        syncParticipant.resetSyncResource(selectedResources);
                    }

                    syncParticipant.execute(monitor);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        });
    }
}
