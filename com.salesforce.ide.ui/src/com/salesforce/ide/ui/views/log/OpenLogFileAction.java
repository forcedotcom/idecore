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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.ui.internal.ForceImages;

/**
 * Opens Force.com IDE log in text editor
 * 
 * @author cwall
 * 
 */
public class OpenLogFileAction extends Action {

    private static final Logger logger = Logger.getLogger(OpenLogFileAction.class);

    public static final String OPEN_LOG_FILE_TEXT = "Open Force.com IDE Log";

    private File logFile = null;

    //   C O N S T R U C T O R S
    public OpenLogFileAction() {
        IPath path = Platform.getStateLocation(ForceIdeCorePlugin.getDefault().getBundle());
        path = path.append(File.separator + Constants.LOG_FILE_NAME);
        this.logFile = path.toFile();
        init();
    }

    //   M E T H O D S
    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    private void init() {
        setText(OPEN_LOG_FILE_TEXT);
        setImageDescriptor(ForceImages.getDesc(ForceImages.OPEN_FILE_ICON));
        setToolTipText(OPEN_LOG_FILE_TEXT);
    }

    @Override
    public void run() {
        if (logFile == null || !logFile.exists()) {
            // TODO: pop-up error?
            logger.warn("Unable to open log file - file is null or does not exist");
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    IDE.openEditor(page, logFile.toURI(), org.eclipse.ui.editors.text.EditorsUI.DEFAULT_TEXT_EDITOR_ID,
                        true);
                } catch (Exception ex) {
                    logger.warn("Unable to open file '" + logFile.getAbsolutePath() + "'", ex);
                }
            }
        });
    }

}
