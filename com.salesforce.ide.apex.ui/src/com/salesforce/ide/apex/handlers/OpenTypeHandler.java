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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.google.common.collect.Maps;
import com.salesforce.ide.apex.internal.core.ApexSourceUtils;
import com.salesforce.ide.apex.internal.core.CompilerService;
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
		Map<String, OpenTypeClassHolder> resources = Maps.newHashMap();
		for (IProject project : projects) {
			List<IResource> sources = ApexSourceUtils.INSTANCE.findLocalSourcesInProject(project);
			List<IResource> managedSources = ApexSourceUtils.INSTANCE.findReferencedSourcesInProject(project);
			sources.addAll(managedSources);
			List<IResource> typeRef = ApexSourceUtils.INSTANCE.filterSourcesByClassOrTrigger(sources);
			for (IResource resource : typeRef) {
				OpenTypeVisitor visitor = new OpenTypeVisitor();
			    CompilerService.INSTANCE.visitAstFromFile((IFile) resource, visitor);
				Map<String, Integer> mapping = visitor.getNumberLineMapping();
				OpenTypeClassHolder holder = null;
				for (String className : mapping.keySet()) {
					holder = new OpenTypeClassHolder(resource, project.getName(), className, mapping.get(className));
					resources.put(resource.getFullPath().toString() + className, holder);
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
	
	private OpenTypeClassHolder[] getType(Shell shell, Map<String, OpenTypeClassHolder> resources) {
		FilteredItemsSelectionDialog filteredDialog = new FilteredApexResourcesSelectionDialog(shell, resources);
		if (filteredDialog.open() == Window.OK) {
			Object[] selected = filteredDialog.getResult();
			if (selected.length > 0) {
				return Arrays.copyOf(selected, selected.length, OpenTypeClassHolder[].class);
			}
		}
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

		/**
		 * line is excluded from hashCode and equals because we don't want selection history to think 
		 * two resources are different if only their line number has changed. 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
			result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
			result = prime * result + ((resource == null) ? 0 : resource.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OpenTypeClassHolder other = (OpenTypeClassHolder) obj;
			if (displayName == null) {
				if (other.displayName != null)
					return false;
			} else if (!displayName.equals(other.displayName))
				return false;
			if (projectName == null) {
				if (other.projectName != null)
					return false;
			} else if (!projectName.equals(other.projectName))
				return false;
			if (resource == null) {
				if (other.resource != null)
					return false;
			} else if (!resource.equals(other.resource))
				return false;
			return true;
		}
	}
}
