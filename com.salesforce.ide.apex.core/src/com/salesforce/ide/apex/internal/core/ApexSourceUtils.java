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
package com.salesforce.ide.apex.internal.core;

import static apex.jorje.semantic.symbol.type.AnnotationTypeInfos.IS_TEST;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.TEST_METHOD;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.services.ProjectService;

import apex.jorje.data.Locations;
import apex.jorje.data.Location;
import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.member.Method;
import apex.jorje.semantic.ast.modifier.ModifierGroup;
import apex.jorje.semantic.ast.visitor.AdditionalPassScope;
import apex.jorje.semantic.ast.visitor.AstVisitor;

/**
 * Utilities for working on Apex source files.
 * 
 * @author jwidjaja, nchen
 * 
 */
public class ApexSourceUtils {
    private static final Logger logger = Logger.getLogger(ApexSourceUtils.class);

    public static final ApexSourceUtils INSTANCE = new ApexSourceUtils();
    public static String CLS_SUFFIX = ".cls";
    public static String TRG_SUFFIX = ".trigger";

    private ApexSourceUtils() {}

    /**
     * Find test classes in a given project. Test classes are annotated with @IsTest.
     * 
     * @param project
     * @return Map of test resources whose key is the resource ID and values are test method names
     */
    public Map<IResource, List<String>> findTestClassesInProject(IProject project) {
        final Map<IResource, List<String>> allTests = Maps.newLinkedHashMap();

        List<IResource> projectResources = findLocalSourcesInProject(project);
        List<IResource> projectClasses = filterSourcesByClass(projectResources);
        try {
            for (final IResource projectResource : projectClasses) {
            	List<String> methodNames = findTestMethodNamesInFile(projectResource);
            	if (methodNames != null && methodNames.size() > 0) {
            		allTests.put(projectResource, methodNames);
            	}
            }
        } catch (Exception e) {
            logger.error("Encountered an issue trying to find test classes in the project", e);
        }

        return allTests;
    }

    /**
     * Find names of test methods in a given resource.
     * Test methods are annotated with @IsTest or 'testmethod'.
     * 
     * @param resource
     * @return List of test method names
     */
    public List<String> findTestMethodNamesInFile(IResource resource) {
        final List<String> methodNames = Lists.newArrayList();

        try {
            CompilerService.INSTANCE.visitAstFromFile((IFile) resource, new AstVisitor<AdditionalPassScope>() {

                @Override
                public boolean visit(UserClass node, AdditionalPassScope scope) {
                    return true;
                }

                @Override
                public void visitEnd(UserClass node, AdditionalPassScope scope) {
                    super.visitEnd(node, scope);
                }

                @Override
                public boolean visit(Method method, AdditionalPassScope scope) {
                    if (isTestMethod(method.getModifiers())) {
                        methodNames.add(method.getMethodInfo().getName());
                    }
                    return super.visit(method, scope);
                }
                
            });
        } catch (Exception e) {
            logger.error("Encountered an issue trying to find test method names for " + resource.getName(), e);
        }

        return methodNames;
    }
    
    /**
     * Find location of test methods in a given resource.
     * Test methods are annotated with @IsTest or 'testmethod'.
     * 
     * @param resource
     * @return Map of test method names and their location
     */
    public Map<String, ApexCodeLocation> findTestMethodLocsInFile(IResource resource) {
    	final Map<String, ApexCodeLocation> testMethods = Maps.newHashMap();
    	
    	try {
            CompilerService.INSTANCE.visitAstFromFile((IFile) resource, new AstVisitor<AdditionalPassScope>() {

                @Override
                public boolean visit(UserClass node, AdditionalPassScope scope) {
                    return true;
                }

                @Override
                public void visitEnd(UserClass node, AdditionalPassScope scope) {
                    super.visitEnd(node, scope);
                }

                @Override
                public boolean visit(Method method, AdditionalPassScope scope) {
                    if (isTestMethod(method.getModifiers())) {
                    	Location realLoc = method.getLoc();
                    	if (Locations.isReal(realLoc)) {
                    		ApexCodeLocation loc = new ApexCodeLocation((IFile) resource, realLoc.line, realLoc.column);
                    		testMethods.put(method.getMethodInfo().getCanonicalName(), loc);
                    	}
                    }
                    return super.visit(method, scope);
                }
                
            });
        } catch (Exception e) {
            logger.error("Encountered an issue trying to find test methods for " + resource.getName(), e);
        }
    	
    	return testMethods;
    }
    
