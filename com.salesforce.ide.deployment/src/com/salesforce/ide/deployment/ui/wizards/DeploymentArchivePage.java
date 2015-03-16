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
package com.salesforce.ide.deployment.ui.wizards;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.deployment.ForceIdeDeploymentPlugin;
import com.salesforce.ide.deployment.internal.utils.DeploymentConstants;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 * 
 * @author cwall
 * 
 */
public class DeploymentArchivePage extends BaseDeploymentPage {
    private static final Logger logger = Logger.getLogger(DeploymentArchivePage.class);

    public static final String WIZARDPAGE_ID = "deploymentArchiveWizardPage";

    // M E M B E R   V A R I A B L E S
    private DeploymentArchiveComposite archiveComposite = null;

    // C O N S T R U C T O R S
    public DeploymentArchivePage(DeploymentWizard deploymentWizard) {
        super(WIZARDPAGE_ID, deploymentWizard);
    }

    // M E T H O D S

    /**
     * Assemble connection page wizard.
     */
    @Override
    public void createControl(Composite parent) {
        archiveComposite = new DeploymentArchiveComposite(parent, SWT.NULL, this);
        //updatePageComplete();
        setControl(archiveComposite);

        UIUtils.setHelpContext(archiveComposite, this.getClass().getSimpleName());
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setTitleAndDescription(DeploymentMessages.getString("DeploymentWizard.ArchivePage.title") + " "
                    + getStepString(), DeploymentMessages.getString("DeploymentWizard.ArchivePage.description"));

            String lastSourceDeploymentArchiveDirSelected =
                    ForceIdeDeploymentPlugin
                            .getPreferenceString(DeploymentConstants.LAST_SOURCE_DEPLOYMENT_ARCHIVE_DIR_SELECTED);
            if (Utils.isNotEmpty(lastSourceDeploymentArchiveDirSelected)) {
                archiveComposite.setTxtSourceDirectory(lastSourceDeploymentArchiveDirSelected);
            }

            String lastDestDeploymentArchiveDirSelected =
                    ForceIdeDeploymentPlugin
                            .getPreferenceString(DeploymentConstants.LAST_DEST_DEPLOYMENT_ARCHIVE_DIR_SELECTED);
            if (Utils.isNotEmpty(lastDestDeploymentArchiveDirSelected)) {
                archiveComposite.setTxtDestinationDirectory(lastDestDeploymentArchiveDirSelected);
            }

            archiveComposite.getChkDestinationArchive().setSelection(true);
            validateUserInput();
        }

        super.setVisible(visible);
    }

    public void validateUserInput() {
        setPageComplete(false);

        if (archiveComposite.getChkSourceArchive().getSelection()
                && isValidDirectory(archiveComposite.getTxtSourceDirectory())) {
            updateInfoStatus(DeploymentMessages
                    .getString("DeploymentWizard.ArchiveComposite.SourceArchiveNotExist.error"));
            return;
        }

        if (archiveComposite.getChkDestinationArchive().getSelection()
                && isValidDirectory(archiveComposite.getTxtDestinationDirectory())) {
            updateInfoStatus(DeploymentMessages
                    .getString("DeploymentWizard.ArchiveComposite.DestinationArchiveNotExist.error"));
            return;
        }

        updateInfoStatus(null);

        saveUserInput();

        setPageComplete(true);
    }

    public void saveUserInput() {
        DeploymentWizardModel deploymentWizardModel =
                deploymentWizard.getDeploymentController().getDeploymentWizardModel();
        File destinationDir = new File(getText(archiveComposite.getTxtDestinationDirectory()));
        deploymentWizardModel.setDestinationArchivePath(destinationDir);
        File sourceDir = new File(getText(archiveComposite.getTxtSourceDirectory()));
        deploymentWizardModel.setSourceArchivePath(sourceDir);
    }

    @Override
    public boolean canFlipToNextPage() {
        saveUserInput();
        return isPageComplete();
    }

    @Override
    public IWizardPage getNextPage() {
        try {
            deploymentWizard.getDeploymentController().saveArchiveSettings(new NullProgressMonitor());
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled by user");
        }

        return super.getNextPage();
    }
}
