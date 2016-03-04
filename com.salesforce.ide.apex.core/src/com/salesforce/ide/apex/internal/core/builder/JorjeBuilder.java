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
package com.salesforce.ide.apex.internal.core.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.apex.internal.core.CompilerService;
import com.salesforce.ide.apex.internal.core.db.EclipseGraphHandleProvider;

import apex.jorje.ide.db.api.GraphDatabaseService;
import apex.jorje.ide.db.impl.StandardGraphDatabaseService;
import apex.jorje.semantic.compiler.CompilerStage;

/**
 * Builder that uses the Jorje compiler to calculate metadata about your source code.
 * 
 * @author nchen
 */
public class JorjeBuilder extends IncrementalProjectBuilder {
    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        switch (kind) {
        case FULL_BUILD:
            fullBuild(getProject());
            break;
        case INCREMENTAL_BUILD:
            partialBuild(getProject());
            break;
        default:
        }
        return null;
    }
    
    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        super.clean(monitor);
        GraphDatabaseService service = StandardGraphDatabaseService.INSTANCE;
        service.drop(new EclipseGraphHandleProvider(getProject()));
    }
    
    private void fullBuild(IProject project) throws CoreException {
        ApexResourcesVisitor visitor = new ApexResourcesVisitor();
        project.accept(visitor, IResource.NONE);
        
        ModelBuilder modelBuilder = new ModelBuilder(project);
        try {
            CompilerService.INSTANCE.visitAstsFromFiles(visitor.getFiles(), modelBuilder, CompilerStage.POST_TYPE_RESOLVE);
        } finally {
            modelBuilder.done();
        }
    }
    
    private void partialBuild(IProject project) throws CoreException {}
    
}
