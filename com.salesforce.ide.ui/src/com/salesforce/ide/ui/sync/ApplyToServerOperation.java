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
 * Applies selected project-found differences to server.
 * 
 * @author cwall
 */
class ApplyToServerOperation extends BaseComponentSynchronizeModelOperation {
    static final Logger logger = Logger.getLogger(ApplyToServerOperation.class);

    static final String OPERATION_TITLE = UIMessages.getString("SynchronizeHandler.ApplyLocalToOrganization.label");

    ApplyToServerOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements,
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
            if (SyncInfo.getDirection(syncInfo.getKind()) == SyncInfo.INCOMING) {
                logger.warn("Request to apply incoming change to remote");
                StringBuffer strBuff = new StringBuffer("Change is ");
                strBuff.append(getDirectionString(syncInfo)).append(".\n\nAre you sure you want to override ").append(
                    getDirectionString(syncInfo)).append(" ").append(getDeltaString(syncInfo)).append(
                            " and apply local ").append(getReverseDeltaString(syncInfo)).append(" to server?");
                ConfirmRunnable confirm = new ConfirmRunnable("Incoming Change Found", strBuff.toString());
                Display.getDefault().syncExec(confirm);
                if (!confirm.getResult()) {
                    return;
                }
            }
        }

        try {
            applyToServer(getSubscriber(), infos, monitor);
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance()
                        .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
            } else {
                logger.error("Unable to apply change to server", e);
                showErrorAsync(e, true, "Unable to apply change to server");
            }
        }
    }

    private void applyToServer(final ComponentSubscriber subscriber, final SyncInfo[] infos, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        if (Utils.isEmpty(infos)) {
            return;
        }

        logChange(infos, "server");

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                if (monitor != null) {
                    monitor.beginTask(OPERATION_TITLE, 4 * infos.length);
                }
                try {
                    getSyncController().applyToServer(subscriber, infos, monitor);
                } catch (InterruptedException e) {
                    logger.warn("Operation cancelled: " + e.getMessage());
                } catch (Exception e) {
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
