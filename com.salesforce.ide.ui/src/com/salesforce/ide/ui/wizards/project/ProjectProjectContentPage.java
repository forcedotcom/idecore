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
package com.salesforce.ide.ui.wizards.project;

import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;

import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.PackageManifestDocumentUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ProjectContentSummaryAssembler;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 * FXIME
 *
 * @author cwall
 */
public class ProjectProjectContentPage extends BaseProjectCreatePage {

    private static final Logger logger = Logger.getLogger(ProjectProjectContentPage.class);

    public static final String WIZARDPAGE_ID = "projectProjectContentWizardPage";

    private ProjectProjectContentComposite projectProjectContentComposite = null;
    private List<String> summaryContent = null;

    // C O N S T R U C T O R S
    public ProjectProjectContentPage(ProjectCreateWizard projectWizard) {
        super(WIZARDPAGE_ID, projectWizard);
    }

    public ProjectProjectContentPage() {
        super(WIZARDPAGE_ID);
    }

    // M E T H O D S
    public List<String> getSummaryContent() {
        return summaryContent;
    }

    /**
     * Assemble connection page wizard.
     */
    @Override
    public void createControl(Composite parent) {
        projectProjectContentComposite = new ProjectProjectContentComposite(parent, SWT.NULL, this, getProjectModel());
        setControl(projectProjectContentComposite);
        initialize();
        setPageComplete(true);

        UIUtils.setHelpContext(projectProjectContentComposite, getClass().getSimpleName());
    }

    private void initialize() {
        if (projectProjectContentComposite.getCmbPackageName() != null) {
            projectProjectContentComposite.getCmbPackageName().removeAll();
        }

        if (projectProjectContentComposite.getBtnAllApex() != null) {
            projectProjectContentComposite.getBtnAllApex().setSelection(true);
            projectProjectContentComposite.getBtnAllApex().setFocus();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setTitleAndDescription(UIMessages.getString("ProjectCreateWizard.ProjectContent.title"), UIMessages
                    .getString("ProjectCreateWizard.ProjectContent.description"));

            initPackageCombo();

            setContentSummaryText();

            validateUserInput();
        }
        super.setVisible(visible);
    }

    public void setContentSummaryText() {
        if (projectProjectContentComposite == null || projectProjectContentComposite.getTxtContentSummary() == null
                || getProjectModel().getPackageManifestModel() == null) {
            return;
        }

        // enable summary content widget and generate content
        String summary = Messages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.NoContent.message");
        FileMetadataExt fileMetadata = getProjectModel().getPackageManifestModel().getFileMetadatExt();

        StyleRange[] ranges = null;

        ProjectContentSummaryAssembler summaryAssembler = getProjectController().getProjectContentSummaryAssembler();

        // set summary based on create option
        if (projectProjectContentComposite.isAll()) { /* disabled */
            projectProjectContentComposite.setLblIntroContentSummaryTxt(UIMessages
                    .getString("ProjectCreateWizard.ProjectContent.IntroContentSummary.label"));
            projectProjectContentComposite.showContentSummary(true);
            summaryContent = summaryAssembler.generateSummaryText(fileMetadata, null, true);
            if (Utils.isNotEmpty(summaryContent)) {
                Object[] stylizedText = UIUtils.getStylizedSummary(summaryContent);
                summary = (String) stylizedText[0];
                ranges = (StyleRange[]) stylizedText[1];
            }
        } else if (projectProjectContentComposite.isAllDevCode()) { /* all developer code content */
            projectProjectContentComposite.setLblIntroContentSummaryTxt(
                UIMessages.getString("ProjectCreateWizard.ProjectContent.IntroContentSummary.label"));
            projectProjectContentComposite.showContentSummary(true);
            summaryContent = summaryAssembler.generateSummaryText(fileMetadata, getProjectModel().getForceProject().getEnabledComponentTypes(), true);
            if (Utils.isNotEmpty(summaryContent)) {
                Object[] stylizedText = UIUtils.getStylizedSummary(summaryContent);
                summary = (String) stylizedText[0];
                ranges = (StyleRange[]) stylizedText[1];
            }
        }
        if (projectProjectContentComposite.isCustomComponents()) { /* custom content */
            if (getProjectModel().getPackageManifestModel() != null
                    && PackageManifestDocumentUtils.hasContent(getProjectModel().getPackageManifestModel()
                            .getManifestDocument())) {
                projectProjectContentComposite.setLblIntroContentSummaryTxt(UIMessages
                        .getString("ProjectCreateWizard.ProjectContent.IntroContentSummary.label"));
                projectProjectContentComposite.showContentSummary(true);
                summaryContent = summaryAssembler.generateSummaryText(getProjectModel().getPackageManifestModel());
                if (Utils.isNotEmpty(summaryContent)) {
                    Object[] stylizedText = UIUtils.getStylizedSummary(summaryContent);
                    summary = (String) stylizedText[0];
                    ranges = (StyleRange[]) stylizedText[1];
                }
            } else {
                // if nothing was selected
                projectProjectContentComposite.setLblIntroContentSummaryTxt(UIMessages
                        .getString("ProjectCreateWizard.ProjectContent.IntroContentSummary.label"));
                projectProjectContentComposite.showContentSummary(true);
                summary = Messages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.NoContent.message");
            }
        } else if (projectProjectContentComposite.isNone()) { /* none */
            // no project content desired
            projectProjectContentComposite.setLblIntroContentSummaryTxt(UIMessages
                    .getString("ProjectCreateWizard.ProjectContent.None.ContentSummary.message"));
            projectProjectContentComposite.showContentSummary(false);
        }

        if (Utils.isEmpty(summary)) {
            summary = UIMessages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.Unknown.message");
        }

        projectProjectContentComposite.setTxtContentSummaryTxt(summary, ranges);
        projectProjectContentComposite.layout(true, true);
    }

