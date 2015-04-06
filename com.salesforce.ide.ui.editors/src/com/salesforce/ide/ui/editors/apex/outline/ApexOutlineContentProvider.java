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

import apex.jorje.data.ast.BlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * TODO: Factor this out so that there is no dependency on the UI for easy unit testing
 * 
 * @author nchen
 * 
 */
public class ApexOutlineContentProvider implements ITreeContentProvider {
    private static final Object[] NO_CHILDREN = new Object[0];

    private CompilationUnit fCompilationUnit;
    private boolean displayingTopLevelTrigger = true;

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof CompilationUnit) {
            fCompilationUnit = (CompilationUnit) newInput;
            displayingTopLevelTrigger = true;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (hasValidCompilationUnit()) {
            if (inputElement instanceof TriggerDeclUnit) {
                // This part is complicated due to an implementation detail with triggers.
                // Unlike its counterparts (Class, Enum, etc) there is no TriggerDecl under TriggerDeclUnit
                // We want to display the top level Trigger node, while also processing its subnodes.
                // The main idea here is that we want the node to play two roles: the first time, display the node itself,
                // the second time, display its children.
                if (displayingTopLevelTrigger) {
                    displayingTopLevelTrigger = false;
                    RootElementFilter switchBlock = new RootElementFilter();
                    fCompilationUnit._switch(switchBlock);
                    return switchBlock.getRootElements();
                } else {
                    return childrenOf((TriggerDeclUnit) inputElement);
                }
            } else {
                RootElementFilter switchBlock = new RootElementFilter();
                fCompilationUnit._switch(switchBlock);
                return switchBlock.getRootElements();
            }
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
        } else if (parentElement instanceof TriggerDeclUnit) {
            return childrenOf((TriggerDeclUnit) parentElement);
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

    private static Object[] childrenOf(TriggerDeclUnit triggerDeclUnit) {
        if (triggerDeclUnit.members != null && triggerDeclUnit.members.values != null) {
            List<BlockMember> values = triggerDeclUnit.members.values;
            return validTriggerBlockMembers(values);
        }
        return NO_CHILDREN;
    }

    public static Object[] validBlockMembers(List<BlockMember> values) {
        final List<Object> children = new ArrayList<>();
        for (BlockMember member : values) {
            if (member != null) { // the parser can produce partial jADT with null members
                member._switch(new BlockMemberFilter(children));
            }
        }
        // Basically just the class members
        return children.toArray();
    }

    public static Object[] validTriggerBlockMembers(List<BlockMember> values) {
        final List<Object> children = new ArrayList<>();
        for (BlockMember member : values) {
            if (member != null) { // the parser can produce partial jADT with null members
                member._switch(new TriggerMemberFilter(children));
            }
        }
        // Basically just the main members, minus any statements
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
