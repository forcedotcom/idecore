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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ResultsViewShell {

    Shell shell = null;
    LogViewShell logShellView;
    DeploymentResultsComposite resultsComposite;
    private ToolBar barCommands;

    public ResultsViewShell(Shell shell, String deployLog, String debugLog, String deployFilePrefix) {
        super();
        //this.shell = new Shell(shell, SWT.SHELL_TRIM | SWT.ON_TOP);
        this.shell = new Shell(shell, SWT.SHELL_TRIM);
        resultsComposite = new DeploymentResultsComposite(this.shell, SWT.BORDER);
    }

    /**
     * This method initializes shell
     */
    private void createShell() {
        shell.setText("Test Deployment Results View");
        shell.setLayout(new GridLayout(1, true));
        shell.setSize(new Point(500, 400));

        resultsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label filler = new Label(shell, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

        createBarCommands();
    }

    /**
     * This method initializes barCommands
     *
     */
    private void createBarCommands() {
        barCommands = new ToolBar(shell, SWT.HORIZONTAL);
        barCommands.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

        ToolItem btnClose = new ToolItem (barCommands, SWT.PUSH | SWT.BORDER);
        btnClose.setText("Close");
        btnClose.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                shell.close();
                if (logShellView != null) {
                    logShellView.close();
                }
            }
            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

    public void open() {
        createShell();
        shell.open();
        shell.setFocus();
        while (!shell.isDisposed ()) {
            if (!shell.getDisplay().readAndDispatch ()) {
                shell.getDisplay().sleep();
            }
        }
    }

    public DeploymentResultsComposite getResultsComposite() {
        return resultsComposite;
    }

    public void setResultsComposite(DeploymentResultsComposite resultsComposite) {
        this.resultsComposite = resultsComposite;
    }

    public LogViewShell getLogShellView() {
        return logShellView;
    }

    public void setLogShellView(LogViewShell logShellView) {
        this.logShellView = logShellView;
    }

}
