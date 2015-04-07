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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantDescriptor;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.utils.UIConstants;

public class ComponentSyncParticipant extends SubscriberParticipant {

    private static final Logger logger = Logger.getLogger(ComponentSyncParticipant.class);

    private static final String CONTRIBUTION_GROUP = "context_group_1";

    //   M E N U   O P E R A T I O N S
    // TODO: need to enable actions conditionally
    final class ApplyToProjectAction extends SynchronizeModelAction {
        ApplyToProjectAction(String text, ISynchronizePageConfiguration configuration) {
            super(text, configuration);
        }

        @Override
        public SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration,
                IDiffElement[] elements) {
            return new ApplyToProjectOperation(configuration, elements, componentSubscriber);
        }
    }

    final class ApplyToServerAction extends SynchronizeModelAction {
        ApplyToServerAction(String text, ISynchronizePageConfiguration configuration, ComponentSubscriber subscriber) {
            super(text, configuration);
        }

        @Override
        public SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration,
                IDiffElement[] elements) {
            return new ApplyToServerOperation(configuration, elements, componentSubscriber);
        }
    }

    class ComponentSyncActionContribution extends SynchronizePageActionGroup {
        @Override
        public void initialize(ISynchronizePageConfiguration configuration) {
            super.initialize(configuration);
            appendToGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, CONTRIBUTION_GROUP, new ApplyToProjectAction(
                    ApplyToProjectOperation.OPERATION_TITLE, configuration));
            appendToGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, CONTRIBUTION_GROUP, new ApplyToServerAction(
                    ApplyToServerOperation.OPERATION_TITLE, configuration, componentSubscriber));
        }
    }

    private IProject project = null;
    private ComponentSubscriber componentSubscriber = null;
    private List<IResource> syncResources = null;

    public ComponentSyncParticipant(IProject project, List<IResource> syncResources) {
        super();
        this.project = project;
        this.syncResources = syncResources;
    }

    public void execute(IProgressMonitor monitor) throws TeamException, InterruptedException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        monitorCheck(monitor);

        if (Utils.isEmpty(syncResources)) {
            this.syncResources = new ArrayList<>(1);
            syncResources.add(project);
        }

        componentSubscriber = new ComponentSubscriber(project, syncResources);
        componentSubscriber.loadRemoteComponents(monitor);

        monitorWorkCheck(monitor);

        setSubscriber(componentSubscriber);
        monitorWorkCheck(monitor);
    }

    @Override
    public void setSubscriber(Subscriber subscriber) {
        super.setSubscriber(subscriber);
        try {
            ISynchronizeParticipantDescriptor descriptor =
                    TeamUI.getSynchronizeManager().getParticipantDescriptor(UIConstants.SYNC_PARTICIPANT_ID);
            setInitializationData(descriptor);
            setSecondaryId(Long.toString(System.currentTimeMillis()));
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to initialize subscriber: " + logMessage, e);
        }
    }

    @Override
    public void initializeConfiguration(ISynchronizePageConfiguration configuration) {
        super.initializeConfiguration(configuration);
        configuration.addMenuGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, CONTRIBUTION_GROUP);
        configuration.addActionContribution(new ComponentSyncActionContribution());
    }

    private static void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            if (monitor.isCanceled()) {
                throw new InterruptedException("Operation cancelled");
            }
        }
    }

    private static void monitorWorkCheck(IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor);
    }

    private static void monitorWork(IProgressMonitor monitor) {
        if (monitor == null) {
            return;
        }

        monitor.worked(1);
    }
}
