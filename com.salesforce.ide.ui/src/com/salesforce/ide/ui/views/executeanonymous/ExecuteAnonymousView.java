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
package com.salesforce.ide.ui.views.executeanonymous;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.views.BaseViewPart;

public class ExecuteAnonymousView extends BaseViewPart {

    protected ExecuteAnonymousViewComposite executeAnonymousViewComposite = null;
    protected ISelectionListener fPostSelectionListener = null;
    protected ExecuteAnonymousController executeAnonymousController = null;

    public ExecuteAnonymousView() {
        super();
        executeAnonymousController = new ExecuteAnonymousController();
        createSelectionListener();
    }

    public ExecuteAnonymousController getExecuteAnonymousController() {
        return executeAnonymousController;
    }

    private void createSelectionListener() {
        fPostSelectionListener = new ISelectionListener() {
            @Override
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                IProject project = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProject(selection);
                if (project != null && ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedProject(project)) {
                    executeAnonymousController.setProject(project);
                    if (!executeAnonymousViewComposite.isDisposed()) {
                        executeAnonymousViewComposite.setActiveProject(project);
                    }
                }
            }
        };
    }

    @Override
    public void createPartControl(Composite parent) {
        executeAnonymousViewComposite = new ExecuteAnonymousViewComposite(parent, SWT.NONE, executeAnonymousController);
        setPartName("Execute Anonymous");
        setTitleImage(getImage());
        getSite().getPage().addSelectionListener(fPostSelectionListener);

        UIUtils.setHelpContext(executeAnonymousViewComposite, this.getClass().getSimpleName());
    }

    @Override
    public void setFocus() {
        if (executeAnonymousViewComposite != null) {
            executeAnonymousViewComposite.setFocus();
        }
    }

    @Override
    public void dispose() {
        getSite().getPage().removeSelectionListener(fPostSelectionListener);
        executeAnonymousViewComposite.dispose();
        super.dispose();
    }
}
