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
package com.salesforce.ide.ui.handlers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.ForceIdeUrlParser;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.ui.wizards.project.ProjectCreateOperation;

/**
 * Urls can be dragged from the "Start Partner Debug Session" Dialog on Apex
 * Debugger page, when an LMO logs into a subscriber org. This handler uses the
 * data in the Url to create a Force.com project for the debug session
 * 
 * @since 202
 * @author dbaker
 */
public class ForceIdeUrlActionHandler {

	private static final Logger logger = Logger.getLogger(ForceIdeUrlActionHandler.class);

	private final ForceIdeUrlParser urlParser;
	private final Display display;
	private ProjectAction result = ProjectAction.UNSET;
		
	public ForceIdeUrlActionHandler(final String url, final Display display) {
		this.urlParser = new ForceIdeUrlParser(url);
		this.display = display;
	}

	/**
	 * Actions that can be taken upon a project by this handler
	 */
	public enum ProjectAction {
		UNSET, CREATE, UPDATE, INVALID, IGNORE
	};

	/**
	 * Commands that can be passed as a parameter of of the Url
	 */
	public enum Commands {
		CREATE_PROJECT("createproject"), INVALID("invalid");

		private final String command;

		private Commands(String command) {
			this.command = command;
		}

		public String toString() {
			return this.command;
		}
	}
	
	public ProjectAction getResult() {
		return this.result;
	}
	
	public ForceIdeUrlParser getUrlParser() {
		return this.urlParser;
	}
	
	public Display getDisplay() {
		return this.display;
	}
	
	public ProjectAction processCommand() {
		if (getDisplay() == null || getUrlParser() == null || 
				getDisplay().isDisposed() || !getUrlParser().isValid()) {
			return (result = ProjectAction.INVALID);
		}
		
		if (getUrlParser().getCommand().equals(Commands.CREATE_PROJECT.toString())) {
			return runCreateOrUpdate();
		} else {
			logger.error("Invalid command for forceUrlHandler: " + getUrlParser().getCommand());
			return (result = ProjectAction.INVALID);
		}
	}

	/**
	 * Invoke createOrUpdateJob in display thread so user see dialog with
	 * progress
	 */
	@VisibleForTesting
	public ProjectAction runCreateOrUpdate() {
		if (Thread.currentThread() == getDisplay().getThread()) {
			return createOrUpdateJob(getUrlParser().asForceProject());
		} else {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						result = createOrUpdateJob(getUrlParser().asForceProject());
					} finally {
						getResult().notify();
					}
				}
			});
			synchronized (result) {
				try {
					getResult().wait();
				} catch (InterruptedException e) {
					logger.error(e);
					return ProjectAction.INVALID;
				}
			}
			return result;
		}
	}

	@VisibleForTesting
	public ProjectAction createOrUpdateJob(ForceProject forceProject) {
		ProjectModel projModel = buildProjectModel(forceProject);
		ProjectController projController = new ProjectController(null);
		projController.setModel(projModel);
		
		return (result = runProjectCreateOperation(projController));
	}
	
	@VisibleForTesting
	public ProjectAction runProjectCreateOperation(ProjectController projController) {
		ProjectAction createResult = ProjectAction.INVALID;
		
		IProgressMonitor monitor = new NullProgressMonitor();
		ProjectCreateOperation createOperation = new ProjectCreateOperation(projController);
		try {
			if (createOperation.create()) {
				createResult = ProjectAction.CREATE;
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			monitor.done();
		}
		
		return createResult;
	}
	
	@VisibleForTesting
	public ProjectModel buildProjectModel(ForceProject forceProject) {
		final String newProjectName = getUrlParser().getOrgName();
		ProjectModel pm = new ProjectModel(forceProject);
		pm.setProjectName(decideProjectName(newProjectName, getUrlParser().getOrgId()));
		if (forceProject != null) {
			pm.setEnvironment(forceProject.getEndpointEnvironment());
		}
		// Only managed code is downloaded automatically. The debugging
		// user must trigger the download of subscriber code manually.
		pm.setContentSelection(ProjectController.ALL_PACKAGES);
		
		return pm;
	}
	
	@VisibleForTesting
	public List<IProject> getExistingProjects() {
		ProjectService ps = ContainerDelegate.getInstance().getServiceLocator().getProjectService();
		return ps.getForceProjects();
	}
	
	@VisibleForTesting
	public String getProjectSid(IProject project) {
		ProjectService ps = ContainerDelegate.getInstance().getServiceLocator().getProjectService();
		ForceProject fp = ps.getForceProject(project);
		return fp.getSessionId();
	}
	
	@VisibleForTesting
	public String decideProjectName(String newProjectName, String newOrgId) {
		List<IProject> forceProjects = getExistingProjects();
		if (forceProjects == null || forceProjects.isEmpty()) {
			// If there are no existing projects, use the desired project name.
			return newProjectName;
		}
		
		String pattern = String.format("^%s \\((\\d+)\\)$", newProjectName);
		Pattern r = Pattern.compile(pattern);
		Matcher m;
		Set<Integer> suspects = Sets.newHashSet();
		
		boolean sameName, samePattern, sameOrg;
		for (IProject existingProject : forceProjects) {
			sameName = existingProject.getName().equals(newProjectName);
			
			m = r.matcher(existingProject.getName());
			samePattern = m.find();
			
			String fpSid = getProjectSid(existingProject);
			String fpOrg = (fpSid != null && !fpSid.isEmpty()) ? fpSid.substring(0, fpSid.indexOf("!")) : "";
			sameOrg = fpOrg.equals(newOrgId);
			
			if ((sameName && sameOrg) || (samePattern && sameOrg)) {
				// If we already have the org in the workspace and the project
				// name is either exact or follows the pattern, just re-use the project.
				return existingProject.getName();
			} else if (sameName && !sameOrg) {
				// If the project name we want to use already exists in the workspace
				// and it's for a different org, start the counter for the new name.
				suspects.add(1);
			} else if (samePattern && !sameOrg) {
				// If the name pattern exists in the workspace and it's for a
				// different org, keep track of the number in the pattern.
				suspects.add(Integer.valueOf(m.group().replaceAll("[^0-9]+", "")));
			}
		}
		
		// Use first smallest available positive number for name uniqueness.
		if (!suspects.isEmpty()) {
			Collections.sort(Lists.newArrayList(suspects));
			// Starts at 2 to follow Eclipse's naming convention
			for (int ret = 2; ret <= Integer.MAX_VALUE; ret++) {
				if (!suspects.contains(ret)) {
					return String.format("%s (%d)", newProjectName, ret);
				}
			}
		}
		
		return newProjectName;
	}
}