    protected void initPackageCombo() {
        if (projectProjectContentComposite.getCmbPackageName() == null) {
            return;
        }

        projectProjectContentComposite.getCmbPackageName().removeAll();
        projectProjectContentComposite.getCmbPackageName().add(
            UIMessages.getString("ProjectCreateWizard.ProjectContent.SpecificPackage.ComboDefault.label"), 0);
        Set<String> packageNames = getProjectController().getProjectModel().getPackageNames();
        if (Utils.isNotEmpty(packageNames)) {
            for (String packageName : packageNames) {
                projectProjectContentComposite.getCmbPackageName().add(packageName);
            }
        } else {
            projectProjectContentComposite.disableCmbPackageName();
        }
        projectProjectContentComposite.getCmbPackageName().select(0);
    }

    @Override
    protected void setTitleAndDescription(String titleKey, String descriptionKey) {
        setTitle(titleKey);
        setDescription(descriptionKey);
    }

    // validates input and, if applicable, displays messages
    public void validateUserInput() {
        boolean complete = true;
        updateInfoStatus(null);

        if (projectProjectContentComposite.getContentSelection() == ProjectController.SPECIFIC_PACKAGE) {
            String packageName = projectProjectContentComposite.getCmbPackageNameString();
            if (Utils.isEmpty(packageName)
                    || packageName.startsWith("<")
                    || packageName.equals(UIMessages
                            .getString("ProjectCreateWizard.ProjectContent.SpecificPackage.ComboDefault.label"))) {
                updateErrorStatus("Please select a package");
                complete = false;
            }
        } else if (projectProjectContentComposite.getContentSelection() == ProjectController.CUSTOM_COMPONENTS
                && getProjectModel().getPackageManifestModel() != null) {
            Document packageManifestDocument = getProjectModel().getPackageManifestModel().getManifestDocument();
            if (!PackageManifestDocumentUtils.hasContent(packageManifestDocument)) {
                updateErrorStatus("Please select metadata components");
                complete = false;
            }

            if (logger.isDebugEnabled()) {
                PackageManifestDocumentUtils.log(packageManifestDocument);
            }
        }

        setComplete(complete);
        if (getWizard().getContainer() != null) {
            getWizard().getContainer().updateButtons();
        } else {
            logger.warn("Unable to updated buttons - wizard container is null");
        }

    }

    // save user input, call by wizard performFinish
    public void saveUserInput() {
        getProjectModel().setContentSelection(projectProjectContentComposite.getContentSelection());
        if (projectProjectContentComposite.getContentSelection() == ProjectController.SPECIFIC_PACKAGE) {
            getProjectModel().setSelectedPackageName(projectProjectContentComposite.getCmbPackageNameString());
        } else if (projectProjectContentComposite.getContentSelection() == ProjectController.CUSTOM_COMPONENTS) {
            try {
                Document manifestDocument = getProjectModel().getPackageManifestModel().getManifestDocument();
                getProjectController().setPackageManifest(manifestDocument);
            } catch (JAXBException e) {
                logger.error("Unable to save custom component selection", e);
                Utils.openError(e, true, "Unable to save custom component selection:\n" + e.getMessage());
            }
        }
    }

    public void disableServerContentOptions() {
        projectProjectContentComposite.disableServerContentOptions();
    }

    public void enableServerContentOptions() {
        projectProjectContentComposite.defaultServerContentOptions();
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }
}