    /**
     * Find location of the class declaration.
     * Default to the first line and first column of the file.
     * 
     * @param resource
     * @return Location of class decl
     */
    public ApexCodeLocation findClassLocInFile(IResource resource) {
        ApexCodeLocation[] returnValue = new ApexCodeLocation[1];
        try {
            CompilerService.INSTANCE.visitAstFromFile((IFile) resource, new AstVisitor<AdditionalPassScope>() {
                
                @Override
                public boolean visit(UserClass userClass, AdditionalPassScope scope) {
                	Location realLoc = userClass.getLoc();
                	if (Locations.isReal(realLoc)) {
                		returnValue[0] = new ApexCodeLocation((IFile) resource, realLoc.line, realLoc.column);
                		return super.visit(userClass, scope);
                	} else {
                		return false;
                	}
                }
                
            });
            return returnValue[0];
        } catch (Exception e) {
            logger.error("Encountered an issue trying to find location of class declaration " + resource.getName(), e);
        }
        
        return null;
    }
    
    /**
     * Find all local IResource (excluding managed package) in a given project.
     * 
     * @param project
     * @return List of IResource
     */
    public List<IResource> findLocalSourcesInProject(IProject project) {
        List<IResource> projectSources = Lists.newArrayList();
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
     * Find all managed IResource (in a Referenced Package) in a given project.
     * 
     * @param project
     * @return List of IResource
     */
    public List<IResource> findReferencedSourcesInProject(IProject project) {
        List<IResource> projectSources = Lists.newArrayList();
        if (project == null)
            return projectSources;

        ProjectService ps = ContainerDelegate.getInstance().getServiceLocator().getProjectService();
        IFolder folder = ps.getReferencedPackagesFolder(project);
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
    public List<IResource> filterSourcesByClass(final List<IResource> projectResources) {
        return filterSourcesByType(projectResources, "ApexClass");
    }

    /**
     * Filter a list of IResource for Apex triggers.
     * 
     * @param projectResources
     * @return List of IResource that are only Apex triggers
     */
    public List<IResource> filterSourcesByTrigger(final List<IResource> projectResources) {
        return filterSourcesByType(projectResources, "ApexTrigger");
    }
    
    /**
     * Filter a list of IFile for Apex classes and triggers
     * 
     * @param projectResources
     * @return List of IResource that are only Apex classes and triggers
     */
    public List<IResource> filterSourcesByClassOrTrigger(final List<IResource> projectResources) {
        if (projectResources == null) {
            return Lists.newArrayList();
        } else {
            ComponentFactory componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory(); Component classComponent = componentFactory.getComponentByComponentType("ApexClass");
            String classExt = classComponent == null ? CLS_SUFFIX : classComponent.getFileExtension();
            Component trgComponent = componentFactory.getComponentByComponentType("ApexTrigger");
            String trgExt = trgComponent == null ? TRG_SUFFIX : trgComponent.getFileExtension();
            
            return projectResources.stream()
                .filter(r -> !StringUtils.isEmpty(r.getFileExtension()) && (r.getFileExtension().equals(classExt) || r.getFileExtension().equals(trgExt)))
                .collect(Collectors.toList());
        }
    }

    /**
     * Filter a list of IResource for a given type.
     * 
     * @param projectResources
     * @param type
     * @return List of IResource that are only the wanted type
     */
    public List<IResource> filterSourcesByType(final List<IResource> projectResources, final String type) {
        if (projectResources == null) {
            return Lists.newArrayList();
        } else {
            ComponentFactory componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
            Component component = componentFactory.getComponentByComponentType(type);
            String extension = component.getFileExtension();
            
            return projectResources.stream()
                .filter(r -> r.getFileExtension().equals(extension))
                .collect(Collectors.toList());
        }
    }

    private boolean isTestMethod(ModifierGroup modifiers) {
        return modifiers != null && (modifiers.has(TEST_METHOD) || modifiers.has(IS_TEST));
    }
}