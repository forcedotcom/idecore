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
package com.salesforce.ide.schemabrowser.ui.tableviewer;

import javax.xml.namespace.QName;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.salesforce.ide.core.internal.utils.XmlConstants;
import com.sforce.ws.bind.XmlObject;

/**
 *
 * @author dcarroll
 */
public class CellLabelProvider extends LabelProvider implements ITableLabelProvider {

    // TODO: Are these images used?
    // Names of images used to represent checkboxes
    public static final String CHECKED_IMAGE = "checked";
    public static final String UNCHECKED_IMAGE = "unchecked";

    // For the checkbox images
    private static ImageRegistry imageRegistry = new ImageRegistry();

    static String ICON_PATH = "icons/";

    /**
     * Note: An image registry owns all of the image objects registered with it, and automatically disposes of them the
     * SWT Display is disposed.
     */
    static {
        imageRegistry.put(CHECKED_IMAGE, ImageDescriptor.createFromFile(QueryTableViewer.class, ICON_PATH
                + CHECKED_IMAGE + ".gif"));
        imageRegistry.put(UNCHECKED_IMAGE, ImageDescriptor.createFromFile(QueryTableViewer.class, ICON_PATH
                + UNCHECKED_IMAGE + ".gif"));
    }

    /**
     * Returns the image with the given key, or <code>null</code> if not found.
     */
    private Image getImage(boolean isSelected) {
            return imageRegistry.get(isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE);
    }

    /**
     * Element should be an sobject or XmlObject. We want the index of the field in the XmlObject
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        String result = "";
        if (((DataRow) element).getRecord() != null) {
            XmlObject field = (((DataRow) element).getRecord()).get(columnIndex);
            if (field.getXmlType() != null) {
                // This is an sobject or a queryResult
                QName xmlType = field.getXmlType();
                if ("sObject".equals(xmlType.getLocalPart())) {
                    result = (String) field.getChildren(XmlConstants.ELEM_TYPE).next().getValue();
                } else if (xmlType.getLocalPart().equals(XmlConstants.XMLTYPE_QUERY_RESULT)) {
                    result = field.getName().getLocalPart() + "(" + field.getChild("size").getValue() + ")";
                }
            } else {
                result = (String) field.getValue();
            }
        } else {
            DataRow task = (DataRow) element;

            switch (columnIndex) {
            case 0: // COMPLETED_COLUMN
                break;
            case 1:
                result = task.getDescription();
                break;
            case 2:
                result = task.getOwner();
                break;
            case 3:
                result = task.getPercentComplete() + "";
                break;
            default:
                break;
            }
        }
        return result;
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        return (columnIndex == 0) ? // COMPLETED_COLUMN?
        getImage(((DataRow) element).isCompleted())
                : null;
    }

}
