/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.apex.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.salesforce.ide.apex.core.util.ApexVisitorUtil;
import com.salesforce.ide.apex.internal.core.ApexSourceUtils;
import com.salesforce.ide.apex.ui.Messages;
import com.salesforce.ide.apex.ui.views.FilteredApexResourcesSelectionDialog;
import com.salesforce.ide.apex.visitors.OpenTypeVisitor;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.handlers.BaseHandler;

/**
 * Invoked when openType command is executed
 * 
 * @author wchow
 *
 */
public class OpenTypeHandler extends BaseHandler {

    private static final Logger logger = Logger.getLogger(OpenTypeHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        final List<IProject> projects = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects();
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        openTypeDialog(workbench.getActiveWorkbenchWindow().getShell(), projects);
        
		return null;
	}
	
	private void openTypeDialog(Shell shell, List<IProject> projects) {
		List<OpenTypeClassHolder> resources = Lists.newArrayList();
		for (IProject project : projects) {

			List<IResource> sources = ApexSourceUtils.INSTANCE.findSourcesInProject(project);
			List<IResource> typeRef = ApexSourceUtils.INSTANCE.filterSourcesByClassOrTrigger(sources);
			for (IResource resource : typeRef) {
				OpenTypeVisitor visitor = new OpenTypeVisitor();
				ApexVisitorUtil.INSTANCE.traverse(visitor, resource);
				Map<String, Integer> mapping = visitor.getNumberLineMapping();
				OpenTypeClassHolder holder = null;
				for (String className : mapping.keySet()) {
					holder = new OpenTypeClassHolder(resource, project.getName(), className, mapping.get(className));
					resources.add(holder);
				}
			}
		}
		OpenTypeClassHolder[] selectedResource = getType(shell, resources);
		if (selectedResource == null) {
			// Nothing selected, just pressed cancel
			return;
		}

		try {
			for (OpenTypeClassHolder selected : selectedResource) {
				IResource resource = selected.resource;
				Map<String, Object> attributes = Maps.newHashMap();
				attributes.put(IMarker.LINE_NUMBER, selected.line);
				IMarker marker = resource.createMarker(IMarker.TEXT);
				marker.setAttributes(attributes);
				IDE.openEditor(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage(), marker);
				marker.delete();
			}
		} catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to update open a selected apex resource: " + logMessage);		
		}
	}

	private OpenTypeClassHolder[] getType(Shell shell, List<OpenTypeClassHolder> resources) {
		ILabelProvider listLabelProvider = new LabelProvider() {
			@Override 
			public String getText(Object element) {
				if (element != null && element instanceof OpenTypeClassHolder) {
					OpenTypeClassHolder resource = (OpenTypeClassHolder) element;
					return resource.displayName;
				}
				return null;
			}
		};
		
		ILabelProvider detailsLabelProvider = new LabelProvider() {
			@Override 
			public String getText(Object element) {
				if (element != null && element instanceof OpenTypeClassHolder) {
					OpenTypeClassHolder resource = (OpenTypeClassHolder) element;
					return resource.projectName;
				}
				return null;
			}
		};

		FilteredItemsSelectionDialog filteredDialog = new FilteredApexResourcesSelectionDialog(shell, true, resources, 
				listLabelProvider, detailsLabelProvider);
		filteredDialog.setTitle(Messages.OpenTypeView_View_Title);
		filteredDialog.setMessage(Messages.OpenTypeView_View_Message);
		if (filteredDialog.open() == Window.OK) {
			Object[] selected = filteredDialog.getResult();
			if (selected.length > 0) {
				return Arrays.copyOf(selected, selected.length, OpenTypeClassHolder[].class);
			}
		};
		return null;
	}
	
	public static class OpenTypeClassHolder {
		public final IResource resource;
		public final String projectName;
		public final String displayName;
		public final int line;
		
		public OpenTypeClassHolder(IResource resource, String projectName, String displayName, int line) {
			this.resource = resource;
			this.projectName = projectName;
			this.displayName = displayName;
			this.line = line;
		}
		
		@Override
		public String toString() {
			return displayName;
			
		}
	}
	

}
