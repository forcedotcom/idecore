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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.ForceIdeUIPlugin;

public abstract class BaseChangeNatureAction extends BaseAction {
    private static final Logger logger = Logger.getLogger(BaseChangeNatureAction.class);

    class Decorator extends LabelProvider implements ILightweightLabelDecorator {

        @Override
        public void decorate(Object element, IDecoration decoration) {
        }

        @SuppressWarnings("synthetic-access")
        public void fireUpdateDecorators(IResource resource) {
            // generate event to update the decorators for a given resource
            final LabelProviderChangedEvent ev = new LabelProviderChangedEvent(this, resource);

            if (logger.isInfoEnabled()) {
                logger.info("Firing update decorators for resource "
                        + resource.getProjectRelativePath().toPortableString());
            }

            Display.getDefault().asyncExec(new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    fireLabelProviderChanged(ev);
                }
            });
        }
    }

    public BaseChangeNatureAction() throws ForceProjectException {
        super();
    }

    public BaseChangeNatureAction(IProject project) throws ForceProjectException {
        super();
        this.project = project;
    }

    public void updateDecorators() {
        // let the workbench generate events to update all resources affected by a decorator
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void run() {
                if (logger.isDebugEnabled()) {
                    logger.debug("Updating " + Constants.FORCE_PLUGIN_PREFIX + ".decorator.project.* decorators");
                }
                ForceIdeUIPlugin.getDefault().getWorkbench().getDecoratorManager().update(
                    Constants.FORCE_PLUGIN_PREFIX + ".decorator.project");
                ForceIdeUIPlugin.getDefault().getWorkbench().getDecoratorManager().update(
                    Constants.FORCE_PLUGIN_PREFIX + ".decorator.project.online");
            }
        });
    }

    public void fireLabelProviderChange(IResource resource) {
        (new Decorator()).fireUpdateDecorators(resource);
    }
}
