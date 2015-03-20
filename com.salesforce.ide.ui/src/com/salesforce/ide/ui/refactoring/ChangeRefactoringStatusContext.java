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

import org.eclipse.core.resources.IResource;

public class ChangeRefactoringStatusContext extends ResourceRefactoringStatusContext {		
	
	//   C O N S T R U C T O R S
	public ChangeRefactoringStatusContext(ChangeRefactorModel refactorModel, String message) {
		super(refactorModel, message);
	}
	
	//   M E T H O D S
	@Override
    public ChangeRefactorModel getRefactorModel() {
		return (ChangeRefactorModel) refactorModel;
	}

	public IResource getDestinationResource() {		
		return getRefactorModel().getDestinationResource();
	}

	@Override
	public Object getCorrespondingElement() {
		return getDestinationResource();
	}		
}
