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
package com.salesforce.ide.ui.widgets;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import com.salesforce.ide.ui.ForceIdeUIPlugin;

public class MultiCheckboxButton extends ManyStateButton {
    public final static String ICONS_PATH = "icons/"; //$NON-NLS-1$

    private static final String MULTICHECK_ACTIONS = ICONS_PATH + "multicheck/"; //$NON-NLS-1$

    private static final ImageDescriptor IMG_ENABLED = create(MULTICHECK_ACTIONS, "checkbox_unchecked_normal.gif"); //$NON-NLS-1$
    private static final ImageDescriptor IMG_DISABLED = create(MULTICHECK_ACTIONS, "checkbox_unchecked_disabled.gif"); //$NON-NLS-1$
    private static final ImageDescriptor IMG_ENABLED_CHECKED =
            create(MULTICHECK_ACTIONS, "checkbox_checked_normal.gif"); //$NON-NLS-1$
    private static final ImageDescriptor IMG_DISABLED_CHECKED =
            create(MULTICHECK_ACTIONS, "checkbox_graycheck_normal.gif"); //$NON-NLS-1$
    private static final ImageDescriptor IMG_ENABLED_SCHROEDINGER =
            create(MULTICHECK_ACTIONS, "checkbox_schrodinger_normal.gif"); //$NON-NLS-1$
    private static final ImageDescriptor IMG_DISABLED_SCHROEDINGER =
            create(MULTICHECK_ACTIONS, "checkbox_schrodinger_normal.gif"); //$NON-NLS-1$

    public static final int DISABLED = 0x00;
    public static final int ENABLED = 0x01;
    public static final int CHECKED = 0x02;
    public static final int SCHROEDINGER = 0x04;

    private static ImageDescriptor create(String prefix, String name) {
        return ImageDescriptor.createFromURL(makeImageURL(prefix, name));
    }

    private static URL makeImageURL(String prefix, String name) {
        String path = "$nl$/" + prefix + name; //$NON-NLS-1$
        return FileLocator.find(ForceIdeUIPlugin.getDefault().getBundle(), new Path(path), null);
    }

    static List<Image> images = new ArrayList<>();
    static {
        images.add(IMG_DISABLED.createImage()); // 0x000
        images.add(IMG_ENABLED.createImage()); // 0x001

        images.add(IMG_DISABLED_CHECKED.createImage()); // 0x010
        images.add(IMG_ENABLED_CHECKED.createImage()); // 0x011

        images.add(IMG_DISABLED_SCHROEDINGER.createImage()); // 0x100
        images.add(IMG_ENABLED_SCHROEDINGER.createImage()); // 0x101

        images.add(IMG_DISABLED_SCHROEDINGER.createImage()); // 0x110
        images.add(IMG_ENABLED_SCHROEDINGER.createImage()); // 0x111
    }

    MouseListener mouseClickListener;
    MouseMoveListener mouseMoveListener;

    private Set<SelectionListener> selectionListeners = new HashSet<>();

    public MultiCheckboxButton(Composite parent, int style) {
        super(parent, style);
        setImages(images);
        state = ENABLED;
    }

    @Override
    protected void init() {
        super.init();
        mouseClickListener = new MultiCheckMouseClickListener();
        mouseMoveListener = new MultiCheckMouseMoveListener();
        addMouseListener(mouseClickListener);
        addMouseMoveListener(mouseMoveListener);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int width = 13, height = 13;

        if (text != null && text.length() > 0) {
            GC gc = new GC(this);
            Point extent = gc.stringExtent(text);
            gc.dispose();
            width += extent.x;
            height = Math.max(height, extent.y);
        }

        if (wHint != SWT.DEFAULT)
            width = wHint;
        if (hHint != SWT.DEFAULT)
            height = hHint;
        return new Point(width, height);
    }

//    public boolean getEnabled() {
//        return (state & ENABLED) == ENABLED;
//    }
//
//    public void setEnabled(boolean enabled) {
//        state = (enabled) ? (state |= ENABLED) : (state &= ~ENABLED);
//        this.redraw();
//    }

