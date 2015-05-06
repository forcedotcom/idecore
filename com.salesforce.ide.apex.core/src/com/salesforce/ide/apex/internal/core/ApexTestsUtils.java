/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.apex.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import apex.jorje.data.ast.BlockMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.ClassDeclUnit;
import apex.jorje.data.ast.Modifier;
import apex.jorje.data.ast.Modifier.Annotation;
import apex.jorje.data.ast.Modifier.TestMethodModifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.internal.utils.ResourceProperties;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.services.ProjectService;

/**
 * Utilities for working on Apex source files that are marked as tests.
 * 
 * @author jwidjaja, nchen
 * 
 */
public class ApexTestsUtils {
    private final class TestModifierDeterminer extends Modifier.SwitchBlockWithDefault {
        boolean hasSeenTestModifier;

        @Override
        public void _case(TestMethodModifier x) {
            hasSeenTestModifier = true;
        }

        @Override
        public void _case(Annotation x) {
            if (x.name.value.equalsIgnoreCase("istest")) {
                hasSeenTestModifier = true;
            }
        }

        @Override
        protected void _default(Modifier x) {}
    }

    private static final Logger logger = Logger.getLogger(ApexTestsUtils.class);

    public static final ApexTestsUtils INSTANCE = new ApexTestsUtils();

    private ApexTestsUtils() {}

    /**
     * Find test classes in a given project. Test classes are annotated with @IsTest.
     * 
     * @param project
     * @return Map of test resources whose key is the resource ID
     */
    public Map<String, IResource> findTestClassesInProject(IProject project) {
        final Map<String, IResource> testClasses = Maps.newHashMap();

        List<IResource> projectResources = findSourcesInProject(project);
        List<IResource> projectClasses = filterSourcesByClass(projectResources);
        try {
            for (final IResource projectResource : projectClasses) {
                IFile projectClass = (IFile) projectResource;
                CompilationUnit compilationUnit = ApexModelManager.INSTANCE.getCompilationUnit(projectClass);
                compilationUnit._switch(new CompilationUnit.SwitchBlockWithDefault() {

                    @Override
                    public void _case(ClassDeclUnit classDeclUnit) {
                        if (classDeclUnit.body != null) {
                            if (hasTestModifier(classDeclUnit.body.modifiers)) {
                                String resourceId =
                                        ResourceProperties.getProperty(projectResource, QualifiedNames.QN_ID);
                                testClasses.put(resourceId, projectResource);
                            }
                        }
                    }

                    @Override
                    protected void _default(CompilationUnit x) {}
                });

            }
        } catch (Exception e) {
            logger.error("Encountered an issue trying to find test classes in the project", e);
        }

        return testClasses;
    }

    /**
     * Find test methods in a given resource. Test methods are annotated with @IsTest or 'testmethod'.
     * 
     * @param resource
     * @return
     */
    public List<String> findTestMethodsInFile(IResource resource) {
        final List<String> methodNames = Lists.newArrayList();

        try {
            // Get the compilation unit of this file
            IFile file = (IFile) resource;
            CompilationUnit compilationUnit = ApexModelManager.INSTANCE.getCompilationUnit(file);
            compilationUnit._switch(new CompilationUnit.SwitchBlockWithDefault() {

                @Override
                public void _case(ClassDeclUnit classDeclUnit) {
                    if (classDeclUnit.body != null && classDeclUnit.body.members != null) {
                        for (BlockMember member : classDeclUnit.body.members.values) {
                            member._switch(new BlockMember.SwitchBlockWithDefault() {

                                @Override
                                public void _case(MethodMember x) {
                                    if (hasTestModifier(x.methodDecl.modifiers)) {
                                        methodNames.add(x.methodDecl.name.value);
                                    }
                                }

                                @Override
                                protected void _default(BlockMember x) {}
                            });
                        }
                    }
                }

                @Override
                protected void _default(CompilationUnit x) {}
            });
        } catch (Exception e) {
            logger.error("Encountered an issue trying to find test methods for " + resource.getName(), e);
        }

        return methodNames;
    }

    /**
     * Find all IResource in a given project.
     * 
     * @param project
     * @return List of IResource
     */
    public List<IResource> findSourcesInProject(IProject project) {
        List<IResource> projectSources = new ArrayList<IResource>();
        if (project == null)
            return projectSources;

        ProjectService ps = ContainerDelegate.getInstance().getServiceLocator().getProjectService();
        IFolder folder = ps.getSourceFolder(project);
        if (folder == null)
            return projectSources;

        try {
            projectSources = ps.getAllFilesOnly(Arrays.asList(folder.members()));
        } catch (CoreException e) {}

        return projectSources;
    }

    /**
     * Filter a list of IResource for Apex classes.
     * 
     * @param projectResources
     * @return List of IResource that are only Apex classes
     */
    public List<IResource> filterSourcesByClass(List<IResource> projectResources) {
        return filterSourcesByType(projectResources, "ApexClass");
    }

    /**
     * Filter a list of IResource for Apex triggers.
     * 
     * @param projectResources
     * @return List of IResource that are only Apex triggers
     */
    public List<IResource> filterSourcesByTrigger(List<IResource> projectResources) {
        return filterSourcesByType(projectResources, "ApexTrigger");
    }

    /**
     * Filter a list of IResource for a given type.
     * 
     * @param projectResources
     * @param type
     * @return List of IResource that are only the wanted type
     */
    public List<IResource> filterSourcesByType(List<IResource> projectResources, String type) {
        if (projectResources == null || projectResources.isEmpty())
            return projectResources;

        ComponentFactory componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
        Component component = componentFactory.getComponentByComponentType(type);
        for (Iterator<IResource> iterator = projectResources.iterator(); iterator.hasNext();) {
            IResource res = iterator.next();
            if (component != null && !res.getFileExtension().equals(component.getFileExtension())) {
                iterator.remove();
            }
        }

        return projectResources;
    }

    /**
     * Check if a list of modifiers has a test modifier.
     * 
     * @param modifiers
     * @return True if there is a test modifier. False otherwise.
     */
    private boolean hasTestModifier(List<Modifier> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return false;
        }

        for (Modifier modifier : modifiers) {
            TestModifierDeterminer switchBlock = new TestModifierDeterminer();
            modifier._switch(switchBlock);

            if (switchBlock.hasSeenTestModifier)
                return true;
        }

        return false;
    }
}
