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

import org.apache.log4j.Logger;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ProjectPackageList;

public abstract class BaseComponentSynchronizeModelOperation extends SynchronizeModelOperation {
    private static final Logger logger = Logger.getLogger(BaseComponentSynchronizeModelOperation.class);

    protected SyncController syncController = null;
    protected ComponentSubscriber subscriber = null;

    protected BaseComponentSynchronizeModelOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements,
            ComponentSubscriber subscriber) {
        super(configuration, elements);
        this.subscriber = subscriber;
        this.syncController = subscriber.getSyncController();
    }

    @Override
    protected boolean canRunAsJob() {
        return true;
    }

    protected boolean clearFromRemoteCache(SyncInfo[] syncInfos, IProgressMonitor monitor) {
        boolean result = false;
        if (Utils.isNotEmpty(syncInfos) && syncController != null) {
            ProjectPackageList remoteProjectPackageList = syncController.getRemoteProjectPackageList();
            for (SyncInfo syncInfo : syncInfos) {
                ComponentVariant componentVariant = (ComponentVariant) syncInfo.getRemote();
                if (componentVariant != null) {
                    Component remoteComponent = componentVariant.getComponent();
                    result = remoteProjectPackageList.removeComponent(remoteComponent);
                    if (logger.isDebugEnabled()) {
                        logger.info("Removed component " + remoteComponent.getFullDisplayName() + " from sync cache");
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected Shell getShell() {
        return Display.getDefault().getActiveShell();
    }

    protected void logChange(SyncInfo[] infos, String target) {
        if (logger.isDebugEnabled()) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("Applying the following to " + target + ":\n");
            for (int i = 0; i < infos.length; i++) {
                strBuff.append("  (");
                strBuff.append(i + 1);
                strBuff.append(") ");
                IResource projectResource = infos[i].getLocal();
                strBuff.append(projectResource.getProjectRelativePath().toPortableString());
                strBuff.append(", direction = ");
                strBuff.append(getDirectionString(infos[i]));
                strBuff.append(", change = ");
                strBuff.append(getDeltaString(infos[i]));
            }
            logger.debug(strBuff.toString());
        }
    }

    protected String getDirectionString(SyncInfo info) {
        switch (SyncInfo.getDirection(info.getKind())) {
        case SyncInfo.OUTGOING:
            return "outgoing";
        case SyncInfo.INCOMING:
            return "incoming";
        case SyncInfo.CONFLICTING:
            return "conflicting";
        default:
            return "unknown";
        }
    }

    protected String getDeltaString(SyncInfo info) {
        switch (SyncInfo.getChange(info.getKind())) {
        case SyncInfo.ADDITION:
            return "addition";
        case SyncInfo.DELETION:
            return "deletion";
        case SyncInfo.CHANGE:
            return "change";
        default:
            return "unknown";
        }
    }

    protected String getReverseDeltaString(SyncInfo info) {
        switch (SyncInfo.getChange(info.getKind())) {
        case SyncInfo.ADDITION:
            return "deletion";
        case SyncInfo.DELETION:
            return "addition";
        case SyncInfo.CHANGE:
            return "change";
        default:
            return "unknown";
        }
    }

    protected void showErrorAsync(final Exception e, final boolean displayTrace, final String message) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                Utils.openError(e, displayTrace, message);
            }
        });
    }

    protected class ConfirmRunnable implements Runnable {
        private String title = null;
        private String message = null;
        boolean confirm = false;

        public ConfirmRunnable(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public void run() {
            confirm = Utils.openQuestion(title, message);
        }

        public boolean getResult() {
            return confirm;
        }
    }
}
