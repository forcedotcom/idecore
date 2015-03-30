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

/**
 *
 * Popup dialog for user to review which running user in which dashbaord needs replacing running user before able to proceed deployment.
 *
 * @author fchang
 */
public class ReplaceRunningUserViewShell {

    Shell shell = null;
    ReplaceRunningUserTableComposite replaceRunningUserTableComposite;
    private final ReplaceRunningUserController deployDashboardProcess;

    public ReplaceRunningUserViewShell(Shell shell, ReplaceRunningUserController deployDashboardProcess) {
        super();
        this.shell = new Shell(shell, SWT.SHELL_TRIM);
        this.deployDashboardProcess = deployDashboardProcess;
        replaceRunningUserTableComposite = new ReplaceRunningUserTableComposite(this.shell, SWT.BORDER);
    }

    public void open() {
        createShell();
        shell.open();
        shell.setFocus();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

    /**
     * This method initializes shell
     */
    private void createShell() {
        shell.setText("Replace Running Users View");
        shell.setLayout(new GridLayout(1, true));
        shell.setSize(new Point(375, 400));

        // init replace dashboad table and default replacement running user name in ReplaceRunningUserTableComposite
        replaceRunningUserTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        replaceRunningUserTableComposite.getTblViewer().setInput(
            deployDashboardProcess.getReplaceRunningUserSet().toArray());
        replaceRunningUserTableComposite.getTxtReplaceRunningUser().setText(
            deployDashboardProcess.getDestinationOrgUsername());

        Label filler = new Label(shell, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

        createBarCommands();
    }

    /**
     * This method initializes barCommands
     *
     */
    private void createBarCommands() {
        ToolBar barCommands = new ToolBar(shell, SWT.HORIZONTAL);
        barCommands.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

        ToolItem btnReplace = new ToolItem(barCommands, SWT.PUSH | SWT.BORDER);
        btnReplace.setText("Replace");
        btnReplace.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                String runningUser = replaceRunningUserTableComposite.getTxtReplaceRunningUserString();
                if (deployDashboardProcess.replaceRunningUserWith(runningUser)) {
                    shell.close();
                } else {
                    replaceRunningUserTableComposite.enableErrorMessage();
                }
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        ToolItem btnClose = new ToolItem(barCommands, SWT.PUSH | SWT.BORDER);
        btnClose.setText("Cancel");
        btnClose.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                shell.close();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

}
