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
package com.salesforce.ide.ui.refactoring;

import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

public class ResourceRefactoringStatusContext extends RefactoringStatusContext {		
	protected RefactorModel refactorModel = null;
	protected String message = null;
	
	public ResourceRefactoringStatusContext(RefactorModel refactorModel, String message) {
		this.refactorModel = refactorModel;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public Object getCorrespondingElement() {
		return refactorModel.getChangeResources();
	}

	public RefactorModel getRefactorModel() {
		return refactorModel;
	}

	public void setRefactorModel(RefactorModel refactorModel) {
		this.refactorModel = refactorModel;
	}

	public void setMessage(String message) {
		this.message = message;
	}		
}
