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
package com.salesforce.ide.schemabrowser.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.schemabrowser.ui.tableviewer.QueryTableViewer;
import com.salesforce.ide.ui.editors.internal.BaseMultiPageEditorPart;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.sforce.soap.partner.wsc.ChildRelationship;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;
import com.sforce.soap.partner.wsc.Field;
import com.sforce.soap.partner.wsc.PicklistEntry;
import com.sforce.soap.partner.wsc.QueryResult;

/**
 * TODO: Can this be made into a single page editor?
 *
 * Legacy class
 *
 * @author dcarroll
 */
public class SchemaBrowser extends BaseMultiPageEditorPart {
    private static final Logger logger = Logger.getLogger(SchemaBrowser.class);

    private static final String MESSAGE_GETTING_CHILD_FIELDS = "Getting child fields and relationships...";
    private static final String MESSAGE_ERROR_LOADING_SCHEMA = "Error Loading Schema";
    private static final String LABEL_SCHEMA_EXPLORER = "Schema Explorer";
    private static final String MESSAGE_SEE_DETAILS = "See details for more info.";
    private static final String MESSAGE_COULD_NOT_FETCH_META_DATA = "Could not fetch meta-data";
    private static final String FQN = "fqn";
    private static final String RESTRICTED = "restricted";
    private static final String NILLABLE = "nillable";
    private static final String NAME_FIELD = "name field";
    private static final String FILTERABLE = "filterable";
    private static final String DEFAULTED_ON_CREATE = "defaulted on create";
    private static final String FORMULA = "formula";
    private static final String AUTO_NUMBER = "auto number";
    private static final String UPDATEABLE = "updateable";
    private static final String UNDELETEABLE = "undeleteable";
    private static final String SEARCHABLE = "searchable";
    private static final String RETRIEVEABLE = "retrieveable";
    private static final String REPLICATABLE = "replicatable";
    private static final String QUERYABLE = "queryable";
    private static final String LAYOUTABLE = "layoutable";
    private static final String DELETEABLE = "deleteable";
    private static final String CUSTOM = "custom";
    private static final String CREATEABLE = "createable";
    private static final String ACTIVATABLE = "activatable";
    private static final String NODE_LABEL_CHILD_RELATIONSHIPS = "Child Relationships";
    private static final String MESSAGE_GETTING_LOOKUP = "Getting Lookup Relationship object definition...";
    private static final String MESSAGE_GETTING_CHILD = "Getting Child Relationship object definition...";
    private static final String IS_TOP_LEVEL = "isTopLevel";
    private static final String LOADED = "loaded";
    private static final String HAS_CHECKABLE_CHILDREN = "hasCheckableChildren";
    private static final String IMAGE_TYPE = "imageType";
    private static final int IMAGE_TYPE_CHECKED = 0;
    private static final String TYPE = "type";
    private static final int TYPE_MINUSONE = -1;
    static final Integer PRIMARY_ROOT_NODE = 0;
    static final Integer PRIMARY_ROOT_FIELD = 1;
    // The tree node
    static final Integer PRIMARY_OBJECT_FIELDS_NODE = 2;
    // named Fields on the root object
    static final Integer LOOKUP_RELATIONSHIP_NODE = 3;
    static final Integer LOOKUP_RELATIONSHIP_FIELD = 4;
    static final Integer LOOKUP_RELATIONSHIP_FIELDS_NODE = 5;
    static final Integer CHILD_RELATIONSHIP_FIELD = 6;
    static final Integer CHILD_FIELDS_NODE = 7;
    static final Integer CHILD_RELATIONSHIP_NODE = 8;
    static final Integer DATA_TYPE_NODE = 9;
    static final Integer REFERENCE_TO_NODE = 10;

    protected DescribeSObjectResult dr = null;
    protected SchemaTreeLabelProvider provider = null;
    protected TreeItem selectedItem = null;
    protected boolean wasExpanded = false;
    protected Image imageNotChecked = null;
    protected Image imageChecked = null;
    protected Image imageArrowUp = null;
    protected Image imageArrowDown = null;
    protected Image imageBlank = null;
    protected IFile file = null;
    protected SashForm sashForm = null;
    protected StyledText textSOQL = null;
    protected SchemaEditorComposite schemaEditorComposite = null;
    protected QueryTableViewer queryTableViewer = null;

    // REVIEWME: why stored when we store in describe registry?
    private final Hashtable<String, DescribeSObjectResult> describeCache =
            new Hashtable<>();

    // C O N S T R U C T O R S
    public SchemaBrowser() {
        super();
        queryTableViewer = new QueryTableViewer();
    }

    // M E T H O D S
    @Override
    protected String getEditorName() {
        return "Schema Browser";
    }

    public QueryTableViewer getQueryTableViewer() {
        return queryTableViewer;
    }

    @Override
    protected IEditorSite createSite(IEditorPart editor) {
        IEditorSite site = null;
        site = super.createSite(editor);
        return site;
    }

    /**
     * Creates the pages of the multi-page editor.
     */
    @Override
    protected void createPages() {
        setPartName(file.getProject().getName());
        int index;
        try {
            index = addPage(createPage(new Composite(getContainer(), SWT.NONE)));
            setPageText(index, LABEL_SCHEMA_EXPLORER);
        } catch (Exception e) {
            logger.error("Unable to open Schema Browser", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to open Schema Browser.");
            index = addPage(new Composite(getContainer(), SWT.NONE));
            setPageText(index, MESSAGE_ERROR_LOADING_SCHEMA);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Opening Schema Browser");
        }
    }

    private Composite createPage(Composite composite) throws ForceConnectionException, ForceRemoteException {
        schemaEditorComposite =
                new SchemaEditorComposite(getContainer(), SWT.NONE, file.getProject(), queryTableViewer);
        wireUpComposite();
        UIUtils.setHelpContext(schemaEditorComposite, this.getClass().getSimpleName());
        return schemaEditorComposite;
    }

    private void wireUpComposite() {
        schemaEditorComposite.getButtonRun().addMouseListener(new org.eclipse.swt.events.MouseListener() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                try {
                    PlatformUI.getWorkbench().getProgressService().run(false, true, runQuery);
                } catch (InvocationTargetException e1) {
                    logger.error("Unable to open Schema Browser", ForceExceptionUtils.getRootCause(e1));
                    Utils.openError(ForceExceptionUtils.getRootCause(e1), false, "Unable to open Schema Browser.");
                } catch (InterruptedException e1) {
                    ;
                }
            }

            @Override
            public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {}

            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {}
        });

