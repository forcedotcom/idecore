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
package com.salesforce.ide.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;

import com.salesforce.ide.core.internal.utils.Utils;

public class RemoveNatureAction extends BaseChangeNatureAction {

    public RemoveNatureAction() {
        super();
    }

    public RemoveNatureAction(IProject project) {
        super();
        this.project = project;
    }

    @Override
    public void init() {}

    @Override
    public void execute(IAction action) {
        boolean response =
                Utils.openQuestion("Remove Force.com Nature",
                    "Are you sure you want to remove Force.com Nature for project '" + project.getName() + "'?");
        if (response) {
            removeNature();
        }
    }

    public boolean removeNature() {
        getProjectService().removeNatures(project, null);
        updateDecorators();
        return true;
    }
}
