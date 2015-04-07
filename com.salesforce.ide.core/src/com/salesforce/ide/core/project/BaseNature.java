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
package com.salesforce.ide.core.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.Utils;

public abstract class BaseNature implements IProjectNature {

    private static final Logger logger = Logger.getLogger(BaseNature.class);

    protected IProject project;

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    public void configure(String builderId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Configuring '" + builderId + "' builder on project '" + project.getName() + "'");
        }
        // cannot modify closed projects.
        if (!getProject().isOpen()) {
            return;
        }

        // get the description.
        IProjectDescription description;
        try {
            description = getProject().getDescription();
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to obtain project description: " + logMessage, e);
            return;
        }

        // look for builder already associated.
        ICommand[] cmds = description.getBuildSpec();
        for (int j = 0; j < cmds.length; j++) {
            if (cmds[j].getBuilderName().equals(builderId)) {
                return;
            }
        }

        // associate builder with project.
        ICommand newCmd = description.newCommand();
        newCmd.setBuilderName(builderId);
        List<ICommand> newCmds = new ArrayList<>();
        newCmds.addAll(Arrays.asList(cmds));
        newCmds.add(newCmd);
        description.setBuildSpec(newCmds.toArray(new ICommand[newCmds.size()]));

        try {
            project.setDescription(description, null);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to set project description: " + logMessage, e);
        }
    }

    public void deconfigure(String builderId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deconfiguring " + builderId + " builder on project " + project.getName());
        }
        // cannot modify closed projects.
        if (!getProject().isOpen()) {
            return;
        }

        // get the description.
        IProjectDescription description;
        try {
            description = getProject().getDescription();
        } catch (CoreException e) {
            logger.warn("Unable to obtain project description", e);
            return;
        }

        // look for builder.
        int index = -1;
        ICommand[] cmds = description.getBuildSpec();
        for (int j = 0; j < cmds.length; j++) {
            if (cmds[j].getBuilderName().equals(builderId)) {
                index = j;
                break;
            }
        }
        if (index == -1) {
            return;
        }

        // remove builder from project.
        List<ICommand> newCmds = new ArrayList<>();
        newCmds.addAll(Arrays.asList(cmds));
        newCmds.remove(index);
        description.setBuildSpec(newCmds.toArray(new ICommand[newCmds.size()]));
        try {
            project.setDescription(description, null);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to set project description: " + logMessage, e);
        }
    }

    protected static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
        if (logger.isDebugEnabled()) {
            logger.debug("Add " + natureId + " nature to project " + project.getName());
        }
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        if (Arrays.asList(prevNatures).contains(natureId)) {
            if (logger.isInfoEnabled()) {
                logger.info("Project '" + project.getName() + "' already has nature '" + natureId + "'");
            }
            return;
        }

        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = natureId;
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
        if (monitor != null) {
            monitor.worked(1);
        }
    }

    public static void addNature(IProject project, String[] natureIds, IProgressMonitor monitor) throws CoreException {
        if (logger.isDebugEnabled()) {
            logger.debug("Add multiple natures to project " + project.getName());
        }

        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();

        List<String> newNatures = new ArrayList<>();
        for (String natureId : prevNatures) {
            newNatures.add(natureId);
        }

        for (String natureId : natureIds) {
            newNatures.add(natureId);
        }

        description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
        project.setDescription(description, monitor);
        if (monitor != null) {
            monitor.worked(1);
        }
    }

    protected static boolean removeNature(IProject currentProject, String natureId, IProgressMonitor monitor) {
        if (logger.isDebugEnabled()) {
            logger.debug("Remove " + natureId + " nature from project " + currentProject.getName());
        }
        try {
            if (currentProject.hasNature(natureId)) {
                IProjectDescription description = currentProject.getDescription();
                String[] natures = description.getNatureIds();
                if (!Arrays.asList(natures).contains(natureId)) {
                    if (logger.isInfoEnabled()) {
                        logger
                                .info("Project '" + currentProject.getName() + "' doesn't have nature '" + natureId
                                        + "'");
                    }
                    return false;
                }

                String[] newNatures = new String[natures.length - 1];
                for (int i = 0; i < newNatures.length; i++) {
                    if (!natures[i].equals(natureId)) {
                        newNatures[i] = natures[i];
                    }
                }
                description.setNatureIds(newNatures);
                currentProject.setDescription(description, null);
                currentProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
            }
            if (monitor != null) {
                monitor.worked(1);
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to remove " + natureId + " nature: " + logMessage);
            return false;
        }
        return true;
    }

    protected static boolean removeNature(IProject currentProject, String[] natureIds, IProgressMonitor monitor) {
        if (logger.isDebugEnabled()) {
            logger.debug("Remove multiple natures from project " + currentProject.getName());
        }

        try {
            IProjectDescription description = currentProject.getDescription();
            String[] natures = description.getNatureIds();

            List<String> removeNatures = Arrays.asList(natureIds);
            List<String> newNatures = new ArrayList<>();
            for (int i = 0; i < natures.length; i++) {
                if (!removeNatures.contains(natures[i])) {
                    newNatures.add(natures[i]);
                }
            }

            description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
            currentProject.setDescription(description, null);
            currentProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

            if (monitor != null) {
                monitor.worked(1);
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to multiple natures from project: " + logMessage);
            return false;
        }
        return true;
    }

    protected static boolean replaceNature(IProject currentProject, String existingNatureId, String newNatureId,
            IProgressMonitor monitor) {
        if (logger.isDebugEnabled()) {
            logger.debug("Replacing " + existingNatureId + "nature with " + newNatureId + " on project "
                    + currentProject.getName());
        }
        try {
            if (Utils.isNotEmpty(existingNatureId) && Utils.isNotEmpty(newNatureId)
                    && currentProject.hasNature(existingNatureId)) {
                IProjectDescription description = currentProject.getDescription();
                String[] natures = description.getNatureIds();
                String[] newNatures = new String[natures.length];
                for (int i = 0; i < natures.length; i++) {
                    if (natures[i].equals(existingNatureId)) {
                        newNatures[i] = newNatureId;
                    } else {
                        newNatures[i] = natures[i];
                    }
                }
                description.setNatureIds(newNatures);
                currentProject.setDescription(description, null);
                currentProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
            }
            if (monitor != null) {
                monitor.worked(1);
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to replace existing" + existingNatureId + " nature with new " + newNatureId
                    + " nature: " + logMessage, e);
            return false;
        }
        return true;
    }
}
