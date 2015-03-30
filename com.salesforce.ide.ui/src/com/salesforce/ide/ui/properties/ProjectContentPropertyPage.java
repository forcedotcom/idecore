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
package com.salesforce.ide.ui.properties;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.w3c.dom.Document;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.ui.handlers.RefreshResourceHandler;
import com.salesforce.ide.ui.internal.startup.ForceStartup;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 *
 * Property page for setting project manifest content.
 *
 * @author fchang
 */
public class ProjectContentPropertyPage extends BasePropertyPage {
    private static final Logger logger = Logger.getLogger(ProjectContentPropertyPage.class);
    private ProjectContentPropertyComposite projectContentPropertyComposite = null;
    private ProjectController projectController = null;
    private String currentSavedSummary = null;
    private String originalSummary = null;
    private boolean updated = false;

    public ProjectContentPropertyPage() {
        super();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        setTitle(UIMessages.getString("ProjectProperties.ProjectContent.title"));
        enableButtons(updated);
        UIUtils.setHelpContext(getControl(), this.getClass().getSimpleName());
    }

    @Override
    protected Control createContents(Composite parent) {
        Package packageManifest = null;
        try {
            packageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory().getPackageManifest(getProject());
        } catch (FactoryException e) {
            String errorMsg =
                    Messages.getString("ProjectProperties.ProjectContent.ContentSummary.FactoryException.message");
            logger.error(errorMsg, e);
            enableApplyButton(false);
            Utils.openError(e, true, errorMsg
                    + ", please make sure package manifest is well-form and present in proper location.");
        }
        projectContentPropertyComposite = new ProjectContentPropertyComposite(parent, SWT.NULL, this, packageManifest);

        return projectContentPropertyComposite;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            ProjectModel projectModel = getProjectController().getProjectModel();
            ForceProject forceProject = getProjectService().getForceProject(getProject());
            projectModel.setForceProject(forceProject);
            projectModel.getPackageManifestModel().setProject(getProject());

            // get package manifest file to tell pme where cache exists
            IFile packageManifestFile =
                    ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory().getPackageManifestFile(getProject());
            projectModel.getPackageManifestModel().setPackageManifestFile(packageManifestFile);

            // set cache, may not exists
            Document packageManifestCache = Utils.loadDocument(Utils.getCacheUrl(getProject()));
            if (packageManifestCache != null) {
                projectModel.getPackageManifestModel().setManifestCache(packageManifestCache);
            }

            // load project manifest
            Package packageManifest = null;
            try {
                packageManifest =
                        ContainerDelegate.getInstance().getFactoryLocator().getPackageManifestFactory().getPackageManifestFromFile(packageManifestFile);
                projectModel.getPackageManifestModel().setPackageManifest(packageManifest);

                Document packageManifestDoc =
                        ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory().getPackageManifestDOMDocument(getProject());
                projectModel.getPackageManifestModel().setManifestDocument(packageManifestDoc);
            } catch (FileNotFoundException e) {
                String errorMsg =
                        Messages.getString("ProjectProperties.ProjectContent.ContentSummary.JaxbException.message");
                logger.error(errorMsg, e);
                enableApplyButton(false);
                Utils.openError(e, true, "Project Package Manifest is missing or not found");
            } catch (JAXBException e) {
                String errorMsg =
                        Messages.getString("ProjectProperties.ProjectContent.ContentSummary.JaxbException.message");
                logger.error(errorMsg, e);
                enableApplyButton(false);
                Utils.openError(e, true, errorMsg
                        + ", please make sure package manifest is well-form and present in proper location.");
            } catch (FactoryException e) {
                String errorMsg =
                        Messages.getString("ProjectProperties.ProjectContent.ContentSummary.FactoryException.message");
                logger.error(errorMsg, e);
                enableApplyButton(false);
                Utils.openError(e, true, errorMsg
                        + ", please make sure package manifest is well-form and present in proper location.");
            }

            // assemble project content summary tree
            setContentSummaryText(false);
        }

