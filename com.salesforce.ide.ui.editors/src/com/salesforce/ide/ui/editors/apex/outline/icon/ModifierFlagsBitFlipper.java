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

import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.ABSTRACT;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.FINAL;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.GLOBAL;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.OVERRIDE;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.PRIVATE;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.PROTECTED;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.PUBLIC;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.STATIC;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.TEST_METHOD;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.TRANSIENT;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.VIRTUAL;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.WEB_SERVICE;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.WITHOUT_SHARING;
import static apex.jorje.semantic.symbol.type.ModifierTypeInfos.WITH_SHARING;
import static apex.jorje.semantic.symbol.type.AnnotationTypeInfos.IS_TEST;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;

import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

import apex.jorje.semantic.ast.modifier.ModifierGroup;

/**
 * This class is used so that we can compute both the JVM and SWT-style accessor flags at the same time for the
 * modifiers. Apparently, both set of flags represent slightly different things but they chose to use same bit locations
 * - so, sticking them into the same bitvector will clobber one another. Thus, we have to maintain two different
 * bitvectors.
 * 
 * @author nchen
 */
public final class ModifierFlagsBitFlipper {
    public static final AccessorFlags flipBits(ModifierGroup modifiers) {
        Integer accessorFlag_JVM = 0;
        Integer accessorFlag_JDT = 0;
        
        if (modifiers.has(TEST_METHOD)) {
            accessorFlag_JVM |= ApexElementImageDescriptor.TESTMETHOD;
            accessorFlag_JDT |= ApexElementImageDescriptor.TESTMETHOD;
        }
        
        if (modifiers.has(IS_TEST)) {
            accessorFlag_JVM |= ApexElementImageDescriptor.TESTMETHOD;
            accessorFlag_JDT |= ApexElementImageDescriptor.TESTMETHOD;
        }

        if (modifiers.has(GLOBAL)) {
            accessorFlag_JVM |= ApexElementImageDescriptor.GLOBAL;
            accessorFlag_JDT |= ApexElementImageDescriptor.GLOBAL;
        }
        
        if (modifiers.has(WEB_SERVICE)) {
            accessorFlag_JVM |= ApexElementImageDescriptor.WEBSERVICE;
            accessorFlag_JDT |= ApexElementImageDescriptor.WEBSERVICE;
        }
        
        if (modifiers.has(PUBLIC)) {
            accessorFlag_JVM |= Flags.AccPublic;
        }
        
        if (modifiers.has(PRIVATE)) {
            accessorFlag_JVM |= Flags.AccPrivate;
        }
        
        if (modifiers.has(PROTECTED)) {
            accessorFlag_JVM |= Flags.AccProtected;
        }
        
        if (modifiers.has(WITH_SHARING)) {
            accessorFlag_JVM |= ApexElementImageDescriptor.WITHSHARING;
            accessorFlag_JDT |= ApexElementImageDescriptor.WITHSHARING;
        }
        
        if (modifiers.has(WITHOUT_SHARING)) {
            accessorFlag_JVM |= ApexElementImageDescriptor.WITHOUTSHARING;
            accessorFlag_JDT |= ApexElementImageDescriptor.WITHOUTSHARING;
        }
        
        if (modifiers.has(STATIC)) {
            accessorFlag_JVM |= Flags.AccStatic;
            accessorFlag_JDT |= JavaElementImageDescriptor.STATIC;
        }
        
        if (modifiers.has(TRANSIENT)) {
            accessorFlag_JVM |= Flags.AccTransient;
            accessorFlag_JDT |= JavaElementImageDescriptor.TRANSIENT;
        }
        
        if (modifiers.has(ABSTRACT)) {
            accessorFlag_JVM |= Flags.AccAbstract;
            accessorFlag_JDT |= JavaElementImageDescriptor.ABSTRACT;
        }
        
        if (modifiers.has(FINAL)) {
            accessorFlag_JVM |= Flags.AccFinal;
            accessorFlag_JDT |= JavaElementImageDescriptor.FINAL;
        }
        
        if (modifiers.has(OVERRIDE)) {
            accessorFlag_JDT |= JavaElementImageDescriptor.OVERRIDES;
        }
        
        if (modifiers.has(VIRTUAL)) {
            accessorFlag_JVM |= ApexElementImageDescriptor.VIRTUAL;
            accessorFlag_JDT |= ApexElementImageDescriptor.VIRTUAL;
        }
        
        return new AccessorFlags(accessorFlag_JVM, accessorFlag_JDT);
    }
}