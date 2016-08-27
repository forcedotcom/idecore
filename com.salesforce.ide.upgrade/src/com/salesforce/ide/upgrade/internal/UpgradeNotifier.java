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
package com.salesforce.ide.upgrade.internal;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.ui.IPackagesViewPart;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.ui.editors.internal.BaseMultiPageEditorPart;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.upgrade.internal.utils.UpgradeMessages;
import com.salesforce.ide.upgrade.project.UpgradeNature;
import com.salesforce.ide.upgrade.ui.wizards.UpgradeWizard;

/**
 * Upgrade notifier that alerts
 * 
 * @author chris
 * 
 */
@SuppressWarnings("restriction")
public class UpgradeNotifier implements IPartListener2, ISelectionChangedListener, ITreeViewerListener {

    private static final Logger logger = Logger.getLogger(UpgradeProjectInspector.class);

    private Set<String> notifiedProjectNames = new HashSet<>();
    private ServiceLocator serviceLocator = null;
    private IWorkbenchWindow window = null;
    private IWorkbenchPage page = null;
    private IPackagesViewPart packagesViewPart = null;
    private CommonNavigator projectExplorer = null;
    private boolean enabled = true;
    private boolean save = true; // set to false for development - does not add to notifiedProjectNames, clears existing

    public UpgradeNotifier() {
        init();
    }

    //   M E T H O D S
    public Set<String> getNotifiedProjectNames() {
        return notifiedProjectNames;
    }

