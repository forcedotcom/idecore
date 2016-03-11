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
package com.salesforce.ide.ui.editors.apex.outline.icon;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.salesforce.ide.ui.editors.apex.outline.IOutlineViewElementHandler;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.member.Field;
import apex.jorje.semantic.ast.member.Method;
import apex.jorje.semantic.ast.member.Property;
import apex.jorje.semantic.ast.modifier.ModifierGroup;

/**
 * Provides the icons for the outline view. Most, if not all, of the icons borrow from JDT. Thus, it is necessary to
 * have the JDT installed before using the Force.com IDE.
 * 
 * @author nchen
 *         
 */
@SuppressWarnings("restriction")
public class OutlineViewIconProvider implements IOutlineViewElementHandler<Image> {
    public static final Point APEX_ICON_SIZE = new Point(20, 20);
    
    private static Image getTypeImage(int accessorFlags_JVM, int accessorFlags_JDT, boolean isInner) {
        if ((accessorFlags_JVM & ApexElementImageDescriptor.GLOBAL) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_CLASS);
            ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_GLOBAL_CLASS, accessorFlags_JVM, decoratedDesc);
        }
        
        ImageDescriptor desc =
            JavaElementImageProvider.getTypeImageDescriptor(isInner, false, accessorFlags_JVM, false);
        ApexElementImageDescriptor decoratedDesc =
            new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.JDT_CLASS, accessorFlags_JVM, decoratedDesc);
    }
    
    private static Image getImageForMethodLikeElements(
        MethodMember element,
        int accessorFlags_JVM,
        int accessorFlags_JDT) {
        if ((accessorFlags_JVM & ApexElementImageDescriptor.WEBSERVICE) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_METHOD);
            
            if (element != null) {
                accessorFlags_JDT = adornWithMethodClassDetails(element, accessorFlags_JDT);
            }
            
            ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_GLOBAL_METHOD, accessorFlags_JVM, decoratedDesc);
        } else if ((accessorFlags_JVM & ApexElementImageDescriptor.GLOBAL) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_METHOD);
            int decorationFlags = 0; // no further decoration needed.
            ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, decorationFlags, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_GLOBAL_METHOD, accessorFlags_JVM, decoratedDesc);
        } else if ((accessorFlags_JVM & ApexElementImageDescriptor.TESTMETHOD) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_TEST_METHOD);
            int decorationFlags = 0; // no further decoration needed.
            ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, decorationFlags, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_TEST_METHOD, accessorFlags_JVM, decoratedDesc);
        } else {
            
            // TODO: The first parameter determines if we are in an interface for annotation, where all methods are public.
            // There is no way to determine this easily using JADT (cannot check parent) unless we store some environment variables.
            // Worse, interfaces in Apex cannot annotate their methods as public so we have no way to use local information to check for "publicness".
            
            ImageDescriptor desc = JavaElementImageProvider.getMethodImageDescriptor(false, accessorFlags_JVM);
            
            if (element != null) {
                accessorFlags_JDT = adornWithMethodClassDetails(element, accessorFlags_JDT);
            }
            
            ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.JDT_METHOD, accessorFlags_JVM, decoratedDesc);
        }
    }
    
    AccessorFlags computeAccessorFlags(ModifierGroup modifiers) {
        return ModifierFlagsBitFlipper.flipBits(modifiers);
    }
    
    /*
     * Adds decoration for constructor
     */
    private static int adornWithMethodClassDetails(MethodMember element, int accessorFlags_JDT) {
        int flags = accessorFlags_JDT;
        if (!element.methodDecl.type.isPresent()) { // No return type means that it is a constructor
            flags |= JavaElementImageDescriptor.CONSTRUCTOR;
        }
        return flags;
    }
    
    @Override
    public Image handle(UserClass userClass) {
        ModifierGroup modifiers = userClass.getModifiers().getModifiers();
        AccessorFlags flags = computeAccessorFlags(modifiers);
        boolean isInner = false;
        return getTypeImage(flags.accessorFlags_JVM, flags.accessorFlags_JDT, isInner);
    }
    
    @Override
    public Image handle(UserInterface userInterface) {
        ModifierGroup modifiers = userInterface.getModifiers().getModifiers();
        AccessorFlags flags = computeAccessorFlags(modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;
        accessorFlags_JVM |= Flags.AccInterface;
        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, false);
    }
    
    @Override
    public Image handle(UserTrigger userTrigger) {
        int accessorFlags = 0;
        ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_TRIGGER);
        int decorationFlags = 0; // no further decoration needed.
        ApexElementImageDescriptor decoratedDesc =
            new ApexElementImageDescriptor(desc, decorationFlags, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.APEX_TRIGGER, accessorFlags, decoratedDesc);
    }
    
    @Override
    public Image handle(UserEnum userEnum) {
        ModifierGroup modifiers = userEnum.getModifiers().getModifiers();
        AccessorFlags flags = computeAccessorFlags(modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;
        accessorFlags_JVM |= Flags.AccEnum;
        boolean isInner = false;
        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, isInner);
    }
    
    @Override
    public Image handle(Method method) {
        ModifierGroup modifiers = method.getModifiers();
        AccessorFlags flags = computeAccessorFlags(modifiers);

        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        return getImageForMethodLikeElements(null, accessorFlags_JVM, accessorFlags_JDT);
    }
    
    @Override
    public Image handle(Property property) {
        ModifierGroup modifiers = property.getModifiers();
        AccessorFlags flags = computeAccessorFlags(modifiers);

        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        return getImageForMethodLikeElements(null, accessorFlags_JVM, accessorFlags_JDT);
    }
    
    @Override
    public Image handle(Field field) {
        ModifierGroup modifiers = field.getModifierInfo();
        AccessorFlags flags = computeAccessorFlags(modifiers);
        
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;
        
        if ((accessorFlags_JVM & ApexElementImageDescriptor.WEBSERVICE) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_FIELD);
            ApexElementImageDescriptor decoratedDesc = new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_GLOBAL_METHOD, accessorFlags_JVM, decoratedDesc);
        } else if ((accessorFlags_JVM & ApexElementImageDescriptor.GLOBAL) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_FIELD);
            int decorationFlags = 0; // no further decoration needed.
            ApexElementImageDescriptor decoratedDesc = new ApexElementImageDescriptor(desc, decorationFlags, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_GLOBAL_FIELD, accessorFlags_JVM, decoratedDesc);
        } else {
            ImageDescriptor desc = JavaElementImageProvider.getFieldImageDescriptor(false, accessorFlags_JVM);
            ApexElementImageDescriptor decoratedDesc = new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.JDT_FIELD, accessorFlags_JVM, decoratedDesc);
        }
    }
}
