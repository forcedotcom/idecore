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
package com.salesforce.ide.core.internal.components.layout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;
import com.salesforce.ide.core.services.ServiceException;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;

public class LayoutComponentController extends ComponentController {
    private static final Logger logger = Logger.getLogger(LayoutComponentController.class);

    public LayoutComponentController() throws ForceProjectException {
        super(new LayoutModel());
    }

    /**
     * For layout, before save to f/s and deploy to server. We retrieve default layout from target object(standard and
     * custome objects) and use it's content as template for new-created layout.
     * @throws InvocationTargetException 
     */
    @Override
    protected void preSaveProcess(ComponentModel layoutWizardModel, IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        Component layout = layoutWizardModel.getComponent();
        Component defaultLayout = getDefaultLayout(layout);
        defaultLayout.setPackageName(layout.getPackageName());

        ComponentList components = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentListInstance();
        components.add(defaultLayout);
        ProjectPackageList projectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        projectPackageList.setProject(layoutWizardModel.getProject());
        projectPackageList.addComponents(components, false);

        RetrieveResultExt retrieveResultHandler;
        try {
            monitorWorkCheck(monitor, "Retrieving default layout '" + defaultLayout.getName()
                    + "' to project as template...");
            retrieveResultHandler = ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveSelective(projectPackageList, monitor);

            monitor.worked(1);

            projectPackageList.generateComponents(retrieveResultHandler.getZipFile(), retrieveResultHandler
                    .getFileMetadataHandler(), monitor);
        } catch (RuntimeException | ForceConnectionException | ForceRemoteException | ServiceException | IOException e) {
            logger.error("Exception happened when trying to retrieve default layout component", e);
            throw new InvocationTargetException(e);
        }

        String body = projectPackageList.getComponentByFilePath(defaultLayout.getMetadataFilePath()).getBody();
        if (body != null) {
            // TODO: add notification window: The template from default layout is not found, apply blank template to +
            // layout.getName()
            layout.initNewBody(body);
        }
    }

    private Component getDefaultLayout(Component layout) throws InvocationTargetException {
        Component defaulLayout = null;
        defaulLayout = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentByComponentType(Constants.LAYOUT);
        String objectAPIName = layout.getName().substring(0, layout.getName().indexOf("-"));
        // need to construct the object label because that's used in the default layout name.
        String objectLabel = null;

        //get the object from the cache based on the object api name
        DescribeObjectRegistry describeObjectRegistry = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry();
        DescribeSObjectResult cachedDescribeSObject = null;
        try {
            cachedDescribeSObject =
                    describeObjectRegistry.getCachedDescribeSObjectByApiName(getComponentWizardModel().getProject()
                            .getName(), objectAPIName);
        } catch (Exception e) {
            logger.error("Unable to get object given the object name: " + e.getMessage());
            throw new InvocationTargetException(e);
        }
        //get the label
        String tempobjectLabel = Utils.isNotEmpty(cachedDescribeSObject) ? cachedDescribeSObject.getLabel() : null;

        //set the object label
        objectLabel =
                (Utils.isNotEmpty(tempobjectLabel) && cachedDescribeSObject.getCustom()) ? tempobjectLabel
                        : objectAPIName;

        String defaultLayoutName = objectAPIName + "-" + objectLabel + " " + Constants.LAYOUT;
        defaulLayout.setName(defaultLayoutName);

        StringBuffer filePath = new StringBuffer(layout.getDefaultFolder());
        filePath.append(Constants.FOWARD_SLASH).append(defaultLayoutName).append(Constants.DOT).append(
            layout.getFileExtension());
        defaulLayout.setFilePath(filePath.toString());
        return defaulLayout;
    }

    /**
     * get layoutable objects and remove prepended namespace because naming convention is different between partner api
     * and md api.
     * @throws ForceRemoteException 
     * 
     * @throws ToolkitConnectionException
     * @throws ToolkitRemoteException
     * @throws InterruptedException
     */
    @Override
    public SortedSet<String> getObjectNames(boolean refresh) throws ForceConnectionException, ForceRemoteException {
        IProject project = getComponentWizardModel().getProject();

        if (project == null) {
            return null;
        }

        String namespaceTobeStrip = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getNamespacePrefix(project);
        DescribeObjectRegistry describeObjectRegistry = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry();

        return describeObjectRegistry.getCachedLayoutableDescribeTypes(project, refresh, namespaceTobeStrip);
    }
}
