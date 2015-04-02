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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.salesforce.ide.core.internal.components.MultiClassComponentController;
import com.salesforce.ide.core.internal.components.apex.clazz.ApexClassModel;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.ui.wizards.components.apex.clazz.ApexClassWizard;
import com.salesforce.ide.ui.wizards.components.apex.clazz.ApexClassWizardPage;
import com.salesforce.ide.wsdl2apex.core.Wsdl2Apex;

/**
 * Wizard to create apex classes from a wsdl
 * 
 * @author kevin.ren
 * 
 */

public class WSDL2ApexWizard extends Wizard implements INewWizard {
    private IStructuredSelection initialSelection;
    private WSDL2apexWizardFindPage convertPage;
    private Wsdl2apexWizardRenameClasses createPage;
    private IWorkbench currentWorkBench;
    private static final Logger logger = Logger.getLogger(ProjectPackageList.class);
    private Wsdl2Apex generator;
    private ProjectPackageList allPackages = null;

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.initialSelection = selection;
        this.currentWorkBench = workbench;
        this.generator = new Wsdl2Apex();
        this.setForcePreviousAndNextButtons(true);
    }

    public WSDL2apexWizardFindPage getConvertPage() {
        return this.convertPage;
    }

    public Wsdl2apexWizardRenameClasses getCreatePage() {
        return this.createPage;
    }

    public String getWsdlFileLocation() {
        return this.convertPage.getWsdlFileLocation();
    }

    public Wsdl2Apex getApexGenerator() {
        return this.generator;
    }

    @Override
    public void addPages() {
        setWindowTitle("Convert WSDL File to Apex");
        this.convertPage = new WSDL2apexWizardFindPage();
        this.createPage = new Wsdl2apexWizardRenameClasses();
        addPage(convertPage);
        addPage(createPage);
    }

    @Override
    public boolean canFinish() {
        if (this.convertPage.getWsdlFileLocation() != null && this.convertPage.getWsdlFileLocation() != ""
                && this.convertPage.getErrorMessage() == null && this.createPage.getErrorMessage() == null) //check if all fields are filled in and completed
        {
            return true;
        } else
            return false;
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    exec();
                }
            });
        } catch (InvocationTargetException e) {
            MessageDialog dialog =
                    new MessageDialog(null, "Error", null, e.getMessage(), MessageDialog.ERROR, new String[] { "Ok" },
                            0);
            dialog.open();
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            MessageDialog dialog =
                    new MessageDialog(null, "Error", null, e.getMessage(), MessageDialog.ERROR, new String[] { "Ok" },
                            0);
            dialog.open();
            logger.error(e.getMessage());
        } catch (Exception e) {
            MessageDialog dialog =
                    new MessageDialog(null, "Error", null, e.getMessage(), MessageDialog.ERROR, new String[] { "Ok" },
                            0);
            dialog.open();
            logger.error(e.getMessage());
        }
        return true;
    }

    public void exec() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                ApexClassWizard a;
                try {
                    String[] arguements = new String[1];
                    arguements[0] = convertPage.getAsyncTrue().toString(); //checks if we want async methods
                    generator.generate(arguements);

                    //we have all of the generated classes now
                    Iterator<String> ii = generator.getAllClasses().iterator();
                    Iterator<String> jj = generator.getAllClassNames().iterator();
                    allPackages =
                            ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                                    .getProjectPackageListInstance();
                    while (ii.hasNext() && jj.hasNext()) //this loop here purely for checking if the class name is already used
                    {
                        String classBody = ii.next().trim();
                        String className = jj.next().trim();
                        a = new ApexClassWizard(true, allPackages);

                        ApexClassWizardPage ap = new ApexClassWizardPage(a);
                        ApexClassModel apexClassModel = (ApexClassModel) ap.getComponentWizardModel();

                        a.init(currentWorkBench, initialSelection);

                        Component apexComponent = apexClassModel.getComponent();
                        apexComponent.setBodyFromTemplateString(classBody);
                        apexComponent.setName(className);
                        if (!ap.getComponentController().isNameUniqueLocalCheck()) {
                            throw new IOException("There is already a class named " + className);
                        }
                    }

                    Iterator<String> i = generator.getAllClasses().iterator();
                    Iterator<String> j = generator.getAllClassNames().iterator();

                    while (i.hasNext() && j.hasNext()) //this loop for creating apex classes by programmatically using the ApexClassWizard
                    {
                        String classBody = i.next();
                        String className = j.next();
                        a = new ApexClassWizard(true, allPackages);

                        ApexClassWizardPage ap = new ApexClassWizardPage(a);
                        ApexClassModel apexClassModel = (ApexClassModel) ap.getComponentWizardModel();

                        a.init(currentWorkBench, initialSelection);

                        Component apexComponent = apexClassModel.getComponent();

                        //set the body and class name of the component
                        apexComponent.setBodyFromTemplateString(classBody);
                        apexComponent.setName(className);

                        if (!i.hasNext() && !j.hasNext()) {
                            //this is the last class we are creating for this wsdl, so we want to save the packagelist to the server
                            MultiClassComponentController c =
                                    (MultiClassComponentController) ap.getComponentController();
                            c.setShouldSaveToServer(true);
                        }

                        if (ap.getComponentController().isNameUniqueLocalCheck()) {
                            a.executeCreateOperation();
                        }
                    }

                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        new MessageDialog(null, "Error", null, e.getMessage(), MessageDialog.ERROR, new String[] { "Ok" }, 0).open();
                    }
                    logger.error(e);
                }
            }
        });
    }
}
