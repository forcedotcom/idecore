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

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.PackageManifestModel;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.packagemanifest.PackageManifestTree;
import com.salesforce.ide.ui.packagemanifest.PackageTreeNode;

/**
 * Captures custom project content
 * 
 * @author cwall
 */
public class ProjectCustomComponentsComposite extends Composite {

    private static Logger logger = Logger.getLogger(ProjectCustomComponentsComposite.class);

    protected ProjectPackageManifestTree packageManifestTree = null;
    protected PackageManifestModel packageManifestModel = null;
    protected Connection connection = null;
    private final int STYLE = SWT.NONE; // use SWT.BORDER for testing/visualizing layout
    protected Label lblLastUpdatedPackageManifestCache = null;
    protected StatusLineLink statusLine1 = null;
    protected StatusLineLink statusLine2 = null;

    public ProjectCustomComponentsComposite(Composite parent, int style, PackageManifestModel packageManifestModel,
            Connection connection) {
        super(parent, style);
        this.packageManifestModel = packageManifestModel;
        this.connection = connection;
        initialize();
    }

    protected void initialize() {
        setLayout(new GridLayout(1, false));

        // adjust size of manifest tree
        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Rectangle rect = UIUtils.getClientArea(getShell());
                GridData gdPackageManifestTree = (GridData) packageManifestTree.getLayoutData();
                gdPackageManifestTree.heightHint = (int) (rect.height * .75);
                packageManifestTree.layout(true, true);
                layout(true, true);
            }
        });

        // tree init
        packageManifestTree =
                new ProjectPackageManifestTree(this, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE
                        | SWT.BORDER);
        GridLayout grdLayout = new GridLayout(2, false);
        grdLayout.horizontalSpacing = 0;
        grdLayout.verticalSpacing = 0;
        grdLayout.marginBottom = 0;
        grdLayout.marginWidth = 0;
        packageManifestTree.setLayout(grdLayout);
        packageManifestTree.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

        // tree inputs
        if (packageManifestModel != null) {
            if (packageManifestModel.getFileMetadatExt() != null) {
                packageManifestTree.setFileMetadatExt(packageManifestModel.getFileMetadatExt());
            }
            packageManifestTree.setDocument(packageManifestModel.getManifestDocument());
            packageManifestTree.setConnection(connection);
        }

        // cosmetics
        packageManifestTree.getClmComponent().setWidth(265);
        packageManifestTree.getClmWildcard().setWidth(265);

        // load tree
        packageManifestTree.updateTree();
        packageManifestTree.update();

        // informative dyanmic messages (x2)
        statusLine1 = new StatusLineLink(this, SWT.NONE, "ProjectCustomComponentsDialog_FLS_WARN"); //$NON-NLS-1$
        statusLine1.setMessage(Constants.NEW_LINE + Constants.NEW_LINE);
        statusLine1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 2));
        statusLine2 = new StatusLineLink(this, SWT.NONE, "ProjectCustomComponentsDialog_FLS_WARN"); //$NON-NLS-1$
        statusLine2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 2));

        updateCacheTimestamp(packageManifestTree);

        // handles storing package manifest cache w/ newly create project
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

            @Override
            public void resourceChanged(IResourceChangeEvent event) {
                if (packageManifestModel == null || packageManifestModel.getProject() == null
                        || packageManifestTree == null) {
                    return;
                }

                // only care about changes to the workspace, specifically additions
                if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
                    return;
                }

                IResourceDelta delta = event.getDelta();

                // only care about newly create projects
                // IResourceDelta.CHANGED added because 
                if (Utils.isNotEmpty(delta.getAffectedChildren())
                        && delta.getAffectedChildren()[0].getResource().getType() != IResource.PROJECT
                        && delta.getAffectedChildren()[0].getKind() != IResourceDelta.ADDED
                        || delta.getAffectedChildren()[0].getKind() == IResourceDelta.CHANGED) {
                    return;
                }

                IProject project = (IProject) delta.getAffectedChildren()[0].getResource();

                // only care about DefaultNature.NATURE_ID new projects
                try {
                    if (!project.hasNature(DefaultNature.NATURE_ID)) {
                        return;
                    }
                } catch (CoreException e) {
                    String logMessage = Utils.generateCoreExceptionLog(e);
                    logger.warn("Unable to determine if project has '" + DefaultNature.NATURE_ID + "' nature: "
                            + logMessage);
                    return;
                }

                // if project cache already exists, don't move
                File cache = Utils.getCacheFile(project);
                if (project == packageManifestModel.getProject() && (cache != null && !cache.exists())) {
                    packageManifestTree.storeManifestCache(project);
                    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
                }
            }
        });
    }

    protected void updateCacheTimestamp(ProjectPackageManifestTree packageManifestTree) {
        String cacheTimeStamp = packageManifestTree.getCacheFileTimestap();
        lblLastUpdatedPackageManifestCache.setText("Last Refreshed: "
                + (Utils.isNotEmpty(cacheTimeStamp) ? cacheTimeStamp : Utils.getLocaleFormattedDateTime((new Date())
                        .getTime())));
        layout(true, true);
    }

    protected void updateStatus(IStatus status) {
        if (status.isMultiStatus() && Utils.isNotEmpty(((MultiStatus) status).getChildren())) {
            MultiStatus multiStatus = (MultiStatus) status;
            updateStatusWork(multiStatus.getChildren()[0], statusLine1);
            updateStatusWork(multiStatus.getChildren().length > 1 ? multiStatus.getChildren()[1] : null, statusLine2);
        } else if (!status.isMultiStatus()) {
            updateStatusWork(status, statusLine1);
            updateStatusWork(null, statusLine2);
        } else {
            updateStatusWork(null, statusLine1);
            updateStatusWork(null, statusLine2);
        }

        layout(true, true);
    }

    private static void updateStatusWork(IStatus status, StatusLineLink statusLine) {
        if (statusLine != null && !statusLine.isDisposed()) {
            statusLine.setErrorStatus(status);
        }
    }

    public ProjectPackageManifestTree getProjectPackageManifestTree() {
        return packageManifestTree;
    }

    public void loadPackageManifestTreeSelections() {
        packageManifestTree.loadPackageManifestTreeSelections();
    }

    // override to provide additional controls and wrapper for tree result
    class ProjectPackageManifestTree extends PackageManifestTree {

        protected Button btnSelectAll;
        protected Button btnDeSelectAll;

        protected Button btnExpandAll;
        protected Button btnCollapseAll;
        protected Button btnRefresh;

        public ProjectPackageManifestTree(Composite parent, int treeStyle) {
            super(parent, treeStyle);

            // if null, clears cache (during project create b/c file has not been created yet) 
            // and, if present, tells pme where cache exists
            setFile(packageManifestModel.getPackageManifestFile());

            // done after parent class fully inits vars 
            postInit();
        }

        protected void postInit() {
            btnSelectAll.setText(selectAllAction.getText());
            btnSelectAll.setToolTipText(selectAllAction.getText());

            btnDeSelectAll.setText(deselectAllAction.getText());
            btnDeSelectAll.setToolTipText(deselectAllAction.getText());

            btnRefresh.setImage(getRefreshAction().getImageDescriptor().createImage());
            btnRefresh.setToolTipText(getRefreshAction().getToolTipText());

            btnCollapseAll.setImage(getCollapseAllAction().getImageDescriptor().createImage());
            btnCollapseAll.setToolTipText(getCollapseAllAction().getToolTipText());

            btnExpandAll.setImage(getExpandAllAction().getImageDescriptor().createImage());
            btnExpandAll.setToolTipText(getExpandAllAction().getToolTipText());

            filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 0));
            getTreeControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 0));
        }

        protected void loadPackageManifestTreeSelections() {
            if (packageManifestModel == null || controller == null) {
                return;
            }

            // reset transient selections
            if (packageManifestModel.getNodeAllSelectionMap() != null) {
                packageManifestModel.getNodeAllSelectionMap().clear();
            }

            // only interested in object selections to handle "partial" notation on summary content display
            loadObjectSelections(controller.getNode(Constants.STANDARD_OBJECT + Constants.FOWARD_SLASH));
            loadObjectSelections(controller.getNode(Constants.CUSTOM_OBJECT + Constants.FOWARD_SLASH));
        }

        protected String getCacheFileTimestap() {
            return Utils.getFormattedTimestamp(Utils.getCacheFile(packageManifestModel.getProject()));
        }

        private void loadObjectSelections(PackageTreeNode rootObjectNode) {
            if (rootObjectNode == null || Utils.isEmpty(rootObjectNode.getChildList())) {
                return;
            }

            List<PackageTreeNode> objectNodes = rootObjectNode.getChildList();
            if (Utils.isNotEmpty(objectNodes)) {
                for (PackageTreeNode packageTreeNode : objectNodes) {
                    boolean isAllSelected = true;
                    if (Utils.isNotEmpty(packageTreeNode.getChildList())) {
                        isAllSelected = allChildrenChecked(packageTreeNode);
                    }
                    packageManifestModel.addNodeAllSelection(packageTreeNode.getName(), isAllSelected);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Object '" + packageTreeNode.getName() + "' is all checked " + isAllSelected);
                    }
                }
            }
        }

        // move cache to project
        protected void storeManifestCache(IProject project) {
            controller.setProject(project);
            file = null;
        }

        // this stuff goes in-between the search bar and checkbox and tree box
        @Override
        protected Composite createFilterControls(Composite parent) {
            super.createFilterControls(parent);

            Composite inbetweenComposite = new Composite(this, STYLE);
            GridLayout grdLayout = new GridLayout(3, false);
            grdLayout.horizontalSpacing = 0;
            grdLayout.verticalSpacing = 0;
            grdLayout.marginBottom = 0;
            grdLayout.marginWidth = 0;
            inbetweenComposite.setLayout(grdLayout);
            inbetweenComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));

            ProjectCustomComponentsLeftControlComposite projectCustomComponentsLeftControlComposite =
                    new ProjectCustomComponentsLeftControlComposite(inbetweenComposite, STYLE);
            projectCustomComponentsLeftControlComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BOTTOM, false,
                    false));

            Label label = new Label(inbetweenComposite, STYLE);
            label.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));

            ProjectCustomComponentsRightControlComposite projectCustomComponentsRightControlComposite =
                    new ProjectCustomComponentsRightControlComposite(inbetweenComposite, STYLE);
            projectCustomComponentsRightControlComposite.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, false));

            // last datetime of cache update
            lblLastUpdatedPackageManifestCache = new Label(inbetweenComposite, STYLE);
            lblLastUpdatedPackageManifestCache.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, false, 3, 1));

            return parent;
        }

        class ProjectCustomComponentsLeftControlComposite extends Composite {

            ProjectCustomComponentsLeftControlComposite(Composite parent, int style) {
                super(parent, style);
                initialize(parent);
            }

            protected void initialize(Composite parent) {
                GridLayout grdLayout = new GridLayout(3, false);
                grdLayout.horizontalSpacing = 0;
                grdLayout.verticalSpacing = 0;
                grdLayout.marginBottom = 0;
                grdLayout.marginWidth = 0;
                setLayout(grdLayout);

                btnSelectAll = new Button(this, SWT.PUSH);
                btnSelectAll.setLayoutData(new GridData(SWT.BEGINNING, SWT.BOTTOM, false, true, 1, 0));
                btnSelectAll.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        widgetDefaultSelected(e);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        selectAllAction.run();
                    }
                });

                btnDeSelectAll = new Button(this, SWT.PUSH);
                btnDeSelectAll.setLayoutData(new GridData(SWT.BEGINNING, SWT.BOTTOM, false, true, 1, 0));
                btnDeSelectAll.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        widgetDefaultSelected(e);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        deselectAllAction.run();
                    }
                });

                Label label = new Label(this, STYLE);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            }
        }

        class ProjectCustomComponentsRightControlComposite extends Composite {
            ProjectCustomComponentsRightControlComposite(Composite parent, int style) {
                super(parent, style);
                initialize(parent);
            }

            protected void initialize(Composite parent) {
                GridLayout grdLayout = new GridLayout(3, false);
                grdLayout.horizontalSpacing = 0;
                grdLayout.verticalSpacing = 0;
                grdLayout.marginBottom = 0;
                grdLayout.marginWidth = 0;
                setLayout(grdLayout);

                btnExpandAll = new Button(this, SWT.PUSH);
                btnExpandAll.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, true, 1, 0));
                btnExpandAll.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        widgetDefaultSelected(e);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        getExpandAllAction().run();
                    }
                });

                btnCollapseAll = new Button(this, SWT.PUSH);
                btnCollapseAll.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, true, 1, 0));
                btnCollapseAll.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        widgetDefaultSelected(e);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        getCollapseAllAction().run();
                    }
                });

                btnRefresh = new Button(this, SWT.PUSH);
                btnRefresh.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, true, 1, 0));
                btnRefresh.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        lblLastUpdatedPackageManifestCache.setText("");
                        getRefreshAction().run();

                        getRefreshAction().job.addJobChangeListener(new IJobChangeListener() {
                            @Override
                            public void done(IJobChangeEvent event) {
                                updateCacheTimestamp(packageManifestTree);
                            }

                            @Override
                            public void aboutToRun(IJobChangeEvent event) {}

                            @Override
                            public void awake(IJobChangeEvent event) {}

                            @Override
                            public void running(IJobChangeEvent event) {}

                            @Override
                            public void scheduled(IJobChangeEvent event) {}

                            @Override
                            public void sleeping(IJobChangeEvent event) {}
                        });

                    }

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        widgetDefaultSelected(e);
                    }
                });

            }
        }
    }

    @Override
    public void dispose() {
        if (packageManifestTree != null && !packageManifestTree.isDisposed()) {
            packageManifestTree.dispose();
        }
        super.dispose();
    }
}
