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
package com.salesforce.ide.ui.views.log;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.ForceImages;

/**
 * Opens Force.com IDE log directory in o/s defined f/s utility
 * 
 * @author cwall
 * 
 */
public class OpenLogFolderAction extends Action {

    private static final Logger logger = Logger.getLogger(OpenLogFolderAction.class);

    private static final String WINDOWS_CMD = "explorer.exe";
    private static final String LINUX_A_CMD = "gnome-open";
    private static final String LINUX_B_CMD = "konqueror file:///";
    private static final String MAC_CMD = "open";

    public static final String OPEN_LOG_DIR_TEXT = "Open Force.com IDE Log Folder";
    private final String osName = System.getProperty("os.name");
    private StringBuffer exeCmd = null;

    private File logDir = null;

    public OpenLogFolderAction() {
        IPath path = Platform.getStateLocation(ForceIdeCorePlugin.getDefault().getBundle());
        this.logDir = path.toFile();
        init();
    }

    private void init() {
        setText(OPEN_LOG_DIR_TEXT);
        setImageDescriptor(ForceImages.getDesc(ForceImages.OPEN_FOLDER_ICON));
        setToolTipText(OPEN_LOG_DIR_TEXT);
    }

    @Override
    public void run() {
        if (logDir == null || !logDir.exists() || !logDir.isDirectory() || Utils.isEmpty(osName)) {
            // TODO: pop-up error?
            logger.warn("Unable to open log directory - directory is null or does not exist");
            return;
        }

        exeCmd = new StringBuffer();
        try {
            if (osName.startsWith(("Windows"))) {
                executeCmd(WINDOWS_CMD);
            } else if (osName.indexOf("Linux") != -1) {
                try {
                    executeCmd(LINUX_A_CMD);
                } catch (Exception e) {
                    executeCmd(LINUX_B_CMD);
                }
            } else if (osName.startsWith(("Mac"))) {
                executeCmd(MAC_CMD);
            } else {
                logger.warn("Unable to open log directory - operating system '" + osName + "' not supported");
                Utils.openWarn("Warning", "Unable to open log directory - operating system '" + osName
                        + "' not supported");
            }
        } catch (Exception e) {
            logger.error("Unable to open log directory with command:\n\n'" + exeCmd.toString() + "'", e);
            Utils.openError("Error Opening Directory", "Unable to open log with cmd:\n'" + exeCmd.toString() + "'");
        }
    }

    private void executeCmd(String cmd) throws IOException {
        exeCmd.append(cmd).append(" \"").append(logDir.getAbsolutePath()).append("\"");
        if (logger.isDebugEnabled()) {
            logger.debug("Opening log directory on o/s '" + osName + "' with command: '" + exeCmd.toString() + "\"'");
        }

        Runtime.getRuntime().exec(cmd + " .", null, logDir);
    }
}
