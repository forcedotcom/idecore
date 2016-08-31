package com.salesforce.ide.ui.handlers;
/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 *     dbaker@salesforce.com
 ******************************************************************************/
	
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class ForceIdeUrlDropHandler implements IStartup {

	private static final int[] PREFERRED_DROP_OPERATIONS = { DND.DROP_DEFAULT, DND.DROP_COPY, DND.DROP_MOVE,
			DND.DROP_LINK };

	private static final int DROP_OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT;
	private static final Logger logger = Logger.getLogger(ForceIdeUrlDropHandler.class);
    
	enum ProjectAction{
        CREATE,
        UPDATE,
        INVALID,
        IGNORE
    };

	private final DropTargetAdapter dropListener = new CreatePrjojectDropTargetListener();

	private final WorkbenchListener workbenchListener = new WorkbenchListener();

	private Transfer[] transferAgents;

	public void earlyStartup() {
		UIJob registerJob = new UIJob(PlatformUI.getWorkbench().getDisplay(), "ForceProjectDrop") {
			{
				setPriority(Job.SHORT);
				setSystem(true);
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				workbench.addWindowListener(workbenchListener);
				IWorkbenchWindow[] workbenchWindows = workbench
						.getWorkbenchWindows();
				for (IWorkbenchWindow window : workbenchWindows) {
					workbenchListener.hookWindow(window);
				}
				return Status.OK_STATUS;
			}

		};
		registerJob.schedule();
	}

	public void installDropTarget(final Shell shell) {
		hookUrlTransfer(shell, dropListener);
	}

	private DropTarget hookUrlTransfer(final Shell shell, DropTargetAdapter dropListener) {
		DropTarget target = findDropTarget(shell);
		if (target != null) {
			//target exists, get it and check proper registration
			registerWithExistingTarget(target);
		} else {
			target = new DropTarget(shell, DROP_OPERATIONS);
			if (transferAgents == null) {
				transferAgents = new Transfer[] { URLTransfer.getInstance() };
			}
			target.setTransfer(transferAgents);
		}
		registerDropListener(target, dropListener);

		Control[] children = shell.getChildren();
		for (Control child : children) {
			hookRecursive(child, dropListener);
		}
		return target;
	}

	private void registerDropListener(DropTarget target, DropTargetListener dropListener) {
		target.removeDropListener(dropListener);
		target.addDropListener(dropListener);
	}

	private void hookRecursive(Control child, DropTargetListener dropListener) {
		DropTarget childTarget = findDropTarget(child);
		if (childTarget != null) {
			registerWithExistingTarget(childTarget);
			registerDropListener(childTarget, dropListener);
		}
		if (child instanceof Composite) {
			Composite composite = (Composite) child;
			Control[] children = composite.getChildren();
			for (Control control : children) {
				hookRecursive(control, dropListener);
			}
		}
	}

	private void registerWithExistingTarget(DropTarget target) {
		Transfer[] transfers = target.getTransfer();
		if (transfers != null) {
			for (Transfer transfer : transfers) {
				if (transfer instanceof URLTransfer) 
					return;
			}

			Transfer[] newTransfers = new Transfer[transfers.length + 1];
			System.arraycopy(transfers, 0, newTransfers, 0, transfers.length);
			newTransfers[transfers.length] = URLTransfer.getInstance();
			target.setTransfer(newTransfers);
		}
	}

	private DropTarget findDropTarget(Control control) {
		Object object = control.getData(DND.DROP_TARGET_KEY);
		if (object instanceof DropTarget) {
			return (DropTarget) object;
		}
		return null;
	}

	private class CreatePrjojectDropTargetListener extends DropTargetAdapter {
		
		@Override
		public void dragEnter(DropTargetEvent e) {
			updateDragDetails(e);
		}

		@Override
		public void dragOver(DropTargetEvent e) {
			updateDragDetails(e);
		}

		@Override
		public void dragLeave(DropTargetEvent e) {
			if (e.detail == DND.DROP_NONE) {
				setDropOperation(e);
			}
		}

		@Override
		public void dropAccept(DropTargetEvent e) {
			updateDragDetails(e);
		}

		@Override
		public void dragOperationChanged(DropTargetEvent e) {
			updateDragDetails(e);
		}

		private void setDropOperation(DropTargetEvent e) {
			int allowedOperations = e.operations;
			for (int op : PREFERRED_DROP_OPERATIONS) {
				if ((allowedOperations & op) != 0) {
					traceDropOperation(op);
					e.detail = op;
					return;
				}
			}
			e.detail = allowedOperations;
		}

		private void updateDragDetails(DropTargetEvent e) {
			if (dropTargetIsValid(e, false)) {
				setDropOperation(e);
			}
		}

		private boolean dropTargetIsValid(DropTargetEvent e, boolean isDrop) {
			if (URLTransfer.getInstance().isSupportedType(e.currentDataType)){
				//on Windows, we get the URL already during drag operations...
				//FIXME find a way to check the URL early on other platforms, too...
				if (isDrop || Util.isWindows()) {
					if (e.data == null && !extractEventData(e)) {
						traceMissingEventData(e);
						return !isDrop;
					}
				}
				return true;
			}
			return false;
		}

		private boolean extractEventData(DropTargetEvent e) {
			TransferData transferData = e.currentDataType;
			if (transferData != null) {
				Object data = URLTransfer.getInstance().nativeToJava(transferData);
				if (data != null && getUrl(data) != null) {
					e.data = data;
					return true;
				}
			}
			return false;
		}


		
		@Override
		public void drop(DropTargetEvent event) {
			if (!URLTransfer.getInstance().isSupportedType(event.currentDataType)) {
				traceUnsupportedDataType(event);
				//ignore
				return;
			}
			if (event.data == null) {
				traceMissingEventData(event);
				//reject
				event.detail = DND.DROP_NONE;
				return;
			}
			if (!dropTargetIsValid(event, true)) {
				//reject
				event.detail = DND.DROP_NONE;
				return;
			}
			final String url = getUrl(event.data);
			ForceIdeUrlActionHandler urlActionHandler = new ForceIdeUrlActionHandler(url, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay());
			ForceIdeUrlActionHandler.ProjectAction result = urlActionHandler.processCommand();
			if (result == ForceIdeUrlActionHandler.ProjectAction.IGNORE || result == ForceIdeUrlActionHandler.ProjectAction.INVALID)
				traceInvalidEventData(event);
		}

		private void traceDropOperation(int op) {
			logger.debug("Updating drop event: Setting drop operation to {0}");
		}

		private void traceInvalidEventData(DropTargetEvent event) {
			logger.debug("Drop event: Data is not a solution url: {0}"); 
		}

		private void traceMissingEventData(DropTargetEvent event) {
			logger.debug("Missing drop event data {0}"); 
		}

		private void traceUnsupportedDataType(DropTargetEvent event) {
			logger.debug("Unsupported drop data type: "+event.currentDataType);
		}

		private String getUrl(Object eventData) {
			if (eventData == null) {
				return null;
			}
			if (eventData == null || !(eventData instanceof String)) {
				return null;
			}
			String[] dataLines = ((String) eventData).split(System.getProperty("line.separator")); 
			String url = dataLines[0];
			return url;
		}
	}


	private class WorkbenchListener implements IPartListener2, IPageListener, IPerspectiveListener, IWindowListener {

		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			pageChanged(page);
		}

		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		}

		public void pageActivated(IWorkbenchPage page) {
			pageChanged(page);
		}

		public void pageClosed(IWorkbenchPage page) {
		}

		public void pageOpened(IWorkbenchPage page) {
			pageChanged(page);
		}

		private void pageChanged(IWorkbenchPage page) {
			if (page == null) {
				return;
			}
			IWorkbenchWindow workbenchWindow = page.getWorkbenchWindow();
			windowChanged(workbenchWindow);
		}

		public void windowActivated(IWorkbenchWindow window) {
			windowChanged(window);
		}

		private void windowChanged(IWorkbenchWindow window) {
			if (window == null) {
				return;
			}
			Shell shell = window.getShell();
			runUpdate(shell);
		}

		public void windowDeactivated(IWorkbenchWindow window) {
		}

		public void windowClosed(IWorkbenchWindow window) {
		}

		public void windowOpened(IWorkbenchWindow window) {
			hookWindow(window);
		}

		public void hookWindow(IWorkbenchWindow window) {
			if (window == null) {
				return;
			}
			window.addPageListener(this);
			window.addPerspectiveListener(this);
			IPartService partService = window.getService(IPartService.class);
			partService.addPartListener(this);
			windowChanged(window);
		}

		public void partOpened(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		public void partActivated(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		public void partVisible(IWorkbenchPartReference partRef) {
		}

		public void partClosed(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		public void partHidden(IWorkbenchPartReference partRef) {
			partUpdate(partRef);
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		private void partUpdate(IWorkbenchPartReference partRef) {
			if (partRef == null) {
				return;
			}
			IWorkbenchPage page = partRef.getPage();
			pageChanged(page);
		}

		private void runUpdate(final Shell shell) {
			if (shell == null || shell.isDisposed()) {
				return;
			}
			Display display = shell.getDisplay();
			if (display == null || display.isDisposed()) {
				return;
			}
			try {
				display.asyncExec(new Runnable() {

					public void run() {
						if (!shell.isDisposed()) {
							installDropTarget(shell);
						}
					}
				});
			} catch (SWTException ex) {
				if (ex.code == SWT.ERROR_DEVICE_DISPOSED) {
					//ignore
					return;
				}
			} catch (RuntimeException ex) {
			}
		}
	}
}
