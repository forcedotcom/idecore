/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.handlers;

import static com.salesforce.ide.core.internal.utils.Constants.FORCE_PLUGIN_PREFIX;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.services.ProjectService;

public abstract class BaseHandler extends AbstractHandler {
    private static final Logger logger = Logger.getLogger(BaseHandler.class);

    protected static final ConnectionFactory getConnectionFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory();
    }

    protected static final ComponentFactory getComponentFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
    }

    protected static final ProjectService getProjectService() {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService();
    }

    protected static final List<IResource> filter(final List<IResource> selectedResources) {
        return getProjectService().filterChildren(selectedResources);
    }

    protected static List<IResource> getFilteredResources(final IStructuredSelection selection) {
        return filter(getSelectedResources(selection));
    }

    protected static List<IResource> getSelectedResources(final IStructuredSelection selection) {
        final IAdapterManager adapterManager = Platform.getAdapterManager();

        final List<IResource> selectedResources = new ArrayList<>();
        for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            final IResource selectedResource = (IResource) adapterManager.getAdapter(iterator.next(), IResource.class);
            if (null != selectedResource) {
                selectedResources.add(selectedResource);
            }
        }
        return selectedResources;
    }

    protected static final void updateDecorators(final IWorkbench workbench) {
        // let the workbench generate events to update all resources affected by a decorator
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void run() {
                if (logger.isDebugEnabled()) {
                    logger.debug("Updating " + FORCE_PLUGIN_PREFIX + ".decorator.project.* decorators");
                }
                workbench.getDecoratorManager().update(FORCE_PLUGIN_PREFIX + ".decorator.project");
                workbench.getDecoratorManager().update(FORCE_PLUGIN_PREFIX + ".decorator.project.online");
            }
        });
    }

    protected static final IProject getProjectChecked(final ExecutionEvent event) throws ExecutionException {
        final IStructuredSelection selection = getStructuredSelection(event);
        switch (selection.size()) {
        case 0:
            throw new ExecutionException("No project found while executing " + event.getCommand().getId()); //$NON-NLS-1$
        case 1:
            break;
        default:
            throw new ExecutionException("Too many projects found while executing " + event.getCommand().getId()); //$NON-NLS-1$
        }
        final Object firstElement = selection.getFirstElement();
        final IProject project = (IProject) Platform.getAdapterManager().getAdapter(firstElement, IProject.class);
        if (null == project) {
            throw new ExecutionException("Incorrect type for project found while executing " //$NON-NLS-1$
                    + event.getCommand().getId() + ", expected " + IProject.class.getName() //$NON-NLS-1$
                    + " found " + firstElement.getClass().getName()); //$NON-NLS-1$
        }
        return project;
    }

    protected static final ISelection getSelection(final ExecutionEvent event) throws ExecutionException {
        return HandlerUtil.getCurrentSelectionChecked(event);
    }
    
    protected static final IStructuredSelection getStructuredSelection(final ExecutionEvent event) throws ExecutionException {
        final ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection) selection;
        }
        throw new ExecutionException("Incorrect type for " //$NON-NLS-1$
                + ISources.ACTIVE_CURRENT_SELECTION_NAME + " found while executing " //$NON-NLS-1$
                + event.getCommand().getId() + ", expected " + IStructuredSelection.class.getName() //$NON-NLS-1$
                + " found " + selection.getClass().getName()); //$NON-NLS-1$
    }

}
