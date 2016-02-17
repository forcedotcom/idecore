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
package com.salesforce.ide.ui.internal.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Common user interface utility methods.
 * 
 * @author cwall
 */
public class UIUtils {

    private static final Logger logger = Logger.getLogger(UIUtils.class);

    public static void setHelpContext(Shell shell, String contextId) {
        // set context help for page
        String tmpId = Constants.DOCUMENTATION_PLUGIN_PREFIX + "." + contextId;
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, contextId);
        if (logger.isDebugEnabled()) {
            logger.debug("Set help content on " + shell.toString() + " with id '" + tmpId + "'");
        }
    }

    public static void setHelpContext(Control control, String contextId) {
        // set context help for page
        String tmpId = Constants.DOCUMENTATION_PLUGIN_PREFIX + "." + contextId;
        PlatformUI.getWorkbench().getHelpSystem().setHelp(control, tmpId);
        if (logger.isDebugEnabled()) {
            logger.debug("Set help content on " + control.toString() + " with id '" + tmpId + "'");
        }
    }

    public static void setHelpContext(Composite composite, String contextId) {
        String tmpId = Constants.DOCUMENTATION_PLUGIN_PREFIX + "." + contextId;
        // set context help for page
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, tmpId);
        if (logger.isDebugEnabled()) {
            logger.debug("Set help content on " + composite.toString() + " with id '" + tmpId + "'");
        }
    }

    public static String getHelpUrl(String contextId) {
        if (contextId != null) {
            if (contextId.lastIndexOf(Constants.DOT) == -1) {
                contextId = Constants.DOCUMENTATION_PLUGIN_PREFIX + Constants.DOT + contextId;
            }

            IContext c = HelpSystem.getContext(contextId);
            if (c != null) {
                IHelpResource[] topics = c.getRelatedTopics();

                if (!Utils.isEmpty(topics)) {
                    return topics[0].getHref();
                }
            }
        }

        return null;
    }

    public static void displayExternalHelp(String contextId) {
        String helpUrl = getHelpUrl(contextId);

        if (helpUrl != null) {
            PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
        }
    }

    public static void displayHelp(String contextId) {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(Constants.DOCUMENTATION_PLUGIN_PREFIX + "." + contextId);
    }

    public static void placeDialogInCenter(Shell parent, Shell shell) {
        if (parent == null) {
            parent = Display.getDefault().getActiveShell();
        }

        if (parent == null) {
            logger.warn("Unable to center shell - parent shell is null");
            return;
        }

        Rectangle parentSize = parent.getBounds();
        Rectangle mySize = shell.getBounds();

        int locationX, locationY;
        locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
        locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;

        shell.setLocation(new Point(locationX, locationY));
    }

    public static Rectangle getClientArea(Shell shell) {
        if (shell != null) {
            return shell.getClientArea();
        } else if (Display.getDefault() != null && Display.getDefault().getActiveShell() != null) {
            return Display.getDefault().getActiveShell().getClientArea();
        } else if (Display.getDefault() != null) {
            return Display.getDefault().getBounds();
        } else {
            return new Rectangle(100, 100, 50, 50);
        }
    }

    // create standard size and layout for given button, ie "ok" and "cancel" buttons are same size
    public static void setDefaultButtonLayoutData(Button button) {
        GC gc = new GC(button);
        try {
            gc.setFont(button.getFont());
            FontMetrics fontMetrics = gc.getFontMetrics();
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
            Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            data.widthHint = Math.max(widthHint, minSize.x);
            button.setLayoutData(data);
        } finally {
            gc.dispose();
        }

    }

    public static void addPerspectiveListener(final PerspectiveAdapter perspectivListener) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window != null) {
                    window.addPerspectiveListener(perspectivListener);
                }
            }
        });
    }

    public static Object[] getStylizedSummary(List<String> summaries) {
        StringBuffer strBuff = new StringBuffer();
        StyleRange[] ranges = new StyleRange[summaries.size()];
        int strStart = 0;
        for (int i = 0; i < summaries.size(); i++) {
            String tmpSummary = summaries.get(i);
            strBuff.append(tmpSummary);
            int end = 0;
            String subscribePart = Constants.EMPTY_STRING + Messages.getString("ProjectCreateWizard.ProjectContent.ContentSummary.Subscribe.Part.message");
            end = determineBoldTextOffset(tmpSummary, subscribePart);

            ranges[i] = new StyleRange(strStart, end, null, null, SWT.BOLD);
            strStart += tmpSummary.length();
        }
        return new Object[] { strBuff.toString(), ranges };
    }

    private static int determineBoldTextOffset(String tmpSummary, String subscribePart) {
        int end;
        
        if (tmpSummary.indexOf(subscribePart) > 0) {
            end = tmpSummary.indexOf(" (");
        } else {
            end = tmpSummary.indexOf(Constants.NEW_LINE);
        }
        
        if(end < 0) { // indexOf returns -1 if it doesn't find something, we should safeguard
            end = 0;
        }
        return end;
    }

    public static int convertHeightInCharsToPixels(Composite composite, int chars) {
        GC gc = new GC(composite.getFont().getDevice());
        gc.setFont(composite.getFont());
        FontMetrics fFontMetrics = gc.getFontMetrics();
        gc.dispose();
        return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }
}