        schemaEditorComposite.getButtonRefresh().addMouseListener(new org.eclipse.swt.events.MouseListener() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                initialize();
            }

            @Override
            public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {}

            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {}
        });
        createTree(schemaEditorComposite);
    }

    IRunnableWithProgress runQuery = new IRunnableWithProgress() {
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException {
            try {
                IProject project = file.getProject();
                Connection connection = getConnectionFactory().getConnection(project);
                // get describe object w/o client id
                QueryResult qr = connection.query(schemaEditorComposite.getTextSOQL().getText(), false);

                if (schemaEditorComposite.getTextSOQL().getText().contains("count()")) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("count() executed");
                    }
                    Utils.openInfo("Record Count", (qr != null ? qr.getSize() : 0) + " records");
                }

                fillTable(qr, monitor, true);
            } catch (InsufficientPermissionsException e) {
                logger.error(e);
                throw new InvocationTargetException(e);
            } catch (ForceConnectionException e) {
                logger.error(e);
                throw new InvocationTargetException(e);
            } finally {
                if (monitor != null) {
                    monitor.done();
                }
                ;
            }
        }
    };

    @Override
    public String getTitle() {
        String title = super.getTitle();
        if ((title == null) && (getEditorInput() != null)) {
            title = getEditorInput().getName();
        }
        return title;
    }

    @Override
    public void doSaveAs() {
    // Nothing to save for now
    }

    /**
     * The <code>MultiPageEditorExample</code> implementation of this method checks that the input is an instance of
     * <code>IFileEditorInput</code>.
     */
    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        IFileEditorInput fei = (IFileEditorInput) editorInput;
        file = fei.getFile();
        setPartName("Schema Browser");
    }

    /*
    * (non-Javadoc) Method declared on IEditorPart.
    */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * Calculates the contents of page 2 when the it is activated.
     */
    @Override
    protected void pageChange(int newPageIndex) {
        getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);
        super.pageChange(newPageIndex);
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
        super.setInitializationData(config, propertyName, data);
    }

    @Override
    public void setInput(IEditorInput input) {
        super.setInput(input);
        setPartName(input.getName());
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    // nothing to do, this is only a view on some remote content
    }

    private void createTree(Composite composite) {
        imageNotChecked = ForceImages.get(ForceImages.IMAGE_NOT_CHECKED);
        imageChecked = ForceImages.get(ForceImages.IMAGE_CHECKED);
        imageArrowDown = ForceImages.get(ForceImages.IMAGE_ARROW_DOWN);
        imageArrowUp = ForceImages.get(ForceImages.IMAGE_ARROW_UP);
        imageBlank = ForceImages.get(ForceImages.IMAGE_BLANK);

        final Composite thisComposite = composite;

        provider = new SchemaTreeLabelProvider();

        Tree tree = this.schemaEditorComposite.getTree();
        tree.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.item instanceof TreeItem) {
                    selectedItem = (TreeItem) event.item;
                } else {
                    selectedItem = null;
                }
            }

        });
        tree.addTreeListener(new org.eclipse.swt.events.TreeListener() {
            @Override
            public void treeExpanded(org.eclipse.swt.events.TreeEvent e) {
                final TreeItem selectedItem = (TreeItem) e.item;
                Boolean isTopLevel = (Boolean) selectedItem.getData(IS_TOP_LEVEL);
                if ((isTopLevel != null) && isTopLevel.booleanValue()) {
                    if (selectedItem.getItemCount() == 1) {
                        Runnable lt = new Runnable() {
                            @Override
                            public void run() {
                                ProgressMonitorDialog mon = new ProgressMonitorDialog(getShell());
                                mon.getProgressMonitor();
                                mon.setBlockOnOpen(false);
                                mon.open();
                                loadTreeData(selectedItem, thisComposite, mon.getProgressMonitor());
                                setHasCheckableChildren(selectedItem, Boolean.TRUE);
                                setIsTopLevel(selectedItem, Boolean.TRUE);
                                setItemNotChecked(selectedItem);
                                selectedItem.setImage(provider.getImage(0, selectedItem.getText(), selectedItem
                                        .getParent()));
                                mon.close();
                            }
                        };
                        Runnable lb = new Runnable() {
                            @Override
                            public void run() {
                                ProgressMonitorDialog mon = new ProgressMonitorDialog(getShell());
                                mon.getProgressMonitor();
                                mon.setBlockOnOpen(false);
                                mon.open();
                                mon.getProgressMonitor().beginTask("Get object definition...", 2);
                                loadObject(selectedItem.getText(), mon.getProgressMonitor());
                                mon.close();
                            }
                        };

                        getSite().getShell().getDisplay().asyncExec(lb);
                        getSite().getShell().getDisplay().asyncExec(lt);
                    }
                } else {
                    Integer type = (Integer) selectedItem.getData(TYPE);
                    if (type != null) {
                        if (type.equals(SchemaBrowser.CHILD_RELATIONSHIP_NODE)
                                && selectedItem.getData(LOADED).equals(Boolean.FALSE)) {
                            Runnable getThisChildSchema = new Runnable() {
                                @Override
                                public void run() {
                                    ProgressMonitorDialog mon = new ProgressMonitorDialog(getShell());
                                    mon.getProgressMonitor();
                                    mon.setBlockOnOpen(false);
                                    mon.open();
                                    mon.getProgressMonitor().beginTask(MESSAGE_GETTING_CHILD, 2);
                                    loadOneChildRelationship(selectedItem, mon.getProgressMonitor());
                                    mon.close();
                                }
                            };

                            getSite().getShell().getDisplay().asyncExec(getThisChildSchema);

                        } else if (SchemaBrowser.LOOKUP_RELATIONSHIP_NODE.equals(type)
                                && Boolean.FALSE.equals(selectedItem.getData(LOADED))) {
                            Runnable getThisChildSchema = new Runnable() {
                                @Override
                                public void run() {
                                    ProgressMonitorDialog mon = new ProgressMonitorDialog(getShell());
                                    mon.getProgressMonitor();
                                    mon.setBlockOnOpen(false);
                                    mon.open();
                                    mon.getProgressMonitor().beginTask(MESSAGE_GETTING_LOOKUP, 2);
                                    loadOneChildRelationship(selectedItem, mon.getProgressMonitor());
                                    mon.close();
                                }
                            };

                            getSite().getShell().getDisplay().asyncExec(getThisChildSchema);
                        }
                    }

                }
                wasExpanded = true;
            }

            @Override
            public void treeCollapsed(org.eclipse.swt.events.TreeEvent e) {
                wasExpanded = true;
            }
        });

        tree.addMouseListener(new org.eclipse.swt.events.MouseListener() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                if (!wasExpanded) {
                    if (selectedItem != null) {
                        if (selectedItem.getImage() != null) {
                            Rectangle rect = selectedItem.getBounds();
                            Image img = selectedItem.getImage();
                            Rectangle imgRect = img.getBounds();
                            int leftMost = rect.x - imgRect.width - 3;
                            int rightMost = rect.x - 3;
                            if ((e.x >= leftMost) && (e.x <= rightMost)) {
                                Integer imageType = (Integer) selectedItem.getData(IMAGE_TYPE);
                                if (imageType != null) {
                                    if (imageType.intValue() == IMAGE_TYPE_CHECKED) {
                                        setItemChecked(selectedItem);
                                    } else {
                                        setItemNotChecked(selectedItem);
                                    }

                                    Integer type = (Integer) selectedItem.getData(TYPE);
                                    if (SchemaBrowser.CHILD_RELATIONSHIP_NODE.equals(type)
                                            && Boolean.FALSE.equals(selectedItem.getData(LOADED))) {
                                        if ((selectedItem.getData(LOADED) != null)
                                                && Boolean.FALSE.equals(selectedItem.getData(LOADED))) {
                                            Runnable getThisChildSchema = new Runnable() {
                                                @Override
                                                public void run() {
                                                    ProgressMonitorDialog mon = new ProgressMonitorDialog(getShell());
                                                    mon.getProgressMonitor();
                                                    mon.setBlockOnOpen(false);
                                                    mon.open();
                                                    mon.getProgressMonitor().beginTask(MESSAGE_GETTING_CHILD, 2);
                                                    loadOneChildRelationship(selectedItem, mon.getProgressMonitor());
                                                    mon.close();
                                                }
                                            };

                                            getSite().getShell().getDisplay().syncExec(getThisChildSchema);

                                        }
                                    } else if (SchemaBrowser.LOOKUP_RELATIONSHIP_NODE.equals(type)
                                            && Boolean.FALSE.equals(selectedItem.getData(LOADED))) {
                                        Runnable getThisChildSchema = new Runnable() {
                                            @Override
                                            public void run() {
                                                ProgressMonitorDialog mon = new ProgressMonitorDialog(getShell());
                                                mon.getProgressMonitor();
                                                mon.setBlockOnOpen(false);
                                                mon.open();
                                                mon.getProgressMonitor().beginTask(MESSAGE_GETTING_LOOKUP, 2);
                                                loadOneChildRelationship(selectedItem, mon.getProgressMonitor());
                                                mon.close();
                                            }
                                        };

                                        getSite().getShell().getDisplay().asyncExec(getThisChildSchema);
                                    }

                                    setChildren(selectedItem, ((Integer) selectedItem.getData(IMAGE_TYPE)).intValue());

                                    while (selectedItem.getParentItem() != null) {
                                        TreeItem parent = selectedItem.getParentItem();
                                        boolean setParent = true;
                                        for (int i = 0; i < parent.getItemCount(); i++) {
                                            if (!parent.getItem(i).equals(selectedItem)
                                                    && parent.getItem(i).getImage().equals(imageChecked)) {
                                                setParent = false;
                                                break;
                                            }
                                        }

                                        if (!setParent) {
                                            break;
                                        }
										if (imageType.intValue() == 0) {
										    setItemChecked(parent);
										} else {
										    setItemNotChecked(parent);
										}
                                        selectedItem = parent;
                                    }
                                    fireSelectionChanged(selectedItem);
                                }
                            }
                        }
                    }
                }
                wasExpanded = false;
            }

            @Override
            public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {}

            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {}
        });
        initialize();
    }

    Shell getShell() {
        return getSite().getShell();
    }

    void setChildren(TreeItem treeItem, int imageType) {
        TreeItem[] children = treeItem.getItems();
        for (TreeItem element : children) {
            Integer iType = (Integer) element.getData(IMAGE_TYPE);
            if (iType != null) {
                if (imageType == 1) {
                    setItemChecked(element);
                } else {
                    setItemNotChecked(element);
                }
            }
            if (((Boolean) element.getData(HAS_CHECKABLE_CHILDREN)).booleanValue()) {
                setChildren(element, imageType);
            }
        }
    }

    void fireSelectionChanged(TreeItem treeItem) {
        SelectListChangedArguments slca = generateSOQL(treeItem);
        if (slca == null) {
            schemaEditorComposite.getTextSOQL().setText("");
        } else {
            String soql = "Select ";
            ArrayList<String> fieldList = slca.getSelectedFields();
            for (int i = 0; i < fieldList.size(); i++) {
                String fieldName = fieldList.get(i);
                soql += fieldName;
                if (i < fieldList.size() - 1) {
                    soql += ", ";
                }
            }
            soql += " From " + slca.getTableName();
            schemaEditorComposite.getTextSOQL().setText(soql);
        }
    }

    private static TreeItem getPrimaryFieldsNode(TreeItem customObjectNode) {
        TreeItem fieldsNode = null;
        for (int i = 0; i < customObjectNode.getItemCount(); i++) {
            TreeItem nodeChild = customObjectNode.getItem(i);
            Integer nodeType = (Integer) nodeChild.getData(TYPE);
            if (nodeType != null) {
                if (SchemaBrowser.PRIMARY_OBJECT_FIELDS_NODE.equals(nodeType)
                        || SchemaBrowser.CHILD_FIELDS_NODE.equals(nodeType)) {
                    fieldsNode = customObjectNode.getItem(i);
                    break;
                }
            }
        }
        return fieldsNode;
    }

    private static TreeItem getChildRelationshipsNode(TreeItem customObjectNode) {
        TreeItem childRNode = null;
        for (int i = 0; i < customObjectNode.getItemCount(); i++) {
            String nodeLabel = customObjectNode.getItem(i).getText();
            if (NODE_LABEL_CHILD_RELATIONSHIPS.equals(nodeLabel)) {
                childRNode = customObjectNode.getItem(i);
                break;
            }
        }
        return childRNode;
    }

    private static TreeItem getReferenceToNode(TreeItem primaryField) {
        TreeItem referenceToNode = null;
        TreeItem returnNode = null;

        for (int i = 0; i < primaryField.getItemCount(); i++) {
            if (SchemaBrowser.DATA_TYPE_NODE.equals(primaryField.getItem(i).getData(TYPE))) {
                referenceToNode = primaryField.getItem(i);
                break;
            }
        }
        if (referenceToNode != null) {
            for (int i = 0; i < referenceToNode.getItemCount(); i++) {
                if (SchemaBrowser.REFERENCE_TO_NODE.equals(referenceToNode.getItem(i).getData(TYPE))) {
                    returnNode = referenceToNode.getItem(i);
                    break;
                }
            }
        }
        return returnNode;
    }

    private static TreeItem getReferenceToObjectFieldsNode(TreeItem referenceToObjectNode) {
        TreeItem lookupFieldsNode = null;
        for (int k = 0; k < referenceToObjectNode.getItemCount(); k++) {
            TreeItem objectChildNode = referenceToObjectNode.getItem(k);
            if (SchemaBrowser.LOOKUP_RELATIONSHIP_FIELDS_NODE.equals(referenceToObjectNode.getItem(k).getData(TYPE))) {
                lookupFieldsNode = objectChildNode;
                break;
            }
        }
        return lookupFieldsNode;
    }

    private void checkLookupRelations(TreeItem primaryField, Stack<String> primaryFieldStack, String parentAlias) {
        // We need to find the reference to node
        TreeItem referenceToNode = getReferenceToNode(primaryField);

        if (referenceToNode != null) {
            for (int j = 0; j < referenceToNode.getItemCount(); j++) {
                TreeItem cobject = referenceToNode.getItem(j);
                if (cobject.getImage().equals(imageChecked)) {
                    // We have the lookup object, need to get to the fields node
                    TreeItem lookupFieldsNode = getReferenceToObjectFieldsNode(cobject);
                    if (lookupFieldsNode != null) {
                        for (int k = 0; k < lookupFieldsNode.getItemCount(); k++) {
                            TreeItem objectChildNode = lookupFieldsNode.getItem(k);
                            if (objectChildNode.getImage().equals(imageChecked)) {
                                primaryFieldStack.push(parentAlias + "." + objectChildNode.getData(FQN));
                            }
                        }
                    }
                }
            }
        }

    }

    private void checkChildRelationships(TreeItem childRelationShipsNode, Stack<String> childRelationshipStack) {
        if (childRelationShipsNode != null) {
            for (int i = 0; i < childRelationShipsNode.getItemCount(); i++) {
                TreeItem item = childRelationShipsNode.getItem(i);
                if (item.getImage().equals(imageChecked)) {
                    TreeItem fields = null;
                    String from = ((ChildRelationship) item.getData("relationship")).getRelationshipName();
                    String subQuery = "Select ";
                    for (int j = 0; j < item.getItemCount(); j++) {
                        if ("Fields".equalsIgnoreCase(item.getItem(j).getText())) {
                            fields = item.getItem(j);
                            break;
                        }
                    }
                    if (fields != null) {
                        for (int j = 0; j < fields.getItemCount(); j++) {
                            if (fields.getItem(j).getImage().equals(imageChecked)) {
                                if ("Select ".equalsIgnoreCase(subQuery)) {
                                    subQuery += ((Field) fields.getItem(j).getData("field")).getName();
                                } else {
                                    subQuery += ", " + ((Field) fields.getItem(j).getData("field")).getName();
                                }
                            }
                        }
                    }
                    subQuery += " From " + from;
                    childRelationshipStack.push("(" + subQuery + ")");
                }
            }
        }
    }

    /*
    * How this works:
    *
    * 1. First, we need to get the top most node and that is the customObjectNode.
    *
    * 2. If the top most node is not checked then nothing else is and we can clear the SOQL
    *
    * 3. If the top most node is not check, then we need look for checked items
    *
    * 4. Fields can come from 3 places, the primary fields, child relationships and related records from reference to
    * fields
    *
    * 5. Get the node that contains all the primary fields (PRIMARY_OBJECT_FIELDS_NODE)
    *
    * 6. Check to see if that is checked,
    */
    private SelectListChangedArguments generateSOQL(TreeItem selectedItem) {

        // 1
        TreeItem customObjectNode = getTopOfBranch(selectedItem);
        // 2
        if (customObjectNode.getImage().equals(imageNotChecked)) {
            return null;
        }

        // now we go to the fields node, which is a child of the root
        TreeItem primaryFieldsNode = getPrimaryFieldsNode(customObjectNode);

        // Get the childRelationships node in case we are doing a join
        TreeItem childRelationShipsNode = getChildRelationshipsNode(customObjectNode);

        // Initialize a stack to hold subqueries
        Stack<String> childRelationshipStack = new Stack<>();
        Stack<String> primaryFieldsStack = new Stack<>();

        SelectListChangedArguments slca = null;
        String parentAlias = customObjectNode.getText().toLowerCase().substring(0, 1);

        // Loop through the primary fields and the referenceTo fields
        for (int i = 0; i < primaryFieldsNode.getItemCount(); i++) {
            // For each field node, we see if it is checked
            TreeItem primaryField = primaryFieldsNode.getItem(i);
            if (primaryField.getImage().equals(imageChecked)) {
                // Since the field is checked, we will add it to the field list
                primaryFieldsStack.push(parentAlias + "." + ((Field) primaryField.getData("field")).getName());
                // Now we need to check for lookup relation fields
                if (((Field) primaryField.getData("field")).getRelationshipName() != null) {
                    checkLookupRelations(primaryField, primaryFieldsStack, parentAlias);
                }
            }
        }

        // Look for subqueries based on child relationships
        checkChildRelationships(childRelationShipsNode, childRelationshipStack);

        if (!primaryFieldsStack.isEmpty() || !childRelationshipStack.isEmpty()) {
            slca = new SelectListChangedArguments();
            slca.setTableName(customObjectNode.getText() + " " + parentAlias);
            while (!primaryFieldsStack.isEmpty()) {
                slca.addField(primaryFieldsStack.pop());
            }
            while (!childRelationshipStack.isEmpty()) {
                slca.addField(childRelationshipStack.pop());
            }
        }

        return slca;
    }

    private static TreeItem getTopOfBranch(TreeItem childItem) {
        Boolean isTopLevel = (Boolean) childItem.getData(IS_TOP_LEVEL);
        TreeItem parent;
        if (isTopLevel.booleanValue()) {
            return childItem;
        }
        parent = childItem;
        while ((isTopLevel == null) || !isTopLevel.booleanValue()) {
            parent = parent.getParentItem();
            isTopLevel = (Boolean) parent.getData(IS_TOP_LEVEL);
        }
        return parent;
    }

    private Image getImage(int imageId, String label) {
        return provider.getImage(imageId, label, schemaEditorComposite.getTree());
    }

    private TreeItem createItem(TreeItem item, String label, boolean check, int imageIndex) {
        item.setText(label);
        if (check) {
            setItemNotChecked(item);
        } else {
            Image image = getImage(imageIndex, item.getText());
            item.setImage(image);
        }
        return item;
    }

    private TreeItem createTreeItemChild(TreeItem parent, String label, boolean check, int imageIndex,
            boolean hasCheckableChildren, Integer type) {
        TreeItem item = createTreeItemChild(parent, label, check, imageIndex, hasCheckableChildren);
        item.setData(TYPE, type);
        item.setData(LOADED, Boolean.FALSE);
        return item;
    }

    private TreeItem createTreeItemChild(TreeItem parent, String label, boolean check, int imageIndex,
            boolean hasCheckableChildren) {
        TreeItem treeItem = createItem(new TreeItem(parent, SWT.NONE), label, check, imageIndex);
        treeItem.setData(IS_TOP_LEVEL, Boolean.FALSE);
        treeItem.setData(HAS_CHECKABLE_CHILDREN, Boolean.valueOf(hasCheckableChildren));
        treeItem.setData(TYPE, Integer.valueOf(TYPE_MINUSONE));
        return treeItem;
    }

    private TreeItem createTreeChild(Tree parent, String label, boolean check, int imageIndex, Integer type) {
        TreeItem ret = createTreeChild(parent, label, check, imageIndex);
        ret.setData(TYPE, type);
        return ret;
    }

    private TreeItem createTreeChild(Tree parent, String label, boolean check, int imageIndex) {
        TreeItem treeItem = createItem(new TreeItem(parent, SWT.NONE), label, check, imageIndex);
        treeItem.setData(IS_TOP_LEVEL, Boolean.TRUE);
        treeItem.setData(HAS_CHECKABLE_CHILDREN, Boolean.TRUE);
        treeItem.setData(TYPE, Integer.valueOf(TYPE_MINUSONE));
        return treeItem;
    }

    void setItemChecked(TreeItem item) {
        item.setImage(imageChecked);
        item.setData(IMAGE_TYPE, Integer.valueOf(1));
    }

    void setItemNotChecked(TreeItem item) {
        item.setImage(imageNotChecked);
        item.setData(IMAGE_TYPE, Integer.valueOf(0));
    }

    void setIsTopLevel(TreeItem treeItem, Boolean isTopLevel) {
        treeItem.setData(IS_TOP_LEVEL, isTopLevel);
    }

    void setHasCheckableChildren(TreeItem treeItem, Boolean hasCheckableChildren) {
        treeItem.setData(HAS_CHECKABLE_CHILDREN, hasCheckableChildren);
    }

    private void loadObjectAccessData(TreeItem accessRoot, DescribeSObjectResult dr) {

        if (dr.isActivateable()) {
            createTreeItemChild(accessRoot, ACTIVATABLE, false, 0, false);
        }
        if (dr.isCreateable()) {
            createTreeItemChild(accessRoot, CREATEABLE, false, 0, false);
        }
        if (dr.isCustom()) {
            createTreeItemChild(accessRoot, CUSTOM, false, 0, false);
        }
        if (dr.isDeletable()) {
            createTreeItemChild(accessRoot, DELETEABLE, false, 0, false);
        }
        if (dr.isLayoutable()) {
            createTreeItemChild(accessRoot, LAYOUTABLE, false, 0, false);
        }
        if (dr.isQueryable()) {
            createTreeItemChild(accessRoot, QUERYABLE, false, 0, false);
        }
        if (dr.isReplicateable()) {
            createTreeItemChild(accessRoot, REPLICATABLE, false, 0, false);
        }
        if (dr.isRetrieveable()) {
            createTreeItemChild(accessRoot, RETRIEVEABLE, false, 0, false);
        }
        if (dr.isSearchable()) {
            createTreeItemChild(accessRoot, SEARCHABLE, false, 0, false);
        }
        if (dr.isUndeletable()) {
            createTreeItemChild(accessRoot, UNDELETEABLE, false, 0, false);
        }
        if (dr.isUpdateable()) {
            createTreeItemChild(accessRoot, UPDATEABLE, false, 0, false);
        }

    }

    private void loadFieldAccessData(TreeItem fieldAccessRoot, Field field) {

        if (field.isAutoNumber()) {
            createTreeItemChild(fieldAccessRoot, AUTO_NUMBER, false, 0, false);
        }
        if (field.isCalculated()) {
            createTreeItemChild(fieldAccessRoot, FORMULA, false, 0, false);
        }
        if (field.isCreateable()) {
            createTreeItemChild(fieldAccessRoot, CREATEABLE, false, 0, false);
        }
        if (field.isCustom()) {
            createTreeItemChild(fieldAccessRoot, CUSTOM, false, 0, false);
        }
        if (field.isDefaultedOnCreate()) {
            createTreeItemChild(fieldAccessRoot, DEFAULTED_ON_CREATE, false, 0, false);
        }
        if (field.isFilterable()) {
            createTreeItemChild(fieldAccessRoot, FILTERABLE, false, 0, false);
        }
        if (field.isNameField()) {
            createTreeItemChild(fieldAccessRoot, NAME_FIELD, false, 0, false);
        }
        if (field.isNillable()) {
            createTreeItemChild(fieldAccessRoot, NILLABLE, false, 0, false);
        }
        if (field.isRestrictedPicklist()) {
            createTreeItemChild(fieldAccessRoot, RESTRICTED, false, 0, false);
        }
        if (field.isUpdateable()) {
            createTreeItemChild(fieldAccessRoot, UPDATEABLE, false, 0, false);
        }
    }

    private void loadPickListValues(TreeItem picklistRoot, PicklistEntry[] values) {
        for (PicklistEntry entry : values) {
            String label = entry.getLabel();
            if (label == null) {
                label = entry.getValue();
            }
            if (entry.isDefaultValue()) {
                label += " (default)";
            }
            TreeItem entryRoot = createTreeItemChild(picklistRoot, label, false, 0, false);
            if (entry.getLabel() != null) {
                createTreeItemChild(entryRoot, "Label: " + entry.getLabel(), false, 0, false);
            }
            createTreeItemChild(entryRoot, "Value: " + entry.getValue(), false, 0, false);
            createTreeItemChild(entryRoot, "Active: " + Boolean.valueOf(entry.isActive()).toString(), false, 0, false);
            createTreeItemChild(entryRoot, "Default Value: " + Boolean.valueOf(entry.isDefaultValue()).toString(),
                false, 0, false);
        }
    }

    private void loadReferenceTo(TreeItem referenceToRoot, String[] refTo) {
        for (String element : refTo) {
            TreeItem referenceTo =
                    createTreeItemChild(referenceToRoot, element, true, 0, true, SchemaBrowser.LOOKUP_RELATIONSHIP_NODE);
            createTreeItemChild(referenceTo, " ", false, 0, false);
        }
    }

    private void loadFieldTypeData(TreeItem fieldTypeRoot, Field field) {

        String fieldType = field.getType().toString();
        String ext = "";
        if (field.getExternalId()) {
            ext = " (External Id)";
        }

        fieldTypeRoot.setText(fieldTypeRoot.getText() + " - " + fieldType + ext);

        createTreeItemChild(fieldTypeRoot, "Soap Type: " + field.getSoapType(), false, 0, false);

        if ("int".equalsIgnoreCase(fieldType)) {
            createTreeItemChild(fieldTypeRoot, "digits: " + Integer.valueOf(field.getDigits()).toString(), false, 0,
                false);
        }
        if ("double".equalsIgnoreCase(fieldType) || "currency".equalsIgnoreCase(fieldType)) {
            createTreeItemChild(fieldTypeRoot, "precision: " + Integer.valueOf(field.getPrecision()).toString(), false,
                0, false);
            createTreeItemChild(fieldTypeRoot, "scale: " + Integer.valueOf(field.getScale()).toString(), false, 0,
                false);
        }
        if ("percent".equalsIgnoreCase(fieldType)) {
            createTreeItemChild(fieldTypeRoot, "precision: " + Integer.valueOf(field.getPrecision()).toString(), false,
                0, false);
        }
        if ("string".equalsIgnoreCase(fieldType) || "textarea".equalsIgnoreCase(fieldType)
                || "phone".equalsIgnoreCase(fieldType) || "id".equalsIgnoreCase(fieldType)
                || "url".equalsIgnoreCase(fieldType) || "email".equalsIgnoreCase(fieldType)) {
            createTreeItemChild(fieldTypeRoot, "length: " + Integer.valueOf(field.getLength()).toString(), false, 0,
                false);
            createTreeItemChild(fieldTypeRoot, "byte length: " + Integer.valueOf(field.getByteLength()).toString(),
                false, 0, false);
        }
        if ("picklist".equalsIgnoreCase(fieldType) || "mutlipicklist".equalsIgnoreCase(fieldType)
                || "combobox".equalsIgnoreCase(fieldType)) {
            createTreeItemChild(fieldTypeRoot, "Length: " + Integer.valueOf(field.getLength()).toString(), false, 0,
                false);
            createTreeItemChild(fieldTypeRoot, "Byte Length: " + Integer.valueOf(field.getByteLength()).toString(),
                false, 0, false);
            if (field.getControllerName() != null) {
                createTreeItemChild(fieldTypeRoot, "Controller: " + field.getControllerName(), false, 0, false);
            }
            TreeItem picklistRoot = createTreeItemChild(fieldTypeRoot, "Picklist Values", false, 12, false);
            if (field.getDependentPicklist()) {
                createTreeItemChild(picklistRoot, "Dependent Picklist", false, 0, false);
            }
            if (field.getPicklistValues() != null) {
                loadPickListValues(picklistRoot, field.getPicklistValues());
            }
        }

        if ("reference".equalsIgnoreCase(fieldType)) {
            setHasCheckableChildren(fieldTypeRoot, Boolean.TRUE);
            createTreeItemChild(fieldTypeRoot, "Length: " + Integer.valueOf(field.getLength()).toString(), false, 0,
                false);
            createTreeItemChild(fieldTypeRoot, "Byte Length: " + Integer.valueOf(field.getByteLength()).toString(),
                false, 0, false);
            TreeItem referenceToRoot =
                    createTreeItemChild(fieldTypeRoot, "Reference To", false, 0, true, SchemaBrowser.REFERENCE_TO_NODE);
            loadReferenceTo(referenceToRoot, field.getReferenceTo());
        }

    }

    private void loadFieldsData(TreeItem fieldsRoot, DescribeSObjectResult dr, IProgressMonitor monitor) {

        Field[] fields = dr.getFields();
        TreeItem fieldRoot;
        Arrays.sort(fields, new FieldComparator());
        for (Field element : fields) {
            if (monitor != null) {
                monitor.worked(1);
            }
            Field field = element;
            String fldName = field.getName();
            fldName += " - " + field.getType();

            if (field.isCustom()) {
                fldName += " (custom)";
            }

            fieldRoot = createTreeItemChild(fieldsRoot, fldName, true, 0, false, SchemaBrowser.PRIMARY_ROOT_FIELD);
            fieldRoot.setData("field", element);

            if (field.getHtmlFormatted()) {
                createTreeItemChild(fieldRoot, "Field is HTML Formatted", false, 0, false);
            }
            if (field.getRelationshipName() != null) {
                createTreeItemChild(fieldRoot, "Foreign Key: " + field.getRelationshipName(), false, 0, false);
            }

            TreeItem fieldAccessRoot = createTreeItemChild(fieldRoot, "Access", false, 0, false);
            loadFieldAccessData(fieldAccessRoot, field);

            if (field.getLabel() != null) {
                TreeItem labelRoot = createTreeItemChild(fieldRoot, "Label", false, 0, false);
                createTreeItemChild(labelRoot, field.getLabel(), false, 0, false);
            }

            TreeItem fieldTypeRoot =
                    createTreeItemChild(fieldRoot, "Type Data", false, 0, false, SchemaBrowser.DATA_TYPE_NODE);
            loadFieldTypeData(fieldTypeRoot, field);

        }
    }

    void loadOneChildRelationship(TreeItem crRoot, IProgressMonitor monitor) {
        try {
            Integer childFieldType = SchemaBrowser.LOOKUP_RELATIONSHIP_FIELD;
            Integer childFieldsNode = SchemaBrowser.LOOKUP_RELATIONSHIP_FIELDS_NODE;
            String fieldPrefix = "";
            if (SchemaBrowser.PRIMARY_ROOT_FIELD.equals(crRoot.getData(TYPE))) {
                crRoot = getReferenceToNode(crRoot);
            }
            if (SchemaBrowser.LOOKUP_RELATIONSHIP_NODE.equals(crRoot.getData(TYPE))
                    || SchemaBrowser.PRIMARY_ROOT_FIELD.equals(crRoot.getData(TYPE))) {
                crRoot.removeAll();
                childFieldType = SchemaBrowser.LOOKUP_RELATIONSHIP_FIELD;
                if (SchemaBrowser.PRIMARY_ROOT_FIELD.equals(crRoot.getData(TYPE))) {
                    fieldPrefix = ((Field) crRoot.getData("field")).getRelationshipName();
                } else {
                    fieldPrefix =
                            ((Field) crRoot.getParentItem().getParentItem().getParentItem().getData("field"))
                                    .getRelationshipName();
                }
            } else {
                childFieldType = SchemaBrowser.CHILD_RELATIONSHIP_FIELD;
                childFieldsNode = SchemaBrowser.CHILD_FIELDS_NODE;
                fieldPrefix = ((ChildRelationship) crRoot.getData("relationship")).getRelationshipName();
            }
            DescribeSObjectResult dr = null;
            IProject project = file.getProject();
            Connection connection = getConnectionFactory().getConnection(project);
            // get describe object w/o client id
            dr = connection.describeSObject(crRoot.getText(), false);

            Field[] fields = dr.getFields();
            TreeItem childFields = createTreeItemChild(crRoot, "Fields", true, 0, true, childFieldsNode);

            SubProgressMonitor spm = new SubProgressMonitor(monitor, 1);
            spm.beginTask("Getting relationship definitions...", fields.length);
            for (Field element : fields) {
                spm.worked(1);
                spm.subTask("Getting " + element.getLabel() + " definition...");
                TreeItem thisChild =
                        createTreeItemChild(childFields, element.getLabel(), true, 0, false, childFieldType);
                thisChild.setData(FQN, fieldPrefix + "." + element.getName());
                thisChild.setData("field", element);
            }
            crRoot.setData(LOADED, Boolean.TRUE);
            spm.done();
        } catch (Exception e) {
            Utils.openError(e, MESSAGE_COULD_NOT_FETCH_META_DATA, MESSAGE_SEE_DETAILS);
        }
    }

    private void loadChildRelationships(TreeItem crRoot, ChildRelationship[] relationships, IProgressMonitor monitor) {
        for (ChildRelationship cr : relationships) {
            if (monitor != null) {
                monitor.worked(1);
            }
            TreeItem child;
            if (cr.getRelationshipName() != null) {
                child =
                        createTreeItemChild(crRoot, cr.getChildSObject(), true, 0, true,
                            SchemaBrowser.CHILD_RELATIONSHIP_NODE);
                child.setData("relationship", cr);
            } else {
                child = createTreeItemChild(crRoot, cr.getChildSObject(), false, 0, false);
            }
            if (cr.isCascadeDelete()) {
                createTreeItemChild(child, "Cascade Delete", false, 0, false);
            }
            if (cr.getRelationshipName() != null) {
                if (monitor != null) {
                    monitor.subTask(cr.getRelationshipName());
                }
                createTreeItemChild(child, "Relationship Name: " + cr.getRelationshipName(), false, 0, false);
                createTreeItemChild(child, "Related Field: " + cr.getField(), false, 0, false);
            }

        }
    }

    private void loadFrontDoorUrls(TreeItem objectRoot, DescribeSObjectResult dr) {
        if ((dr.getUrlDetail() != null) || (dr.getUrlEdit() != null) || (dr.getUrlNew() != null)) {
            TreeItem frontDoor = createTreeItemChild(objectRoot, "Frontdoor URLS", false, 0, false);
            if (dr.getUrlDetail() != null) {
                TreeItem urlD = createTreeItemChild(frontDoor, "URL - Detail", false, 0, false);
                createTreeItemChild(urlD, dr.getUrlDetail(), false, 0, false);
            }
            if (dr.getUrlEdit() != null) {
                TreeItem urlD = createTreeItemChild(frontDoor, "URL - Edit", false, 0, false);
                createTreeItemChild(urlD, dr.getUrlEdit(), false, 0, false);
            }
            if (dr.getUrlNew() != null) {
                TreeItem urlD = createTreeItemChild(frontDoor, "URL - New", false, 0, false);
                createTreeItemChild(urlD, dr.getUrlNew(), false, 0, false);
            }
        }

    }

    void loadObject(String type, IProgressMonitor monitor) {
        try {
            dr = getCachedDescribe(type);
        } catch (Exception e) {
            Utils.openError(e, MESSAGE_COULD_NOT_FETCH_META_DATA, MESSAGE_SEE_DETAILS);
        }
    }

    void loadTreeData(TreeItem objectRoot, Composite composite, IProgressMonitor monitor) {
        Cursor wait_cursor = new Cursor(composite.getDisplay(), SWT.CURSOR_WAIT);
        composite.setCursor(wait_cursor);
        objectRoot.removeAll();
        try {
            if (dr.getKeyPrefix() != null) {
                createTreeItemChild(objectRoot, "ID Prefix: " + dr.getKeyPrefix(), false, 0, false);
            }

            loadFrontDoorUrls(objectRoot, dr);

            TreeItem labelsRoot = createTreeItemChild(objectRoot, "Labels", false, 0, false);
            createTreeItemChild(labelsRoot, "Singular: " + dr.getLabel(), false, 0, false);
            createTreeItemChild(labelsRoot, "Plural: " + dr.getLabelPlural(), false, 0, false);

            TreeItem accessRoot = createTreeItemChild(objectRoot, "Access", false, 0, false);
            loadObjectAccessData(accessRoot, dr);

            TreeItem fieldsRoot =
                    createTreeItemChild(objectRoot, "Fields - " + dr.getFields().length, true, 0, true,
                        SchemaBrowser.PRIMARY_OBJECT_FIELDS_NODE);
            int monitorTotal = dr.getFields().length;
            if (dr.getChildRelationships() != null) {
                monitorTotal += dr.getChildRelationships().length;
            }
            monitor.beginTask(MESSAGE_GETTING_CHILD_FIELDS, monitorTotal);

            loadFieldsData(fieldsRoot, dr, monitor);

            if (dr.getChildRelationships() != null) {
                TreeItem childRelsRoot =
                        createTreeItemChild(objectRoot, NODE_LABEL_CHILD_RELATIONSHIPS, false, 0, false);
                loadChildRelationships(childRelsRoot, dr.getChildRelationships(), monitor);
            }

        } catch (Exception e) {
            Utils.openError(e, MESSAGE_COULD_NOT_FETCH_META_DATA, MESSAGE_SEE_DETAILS);
        }
        if (monitor != null) {
            monitor.done();
        }

        wait_cursor.dispose();
        composite.setCursor(null);
    }

    private DescribeSObjectResult getCachedDescribe(String componentType) throws ForceConnectionException,
            ForceRemoteException {
        if (!describeCache.containsKey(componentType.toLowerCase())) {
            IProject project = file.getProject();
            Connection connection = getConnectionFactory().getConnection(project);

            // get describe object w/o client id
            DescribeSObjectResult describeSObject = connection.describeSObject(componentType, false);
            describeCache.put(componentType.toLowerCase(), describeSObject);
        }
        return describeCache.get(componentType.toLowerCase());
    }

    void initialize() {
        Cursor wait_cursor = new Cursor(schemaEditorComposite.getDisplay(), SWT.CURSOR_WAIT);
        schemaEditorComposite.setCursor(wait_cursor);

        IProject project = file.getProject();
        try {
            Connection connection = getConnectionFactory().getConnection(project);
            // get types w/o client id
            String[] types = connection.retrieveTypes(false);

            if (Utils.isNotEmpty(types) && schemaEditorComposite != null && schemaEditorComposite.getTree() != null
                    && schemaEditorComposite.getTree().getItemCount() > 0) {
                schemaEditorComposite.getTree().removeAll();
            }

            clearDescribeCache(project);

            for (int i = 0; i < types.length; i++) {
                // temp remove
                if ("ApexPage".equals(types[i])) {
                    continue;
                }
                createTreeItemChild(createTreeChild(schemaEditorComposite.getTree(), types[i], false, 0,
                    SchemaBrowser.PRIMARY_ROOT_NODE), "", false, 0, false);
            }
        } catch (Exception e) {
            Utils.openError(e, MESSAGE_COULD_NOT_FETCH_META_DATA, MESSAGE_SEE_DETAILS);
        }

        wait_cursor.dispose();
        schemaEditorComposite.setCursor(null);
    }

    private void clearDescribeCache(IProject project) throws ForceConnectionException, ForceRemoteException {
        // Refresh the DescribeSObject cache and also the DescribeObjectRegistry
        describeCache.clear();
        ContainerDelegate.getInstance().getServiceLocator().getProjectService().getDescribeObjectRegistry().refresh(project);
    }

    void fillTable(QueryResult qr, IProgressMonitor monitor, boolean clearTable) {
        schemaEditorComposite.loadTable(qr);
        monitor.done();
    }

    public SchemaEditorComposite getSchemaEditorComposite() {
        return schemaEditorComposite;
    }
}
