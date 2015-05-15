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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.upgrade.internal.UpgradeConflict;
import com.salesforce.ide.upgrade.internal.UpgradeController;

public class UpgradeComponentConflictsCompositeTest_unit extends TestCase {

    private UpgradeComponentConflictsComposite conflictsComposite;    
    private Composite parent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parent = new Composite(new Shell(), 0);
        
        this.conflictsComposite = new UpgradeComponentConflictsComposite(parent, 0, mock(UpgradeController.class));
    }
    
    @Override
    protected void tearDown() throws Exception {
        parent.dispose();
        super.tearDown();
    }
    
    public void testpopulateTree() throws Exception {
        conflictsComposite.setTreeComponentConflicts(new Tree(conflictsComposite, SWT.VERTICAL));
        assertNotNull(conflictsComposite);
        
        Map<String, List<UpgradeConflict>> upgradeConflicts = new HashMap<String, List<UpgradeConflict>>();
        List<UpgradeConflict> conflicts = new ArrayList<UpgradeConflict>();
        final Component mockLocalComponent = mock(Component.class);
        when(mockLocalComponent.getFileName()).thenReturn("fileNameWithExt");
        final Component mockRemoteComponent = mock(Component.class);
        conflicts.add(new UpgradeConflict(mockLocalComponent, mockRemoteComponent));
        upgradeConflicts.put("foo", conflicts);
        
        conflictsComposite.populateTree(upgradeConflicts);
        final TreeItem[] items = conflictsComposite.getTreeComponentConflicts().getItems();
        assertNotNull(items);
        assertEquals(1, items.length);
        final TreeItem treeItem = items[0];
        assertEquals("foo", treeItem.getText());
        final TreeItem[] treeItemChildren = treeItem.getItems();
        assertNotNull(treeItemChildren);
        assertEquals(1, treeItemChildren.length);
        assertEquals("fileNameWithExt", treeItemChildren[0].getText());
    }
    
    public void testComponentCompareItem_ReturnsFileName_WhenGetName() throws Exception {
        final Component component = mock(Component.class);
        final String someFileName = "someFileName";
        when(component.getFileName()).thenReturn(someFileName);
        assertEquals(someFileName, conflictsComposite.new ComponentCompareItem(component).getName());
        verify(component, times(1)).getFileName();
    }
}
