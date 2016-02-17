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
package com.salesforce.ide.apex.ui.views;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import com.salesforce.ide.apex.handlers.OpenTypeHandler.OpenTypeClassHolder;
import com.salesforce.ide.apex.ui.Activator;
import com.salesforce.ide.core.internal.context.ContainerDelegate;

/**
 * Opex apex type dialog (ctrl-shift-t in Force.com perspective)
 * 
 * @author wchow
 *
 */
public class FilteredApexResourcesSelectionDialog extends FilteredItemsSelectionDialog {

	private List<OpenTypeClassHolder> resources;

	public FilteredApexResourcesSelectionDialog(Shell shell, boolean multi, List<OpenTypeClassHolder> resources, 
			ILabelProvider labelProvider, ILabelProvider detailsLabelProvider) {
		super(shell, multi);
		super.setListLabelProvider(labelProvider);
		super.setDetailsLabelProvider(detailsLabelProvider);
		super.setSelectionHistory(new ApexTypeSelectionHistory());
		this.resources = resources;
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";	

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
		}
		return settings;
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ApexItemsFilter();
	}

	private class ApexItemsFilter extends FilteredItemsSelectionDialog.ItemsFilter {
		@Override
		public boolean matchItem(Object item) {
			if (item instanceof OpenTypeClassHolder) {
				OpenTypeClassHolder resource = (OpenTypeClassHolder) item;
				return matches(resource.displayName);
			}
			return true;
		}

		@Override
		public boolean isConsistentItem(Object item) {
			return true;
		}
	}

	@Override
	protected Comparator<OpenTypeClassHolder> getItemsComparator() {
		return new Comparator<OpenTypeClassHolder>() {
			public int compare(OpenTypeClassHolder arg0, OpenTypeClassHolder arg1) {
				int comparision = arg0.displayName.compareTo(arg1.displayName);
				if (comparision == 0) {
					comparision = arg0.projectName.compareTo(arg1.projectName);
				}
				return comparision;
			}
		};	
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
					throws CoreException {
		progressMonitor.beginTask("Searching", resources.size());
		for (Iterator<OpenTypeClassHolder> iter = resources.iterator(); iter.hasNext();) {
			contentProvider.add(iter.next(), itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.done();
	}

	@Override
	public String getElementName(Object item) {
		if (item instanceof OpenTypeClassHolder) {
			return ((OpenTypeClassHolder) item).displayName;
		}
		return null;
	}

	private class ApexTypeSelectionHistory extends SelectionHistory {

		static final String DISPLAY_NAME = "displayName";
		static final String PROJECT_NAME = "projectName";
		static final String LINE = "line";
		static final String RESOURCE = "resource";

		@Override
		protected Object restoreItemFromMemento(IMemento memento) {
			String displayName = memento.getString(DISPLAY_NAME);
			String projectName = memento.getString(PROJECT_NAME);
			int line = memento.getInteger(LINE);
			List<IProject> projects = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects();

			for (IProject project : projects) {
				IFile resource = project.getFile(memento.getString(RESOURCE));
				if (resource.exists() && project.getName().equals(projectName)) {
					return new OpenTypeClassHolder(resource, projectName, displayName, line);
				}
			}
			return null;
		}

		@Override
		protected void storeItemToMemento(Object item, IMemento memento) {
			if (item instanceof OpenTypeClassHolder) {
				OpenTypeClassHolder ref = (OpenTypeClassHolder) item;
				memento.putString(DISPLAY_NAME, ref.displayName);
				memento.putString(PROJECT_NAME, ref.projectName);
				memento.putInteger(LINE, ref.line);
				memento.putString(RESOURCE, ref.resource.getProjectRelativePath().toString());
			}
		}
	}
}
