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

import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.ABSTRACT;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.FINAL;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.GLOBAL;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.PRIVATE;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.PROTECTED;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.PUBLIC;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.STATIC;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.TEST_METHOD;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.TRANSIENT;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.WEB_SERVICE;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.WITHOUT_SHARING;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.WITH_SHARING;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;

import com.salesforce.ide.ui.editors.apex.outline.icon.AccessorFlags;
import com.salesforce.ide.ui.editors.apex.outline.icon.ModifierFlagsBitFlipper;
import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

import apex.jorje.data.Locations;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.Modifier.Annotation;
import apex.jorje.semantic.ast.modifier.ModifierGroup;
import apex.jorje.semantic.symbol.type.ModifierTypeInfos;
import junit.framework.TestCase;

/**
 * Tests that we are flipping the bits correctly for the icons in the outline view.
 * 
 * @author nchen
 *         
 */
public class ModifierFlagsBitFlipperTest_unit extends TestCase {
    
    ModifierFlagsBitFlipper handler;
    AccessorFlags flags;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        flags = new AccessorFlags();
        handler = new ModifierFlagsBitFlipper();
    }
    
    public void testTestMethodModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(TEST_METHOD) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.TESTMETHOD) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.TESTMETHOD) != 0);
    }

    public void testIsTestAnnotation() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addAnnotationAndResolve(new Annotation(Locations.NONE, new Identifier(Locations.NONE, "isTest"), null)) 
            .build()
            .resolve();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.TESTMETHOD) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.TESTMETHOD) != 0);
    }
    
    public void testGlobalModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(GLOBAL) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.GLOBAL) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.GLOBAL) != 0);
    }
    
    public void testWebServiceModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(WEB_SERVICE) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.WEBSERVICE) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.WEBSERVICE) != 0);
    }
    
    public void testPublicModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(PUBLIC) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccPublic) != 0);
        assertTrue(twiddled.accessorFlags_JDT == 0);
    }
    
    public void testPrivateModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(PRIVATE) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccPrivate) != 0);
        assertTrue(twiddled.accessorFlags_JDT == 0);
    }
    
    public void testProtectedModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(PROTECTED) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccProtected) != 0);
        assertTrue(twiddled.accessorFlags_JDT == 0);
    }
    
    public void testWithSharingModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(WITH_SHARING) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.WITHSHARING) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.WITHSHARING) != 0);
    }
    
    public void testWithoutSharingModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(WITHOUT_SHARING) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.WITHOUTSHARING) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.WITHOUTSHARING) != 0);
    }
    
    public void testStaticModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(STATIC) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccStatic) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.STATIC) != 0);
    }
    
    public void testTransientModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(TRANSIENT) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccTransient) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.TRANSIENT) != 0);
    }
    
    public void testAbstractModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(ABSTRACT) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccAbstract) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.ABSTRACT) != 0);
    }
    
    public void testFinalModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(FINAL) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & Flags.AccFinal) != 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.FINAL) != 0);
    }
    
    public void testOverrideModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(ModifierTypeInfos.OVERRIDE) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue(twiddled.accessorFlags_JVM == 0);
        assertTrue((twiddled.accessorFlags_JDT & JavaElementImageDescriptor.OVERRIDES) != 0);
    }
    
    public void testVirtualModifier() {
        ModifierGroup modifiers = ModifierGroup.builder()
            .addModifiers(ModifierTypeInfos.VIRTUAL) 
            .build();
        AccessorFlags twiddled = ModifierFlagsBitFlipper.flipBits(modifiers);
        assertTrue((twiddled.accessorFlags_JVM & ApexElementImageDescriptor.VIRTUAL) != 0);
        assertTrue((twiddled.accessorFlags_JDT & ApexElementImageDescriptor.VIRTUAL) != 0);
    }
}