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
package com.salesforce.ide.core.internal.components;

import java.util.Calendar;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.IModel;

public abstract class ComponentModel implements IModel {
    private static final Logger logger = Logger.getLogger(ComponentModel.class);

    protected Display display = null;
    protected IProject project = null;
    protected ISelection selection = null;
    protected ComponentFactory componentFactory = null;
    protected ComponentList componentList = null;
    protected Component component = null;
    protected MetadataExt metadataExt = null;

    //   C O N S T R U C T O R S
    public ComponentModel() {
        super();
    }

    public void initComponent() {
        this.componentList = componentFactory.getComponentListInstance();

        if (component == null) {
            component = componentFactory.getComponentByComponentType(getComponentType());
        }

        component.setName(getDefaultName());
        component.setPackageName(Constants.DEFAULT_PACKAGED_NAME);
        component.initNewBody(component.getDefaultTemplateString());

        metadataExt = component.getDefaultMetadataExtInstance();

        if (logger.isDebugEnabled()) {
            logger.debug("Initialized " + component.getDisplayName() + " model and component controller");
        }
    }

    public abstract String getComponentType();

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public MetadataExt getMetadataExt() {
        return metadataExt;
    }

    public void setMetadataExt(MetadataExt metadataExt) {
        this.metadataExt = metadataExt;
    }

    public ComponentList getComponentList() {
        return componentList;
    }

    public void setComponentList(ComponentList componentList) {
        this.componentList = componentList;
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }

    public void setComponentFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    public String getName() {
        return component.getName();
    }

    public void setFullName(String name) {
        component.setName(name);
        component.encodeName();
    }

    public String getDisplayName() {
        return component.getDisplayName();
    }

    /**
     * Invoked after user input is collected and validated.
     * 
     * @param componentWizardComposite
     * @throws Exception
     * @throws Exception
     */
    /*    public abstract void saveUserInput(IComponentWizardComposite componentWizardComposite)
                throws InstantiationException, IllegalAccessException;*/

    /**
     * Invoked during component creation operation.
     * 
     * @return
     * @throws FactoryException
     * @throws JAXBException
     */
    public final ComponentList getLoadedComponents() throws FactoryException, JAXBException {
        if (component == null || metadataExt == null) {
            throw new IllegalArgumentException("Component and/or metadata cannot be null");
        }

        if (componentList == null) {
            componentList = componentFactory.getComponentListInstance();
        }

        component.setFileName(getName() + "." + component.getFileExtension());
        component.setFilePath(component.getDefaultFolder() + "/" + component.getFileName());

        loadAdditionalComponentAttributes();

        return componentList;
    }

    /**
     * Invoked during component creation operation.
     * 
     * @return
     * @throws FactoryException
     * @throws JAXBException
     */
    public abstract void loadAdditionalComponentAttributes() throws FactoryException, JAXBException;

    protected boolean saveMetadata(Component component) throws JAXBException {
        if (component == null || metadataExt == null) {
            logger.error("Component and/or metadata object cannot be null");
            throw new IllegalArgumentException("Component and/or metadata object cannot be null");
        }

        component.initNewBody(metadataExt.getXMLString());
        return componentList.add(component);
    }

    protected String getDefaultName() {
        StringBuffer strBuff = new StringBuffer();
        return strBuff
            .append("Default_")
            .append(component.getComponentType())
            .append("_Name_")
            .append(Calendar.getInstance().getTimeInMillis())
            .toString();
    }
}
