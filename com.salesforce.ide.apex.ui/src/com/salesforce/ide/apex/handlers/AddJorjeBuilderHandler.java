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

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import static com.salesforce.ide.apex.core.ApexCoreConstants.APEX_JORJE_BUILDER_ID;
import com.salesforce.ide.ui.handlers.BaseHandler;

/**
 * Adds the Jorje Builder to the Force.com project.
 * 
 * @author nchen
 * 
 */
public class AddJorjeBuilderHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(AddJorjeBuilderHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IProject project = getProjectChecked(event);
		if (project != null) {
			IProjectDescription description;
			try {
				description = project.getDescription();
				ICommand[] buildSpecs = description.getBuildSpec();

				for (ICommand buildSpec : buildSpecs) {
					if (buildSpec.getBuilderName().equals(APEX_JORJE_BUILDER_ID))
						return null; // We already have the builder, don't add again
				}

				ICommand jorjeBuilder = description.newCommand();
				jorjeBuilder.setBuilderName(APEX_JORJE_BUILDER_ID);

				ICommand[] newCopy = new ICommand[buildSpecs.length + 1];
				System.arraycopy(buildSpecs, 0, newCopy, 1, buildSpecs.length);
				newCopy[0] = jorjeBuilder;
				description.setBuildSpec(newCopy);
				project.setDescription(description, new NullProgressMonitor());

			} catch (CoreException e) {
				logger.error(String.format("Failed to add Jorje Builder to project %s", project.getName()), e);
			}
		}
		return null;
	}
}
