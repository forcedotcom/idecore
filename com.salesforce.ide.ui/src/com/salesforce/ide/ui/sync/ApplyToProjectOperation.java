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
package com.salesforce.ide.ui.sync;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.ui.internal.utils.UIMessages;

/**
 * Applies selected server-found differences to local project.
 * 
 * @author cwall
 */
class ApplyToProjectOperation extends BaseComponentSynchronizeModelOperation {
    static final Logger logger = Logger.getLogger(ApplyToProjectOperation.class);

    static final String OPERATION_TITLE = UIMessages.getString("SynchronizeHandler.ApplyRemoteToProject.label");

    ApplyToProjectOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements,
            ComponentSubscriber subscriber) {
        super(configuration, elements, subscriber);
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        final SyncInfo[] infos = getSyncInfoSet().getSyncInfos();
        if (Utils.isEmpty(infos)) {
            return;
        }

        for (SyncInfo syncInfo : infos) {
            if (SyncInfo.getDirection(syncInfo.getKind()) == SyncInfo.OUTGOING) {
                logger.warn("Request to apply outgoing change to project:");
                StringBuffer strBuff = new StringBuffer("Change is ");
                strBuff.append(getDirectionString(syncInfo)).append(".\n\nAre you sure you want to override ").append(
                    getDirectionString(syncInfo)).append(" ").append(getDeltaString(syncInfo)).append(
                            " and apply server ").append(getReverseDeltaString(syncInfo)).append(" to local?");
                ConfirmRunnable confirm = new ConfirmRunnable("Outgoing Change Found", strBuff.toString());
                Display.getDefault().syncExec(confirm);
                if (!confirm.getResult()) {
                    return;
                }
            }
        }

        try {
            applyToProject(infos, OPERATION_TITLE, monitor);
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance()
                        .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
            } else {
                logger.error("Unable to apply change to project", e);
                showErrorAsync(e, true, "Unable to apply change to project");
            }
        }
    }

    private void applyToProject(final SyncInfo[] infos, final String operationType, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        if (Utils.isEmpty(infos)) {
            return;
        }

        logChange(infos, "project");

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws InvocationTargetException {
                if (monitor != null) {
                    monitor.beginTask(operationType, 4 * infos.length);
                }
                try {
                    getSyncController().applyToProject(getSubscriber(), infos, monitor);
                } catch (InterruptedException e) {
                    logger.warn("Operation cancelled: " + e.getMessage());
                } catch (final Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    if (monitor != null) {
                        monitor.done();
                    }
                }
            }
        };

        // go!!!
        operation.run(monitor);
    }
}
