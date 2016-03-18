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
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import com.salesforce.ide.apex.handlers.OpenTypeHandler.OpenTypeClassHolder;
import com.salesforce.ide.apex.ui.Activator;
import com.salesforce.ide.apex.ui.Messages;

/**
 * Opex apex type dialog (ctrl-shift-t in Force.com perspective)
 * 
 * @author wchow
 *
 */
public class FilteredApexResourcesSelectionDialog extends FilteredItemsSelectionDialog {

	private Map<String, OpenTypeClassHolder> resources;

	// Prevent opening of multiple dialogs
	private static boolean alreadyOpen = false;
	public static final ILabelProvider listLabelProvider = new LabelProvider() {
		@Override 
		public String getText(Object element) {
			if (element != null && element instanceof OpenTypeClassHolder) {
				OpenTypeClassHolder resource = (OpenTypeClassHolder) element;
				return resource.displayName + " - " + resource.projectName; //$NON-NLS-1$
			}
			return null;
		}
	};
	public static final ILabelProvider detailsLabelProvider = new LabelProvider() {
		@Override 
		public String getText(Object element) {
			if (element != null && element instanceof OpenTypeClassHolder) {
				OpenTypeClassHolder resource = (OpenTypeClassHolder) element;
				return resource.projectName + " - " + resource.resource.getProjectRelativePath(); //$NON-NLS-1$
			}
			return null;
		}
	};

	public FilteredApexResourcesSelectionDialog(Shell shell, Map<String, OpenTypeClassHolder> resources) {
		super(shell, true);
		super.setListLabelProvider(listLabelProvider);
		super.setDetailsLabelProvider(detailsLabelProvider);
		this.setHelpAvailable(false);
		this.setTitle(Messages.OpenTypeView_View_Title);
		this.setMessage(Messages.OpenTypeView_View_Message);
		super.setSelectionHistory(new ApexTypeSelectionHistory());
		this.resources = resources;
	}
	
	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	@Override
	public String getMessage() {
		return super.getMessage();
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
	
	@Override
	public int open() {
		int result;
		if (!alreadyOpen) {
			alreadyOpen = true;
			try {
				result = super.open();
			} finally {
				alreadyOpen = false;
			}
		} else {
			result = Window.CANCEL;
		}
		return result;
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
		progressMonitor.beginTask(Messages.FilteredApexResourcesSelectionDialog_Searching, resources.size());
		for (String key : resources.keySet()) {
			contentProvider.add(resources.get(key), itemsFilter);
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
		static final String CLASS_NAME = "className";
		static final String RESOURCE = "resource";

		@Override
		protected Object restoreItemFromMemento(IMemento memento) {
			String className = memento.getString(CLASS_NAME);
			String resourcePath = memento.getString(RESOURCE);
			OpenTypeClassHolder holder = resources.get(resourcePath + className);
			return holder;
		}

		@Override
		protected void storeItemToMemento(Object item, IMemento memento) {
			if (item instanceof OpenTypeClassHolder) {
				OpenTypeClassHolder ref = (OpenTypeClassHolder) item;
				memento.putString(CLASS_NAME, ref.displayName);
				memento.putString(RESOURCE, ref.resource.getFullPath().toString());
			}
		}
	}
}
