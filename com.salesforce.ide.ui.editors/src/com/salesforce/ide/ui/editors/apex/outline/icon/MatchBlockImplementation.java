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
package com.salesforce.ide.ui.editors.apex.outline.icon;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;

import apex.jorje.data.ast.Modifier;
import apex.jorje.data.ast.Modifier.AbstractModifier;
import apex.jorje.data.ast.Modifier.Annotation;
import apex.jorje.data.ast.Modifier.FinalModifier;
import apex.jorje.data.ast.Modifier.GlobalModifier;
import apex.jorje.data.ast.Modifier.OverrideModifier;
import apex.jorje.data.ast.Modifier.PrivateModifier;
import apex.jorje.data.ast.Modifier.ProtectedModifier;
import apex.jorje.data.ast.Modifier.PublicModifier;
import apex.jorje.data.ast.Modifier.StaticModifier;
import apex.jorje.data.ast.Modifier.TestMethodModifier;
import apex.jorje.data.ast.Modifier.TransientModifier;
import apex.jorje.data.ast.Modifier.VirtualModifier;
import apex.jorje.data.ast.Modifier.WebServiceModifier;
import apex.jorje.data.ast.Modifier.WithSharingModifier;
import apex.jorje.data.ast.Modifier.WithoutSharingModifier;

import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

/**
 * Determines the accessor flags based on the modifiers set. Since there is a list of modifiers, you may need to bitwise
 * AND the modifiers together. Pass in the value that you want to AND into the constructor.
 * 
 * REVIEWME: Is there a faster way to do this than to rely on static method dispatch on the type?
 * 
 * TODO: Add support for all the different annotations in
 * http://www.salesforce.com/us/developer/docs/apexcode/Content/apex_classes_annotation.htm
 * 
 * @author nchen
 * 
 */
final class MatchBlockImplementation implements Modifier.SwitchBlock {

    private Integer accessorFlag_JVM;
    private Integer accessorFlag_JDT;

    public MatchBlockImplementation(AccessorFlags flags) {
        this.accessorFlag_JVM = flags.accessorFlags_JVM;
        this.accessorFlag_JDT = flags.accessorFlags_JDT;
    }

    @Override
    public void _case(TestMethodModifier x) {
        accessorFlag_JVM |= ApexElementImageDescriptor.TESTMETHOD;
        accessorFlag_JDT |= ApexElementImageDescriptor.TESTMETHOD;
    }

    @Override
    public void _case(GlobalModifier x) {
        accessorFlag_JVM |= ApexElementImageDescriptor.GLOBAL;
        accessorFlag_JDT |= ApexElementImageDescriptor.GLOBAL;
    }

    @Override
    public void _case(WebServiceModifier x) {
        accessorFlag_JVM |= ApexElementImageDescriptor.WEBSERVICE;
        accessorFlag_JDT |= ApexElementImageDescriptor.WEBSERVICE;
    }

    @Override
    public void _case(PublicModifier x) {
        accessorFlag_JVM |= Flags.AccPublic;
    }

    @Override
    public void _case(PrivateModifier x) {
        accessorFlag_JVM |= Flags.AccPrivate;
    }

    @Override
    public void _case(ProtectedModifier x) {
        accessorFlag_JVM |= Flags.AccProtected;
    }

    @Override
    public void _case(WithSharingModifier x) {
        accessorFlag_JVM |= ApexElementImageDescriptor.WITHSHARING;
        accessorFlag_JDT |= ApexElementImageDescriptor.WITHSHARING;
    }

    @Override
    public void _case(WithoutSharingModifier x) {
        accessorFlag_JVM |= ApexElementImageDescriptor.WITHOUTSHARING;
        accessorFlag_JDT |= ApexElementImageDescriptor.WITHOUTSHARING;
    }

    @Override
    public void _case(StaticModifier x) {
        accessorFlag_JVM |= Flags.AccStatic;
        accessorFlag_JDT |= JavaElementImageDescriptor.STATIC;
    }

    @Override
    public void _case(TransientModifier x) {
        accessorFlag_JVM |= Flags.AccTransient;
        accessorFlag_JDT |= JavaElementImageDescriptor.TRANSIENT;
    }

    @Override
    public void _case(AbstractModifier x) {
        accessorFlag_JVM |= Flags.AccAbstract;
        accessorFlag_JDT |= JavaElementImageDescriptor.ABSTRACT;
    }

    @Override
    public void _case(FinalModifier x) {
        accessorFlag_JVM |= Flags.AccFinal;
        accessorFlag_JDT |= JavaElementImageDescriptor.FINAL;
    }

    @Override
    public void _case(OverrideModifier x) {
        accessorFlag_JDT |= JavaElementImageDescriptor.OVERRIDES;
    }

    @Override
    public void _case(VirtualModifier x) {
        accessorFlag_JVM |= ApexElementImageDescriptor.VIRTUAL;
        accessorFlag_JDT |= ApexElementImageDescriptor.VIRTUAL;
    }

    /*
     * Another way to another a test method is to use the @IsTest annotation so be sure to set it here too.
     */
    @Override
    public void _case(Annotation x) {
        if (x.name.value.equalsIgnoreCase("istest")) {
            accessorFlag_JVM |= ApexElementImageDescriptor.TESTMETHOD;
            accessorFlag_JDT |= ApexElementImageDescriptor.TESTMETHOD;
        }
    }
    
    public AccessorFlags getAccessorFlags() {
        return new AccessorFlags(accessorFlag_JVM, accessorFlag_JDT);
    }
}
