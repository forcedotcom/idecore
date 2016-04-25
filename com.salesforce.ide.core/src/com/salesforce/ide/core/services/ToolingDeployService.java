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
package com.salesforce.ide.core.services;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.core.remote.ForceException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.salesforce.ide.core.remote.tooling.ContainerAsyncRequestMessageHandler;
import com.salesforce.ide.core.remote.tooling.ContainerMemberFactory;
import com.salesforce.ide.core.remote.tooling.MetadataContainerFailureHandler;
import com.sforce.soap.tooling.sobject.ContainerAsyncRequest;
import com.sforce.soap.tooling.ContainerAsyncRequestState;
import com.sforce.soap.tooling.sobject.MetadataContainer;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.SaveResult;

/**
 * A service for deploying via ContainerAsyncRequest through the Tooling API. This class takes care of creating the
 * necessary container member from the components in the workspace, when possible.
 * 
 * This service might be run for multiple projects concurrently. Ensure it is threadsafe by making it stateless.
 * 
 * @author nchen
 * 
 */
public class ToolingDeployService extends BaseService {

    private static final int POLL_INTERVAL = 1000;
    private static final Logger logger = Logger.getLogger(ToolingDeployService.class);

    /**
     * <p>
     * Deploys the list of components through the Tooling API. It will attempt to compile and save. If there are
     * compilation errors, it will notify the user.
     * </p>
     * <p>
     * We create a fresh MetadataContainer each time (but with the same name). This is cleaner in case we wind up in
     * some weird state with partially created ContainerMember. This is different from the Dev Console, because we have
     * a local file system when we can store intermediate state. We don't need to exploit ContainerMembers to store
     * intermediate state.
     * </p>
     * 
     * @param list
     *            List of components that CAN be deployed using Tooling API.
     * @param monitor
     *            Monitor to provide feedback to the user.
     */
    public void deploy(ForceProject project, ComponentList list, IProgressMonitor monitor) {
        //TODO: Optimize for the case of a single save, where we can just use the ContainerAsyncRequest without MetadataContainer
        try {
            clearSaveLocallyOnlyMarkers(list);

            ToolingStubExt stub = factoryLocator.getToolingFactory().getToolingStubExt(project);

            MetadataContainer container = new MetadataContainer();
            container.setName(constructProjectIdentifier(project));
            SaveResult[] containerResults = stub.create(new SObject[] { container });

            if (containerResults[0].isSuccess()) {
                String containerId = containerResults[0].getId();
                SObject[] classMembers = createContainerMembers(containerId, list);

                SaveResult[] classMemberResults = stub.create(classMembers);
                boolean allClassMembersCreatedSuccessfully =
                        Iterables.all(Lists.newArrayList(classMemberResults), new Predicate<SaveResult>() {

                            @Override
                            public boolean apply(SaveResult result) {
                                return result.isSuccess();
                            }
                        });

                if (allClassMembersCreatedSuccessfully) {
                    ContainerAsyncRequest request = new ContainerAsyncRequest();
                    request.setIsCheckOnly(false);
                    request.setMetadataContainerId(containerId);
                    SaveResult[] requestResults = stub.create(new SObject[] { request });

                    if (requestResults[0].isSuccess()) {
                        ContainerAsyncRequest onGoingRequest = pollForStatus(stub, requestResults, monitor);
                        handleContainerAsyncMessages(list, onGoingRequest);
                    } else {
                        handleContainerAsyncRequestCreationFailure(list, requestResults);
                    }
                } else {
                    handleClassMembersCreationFailure(list, classMemberResults);
                }

                // Clean up and delete the container member (this also deletes any ContainerMembers still referencing it)
                // If deletion fails, we will see a duplicate container error the next time we deploy and handle it there.
                stub.delete(new String[] { containerResults[0].getId() });

            } else {
                handleMetadataContainerCreationFailure(project, stub, list, containerResults);
            }

        } catch (ForceException e) {
            handleToolingDeployException(e);
        }

    }

    private void handleMetadataContainerCreationFailure(ForceProject project, ToolingStubExt stub, ComponentList list,
            SaveResult[] containerResults) {
        new MetadataContainerFailureHandler(project, stub).handleCreationFailure(containerResults);
        createSaveLocallyOnlyMarkers(list);
    }

    public void clearSaveLocallyOnlyMarkers(ComponentList list) {
        IResource[] resources = obtainListOfAffectedResources(list);
        MarkerUtils.getInstance().clearDirty(resources);
    }

