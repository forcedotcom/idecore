/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 *******************************************************************************/
package com.salesforce.ide.ui.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Opens an external web browser to Salesforce developer documentation.
 * 
 * @author nchen
 *
 */
public class SalesforceDocumentationHandler extends Action implements IHandler2 {

	private static final String ECLIPSE_DOCS = "https://developer.salesforce.com/docs/atlas.en-us.eclipse.meta/eclipse";

	public void run() {
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(ECLIPSE_DOCS));
		} catch (PartInitException | MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getText() {
		return "Feedback/Bugs";
	}

	@Override
	public String getId() {
		return "com.salesforce.ide.ui.command.openSalesforceDocumentatio";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		new SalesforceDocumentationHandler().run();
		return null;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void setEnabled(Object evaluationContext) {
	}
}
