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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import com.salesforce.ide.ui.internal.ForceImages;

public class DeploymentResultsComposite extends BaseDeploymentComposite {

    private Tree treeResults = null;
    private Button btnViewLogs = null;
    private Label lblTreeDescription = null;
    String deployLog = null;
    String debugLog = null;
    String deployFilePrefix = null;
    LogViewShell logShellView;
    private Label lblResult;
    private Label lblReason;
    private Label lblResultImage;

    Font font = null;

    public DeploymentResultsComposite(Composite parent, int style) {
        super(parent, style);
        font = new Font(this.getDisplay(), "Arial", 10, SWT.BOLD);

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
            }
        });

        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(4, false));

        lblResultImage = new Label(this, SWT.NONE);
        lblResultImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        lblResultImage.setImage(ForceImages.get(ForceImages.IMAGE_FAILURE));

        lblResult = new Label(this, SWT.NONE);
        lblResult.setText("<result here>");
        lblResult.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 3, 0));
        lblResult.setFont(font);

        lblReason = new Label(this, SWT.WRAP);
        lblReason.setText("<reason here>");
        lblReason.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));

        Label filler = new Label(this, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));

        Label separator = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 0));

        lblTreeDescription = new Label(this, SWT.WRAP);
        lblTreeDescription
                .setText("The following shows failures and warnings, if applicable, and code coverage details.");
        lblTreeDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 0));

        // results tree
        treeResults = new Tree(this, SWT.BORDER);
        treeResults.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 7));

        // deploy log
        btnViewLogs = new Button(this, SWT.NONE);
        btnViewLogs.setText("View Logs");
        btnViewLogs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        btnViewLogs.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (logShellView != null) {
                    logShellView.open();
                }
            }
        });
    }

    public String getDebugLog() {
        return debugLog;
    }

    public void setDebugLog(String debugLog) {
        this.debugLog = debugLog;
    }

    public String getDeploymentLog() {
        return deployLog;
    }

    public void setDeploymentLog(String deploymentLog) {
        this.deployLog = deploymentLog;
    }

    public Tree getTreeResults() {
        return treeResults;
    }

    public void setTreeResults(Tree treeResults) {
        this.treeResults = treeResults;
    }

    public String getDeployFilePrefix() {
        return deployFilePrefix;
    }

    public void setDeployFilePrefix(String deployFilePrefix) {
        this.deployFilePrefix = deployFilePrefix;
    }

    public void setLblResult(boolean success) {
        if (success) {
            this.lblResult.setText("SUCCESS");
            this.lblResultImage.setImage(ForceImages.get(ForceImages.IMAGE_SUCCESS));
            this.lblResult.setForeground(super.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
        } else {
            this.lblResult.setText("FAILED");
            this.lblResultImage.setImage(ForceImages.get(ForceImages.IMAGE_FAILURE));
            this.lblResult.setForeground(super.getDisplay().getSystemColor(SWT.COLOR_RED));
        }
        this.lblResultImage.pack(true);
        this.lblResult.pack(true);
    }

    public void setLblReason(String reason) {
        this.lblReason.setText(reason);
        this.lblReason.pack(true);
    }

    public LogViewShell getLogShellView() {
        return logShellView;
    }

    public void setLogShellView(LogViewShell logShellView) {
        this.logShellView = logShellView;
    }

}
