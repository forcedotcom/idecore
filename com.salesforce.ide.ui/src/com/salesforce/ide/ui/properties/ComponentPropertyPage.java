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
package com.salesforce.ide.ui.properties;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class ComponentPropertyPage extends BasePropertyPage {
    private static final Logger logger = Logger.getLogger(ComponentPropertyPage.class);

    private IFile file;
    private ComponentPropertyPageComposite componentPropertyPageComposite;

    public ComponentPropertyPageComposite getComponentPropertyPageComposite() {
        return componentPropertyPageComposite;
    }

    public ComponentPropertyPage() {
        super();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        setHelp();
    }

    protected void setHelp() {
        UIUtils.setHelpContext(getControl(), this.getClass().getSimpleName());
    }

    @Override
    protected Control createContents(Composite parent) {
        file = (IFile) getElement().getAdapter(IResource.class);
        componentPropertyPageComposite = new ComponentPropertyPageComposite(parent, SWT.NONE);
        initialize();
        if (getApplyButton() != null) {
            getApplyButton().setEnabled(false);
        }

        return componentPropertyPageComposite;
    }

    private void initialize() {
        Component component = resolveComponentFromFile();

        if (componentPropertyPageComposite == null || component == null) {
            logger.error("Component property component and/or componet is null");
            Utils.openWarn("Initialize Error",
                    "Unable to initialize component property view.  Component property component and/or componet is null.");
            return;
        }

        componentPropertyPageComposite.setTxtName(component.getName());
        componentPropertyPageComposite.setTxtType(component.getDisplayName());
        componentPropertyPageComposite.setTxtEntityId(component.getId());
        componentPropertyPageComposite.setTxtPackageName(component.getPackageName());
        componentPropertyPageComposite.setTxtManaged(component.getState());
        componentPropertyPageComposite.setTxtCreatedBy(component.getCreatedByName());

        Calendar createdDate = component.getCreatedDate();
        if (createdDate != null) {
            componentPropertyPageComposite.setTxtCreatedDate(createdDate.getTime().toString());
        }

        componentPropertyPageComposite.setTxtFileName(component.getFileName());
        componentPropertyPageComposite.setTxtModifiedBy(component.getLastModifiedByName());

        Calendar lastModifiedDate = component.getLastModifiedDate();
        if (lastModifiedDate != null) {
            componentPropertyPageComposite.setTxtModifiedDate(lastModifiedDate.getTime().toString());
        }

        componentPropertyPageComposite.setTxtNamespacePrefix(component.getNamespacePrefix());
    }

    protected Component resolveComponentFromFile() {
        if (file == null) {
            Utils.openError("Component Properties Not Available", "Component properties are not available for file.");
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Display properties for file '" + file.getProjectRelativePath().toPortableString() + "'");
        }

        Component component = null;
        try {
            component = getComponentFactory().getComponentFromFile(file);
            if (logger.isDebugEnabled()) {
                logger.debug("Display properties for " + component.getFullDisplayName() + " for file '"
                        + file.getProjectRelativePath().toPortableString() + "'");
            }
        } catch (FactoryException e) {
            logger.error("Unable to get component from file '" + file.getProjectRelativePath().toPortableString() + "'", e);
            Utils.openError("Component Properties Not Available", "Component properties are not available for file '"
                    + file.getProjectRelativePath().toPortableString() + "'");
                return null;
        }
        return component;
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }

    @Override
    public boolean okToLeave() {
        return super.okToLeave();
    }

    @Override
    protected void performApply() {
    }

    @Override
    public boolean performOk() {
        return true;
    }

    @Override
    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
    }

    @Override
    public void setMessage(String newMessage, int newType) {
        super.setMessage(newMessage, newType);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void setValid(boolean b) {
        super.setValid(b);
    }
}
