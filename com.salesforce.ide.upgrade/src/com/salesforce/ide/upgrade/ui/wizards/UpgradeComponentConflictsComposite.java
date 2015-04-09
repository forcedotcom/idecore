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
package com.salesforce.ide.upgrade.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.upgrade.internal.UpgradeConflict;
import com.salesforce.ide.upgrade.internal.UpgradeController;
import com.salesforce.ide.upgrade.internal.utils.UpgradeMessages;

/**
 * Displays local component upgrade candidates.
 * 
 * @author cwall
 */
public class UpgradeComponentConflictsComposite extends BaseUpgradeComposite {

    private Tree treeComponentConflicts = null;
    private Label lblIntro = null;
    private GridData gdTree = null;

    public UpgradeComponentConflictsComposite(Composite parent, int style, UpgradeController upgradeController) {
        super(parent, style, upgradeController);
    }
    
    public static UpgradeComponentConflictsComposite newInstance(Composite parent, int style, UpgradeController upgradeController){
        final UpgradeComponentConflictsComposite _instance = new UpgradeComponentConflictsComposite(parent, style, upgradeController);
        _instance.initialize();
        return _instance;
    }

    protected void setTreeComponentConflicts(Tree treeComponentConflicts) {
        this.treeComponentConflicts = treeComponentConflicts;
    }

