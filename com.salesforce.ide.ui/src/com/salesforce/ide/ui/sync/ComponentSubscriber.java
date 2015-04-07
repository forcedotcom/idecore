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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public class ComponentSubscriber extends Subscriber {

    static final Logger logger = Logger.getLogger(ComponentSubscriber.class);

    private SyncController syncController = null;
    private SyncInfoSet syncInfoSet;

    public ComponentSubscriber(IProject project, List<IResource> syncResources) {
        super();
        syncController = new SyncController(project, syncResources);
    }

    public SyncInfoSet getSyncInfoSet() {
        return this.syncInfoSet;
    }

    @Override
    public void collectOutOfSync(IResource[] resources, int depth, SyncInfoSet set, IProgressMonitor monitor) {
        this.syncInfoSet = set;
        super.collectOutOfSync(resources, depth, set, monitor);
    }

    SyncController getSyncController() {
        return syncController;
    }

    void loadRemoteComponents(IProgressMonitor monitor) throws TeamException {
        try {
            syncController.loadRemoteComponents(monitor);
        } catch (Exception e) {
            logger.error("Unable to load synchronize with server: " + ForceExceptionUtils.getRootCauseMessage(e));
            throw TeamException.asTeamException(new InvocationTargetException(e));
        }
    }

    @Override
    public String getName() {
        return UIMessages.getString("SyncSubscriber.title");
    }

    @Override
    public IResourceVariantComparator getResourceComparator() {
        return new ComponentVariantComparator();
    }

    @Override
    public SyncInfo getSyncInfo(IResource resource) throws TeamException {
        try {
            return syncController.getSyncInfo(resource);
        } catch (Exception e) {
            logger.error("Unable to synchronize with server", e);
            throw new TeamException("Unable to synchronize with server", e);
        }
    }

    @Override
    public boolean isSupervised(IResource resource) throws TeamException {
        return syncController.isSupervised(resource);
    }

    @Override
    public IResource[] members(IResource resource) throws TeamException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Collection resource members for '" + resource.getName() + "'");
            }
            return syncController.members(resource);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to generate list of " + Constants.PLUGIN_NAME + " managed folders: " + logMessage, e);
            throw TeamException.asTeamException(e);
        }
    }

    void refresh(IProgressMonitor monitor) throws TeamException {
        refresh(null, 0, monitor);
    }

    void refresh(IResource[] resources, IProgressMonitor monitor) throws TeamException {
        refresh(resources, 0, monitor);
    }

    @Override
    public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
        if (logger.isDebugEnabled()) {
            logger.debug("Sync refresh called");
        }

        try {
            loadRemoteComponents(new SubProgressMonitor(monitor, 3));
        } catch (TeamException e) {
            logger.error("Unable to refresh sync view", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unable to refresh sync view", e);
            throw TeamException.asTeamException(new InvocationTargetException(e));
        }

        // TODO: implement refreshing: this is not the right way to do that, there should be some listener that handles
        // refreshing the UI, and avoids the double refresh that actually occurs when first displaying the sync view
        // to have a proper refresh we should have state caching
        final ISynchronizeManager manager = TeamUI.getSynchronizeManager();
        Display display = PlatformUI.getWorkbench().getDisplay();
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                ISynchronizeView view = manager.showSynchronizeViewInActivePage();
                ISynchronizeParticipant participant = view.getParticipant();
                if (participant instanceof SubscriberParticipant) {
                    SubscriberParticipant sp = (SubscriberParticipant) participant;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reset component subscriber...");
                    }
                    sp.reset();
                }
            }
        });
    }

    // the follow narrows the focus of the synchronize action
    @Override
    public IResource[] roots() {
        return syncController.roots();
    }
}