    public void clearSaveErrorMarkers(ComponentList list) {
        IResource[] resources = obtainListOfAffectedResources(list);
        MarkerUtils.getInstance().clearSaveMarkers(resources);
    }

    public void createSaveLocallyOnlyMarkers(ComponentList list) {
        IResource[] resources = obtainListOfAffectedResources(list);
        MarkerUtils.getInstance().applyDirty(resources);
    }

    private static IResource[] obtainListOfAffectedResources(ComponentList list) {
        IResource[] resources = Lists.transform(list, new Function<Component, IResource>() {

            @Override
            public IResource apply(Component cmp) {
                return cmp.getFileResource();
            }
        }).toArray(new IResource[0]);
        return resources;
    }

    ContainerAsyncRequest pollForStatus(ToolingStubExt stub, SaveResult[] requestResults, IProgressMonitor monitor)
            throws ForceRemoteException {
        String requestId = requestResults[0].getId();
        String soql =
                String.format("SELECT Id, State, ErrorMsg, DeployDetails FROM ContainerAsyncRequest where id = '%s'",
                    requestId);
        QueryResult queryResult = stub.query(soql);

        ContainerAsyncRequest onGoingRequest = (ContainerAsyncRequest) queryResult.getRecords()[0];

        return pollUntilUnqueuedOrCancelled(stub, monitor, soql, onGoingRequest);
    }

    ContainerAsyncRequest pollUntilUnqueuedOrCancelled(ToolingStubExt stub, IProgressMonitor monitor, String soql,
            ContainerAsyncRequest onGoingRequest) throws ForceRemoteException {
        QueryResult queryResult;
        int delayMultipler = 1;
        while (onGoingRequest.getState() == ContainerAsyncRequestState.Queued) {
            try {
                Thread.sleep(POLL_INTERVAL * delayMultipler++);
                if (monitor.isCanceled()) { // The user has canceled the task
                    ContainerAsyncRequest abortedRequest = new ContainerAsyncRequest();
                    abortedRequest.setId(onGoingRequest.getId());
                    abortedRequest.setState(ContainerAsyncRequestState.Aborted);
                    stub.update(new SObject[] { abortedRequest });
                    return abortedRequest;
                }
            } catch (InterruptedException e) {
                logger.debug("Exception while polling for ContainerAsyncRequest: ", e);
            }

            queryResult = stub.query(soql);
            onGoingRequest = (ContainerAsyncRequest) queryResult.getRecords()[0];

        }
        return onGoingRequest;
    }

    private static void handleContainerAsyncMessages(ComponentList list, ContainerAsyncRequest onGoingRequest) {
        ContainerAsyncRequestMessageHandler handler = new ContainerAsyncRequestMessageHandler(list, onGoingRequest);
        handler.handle();
    }

    private void handleContainerAsyncRequestCreationFailure(ComponentList list, SaveResult[] requestResults) {
        assert requestResults.length == 1;
        logger.debug("Failed to create ContainerAsyncRequest for deployment: " + requestResults[0]);
        createSaveLocallyOnlyMarkers(list);
    }

    private void handleClassMembersCreationFailure(ComponentList list, SaveResult[] classMemberResults) {
        logger.debug("Failed to create ContainerMembers for deployment: ");
        for (SaveResult saveResult : classMemberResults) {
            logger.debug(saveResult);
        }
        createSaveLocallyOnlyMarkers(list);
    }

    private static void handleToolingDeployException(ForceException e) {
        // TODO: Present useful information to the user in a dialog
        logger.warn(e);
    }

    /*
     * We would like to associate a unique identifer per project so that
     * i) we can easily track usage
     * ii) to prevent collision when we create the MetadataContainer since the name needs to be unique
     * Unfortunately, we cannot force users to upgrade their old projects to the new format so we act defensively here.
     */
    private static String constructProjectIdentifier(ForceProject project) {
        String projectIdentifier = project.getProjectIdentifier();
        if (Utils.isNotEmpty(projectIdentifier)) {
            return projectIdentifier;
        }
        return "IDE - " + System.currentTimeMillis();
    }

    SObject[] createContainerMembers(final String containerId, final ComponentList list) {
        List<SObject> sObjects = Lists.newArrayList();

        for (Component component : list) {
            SObject toAdd =
                    serviceLocator.getToolingService().componentDelegate(component, list,
                        new ContainerMemberFactory(containerId));

            if (toAdd != null)
                sObjects.add(toAdd);
        }

        return sObjects.toArray(new SObject[0]);

    }
}