    protected void initialize() {
        setLayout(new GridLayout());

        lblIntro = new Label(this, SWT.WRAP);
        lblIntro.setText(UpgradeMessages.getString("UpgradeWizard.ConflictsPage.Content.message",
                new String[] { upgradeModel.getIdeReleaseName() }));
        lblIntro.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0));

        treeComponentConflicts = new Tree(this, SWT.BORDER | SWT.V_SCROLL);
        gdTree = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 7);
        treeComponentConflicts.setLayoutData(gdTree);
        treeComponentConflicts.setHeaderVisible(false);
        TreeColumn clmComponent = new TreeColumn(treeComponentConflicts, SWT.LEFT | SWT.MULTI);
        clmComponent.setWidth(415);
        clmComponent.setResizable(true);

        // opens comare dialog
        treeComponentConflicts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                final TreeItem treeItem = (TreeItem)event.item;
                if (treeItem.getData("local") == null || !(treeItem.getData("local") instanceof Component)) { return; }

                final Component localComponent = (Component)treeItem.getData("local");
                final Component remoteComponent = (Component)treeItem.getData("remote");
                final CompareConfiguration componentCompareConfiguration = new CompareConfiguration();
                componentCompareConfiguration.setLeftEditable(false);
                componentCompareConfiguration.setRightEditable(false);
                componentCompareConfiguration.setLeftLabel(UpgradeMessages
                        .getString("UpgradeWizard.Compare.LocalFile.title"));
                componentCompareConfiguration.setRightLabel(UpgradeMessages
                        .getString("UpgradeWizard.Compare.RemoteFile.title"));

                final CompareEditorInput compareEditor = new CompareEditorInput(componentCompareConfiguration) {
                    @Override
                    public Control createContents(Composite parent) {
                        Label lblCompareIntro = new Label(parent, SWT.WRAP);
                        lblCompareIntro.setText(UpgradeMessages.getString("UpgradeWizard.Compare.Content.message",
                                new String[] { upgradeModel.getIdeReleaseName() }));
                        lblCompareIntro.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                        return super.createContents(parent);
                    }

                    @Override
                    protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException,
                            InterruptedException {
                        ComponentCompareItem leftCompare = new ComponentCompareItem(localComponent);
                        ComponentCompareItem rightCompare = new ComponentCompareItem(remoteComponent);
                        return new DiffNode(leftCompare, rightCompare);
                    }
                };

                compareEditor.setTitle(UpgradeMessages.getString("UpgradeWizard.Compare.title",
                        new String[] { localComponent.getFileName() }));

                compareEditor.setFocus2();

                if (compareResultOK(compareEditor, null)) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            CompareDialog dialog = new CompareDialog(getShell(), compareEditor);
                            dialog.open();
                        }
                    };
                    if (Display.getCurrent() == null) {
                        Display.getDefault().syncExec(runnable);
                    } else {
                        runnable.run();
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {}
        });

        // fake a hyperlink
        treeComponentConflicts.addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TreeItem item = (TreeItem)event.item;
                TreeItem[] sel = treeComponentConflicts.getSelection();

                if (item.getData("local") != null && (Utils.isEmpty(sel) || !sel[0].equals(item))) {
                    event.gc.setForeground(item.getForeground());
                    event.gc.drawLine(event.x + 1, event.y + event.height - 2, event.x + event.width - 4, event.y
                            + event.height - 2);
                }
            }
        });

        treeComponentConflicts.addMouseMoveListener(new MouseMoveListener() {
            TreeItem previousItem = null;
            Color previousBgColor = null;
            Color previousFgColor = null;
            Color highlightBgColor = treeComponentConflicts.getBackground();
            Color highlightFgColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
            Cursor handCursor = Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND);

            @Override
            public void mouseMove(MouseEvent e) {
                // get current TreeItem that mouse is over
                TreeItem item = treeComponentConflicts.getItem(new Point(e.x, e.y));

                // turn off default too
                treeComponentConflicts.setToolTipText("");

                // test if we want to highlight it
                if (item != null && item.getData("local") != null) {
                    // make sure it's not already highlighted
                    if (!item.equals(previousItem)) {
                        // unhighlight any previously highlighted item
                        if (previousItem != null) {
                            previousItem.setBackground(previousBgColor);
                            previousItem.setForeground(previousFgColor);
                        }
                        // highlight the new item
                        previousBgColor = item.getBackground();
                        previousFgColor = item.getForeground();
                        previousItem = item;
                        item.setBackground(highlightBgColor);
                        item.setForeground(highlightFgColor);

                        treeComponentConflicts.setCursor(handCursor);
                    }
                }
                // item is null or we don't want to highlight this item
                else {
                    // remove highlighting
                    if (previousItem != null) {
                        previousItem.setBackground(previousBgColor);
                        previousItem.setForeground(previousFgColor);
                        previousItem = null;
                    }

                    treeComponentConflicts.setCursor(null);

                }
            }
        });
    }

    protected Tree getTreeComponentConflicts() {
        return treeComponentConflicts;
    }

    protected boolean compareResultOK(CompareEditorInput input, IRunnableContext context) {
        final Shell shell = getShell();
        try {

            // run operation in separate thread and make it cancelable
            if (context == null) context = PlatformUI.getWorkbench().getProgressService();
            context.run(true, true, input);

            String message = input.getMessage();
            if (message != null) {
                MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), message); //$NON-NLS-1$
                return false;
            }

            if (input.getCompareResult() == null) {
                MessageDialog
                        .openInformation(
                                shell,
                                Utilities.getString("CompareUIPlugin.dialogTitle"), Utilities.getString("CompareUIPlugin.noDifferences")); //$NON-NLS-2$ //$NON-NLS-1$
                return false;
            }

            return true;

        } catch (InterruptedException x) {
            // canceled by user
        } catch (InvocationTargetException x) {
            MessageDialog.openError(shell,
                    Utilities.getString("CompareUIPlugin.compareFailed"), x.getTargetException().getMessage()); //$NON-NLS-1$
        }
        return false;
    }

    // encapsulates local and remote components to be compared
    class ComponentCompareItem implements IStreamContentAccessor, ITypedElement {

        private final Component component;

        public ComponentCompareItem(Component component) {
            super();
            this.component = component;
        }

        @Override
        public InputStream getContents() throws CoreException {
            return new ByteArrayInputStream(StringUtils.defaultString(component.getBody()).getBytes());
        }

        @Override
        public Image getImage() {
            return null;
        }

        @Override
        public String getName() {
            return StringUtils.defaultString(component.getFileName());
        }

        @Override
        public String getType() {
            return StringUtils.defaultString(component.getComponentType());
        }

    }

    // loads compare/upgrade conflict tree nodes
    public void loadUpgradeableComponentsTree(Map<String, List<UpgradeConflict>> upgradeConflicts) {
        getTreeComponentConflicts().removeAll();

        if (Utils.isEmpty(upgradeConflicts)) {
            lblIntro.setText(UpgradeMessages.getString("UpgradeWizard.ConflictsPage.NoContent.message"));
            gdTree.exclude = true;
            getTreeComponentConflicts().setVisible(false);
        } else {
            populateTree(upgradeConflicts);
            // expand tree
            expandAll(getTreeComponentConflicts().getItems());
        }
    }

    protected void populateTree(Map<String, List<UpgradeConflict>> upgradeConflicts) {
        TreeSet<String> sortedComponentTypes = new TreeSet<>();
        sortedComponentTypes.addAll(upgradeConflicts.keySet());

        // loop thru sorted list creating upgrade component tree
        for (String componentType : sortedComponentTypes) {
            // create root component type node
            TreeItem componentTypeTreeItem = new TreeItem(getTreeComponentConflicts(), SWT.NONE);
            componentTypeTreeItem.setText(componentType);
            FontRegistry registry = new FontRegistry();
            Font boldFont = registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());

            componentTypeTreeItem.setFont(boldFont);
            componentTypeTreeItem.setExpanded(true);

            // loop thru each component instance and create named node
            List<UpgradeConflict> upgradeComponents = upgradeConflicts.get(componentType);
            for (UpgradeConflict upgradeConflict : upgradeComponents) {
                TreeItem componentTreeItem = new TreeItem(componentTypeTreeItem, SWT.NONE);
                componentTreeItem.setText(upgradeConflict.getLocalComponent().getFileName());
                componentTreeItem.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
                componentTreeItem.setData("local", upgradeConflict.getLocalComponent());
                componentTreeItem.setData("remote", upgradeConflict.getRemoteComponent());
                componentTreeItem.setExpanded(true);
            }
        }
    }

    private void expandAll(TreeItem[] treeItems) {
        for (TreeItem tmpTreeItem : treeItems) {
            tmpTreeItem.setExpanded(true);
            if (tmpTreeItem.getItemCount() > 0) {
                expandAll(tmpTreeItem.getItems());
            }
        }
    }

    @Override
    public void validateUserInput() {

    }

}
