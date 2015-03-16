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
package com.salesforce.ide.ui.editors.apex.outline.icon;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import apex.jorje.data.Optional;
import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;
import apex.jorje.data.ast.Modifier;

import com.salesforce.ide.ui.editors.apex.outline.IOutlineViewElementHandler;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

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

    @Override
    public Image handle(TriggerDeclUnit element) {
        int accessorFlags = 0;
        ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_TRIGGER);
        int decorationFlags = 0; // no further decoration needed.
        ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, decorationFlags, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.APEX_TRIGGER, accessorFlags, decoratedDesc);
    }

    @Override
    public Image handle(EnumDecl element) {
        AccessorFlags flags = computeAccessorFlags(element.modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        accessorFlags_JVM |= Flags.AccEnum;

        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, false);
    }

    @Override
    public Image handle(ClassDecl element) {
        AccessorFlags flags = computeAccessorFlags(element.modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, false);
    }

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

    @Override
    public Image handle(InterfaceDecl element) {
        AccessorFlags flags = computeAccessorFlags(element.modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        accessorFlags_JVM |= Flags.AccInterface;

        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, false);
    }

    @Override
    public Image handle(InnerEnumMember element) {
        AccessorFlags flags = computeAccessorFlags(element.body.modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        accessorFlags_JVM |= Flags.AccEnum;

        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, true);
    }

    @Override
    public Image handle(InnerClassMember element) {
        AccessorFlags flags = computeAccessorFlags(element.body.modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, true);
    }

    @Override
    public Image handle(InnerInterfaceMember element) {
        AccessorFlags flags = computeAccessorFlags(element.body.modifiers);
        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        accessorFlags_JVM |= Flags.AccInterface;

        return getTypeImage(accessorFlags_JVM, accessorFlags_JDT, true);
    }

    @Override
    public Image handle(StaticStmntBlockMember element) {
        int accessorFlags_JVM = Flags.AccPrivate | Flags.AccStatic;
        int accessorFlags_JDT = JavaElementImageDescriptor.STATIC;

        ImageDescriptor desc = JavaElementImageProvider.getMethodImageDescriptor(false, accessorFlags_JVM);
        ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.JDT_METHOD, accessorFlags_JVM, decoratedDesc);
    }

    @Override
    public Image handle(StmntBlockMember element) {
        int accessorFlags_JVM = Flags.AccPrivate;

        ImageDescriptor desc = JavaElementImageProvider.getMethodImageDescriptor(false, accessorFlags_JVM);
        ApexElementImageDescriptor decoratedDesc = new ApexElementImageDescriptor(desc, 0, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.JDT_METHOD, accessorFlags_JVM, decoratedDesc);
    }

    @Override
    public Image handle(FieldMember element) {
        AccessorFlags flags = computeAccessorFlags(element.variableDecls.modifiers);

        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        if ((accessorFlags_JVM & ApexElementImageDescriptor.WEBSERVICE) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_FIELD);
            ApexElementImageDescriptor decoratedDesc =
                    new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_GLOBAL_METHOD, accessorFlags_JVM, decoratedDesc);
        } else if ((accessorFlags_JVM & ApexElementImageDescriptor.GLOBAL) != 0) {
            ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_FIELD);
            int decorationFlags = 0; // no further decoration needed.
            ApexElementImageDescriptor decoratedDesc =
                    new ApexElementImageDescriptor(desc, decorationFlags, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.APEX_GLOBAL_FIELD, accessorFlags_JVM, decoratedDesc);
        } else {
            ImageDescriptor desc = JavaElementImageProvider.getFieldImageDescriptor(false, accessorFlags_JVM);
            ApexElementImageDescriptor decoratedDesc =
                    new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
            return ForceImages.get(ForceImages.JDT_FIELD, accessorFlags_JVM, decoratedDesc);
        }
    }

    @Override
    public Image handle(MethodMember element) {
        AccessorFlags flags = computeAccessorFlags(element.methodDecl.modifiers);

        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        return getImageForMethodLikeElements(element, accessorFlags_JVM, accessorFlags_JDT);
    }

    @Override
    public Image handle(PropertyMember element) {
        AccessorFlags flags = computeAccessorFlags(element.propertyDecl.modifiers);

        int accessorFlags_JVM = flags.accessorFlags_JVM;
        int accessorFlags_JDT = flags.accessorFlags_JDT;

        return getImageForMethodLikeElements(null, accessorFlags_JVM, accessorFlags_JDT);
    }

    private static Image getImageForMethodLikeElements(MethodMember element, int accessorFlags_JVM, int accessorFlags_JDT) {
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

    /*
     * This just handles the enum fields. It does not handle all identifiers as its name might imply.
     */
    @Override
    public Image handle(Identifier element) {
        int accessorFlags_JVM = Flags.AccFinal | Flags.AccStatic;
        int accessorFlags_JDT = JavaElementImageDescriptor.FINAL | JavaElementImageDescriptor.STATIC;

        ImageDescriptor desc = JavaElementImageProvider.getFieldImageDescriptor(false, accessorFlags_JVM);
        ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.JDT_FIELD, accessorFlags_JVM, decoratedDesc);
    }

    AccessorFlags computeAccessorFlags(Iterable<Modifier> modifiers) {
        AccessorFlags flags = new AccessorFlags();

        for (Modifier modifier : modifiers) {
            ModifierFlagsBitFlipper switchBlock = new ModifierFlagsBitFlipper(flags);
            modifier._switch(switchBlock);
            flags = switchBlock.getAccessorFlags();
        }
        return flags;
    }

    /*
     * Adds decoration for constructor
     */
    private static int adornWithMethodClassDetails(MethodMember element, int accessorFlags_JDT) {
        int flags = accessorFlags_JDT;
        if (element.methodDecl.type instanceof Optional.None) { // No return type means that it is a constructor
            flags |= JavaElementImageDescriptor.CONSTRUCTOR;
        }
        return flags;
    }

}
