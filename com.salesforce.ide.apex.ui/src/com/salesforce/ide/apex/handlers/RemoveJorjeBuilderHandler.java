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

package com.salesforce.ide.apex.handlers;

import static com.salesforce.ide.apex.core.ApexCoreConstants.APEX_JORJE_BUILDER_ID;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.ui.handlers.BaseHandler;

/**
 * Removes the Jorje Builder from the Force.com project.
 * 
 * @author nchen
 * 
 */
public class RemoveJorjeBuilderHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(RemoveJorjeBuilderHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IProject project = getProjectChecked(event);
		if (project != null) {
			IProjectDescription description;
			try {
				description = project.getDescription();
				ICommand[] filteredSpecs = Arrays.stream(description.getBuildSpec())
						.filter(command -> !command.getBuilderName().equals(APEX_JORJE_BUILDER_ID))
						.toArray(ICommand[]::new);
				description.setBuildSpec(filteredSpecs);
				project.setDescription(description, new NullProgressMonitor());
			} catch (CoreException e) {
				logger.error(String.format("Failed to remove Jorje Builder to project %s", project.getName()), e);
			}
		}
		return null;
	}
}
