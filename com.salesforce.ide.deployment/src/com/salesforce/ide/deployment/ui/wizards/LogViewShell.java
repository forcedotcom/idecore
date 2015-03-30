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
package com.salesforce.ide.deployment.ui.wizards;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;

public class LogViewShell {

    static final Logger logger = Logger.getLogger(LogViewShell.class);

    Shell shell = null;
    Shell parentShell = null;
    TabFolder tabLogView = null;
    Text txtSummaryArea = null;
    private ToolBar barCommands = null;
    Text txtDebugArea = null;
    private String deployLog = null;
    private String debugLog = null;
    String deployFilePrefix = null;
    Clipboard clipboard = null;
    boolean async = false;

    public LogViewShell(Shell shell, String deployLog, String debugLog, String deployFilePrefix) {
        super();
        this.deployLog = deployLog;
        this.debugLog = debugLog;
        this.deployFilePrefix = deployFilePrefix;
        this.parentShell = shell;
        init();
    }

    private void init() {
        if (shell == null || shell.isDisposed() && (parentShell != null && !parentShell.isDisposed())) {
            shell = new Shell(parentShell, SWT.SHELL_TRIM);
        }
    }

    public void open() {
        createSShell();
        shell.setFocus();
        shell.open();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

    public void close() {
        if (shell != null && !shell.isDisposed()) {
            if (clipboard != null) {
                clipboard.dispose();
            }
            shell.close();
        }
    }

    // initializes sShell
    private void createSShell() {
        init();
        shell.setText("Deployment Log View");
        shell.setLayout(new GridLayout(2, false));
        shell.setSize(new Point(500, 400));
        shell.setData(this);
        createTabLogView();

        createBarCommands();
    }

    // initializes tabLogView
    private void createTabLogView() {
        tabLogView = new TabFolder(shell, SWT.NONE);
        tabLogView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        TabItem itmSummaryLog = new TabItem(tabLogView, SWT.NONE);
        itmSummaryLog.setText("Summary Log");
        txtSummaryArea = new Text(tabLogView, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        txtSummaryArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtSummaryArea.setText(deployLog);
        txtSummaryArea.setEditable(false);
        itmSummaryLog.setControl(txtSummaryArea);

        TabItem itmDebugLog = new TabItem(tabLogView, SWT.NONE);
        itmDebugLog.setText("Debug Log");
        txtDebugArea = new Text(tabLogView, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        txtDebugArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtDebugArea.setText(Utils.isNotEmpty(debugLog) ? debugLog : "n/a");
        txtDebugArea.setEditable(false);
        itmDebugLog.setControl(txtDebugArea);

    }

    // This method initializes barCommands
    private void createBarCommands() {
        barCommands = new ToolBar(shell, SWT.HORIZONTAL);
        barCommands.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
        ToolItem btnCopy = new ToolItem(barCommands, SWT.PUSH | SWT.BORDER);
        btnCopy.setText("Copy to Clipboard");
        btnCopy.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                clipboard = new Clipboard(shell.getDisplay());
                Text txtArea = (Text) tabLogView.getSelection()[0].getControl();
                if (txtArea.getText().length() > 0) {
                    TextTransfer textTransfer = TextTransfer.getInstance();
                    clipboard.setContents(new Object[] { txtArea.getText() }, new Transfer[] { textTransfer });
                }

            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        new ToolItem(barCommands, SWT.SEPARATOR);
        ToolItem btnSave = new ToolItem(barCommands, SWT.PUSH | SWT.BORDER);
        btnSave.setText("Save");
        btnSave.addSelectionListener(new SelectionListener() {
            class SafeSaveDialog {
                // The wrapped FileDialog
                private final FileDialog dialog;

                /**
                 * SafeSaveDialog constructor
                 *
                 * @param shell the parent shell
                 */
                public SafeSaveDialog(Shell shell) {
                    dialog = new FileDialog(shell, SWT.SAVE);
                    dialog.setFilterNames(new String[] { "All Files (*.*)" });
                    dialog.setFilterExtensions(new String[] { "*.log", "*.txt", "*.*" });
                    dialog.setFilterPath("c:\\");
                    dialog.setFileName(deployFilePrefix + "-deploy.log");
                }

                public String open() {
                    // We store the selected file name in fileName
                    String fileName = null;
                    boolean done = false;

                    while (!done) {
                        // Open the File Dialog
                        fileName = dialog.open();
                        if (fileName == null) {
                            // User has cancelled, so quit and return
                            done = true;
                        } else {
                            // User has selected a file; see if it already exists
                            File file = new File(fileName);
                            if (file.exists()) {
                                // The file already exists; asks for confirmation
                                MessageBox mb = new MessageBox(dialog.getParent(), SWT.ICON_WARNING | SWT.YES | SWT.NO);

                                // We really should read this string from a resource bundle
                                mb.setMessage(fileName + " already exists. Do you want to replace it?");

                                // If they click Yes, we're done and we drop out. If they click No, we redisplay the File Dialog
                                done = mb.open() == SWT.YES;
                            } else {
                                // File does not exist, so drop out
                                done = true;
                            }
                        }
                    }
                    return fileName;
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                StringBuffer strBuff = new StringBuffer(txtSummaryArea.getText());
                strBuff.append("\n\n").append(txtDebugArea.getText());

                SafeSaveDialog dialog = new SafeSaveDialog(shell);
                String fileName = null;

                try {
                    fileName = dialog.open();
                } catch (Exception ex) {
                    logger.error("Unable to capture file name.", ex);
                    Utils.openError(ex, true, "Unable to capture file name.");
                    return;
                }

                writeToFile(fileName, strBuff.toString());
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }

            public void writeToFile(String fileName, String textToWrite) {
                if (Utils.isEmpty(fileName) || Utils.isEmpty(textToWrite)) {
                    logger.warn("Unable to write to file - file or text to write are null");
                    return;
                }

                try (final QuietCloseable<BufferedWriter> c = QuietCloseable.make(new BufferedWriter(new FileWriter(fileName)))) {
                    final BufferedWriter buffWriter = c.get();
                    buffWriter.write(textToWrite);
                } catch (IOException e) {
                    logger.error("Unable to write to file [" + fileName + "]", e);
                    return;
                }
            }
        });

        new ToolItem(barCommands, SWT.SEPARATOR);
        ToolItem btnClose = new ToolItem(barCommands, SWT.PUSH | SWT.BORDER);
        btnClose.setText("Close");
        btnClose.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (clipboard != null) {
                    clipboard.dispose();
                }
                shell.setVisible(false);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
