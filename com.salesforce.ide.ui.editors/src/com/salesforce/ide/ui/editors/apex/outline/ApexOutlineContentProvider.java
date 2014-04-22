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
package com.salesforce.ide.ui.editors.apex.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import apex.jorje.data.ast.BlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;

/**
 * TODO: Factor this out so that there is no dependency on the UI for easy unit testing 
 * TODO: Should probably share the same parse tree as the editor and not have to parse again
 * 
 * @author nchen
 * 
 */
public class ApexOutlineContentProvider implements ITreeContentProvider {
    private CompilationUnit fCompilationUnit;
    private static final Object[] NO_CHILDREN = new Object[0];

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof CompilationUnit) {
            fCompilationUnit = (CompilationUnit) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {

        if (hasValidCompilationUnit()) {
            RootElementFilter switchBlock = new RootElementFilter();
            fCompilationUnit._switch(switchBlock);
            return switchBlock.getRootElements();
        }

        return NO_CHILDREN;
    }

    private boolean hasValidCompilationUnit() {
        return fCompilationUnit != null;
    }

    /**
     * Cannot do static dispatch because we declare the type to be Object. This was probably done by Eclipse in
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof EnumDecl) {
            return childrenOf((EnumDecl) parentElement);
        } else if (parentElement instanceof ClassDecl) {
            return childrenOf(((ClassDecl) parentElement));
        } else if (parentElement instanceof InterfaceDecl) {
            return childrenOf(((InterfaceDecl) parentElement));
        } else if (parentElement instanceof BlockMember) {
            return childrenOf((BlockMember) parentElement);
        }
        return NO_CHILDREN;
    }

    public static Object[] childrenOf(EnumDecl enumDecl) {
        if (enumDecl.members != null && enumDecl.members.values != null) {
            // Basically just the identifiers
            return validIdentifiers(enumDecl.members.values).toArray(Identifier.class);
        }
        
        return NO_CHILDREN;
    }

    public static FluentIterable<Identifier> validIdentifiers(Iterable<Identifier> list) {
        return FluentIterable.from(list).filter(new Predicate<Identifier>() {

            @Override
            public boolean apply(Identifier id) {
                return id != null;
            }
        });
    }

    // These two are identical except for the types
    public static Object[] childrenOf(ClassDecl classDecl) {
        if (classDecl.members != null && classDecl.members.values != null) {
            List<BlockMember> values = classDecl.members.values;
            return validBlockMembers(values);
        }
        return NO_CHILDREN;
    }

    public static Object[] childrenOf(InterfaceDecl interfaceDecl) {
        if (interfaceDecl.members != null && interfaceDecl.members.values != null) {
            List<BlockMember> values = interfaceDecl.members.values;
            return validBlockMembers(values);
        }
        return NO_CHILDREN;
    }

    public static Object[] validBlockMembers(List<BlockMember> values) {
        final List<Object> children = new ArrayList<Object>();
        for (BlockMember member : values) {
            if (member != null) { // the parser can produce partial jADT with null members
                member._switch(new BlockMemberFilter(children));
            }
        }
        // Basically just the class members
        return children.toArray();
    }

    public static Object[] childrenOf(BlockMember classMember) {
        NestedClassMemberFilter filter = new NestedClassMemberFilter();
        classMember._switch(filter);
        return filter.getChildren().toArray();
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     * 
     * TODO: Optimize this. Right now, we are just going to call getChildren and return if we have a non-zero value
     * 
     */
    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length >= 1;
    }
}
