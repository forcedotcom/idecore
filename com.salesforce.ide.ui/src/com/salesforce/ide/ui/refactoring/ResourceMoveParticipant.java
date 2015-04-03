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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

import com.salesforce.ide.core.internal.utils.Constants;

/**
 * Refactoring moves are not supported by the Force.com IDE.  This class invalidates refactor move requests by always
 * returning a FATAL condition.
 *
 * @author cwall
 */
public class ResourceMoveParticipant extends MoveParticipant {
    private static final Logger logger = Logger.getLogger(ResourceMoveParticipant.class);

    private MoveChange moveChange = null;

    //   C O N S T R U C T O R
    public ResourceMoveParticipant() {
        super();
    }

    //   M E T H O D S
    @Override
    public RefactoringStatus checkConditions(IProgressMonitor monitor, CheckConditionsContext context)
            throws OperationCanceledException {
        logger.warn(Constants.PLUGIN_NAME + " does not support moving components. Please use copy instead.");
        return moveChange.getRefactorController().createFatalRefactoringStatus(Constants.PLUGIN_NAME
                + " does not support moving components.  Please use copy instead.");
    }

    /**
     * Evaluates move candidate and aggregates sub-elements.
     *
     * Always return true so that this participant is involved in all move transactions.
     */
    @Override
    protected boolean initialize(Object element) {
        if (logger.isDebugEnabled()) {
            logger.debug("***" + getName() + " initiated ***");
        }

        moveChange = new MoveChange();

        return true;
    }

    @Override
    public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        if (moveChange == null) {
            moveChange = new MoveChange();
        }
        return moveChange;
    }


    @Override
    public String getName() {
        return Constants.PLUGIN_NAME + " Move Resource Participant";
    }
}
