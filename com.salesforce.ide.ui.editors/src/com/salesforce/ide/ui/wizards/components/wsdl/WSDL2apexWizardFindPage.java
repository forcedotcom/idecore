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
package com.salesforce.ide.ui.wizards.components.wsdl;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.core.model.ProjectPackageList;

/**
 * Allows the user to choose a wsdl to parse
 * 
 * @author kevin.ren
 * 
 */
public class WSDL2apexWizardFindPage extends DynamicWizardPage {

    private Text wsdlFileField;
    private String wsdlFileLocation = null; //please do not access this variable directly.  use its getter and setter methods
    private Boolean asyncTrue = true; //please do not access this variable directly.  Use its getter and setter methods
    private Boolean shouldGenerate = false; //flag for if we need to regenerate the apex class
    private static final Logger logger = Logger.getLogger(ProjectPackageList.class);

    @Override
    public void onEnterPage() {
        //doesn't have anything to dynamically add

    }

    @Override
    public DynamicWizardPage getNextPage() {
        DynamicWizardPage nextPage = super.getNextPage();
        //only needs to generate the next page if the wsdl file location changes
        if (shouldGenerate) {
            shouldGenerate = false;
            String[] s = new String[] { getWsdlFileLocation() };
            try {
                WSDL2ApexWizard w = (WSDL2ApexWizard) this.getWizard();
                w.getApexGenerator().parse(s);
            } catch (Exception e) {
                MessageDialog dialog =
                        new MessageDialog(null, "Error", null, e.getMessage(), MessageDialog.ERROR,
                                new String[] { "Ok" }, 0);
                dialog.open();
                logger.error(e.getMessage());
                return null;
            }
            nextPage.onEnterPage();
            return nextPage;
        }
        return nextPage;
    }

    public void setWsdlFileLocation(String newLocation) {
        if (wsdlFileLocation == null || !wsdlFileLocation.equals(newLocation)) {
            wsdlFileLocation = newLocation;
            shouldGenerate = true;
        }
    }

    public Text getWsdlFileField() {
        return wsdlFileField;
    }

    public void setWsdlFileField(String a) {
        this.wsdlFileField.setText(a);
        this.wsdlFileLocation = a;
        setWsdlFileLocation(a);
    }

    public WSDL2apexWizardFindPage() {
        super("selectFiles");
        setTitle("Select file");
        setDescription("Select the source file");
    }

    public String getWsdlFileLocation() {
        return wsdlFileLocation;
    }

    public Boolean getAsyncTrue() {
        return asyncTrue;
    }

    public void setAsyncTrue(Boolean a) {
        if (!a.equals(asyncTrue)) {
            asyncTrue = a;
            shouldGenerate = true;
        }
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);
        setControl(container);

        setPageComplete(false); //ensures upon entering the page, we cannot continue through the wizard

        final Label label = new Label(container, SWT.NONE);
        final GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        label.setLayoutData(gridData);
        label.setText("Select the WSDL file you would like to convert.");

        final Label wsdlFileLabel = new Label(container, SWT.NONE);
        final GridData gridData2 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        wsdlFileLabel.setLayoutData(gridData2);
        wsdlFileLabel.setText("WSDL File:");

        wsdlFileField = new Text(container, SWT.BORDER);
        wsdlFileField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updatePageComplete();
            }
        });

        wsdlFileField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Button button = new Button(container, SWT.NONE);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                browseForWsdlFile();
            }
        });
        button.setText(" Browse ");
        button.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));

        final Label async = new Label(container, SWT.NONE);
        final GridData asyncGridData = new GridData();
        async.setLayoutData(asyncGridData);
        async.setText("Add Async Class?");

        final Combo combo = new Combo(container, SWT.READ_ONLY);
        combo.setItems(new String[] { "True", "False" });
        combo.setText("True");
        combo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                setAsyncTrue(Boolean.valueOf(combo.getText()));
            }
        });
    }

    /**
     * Makes sure the page is complete by checking if the url is filled correctly
     */
    private void updatePageComplete() {
        setPageComplete(false);
        IPath sourceLoc = getWsdlPath();
        if (sourceLoc == null || !sourceLoc.toFile().exists()) { //must give a valid path
            setMessage(null);
            setErrorMessage("Please select an existing Wsdl file");
            return;
        }
        this.setWsdlFileLocation(sourceLoc.toString());
        setPageComplete(true);
        setErrorMessage(null);
    }

    /**
     * Gives the absolute path of the path entered
     * 
     * @return an absolute path
     */
    public IPath getWsdlPath() {
        String text = wsdlFileField.getText().trim();
        if (text.length() == 0)
            return null;
        IPath path = new Path(text);
        if (!path.isAbsolute())
            path = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(path);
        return path;
    }

    /**
     * sets the text of the path the user selected
     */
    protected void browseForWsdlFile() {
        IPath path = browse(getWsdlPath(), false);
        if (path == null)
            return;
        IPath rootLoc = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        if (rootLoc.isPrefixOf(path))
            path = path.setDevice(null).removeFirstSegments(rootLoc.segmentCount());
        wsdlFileField.setText(path.toString());
    }

    /**
     * Opens a dialog for finding a file
     * 
     * @param path
     * @param mustExist
     * @return the path of the file selected
     */
    private IPath browse(IPath path, boolean mustExist) {
        FileDialog dialog = new FileDialog(getShell(), mustExist ? SWT.OPEN : SWT.SAVE);
        if (path != null) {
            if (path.segmentCount() > 1)
                dialog.setFilterPath(path.removeLastSegments(1).toOSString());
            if (path.segmentCount() > 0)
                dialog.setFileName(path.lastSegment());
        }
        String result = dialog.open();
        if (result == null)
            return null;
        return new Path(result);
    }
}
