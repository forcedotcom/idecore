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

import apex.jorje.data.Loc;
import apex.jorje.data.Loc.MatchBlock;
import apex.jorje.data.Loc.RealLoc;
import apex.jorje.data.Loc.SyntheticLoc;
import apex.jorje.semantic.ast.Locatable;
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
     * Filters out only real locations.
     */
    private static final MatchBlock<Boolean> REAL_LOC_FILTER_FN = new Loc.MatchBlock<Boolean>() {
        @Override
        public Boolean _case(RealLoc x) {
            return true;
        }
        
        @Override
        public Boolean _case(SyntheticLoc x) {
            return false;
        }
    };
    
    /**
     * Compares two Loc for ordering. Only RealLocs have an ordering defined. SyntheticLocs have no ordering.
     */
    public static final Comparator<Locatable> LOCATABLE_COMPARATOR = new Comparator<Locatable>() {
        @Override
        public int compare(Locatable left, Locatable right) {
            Loc leftLoc = left.getLoc();
            Loc rightLoc = right.getLoc();
            return leftLoc.match(new Loc.MatchBlock<Integer>() {
                
                @Override
                public Integer _case(RealLoc realLeftLoc) {
                    return rightLoc.match(new Loc.MatchBlock<Integer>() {
                        
                        @Override
                        public Integer _case(RealLoc realRightLoc) {
                            if (realLeftLoc.line != realRightLoc.line) {
                                return Integer.compare(realLeftLoc.line, realRightLoc.line);
                            } else {
                                return Integer.compare(realLeftLoc.column, realRightLoc.column);
                            }
                        }
                        
                        @Override
                        public Integer _case(SyntheticLoc x) {
                            return 0;
                        }
                    });
                }
                
                @Override
                public Integer _case(SyntheticLoc synthetic) {
                    return 0;
                }
            });
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
        return locatables.stream().filter(l -> l.getLoc().match(REAL_LOC_FILTER_FN)).sorted(LOCATABLE_COMPARATOR)
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
