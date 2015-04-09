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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.internal.resources.LocationValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Allows the user to rename the classes generated from wsdl2apex
 * 
 * @author kevin.ren
 * 
 */
@SuppressWarnings("restriction")
public class Wsdl2apexWizardRenameClasses extends DynamicWizardPage {

    private Composite container = null;
    private ArrayList<Text> allText = null;
    private ScrolledComposite sc;
    private ArrayList<Label> allLabels = null;

    public Wsdl2apexWizardRenameClasses() {
        super("createFile");
        setTitle("Create file");
        setDescription("Create the Apex class");
    }

    public ArrayList<Text> getAllText() {
        return allText;
    }

    /**
     * Loads all of the page widgets that are dependent on the parsing of the wsdl The namespaces used are label widgets
     * and the suggested names are in text widgets
     */

    @Override
    public void onEnterPage() {
        setErrorMessage(null);
        setPageComplete(true);
        final WSDL2ApexWizard w = (WSDL2ApexWizard) this.getWizard();
        if (allText != null) //gets rid of all the old text widgets if there are any
        {
            for (Text t : allText) {
                t.dispose();
            }
            allText.clear();
        }
        if (allLabels != null) //get rid of all old labels
        {
            for (Label l : allLabels) {
                l.dispose();
            }
            allLabels.clear();
        }

        final HashMap<String, String> allClassNames = w.getApexGenerator().getResultFromParse();
        allText = new ArrayList<>();
        allLabels = new ArrayList<>();
        //generates the labels and widgets
        for (String classNS : allClassNames.keySet()) {
            Label label = new Label(container, SWT.NONE);
            GridData gridData2 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            label.setLayoutData(gridData2);
            label.setText(classNS);
            allLabels.add(label);

            Text t = new Text(container, SWT.BORDER);
            t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            t.setText(allClassNames.get(classNS));
            allText.add(t);
            t.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (allText.contains(e.getSource())) {
                        int replaceIndex = allText.indexOf(e.getSource());
                        String changedText = allText.get(replaceIndex).getText();
                        allClassNames.put(allLabels.get(replaceIndex).getText(), allText.get(replaceIndex).getText()); //puts in the new class name
                        if (!checkInput(changedText)) //check if name is valid
                            setPageComplete(false);
                        else {
                            setErrorMessage(null);
                            setPageComplete(true);
                        }
                    }
                }
            });
        }
        sc.layout();
        container.pack();
        container.layout();
        sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public Boolean checkInput(String newString) {
        int i = 0;
        for (Text checkText : allText) //make sure all of the classes generated don't have the same name
        {
            for (Text checkOn : allText) {
                if (checkText.getText().equals(checkOn.getText())) {
                    i++;
                }
            }
            if (i > 1) {
                setErrorMessage("Classes cannot have the same name");
                return false;
            }
            i = 0;
        }
        LocationValidator v = new LocationValidator(null);
        for (Text field : allText) {
            String textField = field.getText();
            if (Utils.isEmpty(textField)) {
                setErrorMessage("Name cannot be empty");
                return false;
            }
            if (textField.length() > 40) {
                setErrorMessage("Name length cannot exceed 40");
                return false;
            }
            if (!Utils.isAlphaNumericValid(textField)) {
                setErrorMessage("Name must be alphanumeric");
                return false;
            }
            if (Utils.startsWithNumeric(textField)) {
                setErrorMessage("Name must not start with number");
                return false;
            }
            String fileName = textField + ".cls";
            String metaName = textField + ".cls-meta.xml";
            IStatus status = v.validateName(fileName, 0); //type didn't seem to matter, so just used 0
            if (status.getCode() != IStatus.OK) {
                setErrorMessage("Creating a file name of " + fileName + " is invalid because: " + status.getMessage());
                return false;
            }
            status = v.validateName(metaName, 0); //type didn't seem to matter, so just used 0
            if (status.getCode() != IStatus.OK) {
                setErrorMessage("Creating a file name of" + metaName + "is invalid because: " + status.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public void createControl(Composite parent) {
        final Composite rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayout(GridLayoutFactory.fillDefaults().create());

        sc = new ScrolledComposite(rootComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        sc.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        container = new Composite(sc, SWT.NULL);
        container.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());

        Label instructions = new Label(container, SWT.NONE);
        final GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        instructions.setLayoutData(gridData);
        instructions
                .setText("The following are the namespaces parsed from the WSDL and their corresponding Apex class names.\n"
                        + "If desired, you can rename these classes.");

        sc.setContent(container);
        sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        setControl(rootComposite);
    }
}
