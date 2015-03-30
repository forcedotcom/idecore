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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;

public class DeploymentArchiveComposite extends BaseDeploymentComposite {

    Button chkCreateProject = null;
    Text txtProjectName = null;
    private Label lblCreateArchives = null;
    Button chkSourceArchive = null;
    private Label lblSourceDirectory = null;
    Text txtSourceDirectory = null;
    Button btnSourceDirectory = null;
    Button chkDestinationArchive = null;
    private Label lblDestinationDirectory = null;
    Button btnDestinationDirectory = null;
    Text txtDestinationDirectory = null;
    private DeploymentArchivePage archivePage = null;
    private Group grpArchive = null;

    public DeploymentArchiveComposite(Composite parent, int style, DeploymentArchivePage archivePage) {
        super(parent, style);
        initialize();
        this.archivePage = archivePage;
    }

    private void initialize() {
        setLayout(new GridLayout(1, true));
        createGrpArchives();
        pack();
    }

    // This method initializes grpArchives
    private void createGrpArchives() {
        grpArchive  = new Group(this, SWT.NONE);
        grpArchive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        grpArchive.setLayout(new GridLayout(7, true));
        grpArchive.setText("Archives");

        lblCreateArchives = new Label(grpArchive, SWT.WRAP);
        lblCreateArchives.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 0));
        lblCreateArchives.setText(DeploymentMessages.getString("DeploymentWizard.ArchiveComposite.CreateArchive.description"));

        // source archive
        chkSourceArchive = new Button(grpArchive, SWT.CHECK);
        chkSourceArchive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 0));
        chkSourceArchive.setText("Project archive");
        chkSourceArchive.setSelection(false);
        chkSourceArchive.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (!chkSourceArchive.getSelection()) {
                    txtSourceDirectory.setText("");
                }
                txtSourceDirectory.setEnabled(chkSourceArchive.getSelection());
                btnSourceDirectory.setEnabled(chkSourceArchive.getSelection());
                validateUserInput();
            }
        });

        lblSourceDirectory = new Label(grpArchive, SWT.NONE);
        lblSourceDirectory.setText("Directory: ");
        lblSourceDirectory.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        txtSourceDirectory = new Text(grpArchive, SWT.BORDER);
        txtSourceDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));
        txtSourceDirectory.setEnabled(chkSourceArchive.getSelection());
        addValidateModifyListener(txtSourceDirectory);

        btnSourceDirectory = new Button(grpArchive, SWT.NONE);
        btnSourceDirectory.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        btnSourceDirectory.setText("Browse...");
        btnSourceDirectory.setEnabled(chkSourceArchive.getSelection());
        btnSourceDirectory.addSelectionListener(new DirectoryBrowser(txtSourceDirectory));

        // destination archive
        chkDestinationArchive = new Button(grpArchive, SWT.CHECK);
        chkDestinationArchive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 0));
        chkDestinationArchive.setText("Destination archive");
        chkDestinationArchive.setSelection(true);
        chkDestinationArchive.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (!chkDestinationArchive.getSelection()) {
                    txtDestinationDirectory.setText("");
                }
                txtDestinationDirectory.setEnabled(chkDestinationArchive.getSelection());
                btnDestinationDirectory.setEnabled(chkDestinationArchive.getSelection());
                validateUserInput();
            }
        });

        lblDestinationDirectory = new Label(grpArchive, SWT.NONE);
        lblDestinationDirectory.setText("Directory: ");
        lblDestinationDirectory.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        txtDestinationDirectory = new Text(grpArchive, SWT.BORDER);
        txtDestinationDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 0));
        txtDestinationDirectory.setEnabled(chkDestinationArchive.getSelection());
        addValidateModifyListener(txtDestinationDirectory);

        btnDestinationDirectory = new Button(grpArchive, SWT.NONE);
        btnDestinationDirectory.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        btnDestinationDirectory.setText("Browse...");
        btnDestinationDirectory.setEnabled(chkDestinationArchive.getSelection());
        btnDestinationDirectory.addSelectionListener(new DirectoryBrowser(txtDestinationDirectory));
    }

    class DirectoryBrowser extends SelectionAdapter {
        // The Text this browser is tied to
        private final Text text;

        /**
         * DirectoryBrowser constructor
         *
         * @param text
         */
        public DirectoryBrowser(Text text) {
          this.text = text;
        }

        /**
         * Called when the browse button is pushed
         *
         * @param event
         *            the generated event
         */
        @Override
        public void widgetSelected(SelectionEvent event) {
          DirectoryDialog dlg = new DirectoryDialog(Display.getCurrent().getActiveShell());
          dlg.setFilterPath(text.getText());
          String dir = dlg.open();
          if (dir != null) {
            text.setText(dir);
          }
        }
      }

    @Override
    public void validateUserInput() {
        archivePage.validateUserInput();
    }

    public DeploymentArchivePage getArchivePage() {
        return archivePage;
    }

    public void setArchivePage(DeploymentArchivePage archivePage) {
        this.archivePage = archivePage;
    }

    public Button getChkCreateProject() {
        return chkCreateProject;
    }

    public void setChkCreateProject(Button chkCreateProject) {
        this.chkCreateProject = chkCreateProject;
    }

    public Button getChkDestinationArchive() {
        return chkDestinationArchive;
    }

    public void setChkDestinationArchive(Button chkDestinationArchive) {
        this.chkDestinationArchive = chkDestinationArchive;
    }

    public Button getChkSourceArchive() {
        return chkSourceArchive;
    }

    public void setChkSourceArchive(Button chkSourceArchive) {
        this.chkSourceArchive = chkSourceArchive;
    }

    public Text getTxtDestinationDirectory() {
        return txtDestinationDirectory;
    }

    public void setTxtDestinationDirectory(Text txtDestinationDirectory) {
        this.txtDestinationDirectory = txtDestinationDirectory;
    }

    public void setTxtDestinationDirectory(String txtDestinationDirectory) {
        this.txtDestinationDirectory.setText(txtDestinationDirectory);
    }


    public Text getTxtProjectName() {
        return txtProjectName;
    }

    public void setTxtProjectName(Text txtProjectName) {
        this.txtProjectName = txtProjectName;
    }

    public Text getTxtSourceDirectory() {
        return txtSourceDirectory;
    }

    public void setTxtSourceDirectory(Text txtSourceDirectory) {
        this.txtSourceDirectory = txtSourceDirectory;
    }

    public void setTxtSourceDirectory(String txtSourceDirectory) {
        this.txtSourceDirectory.setText(txtSourceDirectory);
    }
}
