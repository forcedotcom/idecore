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
package com.salesforce.ide.ui.internal.editor.imagesupport;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import com.salesforce.ide.ui.internal.ForceImages;

/**
 * Handle Apex specific image descriptor resolution here and delegate JDT supported functionality. Move
 * JavaElementImageDescriptor's private methods here for reusing.
 * 
 * @author fchang
 * 
 */
public class ApexElementImageDescriptor extends JavaElementImageDescriptor {
    // Flag to render the apex-specific adornment, 'Global', 'WebService', 'with sharing', 'without sharing', and
    // 'testMethod'.
    public final static int GLOBAL = 0x1000000;
    public final static int WEBSERVICE = 0x2000000;
    public static final int WITHSHARING = 0x4000000;
    public static final int WITHOUTSHARING = 0x8000000;
    public static final int TESTMETHOD = 0x10000000;
    public static final int APEX_SYS_CLS = 0x20000000;

    public final static int VIRTUAL = 0x800;
    public final static int TRANSIENT = 0x1000;

    private ImageDescriptor fBaseImage;
    private int fFlags;
//    private Point fSize;

    public ApexElementImageDescriptor(ImageDescriptor baseImage, int flags, Point size) {
        super(baseImage, flags, size);
        fBaseImage = baseImage;
        fFlags = flags;
//        fSize = size;
    }

    /*
     * Method declared in CompositeImageDescriptor
     */
    @Override
    protected void drawCompositeImage(int width, int height) {
        super.drawCompositeImage(width, height);
        ImageData bg = getImageData(fBaseImage);

        drawImage(bg, 0, 0);

        drawTopRight();
        drawBottomRight();
    }

    private static ImageData getImageData(ImageDescriptor descriptor) {
        ImageData data = descriptor.getImageData(); // see bug 51965: getImageData can return null
        if (data == null) {
            data = DEFAULT_IMAGE_DATA;
        }
        return data;
    }

    private void drawTopRight() {
        Point pos = new Point(getSize().x, 0);

        if ((fFlags & VIRTUAL) != 0) {
            addTopRightImage(ForceImages.getDesc(ForceImages.APEX_VIRTUAL_OVERLAY), pos);
        }

        if ((fFlags & WITHSHARING) != 0) {
            addTopRightImage(ForceImages.getDesc(ForceImages.APEX_WITHSHARING_ACCESSOR_OVERLAY), pos);
        }
        if ((fFlags & WITHOUTSHARING) != 0) {
            addTopRightImage(ForceImages.getDesc(ForceImages.APEX_WITHOUTSHARING_ACCESSOR_OVERLAY), pos);
        }
        if ((fFlags & APEX_SYS_CLS) != 0) {
            addTopRightImage(ForceImages.getDesc(ForceImages.APEX_SYS_CLS_OVERLAY), pos);
        }
        if ((fFlags & WEBSERVICE) != 0) {
            // shift top right quadrant to accomondate static overlay icon
            ImageData data = getImageData(JavaPluginImages.DESC_OVR_STATIC);
            int x = pos.x - data.width;
            if (x >= 0) {
                pos.x = x;
            }
            addTopRightImage(ForceImages.getDesc(ForceImages.APEX_WEBSERVICE_ACCESSOR_OVERLAY), pos);
        }
    }

    private void drawBottomRight() {
        Point size = getSize();
        Point pos = new Point(size.x, size.y);

        int flags = fFlags;

        if ((flags & TRANSIENT) != 0) {
            addBottomRightImage(ForceImages.getDesc(ForceImages.APEX_TRANSIENT_OVERLAY), pos);
        }
    }

    private void addTopRightImage(ImageDescriptor desc, Point pos) {
        ImageData data = getImageData(desc);
        int x = pos.x - data.width;
        if (x >= 0) {
            drawImage(data, x, pos.y);
            pos.x = x;
        }
    }

    private void addBottomRightImage(ImageDescriptor desc, Point pos) {
        ImageData data = getImageData(desc);
        int x = pos.x - data.width;
        int y = pos.y - data.height;
        if (x >= 0 && y >= 0) {
            drawImage(data, x, y);
            pos.x = x;
        }
    }
}
