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
package com.salesforce.ide.ui.internal.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.ForceImages;

public abstract class BaseWizard extends Wizard implements INewWizard {

    //private static final Logger logger = Logger.getLogger(BaseWizard.class);

    protected IProject project = null;
    protected Controller controller = null;
    protected IWorkbench workbench = null;
    protected IStructuredSelection selection = null;

    public BaseWizard() {
        super();
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
        controller.getModel().setProject(project);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setNeedsProgressMonitor(false);
        setDefaultPageImageDescriptor(getImageDescriptor());
        setWindowTitle(getWindowTitleString());
    }

    protected ImageDescriptor getImageDescriptor() {
        return ForceImages.getDesc(ForceImages.APEX_WIZARD_IMAGE);
    }

    @Override
    public boolean performFinish() {
        if (controller != null) {
            controller.dispose();
        }
        return true;
    }

    @Override
    public boolean performCancel() {
        super.performCancel();
        controller.dispose();
        return true;
    }

    @Override
    public boolean canFinish() {
        return controller.canComplete();
    }

    protected void centerOnScreen() {
        Shell shell = getShell();
        Point size = shell.getSize();
        Rectangle screenBounds = Display.getDefault().getBounds();

        int x = (screenBounds.width - size.y) / 2;
        int y = (screenBounds.height - size.y) / 2;
        Rectangle bounds = new Rectangle(x, y, size.x, size.y);
        shell.setBounds(bounds);
    }

    protected abstract String getWindowTitleString();

    // U T I L I T I E S
    protected boolean isEmpty(String str) {
        return Utils.isEmpty(str);
    }

}