        super.setVisible(visible);
    }

    private void refreshManifestObjects() {
        ProjectModel projectModel = getProjectController().getProjectModel();

        // update package manifest w/ selected items
        try {
            Document manifestDocument =
                    getProjectController().getProjectModel().getPackageManifestModel().getManifestDocument();
            Package packageManifest =
                ContainerDelegate.getInstance().getFactoryLocator().getPackageManifestFactory().createPackageManifest(manifestDocument);
            projectModel.getPackageManifestModel().setPackageManifest(packageManifest);
        } catch (JAXBException je) {
            String errorMsg =
                    Messages.getString("ProjectProperties.ProjectContent.ContentSummary.JaxbException.message");
            logger.error(errorMsg, je);
            Utils.openError(je, true, errorMsg
                    + ", please make sure package manifest is well-form and present in proper location.");
        }

        // refresh cache, may not have exists during initial opening
        Document packageManifestCache = Utils.loadDocument(Utils.getCacheUrl(getProject()));
        if (packageManifestCache != null) {
            projectModel.getPackageManifestModel().setManifestCache(packageManifestCache);
        }
    }

    public void setContentSummaryText(boolean refresh) {
        // refresh manifest related objects; 
        if (refresh) {
            refreshManifestObjects();
        }

        String summary = UIMessages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.NoContent.message");
        StyleRange[] ranges = null;
        List<String> summaryContent =
                getProjectController().getProjectContentSummaryAssembler().generateSummaryText(
                    getProjectController().getProjectModel().getPackageManifestModel(), true);
        if (Utils.isNotEmpty(summaryContent)) {
            Object[] stylizedText = UIUtils.getStylizedSummary(summaryContent);
            summary = (String) stylizedText[0];
            ranges = (StyleRange[]) stylizedText[1];
        }
        // obtain the original package manifest summary content for future diff.
        if (originalSummary == null) {
            originalSummary = summary;
        }

        projectContentPropertyComposite.setTxtContentSummaryTxt(summary, ranges);
        projectContentPropertyComposite.layout(true, true);
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
        enableApplyButton(updated);
    }

    @Override
    protected void performApply() {
        if (!updated) {
            return;
        }

        // persist the dom doc state to packageManifest
        Document manifestDocument =
                getProjectController().getProjectModel().getPackageManifestModel().getManifestDocument();
        try {
            getProjectController().setPackageManifest(manifestDocument);
        } catch (JAXBException je) {
            String errorMsg =
                    Messages.getString("ProjectProperties.ProjectContent.ContentSummary.JaxbException.message");
            logger.error(errorMsg, je);
            Utils.openError(je, true, errorMsg
                    + ", please make sure package manifest is well-form and present in proper location.");
        }

        Package packageManifest =
                getProjectController().getProjectModel().getPackageManifestModel().getPackageManifest();
        try {
            // replace existing project package manifest file content w/ packageManifest obj
            ForceStartup.removePackageManifestChangeListener();
            getProjectController().savePackageManifest(packageManifest, getNullProgressMonitor());
            currentSavedSummary = projectContentPropertyComposite.getTxtContentSummary().getText();

        } catch (InterruptedException ie) {
            logger.info("Operation cancelled by user");
        } finally {
            ForceStartup.addPackageManifestChangeListener();
        }

        refreshMessageDialogCheck();

        enableApplyButton(false);

        // update complete, set flag to trigger no further updates (eg, if ok is selected do nothing)
        updated = false;
    }

    @Override
    public boolean performOk() {
        performApply();
        return true;
    }

    @Override
    public boolean performCancel() {
        refreshMessageDialogCheck();
        return true;
    }

    protected IProject getProject() {
        return (IProject) getElement();
    }

    @Override
    public void dispose() {
        projectController = null;
    }

    protected ProjectController getProjectController() {
        // lazy init
        if (projectController == null) {
            projectController = new ProjectController(getProject());
        }
        return projectController;
    }

    private void refreshMessageDialogCheck() {
        try {
            // if packageManifest content summary diff than project current/orig one, then prompt refresh option dialog
            if (Utils.isNotEmpty(currentSavedSummary) && !currentSavedSummary.equals(originalSummary)) {
                if (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                    com.salesforce.ide.ui.internal.Messages.PackageManifestChangeListener_dialog_title, NLS.bind(
                        com.salesforce.ide.ui.internal.Messages.PackageManifestChangeListener_dialog_message,
                        getProject().getName()))) {
                    ForceStartup.removePackageManifestChangeListener();
                    IFolder srcFolder = getProjectService().getSourceFolder(getProject());
                    if (srcFolder != null && srcFolder.exists()) {
                        refreshProject(PlatformUI.getWorkbench(), new StructuredSelection(srcFolder));
                        // set original summary to current saved summary only successfully refresh project
                        originalSummary = currentSavedSummary;
                    } else {
                        logger.warn("Unable to refresh package.xml changes - source folder does not exists");
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        } catch (Exception fpe) {
            logger.error(
                "Force project exception occurred when perform refresh as part of saving new project content property",
                fpe);
        } finally {
            ForceStartup.addPackageManifestChangeListener();
        }
    }

    private static void refreshProject(final IWorkbench workbench, final IStructuredSelection selection) throws InvocationTargetException,
            InterruptedException {
        final IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(false, false, new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                monitor.beginTask("Refreshing project...", IProgressMonitor.UNKNOWN);
                try {
                    RefreshResourceHandler.execute(workbench, selection);
                } catch (Throwable e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        });
    }

    private static IProgressMonitor getNullProgressMonitor() {
        return new NullProgressMonitor();
    }

}

