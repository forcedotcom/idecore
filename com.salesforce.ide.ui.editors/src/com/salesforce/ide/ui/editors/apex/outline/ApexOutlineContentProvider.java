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
package com.salesforce.ide.ui.editors.apex.outline;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.Lists;

import apex.jorje.data.Locatable;
import apex.jorje.data.Location;
import apex.jorje.data.Locations;
import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserClassMembers;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserInterfaceMembers;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.compilation.UserTriggerMembers;

/**
 * Provides the contents to be displayed in the outline view.
 * 
 * @author nchen
 */
public class ApexOutlineContentProvider implements ITreeContentProvider {
    /**
     * Compares two Loc for ordering. Only RealLocs have an ordering defined. SyntheticLocs have no ordering.
     */
    public static final Comparator<Locatable> LOCATABLE_COMPARATOR = new Comparator<Locatable>() {
        @Override
        public int compare(Locatable left, Locatable right) {
            Location leftLoc = left.getLoc();
            Location rightLoc = right.getLoc();
            if (Locations.isReal(leftLoc) && Locations.isReal(rightLoc)) {
                if (leftLoc.line != rightLoc.line) {
                    return Integer.compare(leftLoc.line, rightLoc.line);
                } else {
                    return Integer.compare(leftLoc.column, rightLoc.column);
                }
            }
            return 0;
        }
    };
    
    private static final Object[] NO_CHILDREN = new Object[0];
    
    private OutlineViewVisitor visitor;
    
    @Override
    public void dispose() {}
    
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof OutlineViewVisitor) {
            visitor = (OutlineViewVisitor) newInput;
        }
    }
    
    @Override
    public Object[] getElements(Object inputElement) {
        return new Object[] { visitor.getTopLevel() };
    }
    
    /**
     * Cannot do static dispatch because Eclipse declares the static type of the parameter to be Object.
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof UserClass) {
            return childrenOf((UserClass) parentElement);
        } else if (parentElement instanceof UserInterface) {
            return childrenOf(((UserInterface) parentElement));
        } else if (parentElement instanceof UserEnum) {
            return childrenOf(((UserEnum) parentElement));
        } else if (parentElement instanceof UserTrigger) {
            return childrenOf((UserTrigger) parentElement);
        }
        return NO_CHILDREN;
    }
    
    private Object[] childrenOf(UserClass userClass) {
        List<Locatable> children = Lists.newArrayList();
        UserClassMembers members = userClass.getMembers();
        children.addAll(members.getFields());
        children.addAll(members.getProperties());
        children.addAll(members.getInnerTypes());
        children.addAll(members.getMethods());
        return filterNonSyntheticMembers(children).toArray();
    }
    
    private Object[] childrenOf(UserInterface userInterface) {
        List<Locatable> children = Lists.newArrayList();
        UserInterfaceMembers members = userInterface.getMembers();
        children.addAll(members.getMethods());
        return filterNonSyntheticMembers(children).toArray();
    }
    
    private Object[] childrenOf(UserEnum userEnum) {
        List<Locatable> children = Lists.newArrayList();
        children.addAll(userEnum.getFields());
        return filterNonSyntheticMembers(children).toArray();
    }
    
    private Object[] childrenOf(UserTrigger userTrigger) {
        List<Locatable> children = Lists.newArrayList();
        UserTriggerMembers members = userTrigger.getMembers();
        children.addAll(members.getInnerTypes());
        children.addAll(members.getFields());
        children.addAll(members.getMethods());
        return filterNonSyntheticMembers(children).toArray();
    }
    
    private List<Locatable> filterNonSyntheticMembers(List<Locatable> locatables) {
        return locatables.stream()
            .filter(l -> Locations.isReal(l.getLoc()))
            .sorted(LOCATABLE_COMPARATOR)
            .collect(Collectors.toList());
    }
    
    @Override
    public Object getParent(Object element) {
        return null;
    }
    
    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length >= 1;
    }
}