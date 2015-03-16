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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public abstract class BaseWizardPage extends WizardPage {

    private static final Logger logger = Logger.getLogger(BaseWizardPage.class);

    //   C O N S T R U C T O R S
    public BaseWizardPage(String wizardName) {
        super(wizardName);
    }

    //   M E T H O D S
    protected void setTitleKeyAndDescriptionKey(String titleKey, String descriptionKey) {
        setTitle(UIMessages.getString(titleKey));
        setDescription(UIMessages.getString(descriptionKey));
    }

    protected void setTitleAndDescription(String title, String description) {
        setTitle(title);
        setDescription(description);
    }

    public void updateInfoStatus(String message) {
        setMessage(message, IMessageProvider.INFORMATION);
    }

    public void updateErrorStatus(String message) {
        setMessage(message, IMessageProvider.ERROR);
    }

    public void clearMessages() {
        updateInfoStatus(null);
    }

    // U T I L I T I E S
    protected boolean testConnection(final Controller controller, final ForceProject forceProject)
            throws InvocationTargetException {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    monitor.beginTask("Validating connection", 2);
                    monitor.worked(1);
                    try {
                        ContainerDelegate.getInstance().getServiceLocator().getProjectService().getConnectionFactory().getConnection(forceProject);
                        monitor.worked(1);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.subTask("Done");
                    }
                }
            });
        } catch (InvocationTargetException e) {
            throw e;
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        }
        return true;
    }

    protected String getText(Text txt) {
        return txt != null ? txt.getText() : null;
    }

    protected IPath getPath(Text txtDir) {
        String text = getText(txtDir);
        if (Utils.isEmpty(text)) {
            return null;
        }
        return new Path(text);
    }

    protected boolean isValidDirectory(Text txtDir) {
        IPath path = getPath(txtDir);
        return path == null || !path.toFile().isDirectory() || !path.toFile().exists();
    }

    protected void selectCombo(Combo combo, String text) {
        if (combo == null || Utils.isEmpty(text)) {
            return;
        }

        String[] options = combo.getItems();
        if (Utils.isNotEmpty(options)) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(text)) {
                    combo.select(i);
                    return;
                }
            }
        }

        combo.add(text, 0);
        combo.select(0);
    }
}
