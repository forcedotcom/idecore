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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class MoveChange extends BaseChange {

    // C O N S T R U C T O R
    public MoveChange() {
        super();
        this.refactorController = new ChangeRefactorController();
    }

    // M E T H O D S
    @Override
    public ChangeRefactorController getRefactorController() {
        return (ChangeRefactorController) refactorController;
    }

    @Override
    public Object getModifiedElement() {
        return null;
    }

    @Override
    public String getName() {
        return "Move components";
    }

    @Override
    public void initializeValidationData(IProgressMonitor monitor) {
    }

    public RefactoringStatus checkConditions(IResource origDesintation, IProgressMonitor monitor)
            throws OperationCanceledException {
        return null;
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        return null;
    }

    @Override
    public Change perform(IProgressMonitor monitor) throws CoreException {
        // no undo change at this time
        return new NullChange();
    }
}