    //	public boolean getSelection() {
    //		return (state & CHECKED) == CHECKED;
    //	}
    //
    //	public void setSelection(boolean selected) {
    //		state = (selected) ? (state |= CHECKED) : (state &= ~CHECKED);
    //		this.redraw();
    //	}
    //
    //	public boolean getSchroedinger() {
    //		return (state & SCHROEDINGER) == SCHROEDINGER;
    //	}
    //
    //	public void setSchroedinger(boolean schroedinger) {
    //		state = (schroedinger) ? (state |= SCHROEDINGER) : (state &= ~SCHROEDINGER);
    //		this.redraw();
    //	}

    public static boolean isBlackChecked(int test) {
        return test == (ENABLED | CHECKED);
    }

    public boolean isBlackChecked() {
        return isBlackChecked(state);
    }

    public void setChecked() {
        setState(ENABLED | CHECKED);
    }

    public static int getBlackCheckedState() {
        return (ENABLED | CHECKED);
    }

    public static boolean isUnChecked(int test) {
        return test == (ENABLED);
    }

    public boolean isUnChecked() {
        return isUnChecked(state);
    }

    public void setUnchecked() {
        setState(ENABLED);
    }

    public static int getUnCheckedState() {
        return (ENABLED);
    }

    public static boolean isUnCheckedDisabled(int test) {
        return test == (DISABLED);
    }

    public boolean isUnCheckedDisabled() {
        return isUnChecked(state);
    }

    public void setUnCheckedDisabled() {
        setState(DISABLED);
    }

    public static int getUnCheckedDisabled() {
        return (DISABLED);
    }

    public static boolean isGrayChecked(int test) {
        return test == (CHECKED);
    }

    public boolean isGrayChecked() {
        return isGrayChecked(state);
    }

    public void setGrayChecked() {
        setState(CHECKED);
    }

    public static int getGrayCheckedState() {
        return (CHECKED);
    }

    //	public static boolean isGrayCheckedDisabled(int test)
    //    {
    //        return test == (CHECKED);
    //    }
    //    
    //    public boolean isGrayCheckedDisable()
    //    {
    //        return isGrayChecked(state);
    //    }
    //    
    //    public void setGrayCheckedDisable()
    //    {
    //        setState(CHECKED);
    //    }
    //    
    //    public static int getGrayCheckedStateDisable()
    //    {
    //        return (CHECKED);
    //    }

    public static boolean isSchroedinger(int test) {
        return test == (ENABLED | SCHROEDINGER);
    }

    public boolean isSchroedinger() {
        return isSchroedinger(state);
    }

    public void setSchroedinger() {
        setState(ENABLED | SCHROEDINGER);
    }

    public static int getSchroedingerState() {
        return (ENABLED | SCHROEDINGER);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        redraw();
    }

    private class MultiCheckMouseClickListener implements MouseListener {
        boolean downFlag = false;

        @Override
        public void mouseDoubleClick(MouseEvent e) {

        }

        @Override
        public void mouseDown(MouseEvent e) {
            downFlag = true;
            //			if(getEnabled() && e.button == 1 && getClientArea().contains(e.x, e.y))
            //			{
            //				setEnabled(false);
            //			}
        }

        @Override
        public void mouseUp(MouseEvent e) {
            if (downFlag)// && (isSet(savedState, ENABLED)))
            {
                //				state = savedState;
                if (e.button == 1 && getClientArea().contains(e.x, e.y)) {
                    //					setSelection(!getSelection());
                    //					savedState = state;

                    Event ev = new Event();
                    ev.button = e.button;
                    ev.data = e.data;
                    ev.display = e.display;
                    ev.stateMask = e.stateMask;
                    ev.time = e.time;
                    ev.widget = e.widget;
                    ev.x = e.x;
                    ev.y = e.y;

                    for (SelectionListener listener : selectionListeners) {
                        SelectionEvent event = new SelectionEvent(ev);
                        listener.widgetSelected(event);
                    }
                }

                //				else
                //				{
                //					redraw();
                //				}
            }
            downFlag = false;
        }
    }

    boolean isSet(int source, int flag) {
        return (source & flag) == flag;
    }

    private class MultiCheckMouseMoveListener implements MouseMoveListener {
        @Override
        public void mouseMove(MouseEvent e) {

        //			if(!getClientArea().contains(e.x, e.y))
        //			{
        //				state = savedState;
        //				redraw();
        //			}
        //			
        //			else if(state != savedState)
        //			{
        //				setEnabled(false);
        //			}
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        selectionListeners.remove(listener);
    }
}
