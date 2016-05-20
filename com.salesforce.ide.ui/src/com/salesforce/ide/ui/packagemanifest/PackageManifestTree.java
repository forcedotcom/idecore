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
package com.salesforce.ide.ui.packagemanifest;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.progress.WorkbenchJob;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.PackageManifestDocumentUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.Messages;
import com.salesforce.ide.ui.viewer.TreeItemNotifyingTreeViewer;
import com.salesforce.ide.ui.widgets.MultiCheckboxButton;

/**
 * 
 * @author ataylor
 */
public class PackageManifestTree extends FilteredTree {
    private static Logger logger = Logger.getLogger(PackageManifestTree.class);

    final int IMAGE_MARGIN = 2;
    protected Document doc;
    protected IFile file;
    protected Connection connection;

    protected PackageManifestController controller;
    protected MultiStatus status;
    protected Status profileObjectStatus;
    protected Status filterStatus;

    protected Set<IStatusChangedListener> statusChangedListeners = new HashSet<>();
    protected HideNonCheckedFilter checkFilter;
    protected Button hideCheckFilterButton;
    protected TreeColumn clmComponent;
    protected TreeColumn clmWildcard;

    Job refreshJob;

    // actions
    private final CollapseAction collapseAllAction = new CollapseAction();
    private final ExpandAction expandAllAction = new ExpandAction();
    private final RefreshAction refreshAction = new RefreshAction();

    public Action selectAllAction = new Action(Messages.PackageManifestTree_selectAll_text, IAction.AS_PUSH_BUTTON) {
        @Override
        public void run() {
            try {
                getTreeControl().setRedraw(false);
                for (Object o : controller.getEnabledCompTypeTreeNodes()) {
                    ComponentTypeNode componentTypeNode = (ComponentTypeNode) o;

                    if (componentTypeNode.isFiltered()) {
                        continue;
                    }

                    setAllState(componentTypeNode, MultiCheckboxButton.getBlackCheckedState());
                    if (componentTypeNode.getComponent().isWildCardSupported()) {
                        componentTypeNode.setWildcardSelected(shouldAddWildcard(componentTypeNode));
                    }

                    treeViewer.update(componentTypeNode, null);

                    addComponentNode(componentTypeNode, true);
                }

            } finally {
                getTreeControl().setRedraw(true);
                validate();
            }
        }
    };

    public Action deselectAllAction =
            new Action(Messages.PackageManifestTree_deselectAll_text, IAction.AS_PUSH_BUTTON) {
                @Override
                public void run() {
                    try {
                        getTreeControl().setRedraw(false);

                        if (isFiltered()) {
                            TreeItem[] is = treeViewer.getTree().getItems();
                            for (int i = 0; i < is.length; i++) {
                                TreeItem item = is[i];

                                PackageTreeNode componentTypeNode = (PackageTreeNode) item.getData();
                                setAllState(componentTypeNode, MultiCheckboxButton.getUnCheckedState());

                                deselect(item);

                                treeViewer.update(componentTypeNode, null);
                            }
                        }

                        else {
                            for (Object o : controller.getEnabledCompTypeTreeNodes()) {
                                ComponentTypeNode componentTypeNode = (ComponentTypeNode) o;
                                setAllState(componentTypeNode, MultiCheckboxButton.getUnCheckedState());
                            }

                            PackageManifestDocumentUtils.removeAllComponentTypeNodes(doc);
                        }

                    } finally {
                        getTreeControl().setRedraw(true);
                        validate();
                    }
                }
            };

    private void deselect(TreeItem item) {
        for (TreeItem childItem : item.getItems()) {
            deselect(childItem);
        }

        PackageTreeNode node = (PackageTreeNode) item.getData();
        ComponentTypeNode componentTypeNode = getComponentTypeNode(node);

        if (node == componentTypeNode && componentTypeNode.isWildcardSelected()) {
            Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, getComponentTypeName(node));
            Node memberNode = PackageManifestDocumentUtils.getMemberNode(typeNode, getComponentName(node));
            // only wildcard present
            if (memberNode == null) {
                addParentNode(getComponentTypeName(node));
                for (PackageTreeNode child : componentTypeNode.getChildList()) {
                    if (!isUnChecked(child)) {
                        PackageManifestDocumentUtils.addMemberNode(typeNode, getComponentName(child));
                    }
                }

                removeWildCardNode(componentTypeNode);
                componentTypeNode.setWildcardSelected(false);
            }
        }

        if (node instanceof ComponentTypeNode) {
            String componentTypeNodeName = ((ComponentTypeNode) node).getComponent().getComponentType();
            if (!componentTypeNodeName.equals(Constants.STANDARD_OBJECT) && shouldRemoveNode(node)) {
                removeComponentTypeNode(getComponentTypeName(node));
            }
        }

