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

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.ui.internal.ForceImages;

/**
 *
 * @author dcarroll
 *
 */
public class SchemaTreeLabelProvider extends LabelProvider {

    public SchemaTreeLabelProvider() {}

    @Override
    public String getText(Object obj) {
        return obj.toString();
    }

    public Image getImage(int imageId, String label, Tree tree) {
        SchemaTreeItem obj = new SchemaTreeItem();
        obj.setName(label);
        obj.setImageId(imageId);
        return getImage(obj);
    }

    // TODO: Refactor - remove magic number, is this dead code?
    public Image getImage(SchemaTreeItem obj) {

        String imageKey = ISharedImages.IMG_FIELD_PUBLIC;
        SchemaTreeItem to = obj;
        String nodeLabel = to.getName();
        int id = to.getImageId();

        switch (id) {
        case -1:
            return ForceImages.get(ForceImages.SCHEMA_EMPTY_LIST);
        case 0: // entityIcon
            return ForceImages.get(ForceImages.METHOD_ICON);
        case 1:
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        case 4:
            if ("insert".equals(nodeLabel) || "createable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_INSERT);
            } else if ("delete".equals(nodeLabel) || "deletable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_DELETE);
            } else if ("update".equals(nodeLabel) || "updateable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_UPDATE);
            } else if ("idList".equals(nodeLabel) || "selectable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_ID_LIST);
            } else if ("query".equals(nodeLabel) || "queryable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_QUERY);
            } else if ("search".equals(nodeLabel) || "searchable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_SEARCH);
            } else if ("filter".equals(nodeLabel) || "filterable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_FILTER);
            } else if ("select".equals(nodeLabel) || "selectable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_SELECT);
            } else {
                return ForceImages.get(ForceImages.SCHEMA_ENTITY_ACCESS);
            }
        case 3:
            return ForceImages.get(ForceImages.SCHEMA_FIELDS);
        case 2:
            return ForceImages.get(ForceImages.SCHEMA_FIELD);
        case 11:
            return ForceImages.get(ForceImages.SCHEMA_ATTRIBUTE);
        case 12:
            return ForceImages.get(ForceImages.METHOD_ICON);
        case 14:
            if ("insert".equals(nodeLabel) || "createable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_INSERT);
            } else if ("delete".equals(nodeLabel) || "deleteable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_DELETE);
            } else if ("update".equals(nodeLabel) || "updateable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_UPDATE);
            } else if ("idList".equals(nodeLabel) || "selectable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_ID_LIST);
            } else if ("query".equals(nodeLabel) || "queryable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_QUERY);
            } else if ("search".equals(nodeLabel) || "searchable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_SEARCH);
            } else if ("filter".equals(nodeLabel) || "filterable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_FILTER);
            } else if ("select".equals(nodeLabel) || "selectable".equals(nodeLabel)) {
                return ForceImages.get(ForceImages.SCHEMA_SELECT);
            } else {
                return ForceImages.get(ForceImages.SCHEMA_ENTITY_ACCESS);
            }
        case 16:
            return ForceImages.get(ForceImages.SCHEMA_PICKLIST_ITEM);
        case 100:
            return ForceImages.get(ForceImages.SCHEMA_FILTERABLE);
        case 101:
            return ForceImages.get(ForceImages.SCHEMA_SELECTABLE);
        case 102:
            return ForceImages.get(ForceImages.SCHEMA_REPLICATEABLE);
        case 103:
            return ForceImages.get(ForceImages.SCHEMA_NILLABLE);
        case 104:
            return ForceImages.get(ForceImages.SCHEMA_SEARCHABLE);
        case 105:
            return ForceImages.get(ForceImages.SCHEMA_RETRIEVEABLE);
        case 106:
            return ForceImages.get(ForceImages.SCHEMA_CREATABLE);
        case 107:
            return ForceImages.get(ForceImages.SCHEMA_ACTIVATEABLE);
        case 108:
            return ForceImages.get(ForceImages.SCHEMA_UPDATEABLE);
        case 109:
            return ForceImages.get(ForceImages.SCHEMA_DELETEABLE);
        case 110:
            return ForceImages.get(ForceImages.SCHEMA_QUERYABLE);
        case 111:
            return ForceImages.get(ForceImages.SCHEMA_CUSTOM_FIELD);
        case 112:
            return ForceImages.get(ForceImages.SCHEMA_CUSTOMTABLE);
        case 113:
            return ForceImages.get(ForceImages.SCHEMA_REQUIRED);
        default:
            return ForceImages.get(ForceImages.METHOD_ICON);
        }
    }

}