    public boolean removeNotifiedProjectName(String projectName) {
        return notifiedProjectNames.remove(projectName);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void init() {
        if (!PlatformUI.isWorkbenchRunning()) {
            logger
            .warn("Unable to evaluate workspace Force.com projects for upgrade-ability - workbench is not available");
            return;
        }

        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();

        initWorkbenchPage();
        handleOpenedEditors();

        // async add listeners as views might be initialized by getView
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                addOpenedViewListeners();
                addEditorViewOpenListeners();
            }
        });
    }

    private void initWorkbenchPage() {
        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            page = window.getActivePage();
        }

        if (page == null) {
            // Look for a window and get the page off it!
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (int i = 0; i < windows.length; i++) {
                if (windows[i] != null) {
                    window = windows[i];
                    page = window.getActivePage();
                    if (page != null)
                        break;
                }
            }
        }
    }

    private void addEditorViewOpenListeners() {
        if (window != null) {
            // handle editor and view opens
            window.getPartService().addPartListener(this);
        }
    }

    //   P R O J E C T   E V A L U A T I O N
    protected void evaluateProject(final IProject project) {
        if (null == project) return;

        if (!enabled) {
            logger.warn("Upgrade notifier disabled");
            return;
        }

        try {
            if (project.isOpen() && !notifiedProjectNames.contains(project.getName())
                    && project.hasNature(UpgradeNature.NATURE_ID)) {

                // display alert
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public void run() {
                        if (openUpgradeDialog(project.getName()) == 0) {
                            openUpgradeWizard(project);
                        }
                    }
                });

                // only display once per workbench session
                addProjectNotifyCheck(project);
            }
        } catch (Exception e) {
            logger.warn("Unable open upgrade alert for project '" + project.getName() + "': " + e.getMessage());
        }
    }

    protected boolean addProjectNotifyCheck(IProject project) {
        if (project == null || Utils.isEmpty(project.getName())) {
            return false;
        }

        if (!save) {
            if (logger.isDebugEnabled()) {
                logger.debug("Save set to false - NOT excluding project '" + project.getName()
                    + "' from future upgrade alerts");
            }
            notifiedProjectNames.clear();
            return true;
        }

        boolean result = notifiedProjectNames.add(project.getName());

        if (result && logger.isDebugEnabled()) {
            logger.debug("Excluding project '" + project.getName() + "' from future upgrade alerts");
        }

        return true;
    }

    protected int openUpgradeDialog(String projectName) {
        return DialogUtils.getInstance().yesNoMessage(
            UpgradeMessages.getString("UpgradeAlert.title"),
            UpgradeMessages.getString(
            "UpgradeAlert.message",
            new String[] { projectName, serviceLocator.getProjectService().getIdeReleaseName() }),
            MessageDialog.WARNING);
    }

    protected void openUpgradeWizard(IProject project) {
        try {
            UpgradeWizard upgradeWizard = new UpgradeWizard(project);
            upgradeWizard.init(window.getWorkbench(), null);
            WizardDialog dialog = new WizardDialog(window.getShell(), upgradeWizard);
            dialog.create();
            UIUtils.placeDialogInCenter(window.getShell(), dialog.getShell());
            Utils.openDialog(project, dialog);
        } catch (Exception e) {
            logger.warn("Unable to open upgrade wizard", e);
        }
    }

    public void dispose() {
        if (page != null) {
            page.removePartListener(this);
        }

        if (window != null) {
            window.getPartService().removePartListener(this);
        }

        if (packagesViewPart != null && packagesViewPart.getTreeViewer() != null) {
            packagesViewPart.getTreeViewer().removeSelectionChangedListener(this);
            packagesViewPart.getTreeViewer().removeTreeListener(this);
        }

        if (projectExplorer != null && projectExplorer.getCommonViewer() != null) {
            projectExplorer.getCommonViewer().removeSelectionChangedListener(this);
            projectExplorer.getCommonViewer().removeTreeListener(this);
        }
    }

    //   P A C K A G E   E X P L O R E R   L I S T E N E R
    private void addOpenedViewListeners() {
        if (page != null) {
            // listener on package explorer view
            if (page.findView(JavaUI.ID_PACKAGES) != null) {
                addPackagesViewListener();
            }

            // listener on project explorer view
            if (page.findView(Constants.PROJECT_EXPLORER_ID) != null) {
                addProjectsViewListener();
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Unable to add upgrade notifier listeners to views - page null");
            }
        }
    }

    private void addPackagesViewListener() {
        if (page != null && page.findView(JavaUI.ID_PACKAGES) != null) {
            packagesViewPart = (IPackagesViewPart) page.findView(JavaUI.ID_PACKAGES);
            addViewListener(this, packagesViewPart);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Unable to add upgrade notifier to package explorer - page and/or '" + JavaUI.ID_PACKAGES
                    + "' view null or empty");
            }
        }
    }

    private void addProjectsViewListener() {
        if (page != null && page.findView(Constants.PROJECT_EXPLORER_ID) != null) {
            projectExplorer = (CommonNavigator) page.findView(Constants.PROJECT_EXPLORER_ID);
            addViewListener(this, projectExplorer);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Unable to add upgrade notifier to project explorer - page and/or '"
                        + Constants.PROJECT_EXPLORER_ID + "' view null or empty");
            }
        }
    }

    private static void addViewListener(final UpgradeNotifier upgradeNotifier, final IViewPart viewPart) {
        // display alert
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void run() {
                if (viewPart instanceof IPackagesViewPart && ((IPackagesViewPart) viewPart).getTreeViewer() != null) {
                    ((IPackagesViewPart) viewPart).getTreeViewer().addSelectionChangedListener(upgradeNotifier);
                    ((IPackagesViewPart) viewPart).getTreeViewer().addTreeListener(upgradeNotifier);
                } else if (viewPart instanceof CommonNavigator
                        && ((CommonNavigator) viewPart).getCommonViewer() != null) {
                    ((CommonNavigator) viewPart).getCommonViewer().addSelectionChangedListener(upgradeNotifier);
                    ((CommonNavigator) viewPart).getCommonViewer().addTreeListener(upgradeNotifier);
                }
            }
        });

    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            Object selectedObj = ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (selectedObj instanceof IResource && !(selectedObj instanceof IFile)) {
                evaluateProject(((IResource) selectedObj).getProject());
            }
        }
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        if (event.getElement() instanceof IResource) {
            evaluateProject(((IResource) event.getElement()).getProject());
        }
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        if (event.getElement() instanceof IResource) {
            evaluateProject(((IResource) event.getElement()).getProject());
        }
    }

    //   E D I T O R
    public void handleOpenedEditors() {
        if (page != null && page.getActiveEditor() != null) {
            handleEditor(page.getActiveEditor());
        }
    }

    public void handleEditor(IEditorPart editorPart) {
        IFile file = getFile(editorPart);
        if (file != null && file.exists()) {
            evaluateProject(file.getProject());
        }
    }

    private static IFile getFile(IEditorPart editorPart) {
        if (editorPart instanceof IFileEditorInput) {
            return ((IFileEditorInput) editorPart).getFile();
        } else if (editorPart instanceof XMLMultiPageEditorPart
                && ((XMLMultiPageEditorPart) editorPart).getEditorInput() instanceof IFileEditorInput) {
            return ((IFileEditorInput) ((XMLMultiPageEditorPart) editorPart).getEditorInput()).getFile();
        } else if (editorPart instanceof BaseMultiPageEditorPart) {
            return ((BaseMultiPageEditorPart) editorPart).getEditorInputFile();
        } else {
            return null;
        }
    }

    //  E D I T O R   &   V I E W   O P E N   L I S T E N E R
    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof IEditorPart) {
            handleEditor((IEditorPart) part);
        }
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {}

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {}

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {}

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {}

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {}

    // handle views that are opened separately
    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof IViewPart) {
            addViewListener(this, (IViewPart) part);
        }
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {}
}
