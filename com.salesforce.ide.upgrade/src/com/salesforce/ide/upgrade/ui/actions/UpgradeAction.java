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
package com.salesforce.ide.upgrade.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.actions.BaseAction;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.upgrade.ui.wizards.UpgradeWizard;

/**
 * Launches upgrade wizard.
 * 
 * @author cwall
 */
public class UpgradeAction extends BaseAction implements IWorkbenchWindowActionDelegate {
    private static final Logger logger = Logger.getLogger(UpgradeAction.class);

    public UpgradeAction() throws ForceProjectException {
        super();
    }

    // no filtering needed
    @Override
    protected List<IResource> filter(List<IResource> selectedResources) {
        return selectedResources;
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***  U P G R A D E   P R O J E C T   ***");
        }
    }

    @Override
    public void execute(IAction action) {
        try {
            // instantiates the wizard container with the wizard and opens it
            UpgradeWizard upgradeWizard = new UpgradeWizard(project);
            upgradeWizard.init(getWorkbenchWindow().getWorkbench(), (IStructuredSelection) selection);
            WizardDialog dialog = new WizardDialog(getShell(), upgradeWizard);
            dialog.create();
            UIUtils.placeDialogInCenter(getWorkbenchWindow().getShell(), dialog.getShell());
            Utils.openDialog(getProject(), dialog);
        } catch (Exception e) {
            logger.error("Unable to open upgrade wizard", e);
        }
    }

    public void dispose() {

    }

    public void init(IWorkbenchWindow window) {
        setWorkbenchWindow(window);
    }
}
