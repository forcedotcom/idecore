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
package com.salesforce.ide.ui.perspective;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

import com.salesforce.ide.core.internal.utils.Constants;
/**
 * Activates or deactivates Force.com IDE context as perspective changes
 * 
 * @author wchow
 *
 */
public class ForceIdePerspectiveListener extends PerspectiveAdapter {

	private static IContextActivation forceContext;
	
	@Override
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (perspective.getId().equals(Constants.FORCE_PLUGIN_PERSPECTIVE)) {
			activateForceContext();
		} else {
			deactivateForceContext();
		}
	}

	@Override
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (perspective.getId().equals(Constants.FORCE_PLUGIN_PERSPECTIVE)) {
			deactivateForceContext();
		}	
	}

	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (perspective.getId().equals(Constants.FORCE_PLUGIN_PERSPECTIVE)) {
			page.getWorkbenchWindow().getService(IContextService.class).getActiveContextIds();
			activateForceContext();
		} else {
			deactivateForceContext();
		}
	}
	
	@Override
	public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (perspective.getId().equals(Constants.FORCE_PLUGIN_PERSPECTIVE)) {
			deactivateForceContext();
		}
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) {
		if (perspective.getId().equals(Constants.FORCE_PLUGIN_PERSPECTIVE)) {
			activateForceContext();
		} else {
			deactivateForceContext();
		}
	}

	public void activateForceContext() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IContextService service = workbench.getService(IContextService.class);
		if (!service.getActiveContextIds().contains(Constants.FORCE_PLUGIN_CONTEXT_ID)) {
			forceContext = service.activateContext(Constants.FORCE_PLUGIN_CONTEXT_ID);		
		}
	}

	public void deactivateForceContext() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IContextService service = workbench.getService(IContextService.class);
		if (service.getActiveContextIds().contains(Constants.FORCE_PLUGIN_CONTEXT_ID)) {
			service.deactivateContext(forceContext);
			forceContext = null;
		}
	}
}
