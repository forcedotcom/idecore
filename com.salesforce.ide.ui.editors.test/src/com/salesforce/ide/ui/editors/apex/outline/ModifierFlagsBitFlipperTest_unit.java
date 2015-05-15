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

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;

import apex.jorje.data.Loc;
import apex.jorje.data.ast.AnnotationParameter;
import apex.jorje.data.ast.Identifier;
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

import com.salesforce.ide.ui.editors.apex.outline.icon.AccessorFlags;
import com.salesforce.ide.ui.editors.apex.outline.icon.ModifierFlagsBitFlipper;
import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

/**
 * Tests that we are flipping the bits correctly.
 * 
 * @author nchen
 * 
 */
public class ModifierFlagsBitFlipperTest_unit extends TestCase {

    BitsFlipperHandlerProxy handler;
    AccessorFlags flags;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        flags = new AccessorFlags();
        handler = new BitsFlipperHandlerProxy(flags);
    }

    // This is also an interesting pattern of casting null so let me explain what is going on.
    // I want to test that an action is performed upon receiving a modifer of that type, e.g., TestMethodModifier.
    // Instead of creating an actual testMethodModifier, I just cast null to it and force a dispatch.
    // This is fine, since I never use the value of it anyway, so no NullPointerException will happen.
    public void testTestMethodModifier() {
        handler._case((TestMethodModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.TESTMETHOD) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.TESTMETHOD) != 0);
    }

    public void testGlobalModifier() {
        handler._case((GlobalModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.GLOBAL) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.GLOBAL) != 0);
    }

    public void testWebServiceModifier() {
        handler._case((WebServiceModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.WEBSERVICE) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.WEBSERVICE) != 0);
    }

    public void testPublicModifier() {
        handler._case((PublicModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccPublic) != 0);
        assertTrue(twiddled.accessorFlags_JDT == 0);
    }

    public void testPrivateModifier() {
        handler._case((PrivateModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccPrivate) != 0);
        assertTrue(twiddled.accessorFlags_JDT == 0);
    }

    public void testProtectedModifier() {
        handler._case((ProtectedModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccProtected) != 0);
        assertTrue(twiddled.accessorFlags_JDT == 0);
    }

    public void testWithSharingModifier() {
        handler._case((WithSharingModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.WITHSHARING) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.WITHSHARING) != 0);
    }

    public void testWithoutSharingModifier() {
        handler._case((WithoutSharingModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.WITHOUTSHARING) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.WITHOUTSHARING) != 0);
    }

    public void testStaticModifier() {
        handler._case((StaticModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccStatic) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.STATIC) != 0);
    }

    public void testTransientModifier() {
        handler._case((TransientModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccTransient) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.TRANSIENT) != 0);
    }

    public void testAbstractModifier() {
        handler._case((AbstractModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccAbstract) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.ABSTRACT) != 0);
    }

    public void testFinalModifier() {
        handler._case((FinalModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccFinal) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.FINAL) != 0);
    }

    public void testOverrideModifier() {
        handler._case((OverrideModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue(twiddled.accessorFlags_JVM == 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.OVERRIDES) != 0);
    }

    public void testVirtualModifier() {
        handler._case((VirtualModifier) null);
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.VIRTUAL) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.VIRTUAL) != 0);
    }

    public void testAnnotation() {
        handler._case((Annotation) Modifier._Annotation(Loc._SyntheticLoc(), Identifier._Identifier(Loc._SyntheticLoc(), "IsTest"),
            (List<AnnotationParameter>) null));
        AccessorFlags twiddled = handler.getAccessorFlags();
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.TESTMETHOD) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.TESTMETHOD) != 0);
    }

    /*
     * The following is a bit unusual, but I also want to test that our tests handles all the necessary modifiers (by implementing Modifier.SwitchBlock)
     * If it doesn't we should throw an error at compile time (not run-time). 
     */
    final class BitsFlipperHandlerProxy implements Modifier.SwitchBlock {

        ModifierFlagsBitFlipper handler;

        public BitsFlipperHandlerProxy(AccessorFlags flags) {
            handler = new ModifierFlagsBitFlipper(flags);
        }

        public AccessorFlags getAccessorFlags() {
            return handler.getAccessorFlags();
        }

        @Override
        public void _case(TestMethodModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(GlobalModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(WebServiceModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(PublicModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(PrivateModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(ProtectedModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(WithSharingModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(WithoutSharingModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(StaticModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(TransientModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(AbstractModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(FinalModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(OverrideModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(VirtualModifier x) {
            handler._case(x);
        }

        @Override
        public void _case(Annotation x) {
            handler._case(x);
        }
    }
}