        else {
            if (shouldRemoveNode(node)) {
                removeComponentNode(getComponentTypeName(node), getComponentName(node));
            }
        }
    }

    private void removeComponentTypeNode(String componentTypeName) {

        try {
            PackageManifestDocumentUtils.removeComponentTypeNode(doc, componentTypeName);
        } catch (DOMException e) {
            // ignore may have already been deleted by another remove operation
        }
    }

    private void removeComponentNode(String componentTypeName, String memberName) {
        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, componentTypeName);
        PackageManifestDocumentUtils.removeMemberNode(typeNode, memberName);

        if (PackageManifestDocumentUtils.getLastMemberNode(typeNode) == null) {
            removeComponentTypeNode(componentTypeName);
        }
    }

    private String getComponentTypeName(PackageTreeNode packageTreeNode) {
        if (packageTreeNode instanceof ComponentNode) {
            return getComponentTypeName(getComponentTypeNode(packageTreeNode));
        } else if (packageTreeNode instanceof ComponentFolderNode) {
            return ((ComponentFolderNode) packageTreeNode).getValue().toString();
        } else if (packageTreeNode instanceof CustomObjectComponentNode) {
            return ((CustomObjectComponentNode) packageTreeNode).getParent().getValue().toString();
        } else if (packageTreeNode instanceof CustomObjectFolderNode) {
            return ((CustomObjectFolderNode) packageTreeNode).getParent().getValue().toString();
        } else if (packageTreeNode instanceof CustomObjectTypeNode) {
            return getComponentTypeName((PackageTreeNode) packageTreeNode.getParent());
        } else if (packageTreeNode instanceof ComponentTypeNode) {
            String componentTypeName = ((ComponentTypeNode) packageTreeNode).getComponent().getComponentType();
            if (componentTypeName.equals(Constants.STANDARD_OBJECT)) {
                componentTypeName = Constants.CUSTOM_OBJECT;
            }
            return componentTypeName;
        }

        return null;
    }

    private static String getComponentName(PackageTreeNode packageTreeNode) {
        if (packageTreeNode instanceof ComponentNode) {

            ComponentTypeNode componentTypeNode = getComponentTypeNode(packageTreeNode);

            String memberName = packageTreeNode.getName();
            if (componentTypeNode.getComponent().isWithinFolder()) {
                String folderName = ((PackageTreeNode) packageTreeNode.getParent()).getName();
                memberName = folderName + Constants.FOWARD_SLASH + memberName;
            }

            return memberName;

        } else if (packageTreeNode instanceof ComponentFolderNode) {
            return packageTreeNode.getName();
        } else if (packageTreeNode instanceof CustomObjectComponentNode) {
            TreeNode parentTreeNode = packageTreeNode.getParent();

            if (parentTreeNode instanceof CustomObjectFolderNode) {
                return ((PackageTreeNode) packageTreeNode.getParent().getParent()).getName() + Constants.DOT
                        + packageTreeNode.getName();
            } else if (parentTreeNode instanceof CustomObjectTypeNode) {
                return packageTreeNode.getName();
            }
        } else if (packageTreeNode instanceof CustomObjectTypeNode) {
            return packageTreeNode.getName();
        }

        return null;
    }

    private void setAllState(PackageTreeNode node, int state) {

        if (node.isFiltered()) {
            return;
        }

        if (node instanceof CustomObjectTypeNode) {
            initCustomObjectChildState(node, state);
        }

        else {
            for (PackageTreeNode child : node.getChildList()) {
                setAllState(child, state);
            }
        }

        setCheckState(node, state);
    }

    void setCheckState(PackageTreeNode node, int state) {

        if (MultiCheckboxButton.isUnChecked(state)) {
            setUnChecked(node);
        }

        else if (MultiCheckboxButton.isBlackChecked(state)) {
            setBlackChecked(node);
        }

        else if (MultiCheckboxButton.isGrayChecked(state)) {
            setGrayChecked(node);
        }

        else if (MultiCheckboxButton.isSchroedinger(state)) {
            setSchroedingerChecked(node);
        }
    }

    void setCheckState(TreeItem item) {
        PackageTreeNode node = (PackageTreeNode) item.getData();
        int state = node.getState();
        if (MultiCheckboxButton.isUnChecked(state)) {
            item.setGrayed(false);
            item.setChecked(false);
        }

        else if (MultiCheckboxButton.isBlackChecked(state)) {
            item.setGrayed(false);
            item.setChecked(true);
        }

        else if (MultiCheckboxButton.isGrayChecked(state)) {
            item.setGrayed(true);
            item.setChecked(false);
        }

        else if (MultiCheckboxButton.isSchroedinger(state)) {
            item.setGrayed(true);
            item.setChecked(true);
        }
    }

    private void initCustomObjectChildState(PackageTreeNode node, int state) {
        if (node.isFiltered()) {
            return;
        }

        for (PackageTreeNode child : node.getChildList()) {
            initCustomObjectChildState(child, state);
        }

        setCheckState(node, state);
    }

    // C O N S T R U C T O R S
    public PackageManifestTree(Composite parent, int treeStyle) {
    	this(parent, treeStyle, new PackageManifestController());
    }
    public PackageManifestTree(Composite parent, int treeStyle, PackageManifestController controller) {
        super(parent, treeStyle, new ManifestTreeFilter(), false);
        this.controller = controller;

        profileObjectStatus = new Status(IStatus.OK, getClass().getName(), IStatus.OK, "", null); //$NON-NLS-1$
        filterStatus = new Status(IStatus.OK, getClass().getName(), IStatus.OK, "", null); //$NON-NLS-1$

        checkFilter = new HideNonCheckedFilter();
        createMultiStatus();
    }

    // M E T H O D S
    public void setFileMetadatExt(FileMetadataExt ext) {
        controller.setFileMetadatExt(ext);
    }

    public TreeColumn getClmComponent() {
        return clmComponent;
    }

    public TreeColumn getClmWildcard() {
        return clmWildcard;
    }

    @Override
    protected Composite createFilterControls(Composite parent) {
        super.createFilterControls(parent);

        hideCheckFilterButton = new Button(parent, SWT.CHECK);
        hideCheckFilterButton.setText(Messages.PackageManifestTree_showOnlySelected_label);
        hideCheckFilterButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0));
        hideCheckFilterButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (hideCheckFilterButton.getSelection()) {
                    treeViewer.addFilter(checkFilter);
                } else {
                    treeViewer.removeFilter(checkFilter);
                }
            }
        });

        return parent;
    }

    class PackageManifestTreeViewer extends TreeItemNotifyingTreeViewer {
        public boolean checkedAndFiltered = false;
        public List<PackageTreeNode> filteredList = new ArrayList<>();

        public PackageManifestTreeViewer(Composite parent, int style) {
            super(parent, style);
        }

        public PackageManifestTreeViewer(Composite parent) {
            super(parent);
        }

        public PackageManifestTreeViewer(Tree tree) {
            super(tree);
        }

        @Override
        protected void preservingSelection(Runnable updateCode) {
            super.preservingSelection(updateCode);

            TreeItem[] children = getTreeViewer().getTree().getItems();
            for (int i = 0; i < children.length; i++) {
                TreeItem item = children[i];
                setCheckState(item);
            }
        }
    };

    private void internalCollapseAll() {
        TreeItem[] is = treeViewer.getTree().getItems();
        for (int i = 0; i < is.length; i++) {
            TreeItem item = is[i];
            if (item.getExpanded()) {
                treeViewer.setExpandedState(item.getData(), false);
            }
        }
    }

    /**
     * Overriding this method is only necessary in 3.2 -- This should be removed when we no longer support 3.2 in favor
     * of doCreateTreeViewer
     */
    @Override
    protected Control createTreeControl(Composite parent, int style) {
        treeViewer = new PackageManifestTreeViewer(parent, style);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewer.getControl().setLayoutData(data);
        treeViewer.getControl().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                refreshJob.cancel();
            }
        });

        refreshJob = new WorkbenchJob("Refresh Filter") { //$NON-NLS-1$
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        try {
                            if (treeViewer.getControl().isDisposed()) {
                                return Status.CANCEL_STATUS;
                            }

                            getTreeControl().setRedraw(false);
                            // collapse all
                            internalCollapseAll();

                            if (isFiltered()) {
                                treeViewer.refresh(true);
                                treeViewer.expandAll();
                            }

                            else {
                                ((PackageManifestTreeViewer) getTreeViewer()).checkedAndFiltered = false;

                                List<PackageTreeNode> list = ((PackageManifestTreeViewer) getTreeViewer()).filteredList;
                                for (PackageTreeNode node : list) {
                                    node.setFiltered(false);
                                }
                                list.clear();
                            }

                            // filtered out non-enabled component nodes
                            if (controller.getRoot() != null) {
                                Object[] enabledComponents = controller.getEnabledCompTypeTreeNodes();
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Found [" + enabledComponents.length //$NON-NLS-1$
                                            + "] enabled components to display"); //$NON-NLS-1$
                                }

                                for (Object o : enabledComponents) {
                                    ComponentTypeNode componentTypeNode = (ComponentTypeNode) o;

                                    if (componentTypeNode.isFiltered()) {
                                        continue;
                                    }

                                    refreshComponentChecks(componentTypeNode);
                                }
                            }
                        } finally {
                            getTreeControl().setRedraw(true);
                            getViewer().refresh();
                        }
                        validate();
                        return Status.OK_STATUS;
                    }
                };

        refreshJob.setSystem(true);

        treeViewer.addFilter(getPatternFilter());

        final Tree tree = treeViewer.getTree();
        tree.setHeaderVisible(true);

        treeViewer.setContentProvider(new ManifestContentProvider());
        treeViewer.setLabelProvider(new ManifestLabelProvider());
        treeViewer.setInput(doc);

        clmComponent = new TreeColumn(tree, SWT.NONE);
        clmComponent.setText(Messages.PackageManifestTree_columnName_component);
        clmComponent.setResizable(true);
        clmComponent.setWidth(400);

        clmWildcard = new TreeColumn(tree, SWT.NONE);
        clmWildcard.setText(Messages.PackageManifestTree_columnName_wildcard);
        clmWildcard.setResizable(true);
        clmWildcard.setWidth(300);

        treeViewer.setColumnProperties(new String[] { Messages.PackageManifestTree_columnName_component,
                Messages.PackageManifestTree_columnName_wildcard });

        tree.getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.NONE) {
                    tree.redraw();
                }
            }
        });

        getTreeViewer().addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                PackageTreeNode node = (PackageTreeNode) event.getElement();
                handleItemSelection(node);
                ComponentTypeNode componentTypeNode = getComponentTypeNode(node);

                if (isBlackChecked(node)) {
                    addComponentNode(node, true);
                }

                else {
                    removeComponentNode(node);
                }

                if (componentTypeNode.getComponent().isWildCardSupported()) {
                    componentTypeNode.setWildcardSelected(shouldAddWildcard(componentTypeNode));
                    treeViewer.update(componentTypeNode, null);

                    if (componentTypeNode.isWildcardSelected()) {
                        addWildCardNode(componentTypeNode);
                    }
                }

                validate();
            }
        });

        treeViewer.setComparator(new ViewerComparator() {

            @Override
            public int category(Object element) {
                if (element instanceof ComponentTypeNode) {
                    return 1;
                }
                if (element instanceof ComponentFolderNode) {
                    return 2;
                }

                return 3;
            }

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                int cat1 = category(e1);
                int cat2 = category(e2);

                if (cat1 != cat2) {
                    return cat1 - cat2;
                }

                return Collator.getInstance().compare(((PackageTreeNode) e1).getName(),
                    ((PackageTreeNode) e2).getName());
            }
        });

        return treeViewer.getControl();
    }

    protected CheckboxTreeViewer getTreeViewer() {
        return (CheckboxTreeViewer) treeViewer;
    }

    protected Control getTreeControl() {
        return treeViewer.getControl();
    }

    public void addStatusChangedListener(IStatusChangedListener listener) {
        statusChangedListeners.add(listener);
    }

    public void removeStatusChangedListener(IStatusChangedListener listener) {
        statusChangedListeners.remove(listener);
    }

    @Override
    protected void textChanged() {
        super.textChanged();
        refreshJob.cancel();
        refreshJob.schedule(200);
    }

    private void refreshComponentChecks(PackageTreeNode node) {
        for (PackageTreeNode child : node.getChildList()) {
            // refreshChildChecks(child);
            refreshComponentChecks(child);
        }

        if (isFiltered()) {
            if (!(node instanceof CustomObjectTypeNode) && allVisibleChildrenBlackChecked(node)) {
                setBlackChecked(node);
            }

            else if (allVisibleChildrenUnChecked(node)) {
                setUnChecked(node);
            }
        }

        else {
            if (isUnChecked(node)) {
                if (isWildCardSelected(node)) {
                    setBlackChecked(node);
                }

                PackageTreeNode parent = (PackageTreeNode) node.getParent();
                if (isBlackChecked(parent)) {
                    if (!(parent instanceof CustomObjectTypeNode)) {
                        setSchroedingerChecked(parent);
                    }
                }

                else if (anyChildBlackChecked(parent)) {
                    setSchroedingerChecked(parent);
                }
            }
        }
    }

    public Document getDocument() {
        return doc;
    }

    public void setDocument(Document doc) {
        this.doc = (Document) doc.cloneNode(true);
        PackageManifestDocumentUtils.addPackageNode(this.doc);
        controller.setManifestDoc(this.doc);
    }

    public IFile getFile() {
        return file;
    }

    public void setFile(IFile file) {
        this.file = file;
        controller.setProject(file == null ? null : file.getProject());
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        controller.setConnection(connection);
    }

    protected class ManifestContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            return controller.getEnabledCompTypeTreeNodes();
        }

        @Override
        public void dispose() {

        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        // TODO the commented code below was for previous implementation which
        // lazily called metadata api
        // this may need to be brought back for large orgs...
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ComponentTypeNode) {
                ComponentTypeNode node = (ComponentTypeNode) parentElement;

                // if (!node.hasBeenRetrieved()) {
                // controller.updateChildren(node);
                // }

                return node.getChildren();
            } else if (parentElement instanceof ComponentFolderNode) {
                ComponentFolderNode node = (ComponentFolderNode) parentElement;
                // if (!node.hasBeenRetrieved()) {
                // controller.updateChildren(node);
                // }

                return node.getChildren();
            } else if (parentElement instanceof CustomObjectTypeNode) {
                CustomObjectTypeNode node = (CustomObjectTypeNode) parentElement;
                // if (!node.hasBeenRetrieved()) {
                // controller.updateChildren(node);
                // }

                return node.getChildren();
            } else if (parentElement instanceof CustomObjectFolderNode) {
                return ((CustomObjectFolderNode) parentElement).getChildren();
            }

            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof PackageTreeNode) {
                return ((PackageTreeNode) element).getParent();
            }

            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof PackageTreeNode) {
                PackageTreeNode node = (PackageTreeNode) element;
                if (node.hasBeenRetrieved()) {
                    return node.hasChildren();
                }

                return true;
            }

            return false;
        }
    }

    protected class ManifestLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element instanceof PackageTreeNode) {
                    PackageTreeNode node = (PackageTreeNode) element;
                    return node.getImage();
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                if (element instanceof PackageTreeNode) {
                    PackageTreeNode node = (PackageTreeNode) element;
                    setCheckState(node, node.getState());
                    return node.getName();
                }
                break;

            case 1:
                if (element instanceof ComponentTypeNode) {
                    ComponentTypeNode componentTypeNode = (ComponentTypeNode) element;
                    if (componentTypeNode.getComponent().isWildCardSupported()
                            && componentTypeNode.isWildcardSelected()) {
                        return NLS.bind(Messages.PackageManifestTree_columnItemLabel_wildcard, Utils
                                .getPlural(componentTypeNode.getComponent().getDisplayName()));
                    }
                }
                break;
            }

            return null;
        }
    }

    private static ComponentTypeNode getComponentTypeNode(TreeNode node) {
        while (!(node instanceof ComponentTypeNode)) {
            node = node.getParent();
        }

        return (ComponentTypeNode) node;
    }

    private void addWildCardNode(ComponentTypeNode componentTreeNode) {
        String componentName = componentTreeNode.getComponent().getComponentType();

        PackageTreeNode standardNode = controller.getNode(Constants.STANDARD_OBJECT + Constants.FOWARD_SLASH);
        PackageTreeNode customNode = controller.getNode(Constants.CUSTOM_OBJECT + Constants.FOWARD_SLASH);

        if (componentName.equals(Constants.CUSTOM_OBJECT) && !isUnChecked(standardNode)) {
            removeComponentNode(customNode);

            Node componentNode = PackageManifestDocumentUtils.addComponentTypeNode(doc, componentName);
            PackageManifestDocumentUtils.addWildcardMember(componentNode);
        }

        else {
            PackageManifestDocumentUtils.removeComponentTypeNode(doc, componentName);

            for (String sub : componentTreeNode.getComponent().getSubComponentTypes()) {
                PackageManifestDocumentUtils.removeComponentTypeNode(doc, sub);
            }

            Node componentNode = PackageManifestDocumentUtils.addComponentTypeNode(doc, componentName);
            PackageManifestDocumentUtils.addWildcardMember(componentNode);
        }
    }

    private void removeWildCardNode(ComponentTypeNode componentTreeNode) {
        String componentName = componentTreeNode.getComponent().getComponentType();
        Node component = PackageManifestDocumentUtils.getComponentNode(doc, componentName);

        if (component == null) {
            return;
        }

        PackageManifestDocumentUtils.removeWildcardNode(component);

        if (PackageManifestDocumentUtils.getLastMemberNode(component) == null) {
            PackageManifestDocumentUtils.removeComponentTypeNode(doc, componentName);
        }
    }

    private void addComponentNode(PackageTreeNode packageTreeNode, boolean useWildcard) {
        if (packageTreeNode instanceof ComponentNode) {
            addCustomObjectComponentNode((ComponentNode) packageTreeNode);
        } else if (packageTreeNode instanceof ComponentFolderNode) {
            addComponentFolderNode((ComponentFolderNode) packageTreeNode);
        } else if (packageTreeNode instanceof CustomObjectComponentNode) {
            addCustomObjectComponentNode((CustomObjectComponentNode) packageTreeNode);
        } else if (packageTreeNode instanceof CustomObjectFolderNode) {
            addCustomObjectFolderNode((CustomObjectFolderNode) packageTreeNode);
        } else if (packageTreeNode instanceof CustomObjectTypeNode) {
            addCustomObjectTypeNode((CustomObjectTypeNode) packageTreeNode);
        } else if (packageTreeNode instanceof ComponentTypeNode) {
            addComponentTypeNode((ComponentTypeNode) packageTreeNode, useWildcard);
        }
    }

    private void addCustomObjectComponentNode(ComponentNode componentNode) {
        ComponentTypeNode componentTypeNode = getComponentTypeNode(componentNode);
        Node typeNode = addParentNode(componentTypeNode.getComponent().getComponentType());

        String memberName = componentNode.getName();
        if (componentTypeNode.getComponent().isWithinFolder()) {
            String folderName = ((PackageTreeNode) componentNode.getParent()).getName();
            PackageManifestDocumentUtils.addMemberNode(typeNode, folderName);
            memberName = folderName + Constants.FOWARD_SLASH + memberName;
        }
        PackageManifestDocumentUtils.addMemberNode(typeNode, memberName);
    }

    private void addCustomObjectComponentNode(CustomObjectComponentNode customObjectComponentNode) {
        TreeNode parentTreeNode = customObjectComponentNode.getParent();

        if (parentTreeNode instanceof CustomObjectFolderNode) {
            Node typeNode = addParentNode(((CustomObjectFolderNode) parentTreeNode).getValue().toString());
            String memberName =
                    ((PackageTreeNode) customObjectComponentNode.getParent().getParent()).getName() + Constants.DOT
                            + customObjectComponentNode.getName();
            PackageManifestDocumentUtils.addMemberNode(typeNode, memberName);
        } else if (parentTreeNode instanceof CustomObjectTypeNode) {
        	Node typeNode;
        	String memberName;
        	if (customObjectComponentNode.getComponent() == null) {
        		typeNode = addParentNode(((CustomObjectTypeNode) parentTreeNode).getValue().toString());
        		memberName = ((CustomObjectTypeNode ) parentTreeNode).getName();
        	} else {
        		typeNode = addParentNode(customObjectComponentNode.getComponent().getComponentType());
        		memberName = customObjectComponentNode.getName();
        	}
            PackageManifestDocumentUtils.addMemberNode(typeNode, memberName);
        }
    }

    private void addComponentFolderNode(ComponentFolderNode componentFolderNode) {
        Node typeNode = addParentNode(getComponentTypeNode(componentFolderNode).getComponent().getComponentType());

        String folderName = componentFolderNode.getName();
        PackageManifestDocumentUtils.addMemberNode(typeNode, folderName);

        for (PackageTreeNode child : componentFolderNode.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            String memberName = folderName + Constants.FOWARD_SLASH + child.getName();
            PackageManifestDocumentUtils.addMemberNode(typeNode, memberName);
        }
    }

    private Node addParentNode(String nodeName) {
        if (nodeName.equals(Constants.STANDARD_OBJECT)) {
            nodeName = Constants.CUSTOM_OBJECT;
        }

        return PackageManifestDocumentUtils.addComponentTypeNode(doc, nodeName);
    }

    private void addCustomObjectTypeNode(CustomObjectTypeNode customObjectTypeNode) {
        ComponentTypeNode componentTypeNode = getComponentTypeNode(customObjectTypeNode);
        Node typeNode = addParentNode(componentTypeNode.getComponent().getComponentType());
        PackageManifestDocumentUtils.addMemberNode(typeNode, customObjectTypeNode.getName());

        for (PackageTreeNode child : customObjectTypeNode.getChildList()) {
            if (child instanceof CustomObjectFolderNode) {
                String typeName = ((CustomObjectFolderNode) child).getValue().toString();
                PackageManifestDocumentUtils.removeComponentTypeNode(doc, typeName);

            } else if (child instanceof CustomObjectComponentNode) {
                removeCustomObjectComponentNode((CustomObjectComponentNode) child);
            }
        }
    }

    private void addCustomObjectFolderNode(CustomObjectFolderNode customObjectFolderNode) {
        Node typeNode = addParentNode(customObjectFolderNode.getValue().toString());

        for (PackageTreeNode child : customObjectFolderNode.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            String memberName =
                    ((PackageTreeNode) customObjectFolderNode.getParent()).getName() + Constants.DOT + child.getName();
            PackageManifestDocumentUtils.addMemberNode(typeNode, memberName);
        }
    }

    private void removeParentNode(String nodeName) {
        if (nodeName.equals(Constants.STANDARD_OBJECT)) {
            nodeName = Constants.CUSTOM_OBJECT;
        }

        try {
            PackageManifestDocumentUtils.removeComponentTypeNode(doc, nodeName);
        } catch (DOMException e) {
            // ignore may have already been deleted by another remove operation
        }
    }

    private void addComponentTypeNode(ComponentTypeNode componentTypeNode, boolean useWildcard) {
        if (componentTypeNode.getComponent().isWildCardSupported() && useWildcard
                && shouldAddWildcard(componentTypeNode)) {
            addWildCardNode(componentTypeNode);
        }

        else {
            addParentNode(componentTypeNode.getComponent().getComponentType());
            for (PackageTreeNode child : componentTypeNode.getChildList()) {

                if (child.isFiltered()) {
                    continue;
                }

                if (child instanceof CustomObjectTypeNode) {
                    addCustomObjectTypeNode((CustomObjectTypeNode) child);
                } else if (child instanceof ComponentFolderNode) {
                    addComponentFolderNode((ComponentFolderNode) child);
                } else {
                    addComponentNode(child, useWildcard);
                }
            }
        }
    }

    private void removeComponentFolderNode(ComponentFolderNode componentFolderNode) {
        String nodeName = ((ComponentTypeNode) componentFolderNode.getParent()).getComponent().getComponentType();
        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, nodeName);

        if (typeNode == null) {
            return;
        }

        String folderName = componentFolderNode.getName();
        PackageManifestDocumentUtils.removeMemberNode(typeNode, folderName);

        for (PackageTreeNode child : componentFolderNode.getChildList()) {
            String memberName = folderName + Constants.FOWARD_SLASH + child.getName();
            PackageManifestDocumentUtils.removeMemberNode(typeNode, memberName);
        }

        if (PackageManifestDocumentUtils.getLastMemberNode(typeNode) == null) {
            removeParentNode(nodeName);
        }
    }

    private void removeComponentTypeNode(ComponentTypeNode componentTypeNode) {
        String typeName = componentTypeNode.getComponent().getComponentType();

        for (PackageTreeNode child : componentTypeNode.getChildList()) {
            if (child instanceof CustomObjectTypeNode) {
                removeCustomObjectTypeNode((CustomObjectTypeNode) child);
            } else if (child instanceof ComponentFolderNode) {
                removeComponentFolderNode((ComponentFolderNode) child);
            } else {
                removeComponentNode((ComponentNode) child);
            }
        }

        removeParentNode(typeName);
    }

    private void removeCustomObjectFolderNode(CustomObjectFolderNode customObjectFolderNode) {
        String typeName = customObjectFolderNode.getValue().toString();
        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, typeName);

        if (typeNode == null) {
            removeCustomObjectChildren(customObjectFolderNode, customObjectFolderNode.getName());
        }

        for (PackageTreeNode child : customObjectFolderNode.getChildList()) {
            String memberName =
                    ((PackageTreeNode) child.getParent().getParent()).getName() + Constants.DOT + child.getName();

            PackageManifestDocumentUtils.removeMemberNode(typeNode, memberName);
        }

        if (PackageManifestDocumentUtils.getLastMemberNode(typeNode) == null) {
            removeParentNode(typeName);
        }
    }

    private void removeCustomObjectTypeNode(CustomObjectTypeNode customObjectTypeNode) {
        ComponentTypeNode componentTypeNode = getComponentTypeNode(customObjectTypeNode);
        String componentTypeNodeName = componentTypeNode.getComponent().getComponentType();

        if (componentTypeNodeName.equals(Constants.STANDARD_OBJECT)) {
            componentTypeNodeName = Constants.CUSTOM_OBJECT;
        }

        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, componentTypeNodeName);
        String memberName = customObjectTypeNode.getName();
        addAllParentComponents(componentTypeNode, typeNode, (PackageTreeNode) customObjectTypeNode.getParent(),
            memberName);

        for (PackageTreeNode child : customObjectTypeNode.getChildList()) {
            if (child instanceof CustomObjectFolderNode) {
                removeCustomObjectFolderNode((CustomObjectFolderNode) child);
            } else if (child instanceof CustomObjectComponentNode) {
                removeCustomObjectComponentNode((CustomObjectComponentNode) child);
            }
        }

        if (typeNode == null) {
            return;
        }

        PackageManifestDocumentUtils.removeMemberNode(typeNode, memberName);

        if (PackageManifestDocumentUtils.getLastMemberNode(typeNode) == null) {
            removeParentNode(componentTypeNodeName);
        }
    }

    private void removeCustomObjectComponentNode(CustomObjectComponentNode customObjectComponentNode) {
        PackageTreeNode parentTreeNode = (PackageTreeNode) customObjectComponentNode.getParent();

        String nodeName = null;
        String memberName = null;

        if (parentTreeNode instanceof CustomObjectFolderNode) {
            nodeName = ((CustomObjectFolderNode) parentTreeNode).getValue().toString();

            memberName =
                    ((PackageTreeNode) customObjectComponentNode.getParent().getParent()).getName() + Constants.DOT
                            + customObjectComponentNode.getName();
        } else if (parentTreeNode instanceof CustomObjectTypeNode) {
        	nodeName = ((CustomObjectTypeNode) parentTreeNode).getValue().toString();
            memberName = customObjectComponentNode.getName();
        } else {
        	nodeName = ((ComponentTypeNode) parentTreeNode).getComponent().getComponentType();
            memberName = customObjectComponentNode.getName();
        }

        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, nodeName);

        // entire custom object was previously selected -- remove it, then add
        // all children
        if (typeNode == null) {
            removeCustomObjectChildren(parentTreeNode, nodeName);
            typeNode = PackageManifestDocumentUtils.getComponentNode(doc, nodeName);
        }

        PackageManifestDocumentUtils.removeMemberNode(typeNode, memberName);

        if (PackageManifestDocumentUtils.getLastMemberNode(typeNode) == null) {
            removeParentNode(nodeName);
        }
    }

    private void removeCustomObjectChildren(PackageTreeNode parentTreeNode, String nodeName) {
        while (!(parentTreeNode instanceof CustomObjectTypeNode)) {
            parentTreeNode = (PackageTreeNode) parentTreeNode.getParent();
        }

        for (PackageTreeNode child : parentTreeNode.getChildList()) {
            addComponentNode(child, false);
        }

        String componentName = getComponentTypeNode(parentTreeNode).getComponent().getComponentType();
        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, componentName);
        PackageManifestDocumentUtils.removeMemberNode(typeNode, parentTreeNode.getName());

        if (PackageManifestDocumentUtils.getLastMemberNode(typeNode) == null) {
            removeParentNode(componentName);
        }
    }

    private void addAllParentComponents(ComponentTypeNode componentTypeNode, Node typeNode, PackageTreeNode parent,
            String memberName) {
        Node memberNode = PackageManifestDocumentUtils.getMemberNode(typeNode, memberName);
        // only wildcard present
        if (memberNode == null) {
            addComponentNode(parent, false);
            removeWildCardNode(componentTypeNode);
        }
    }

    private void removeComponentNode(ComponentNode componentNode) {
        ComponentTypeNode componentTypeNode = getComponentTypeNode(componentNode);
        String nodeName = componentTypeNode.getComponent().getComponentType();
        Node typeNode = PackageManifestDocumentUtils.getComponentNode(doc, nodeName);

        if (typeNode == null) {
            return;
        }

        String memberName = componentNode.getName();
        if (componentTypeNode.getComponent().isWithinFolder()) {
            String folderName = ((PackageTreeNode) componentNode.getParent()).getName();
            PackageManifestDocumentUtils.removeMemberNode(typeNode, folderName);
            memberName = folderName + Constants.FOWARD_SLASH + memberName;
        }

        addAllParentComponents(componentTypeNode, typeNode, (PackageTreeNode) componentNode.getParent(), memberName);

        PackageManifestDocumentUtils.removeMemberNode(typeNode, memberName);
        if (PackageManifestDocumentUtils.getLastMemberNode(typeNode) == null) {
            removeParentNode(nodeName);
        }
    }

    private void removeComponentNode(PackageTreeNode packageTreeNode) {
        if (packageTreeNode instanceof ComponentTypeNode) {
            removeComponentTypeNode((ComponentTypeNode) packageTreeNode);
        } else if (packageTreeNode instanceof ComponentFolderNode) {
            removeComponentFolderNode((ComponentFolderNode) packageTreeNode);
        } else if (packageTreeNode instanceof CustomObjectFolderNode) {
            removeCustomObjectFolderNode((CustomObjectFolderNode) packageTreeNode);
        } else if (packageTreeNode instanceof CustomObjectTypeNode) {
            removeCustomObjectTypeNode((CustomObjectTypeNode) packageTreeNode);
        } else if (packageTreeNode instanceof CustomObjectComponentNode) {
            removeCustomObjectComponentNode((CustomObjectComponentNode) packageTreeNode);
        } else if (packageTreeNode instanceof ComponentNode) {
            removeComponentNode((ComponentNode) packageTreeNode);
        }
    }

    private static boolean isWildCardSupported(TreeNode node) {
        ComponentTypeNode comp = getComponentTypeNode(node);
        return comp.getComponent().isWildCardSupported();
    }

    private static boolean isWildCardSelected(PackageTreeNode node) {
        if (isWildCardSupported(node)) {
            return getComponentTypeNode(node).isWildcardSelected();
        }
        return false;
    }

    private static boolean isUnChecked(PackageTreeNode node) {
        return MultiCheckboxButton.isUnChecked(node.getState());
    }

    private static boolean isBlackChecked(PackageTreeNode node) {
        return MultiCheckboxButton.isBlackChecked(node.getState());
    }

    private static boolean isGrayChecked(PackageTreeNode node) {
        return MultiCheckboxButton.isGrayChecked(node.getState());
    }

    private static boolean isSchroedingerChecked(PackageTreeNode node) {
        return MultiCheckboxButton.isSchroedinger(node.getState());
    }

    private void setBlackChecked(PackageTreeNode node) {
        getTreeViewer().setGrayed(node, false);
        getTreeViewer().setChecked(node, true);
        node.setState(MultiCheckboxButton.getBlackCheckedState());
    }

    private void setUnChecked(PackageTreeNode node) {
        getTreeViewer().setGrayed(node, false);
        getTreeViewer().setChecked(node, false);
        node.setState(MultiCheckboxButton.getUnCheckedState());
    }

    private void setGrayChecked(PackageTreeNode node) {
        getTreeViewer().setGrayed(node, false);
        getTreeViewer().setChecked(node, true);
        node.setState(MultiCheckboxButton.getGrayCheckedState());
    }

    private void setSchroedingerChecked(PackageTreeNode node) {
        getTreeViewer().setGrayed(node, true);
        getTreeViewer().setChecked(node, true);
        node.setState(MultiCheckboxButton.getSchroedingerState());
    }

    private void handleItemSelection(PackageTreeNode node) {
        // update myself
        // update children
        // update parent
        try {
            getTreeControl().setRedraw(false);

            // update myself
            updateChecks(node);

            // update my children
            updateChildChecks(node);

            // update parent
            updateParentChecks(node);
        }

        finally {
            getTreeControl().setRedraw(true);
        }
    }

    private void updateChecks(PackageTreeNode node) {
        if (isBlackChecked(node)) {
            setUnChecked(node);
        }

        else {
            setBlackChecked(node);
        }
    }

    private void updateChildChecks(PackageTreeNode parent) {
        for (PackageTreeNode child : parent.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            if (isBlackChecked(child)) {
                if (isUnChecked(parent)) {
                    setUnChecked(child);
                }
            }

            else if (isUnChecked(child)) {
                if (isBlackChecked(parent)) {
                    setBlackChecked(child);
                }
            }

            // S
            // parent changes to checked -> if Wn -> C
            else if (isSchroedingerChecked(child)) {
                if (isBlackChecked(parent)) {
                    setBlackChecked(child);
                }
            }

            updateChildChecks(child);
        }
    }

    private void updateParentChecks(PackageTreeNode node) {
        if (node.getParent().getValue() == null) {
            return;
        }

        PackageTreeNode parent = (PackageTreeNode) node.getParent();

        if (isBlackChecked(parent)) {
            if (isUnChecked(node)) {
                if (!anyChildBlackChecked(parent)) {
                    setUnChecked(parent);
                }

                else {
                    setSchroedingerChecked(parent);
                }
            }

            else if (isSchroedingerChecked(node)) {
                setSchroedingerChecked(parent);
            }
        }

        if (isUnChecked(parent)) {
            if (isBlackChecked(node)) {
                if (allChildrenBlackChecked(parent)) {
                    if (parent instanceof CustomObjectTypeNode) {
                        setSchroedingerChecked(parent);
                    }

                    else {
                        setBlackChecked(parent);
                    }
                }

                else {
                    setSchroedingerChecked(parent);
                }
            }

            else if (isSchroedingerChecked(node)) {
                setSchroedingerChecked(parent);
            }
        }

        else if (isSchroedingerChecked(parent)) {
            if (isUnChecked(node)) {
                if (!anyChildBlackChecked(parent) && !anyChildSchroedinger(parent)) {
                    setUnChecked(parent);
                }
            }

            else if (isBlackChecked(node)) {
                if (allChildrenBlackChecked(parent)) {
                    if (!(parent instanceof CustomObjectTypeNode)) {
                        setBlackChecked(parent);
                    }
                }
            }
        }

        updateParentChecks((PackageTreeNode) node.getParent());
    }

    protected boolean shouldAddWildcard(PackageTreeNode node) {
        if (!isBlackChecked(node)) {
            return false;
        }

        for (PackageTreeNode child : node.getChildList()) {
            if (!isBlackChecked(child)) {
                return false;
            }
        }

        return true;
    }

    protected boolean shouldRemoveWildcard(PackageTreeNode node) {
        if (!isBlackChecked(node)) {
            return false;
        }

        for (PackageTreeNode child : node.getChildList()) {
            if (child.isFiltered()) {
                if (!isUnChecked(child)) {
                    return false;
                }
            }
        }

        return true;
    }

    // TODO these methods are duplicated in controller, consolidate
    protected boolean anyChildSchroedinger(PackageTreeNode node) {
        boolean flag = false;
        for (PackageTreeNode child : node.getChildList()) {
            if (isSchroedingerChecked(child)) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    protected boolean anyChildBlackChecked(PackageTreeNode node) {
        for (PackageTreeNode child : node.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            if (isBlackChecked(child)) {
                return true;
            }
        }

        return false;
    }

    protected boolean allVisibleChildrenUnChecked(PackageTreeNode node) {
        boolean anyVisible = false;

        for (PackageTreeNode child : node.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            anyVisible = true;
            if (!isUnChecked(child)) {
                return false;
            }
        }

        return anyVisible;
    }

    protected boolean allVisibleChildrenBlackChecked(PackageTreeNode node) {
        boolean anyVisible = false;

        for (PackageTreeNode child : node.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            anyVisible = true;
            if (!isBlackChecked(child)) {
                return false;
            }
        }

        return anyVisible;
    }

    protected boolean allChildrenBlackChecked(PackageTreeNode node) {
        boolean flag = node.getChildren().length > 0;
        for (PackageTreeNode child : node.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            if (!isBlackChecked(child)) {
                return false;
            }
        }

        return flag;
    }

    protected boolean allChildrenUnChecked(PackageTreeNode node) {
        boolean flag = node.getChildren().length > 0;
        for (PackageTreeNode child : node.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            if (!isUnChecked(child)) {
                return false;
            }
        }

        return flag;
    }

    protected boolean allChildrenChecked(PackageTreeNode node) {
        boolean flag = node.getChildren().length > 0;
        for (PackageTreeNode child : node.getChildList()) {
            if (child.isFiltered()) {
                continue;
            }

            if (!(isBlackChecked(child) || isGrayChecked(child))) {
                return false;
            }
        }

        return flag;
    }

    protected boolean shouldRemoveNode(PackageTreeNode node) {
        for (PackageTreeNode child : node.getChildList()) {
            if (!isUnChecked(child)) {
                return false;
            }
        }

        return true;
    }

    public class CollapseAction extends Action {
        public CollapseAction() {
            super(null, IAction.AS_PUSH_BUTTON);
            setToolTipText(Messages.PackageManifestTree_collapseAll_text);
            setImageDescriptor(ForceImages.getDesc(ForceImages.COLLAPSE_ALL));
        }

        @Override
        public void run() {
            getTreeControl().setRedraw(false);
            getViewer().collapseAll();
            getTreeControl().setRedraw(true);

            getViewer().refresh();
        }
    };

    public class ExpandAction extends Action {
        public ExpandAction() {
            super(null, IAction.AS_PUSH_BUTTON);
            setToolTipText(Messages.PackageManifestTree_expandAll_text);
            setImageDescriptor(ForceImages.getDesc(ForceImages.EXPAND_ALL));
        }

        @Override
        public void run() {
            getTreeControl().setRedraw(false);
            getViewer().expandAll();
            getTreeControl().setRedraw(true);

            getViewer().refresh();
        }
    };

    public class RefreshAction extends Action {
        public WorkbenchJob job = new WorkbenchJob("Refresh Package Manifest Tree") { //$NON-NLS-1$
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (!monitor.isCanceled()) {
                            controller.clearModel();
                            controller.clearCache();
                            updateTree();
                        }
                        return Status.OK_STATUS;
                    }
                };

        public RefreshAction() {
            super(null, IAction.AS_PUSH_BUTTON);
            setToolTipText(Messages.PackageManifestTree_refresh_text);
            setImageDescriptor(ForceImages.getDesc(ForceImages.REFRESH_ENABLED));
            setDisabledImageDescriptor(ForceImages.getDesc(ForceImages.REFRESH_DISABLED));
        }

        @Override
        public void run() {
            getViewer().getTree().removeAll();

            job.setUser(true);
            PlatformUI.getWorkbench().getProgressService().showInDialog(new Shell(), job);
            job.schedule();
        }
    };

    public Action getSelectAllAction() {
        return selectAllAction;
    }

    public Action getDeselectAllAction() {
        return deselectAllAction;
    }

    public CollapseAction getCollapseAllAction() {
        return collapseAllAction;
    }

    public ExpandAction getExpandAllAction() {
        return expandAllAction;
    }

    public RefreshAction getRefreshAction() {
        return refreshAction;
    }

    private void validate() {

        if (controller == null) {
            return;
        }

        boolean changed = validateFilter();
        changed |= validateTypes();

        if (changed) {
            createMultiStatus();

            for (IStatusChangedListener listener : statusChangedListeners) {
                listener.statusChanged(status);
            }
        }
    }

    private boolean validateTypes() {
        PackageTreeNode profileNode = controller.getNode(controller.getPathForComponentType(Constants.PROFILE) + Constants.FOWARD_SLASH);
        PackageTreeNode objectNode = controller.getNode(Constants.STANDARD_OBJECT + Constants.FOWARD_SLASH);
        PackageTreeNode customNode = controller.getNode(Constants.CUSTOM_OBJECT + Constants.FOWARD_SLASH);

        if (profileNode != null && !isUnChecked(profileNode)) {
            if (objectNode != null) {
                if (!isUnChecked(objectNode)) {
                    if (!profileObjectStatus.getMessage().equals(Messages.PackageManifestTree_checkWarning_text)) {
                        profileObjectStatus =
                                new Status(IStatus.WARNING, getClass().getName(), IStatus.WARNING,
                                        Messages.PackageManifestTree_checkWarning_text, null);
                        return true;
                    }

                    return false;
                }
            }

            if (customNode != null) {
                if (!isUnChecked(customNode)) {
                    if (!profileObjectStatus.getMessage().equals(Messages.PackageManifestTree_checkWarning_text)) {
                        profileObjectStatus =
                                new Status(IStatus.WARNING, getClass().getName(), IStatus.WARNING,
                                        Messages.PackageManifestTree_checkWarning_text, null);
                        return true;
                    }

                    return false;
                }
            }
        }

        if (!Utils.isEmpty(profileObjectStatus.getMessage())) {
            profileObjectStatus = new Status(IStatus.OK, getClass().getName(), IStatus.OK, "", null); //$NON-NLS-1$
            return true;
        }

        return false;
    }

    private boolean isFiltered() {
        return Utils.isNotEmpty(getFilterString()) && !getFilterString().equals(getInitialText())
                || hideCheckFilterButton.getSelection();
    }

    private boolean validateFilter() {
        if (isFiltered() && ((PackageManifestTreeViewer) getTreeViewer()).checkedAndFiltered) {
            if (!filterStatus.getMessage().equals(Messages.PackageManifestTree_filterWarning_text)) {
                filterStatus =
                        new Status(IStatus.WARNING, getClass().getName(), IStatus.WARNING,
                                Messages.PackageManifestTree_filterWarning_text, null);
                return true;
            }

            return false;
        }

        if (!Utils.isEmpty(filterStatus.getMessage())) {
            filterStatus = new Status(IStatus.OK, getClass().getName(), IStatus.OK, "", null); //$NON-NLS-1$
            return true;
        }

        return false;
    }

    public MultiStatus getStatus() {
        return status;
    }

    private void createMultiStatus() {
        status = new MultiStatus(getClass().getName(), IStatus.OK, "", null); //$NON-NLS-1$

        if (profileObjectStatus.getSeverity() != IStatus.OK) {
            status.add(profileObjectStatus);
        }

        if (filterStatus.getSeverity() != IStatus.OK) {
            status.add(filterStatus);
        }
    }

    public void updateTree() {
        getViewer().setInput(new Object());
        validate();
    }

    private class HideNonCheckedFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            int state = ((PackageTreeNode) element).getState();
            return !(MultiCheckboxButton.isUnChecked(state) || MultiCheckboxButton.isUnCheckedDisabled(state));
        }
    }
}
