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

import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.ClassDeclUnit;
import apex.jorje.data.ast.CompilationUnit.EnumDeclUnit;
import apex.jorje.data.ast.CompilationUnit.InterfaceDeclUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;

import com.google.common.collect.Lists;

/**
 * This is the top-level visitor to extract all the root elements. We display everything except anonymous blocks.
 * Sometimes we "massage" the structure so that we don't show the pseudo-nodes like what we have in the grammar. This
 * creates a more streamlined outline view.
 * 
 * @author nchen
 * 
 */
public class RootElementFilter extends CompilationUnit.SwitchBlockWithDefault {
    private final List<Object> rootElements;

    public RootElementFilter() {
        this.rootElements = Lists.newArrayList();
    }

    @Override
    public void _case(TriggerDeclUnit x) {
        if (x.name != null) {
            rootElements.add(x);
        }
    }

    @Override
    public void _case(EnumDeclUnit x) {
        if (x.body != null) {
            rootElements.add(x.body);
        }
    }

    @Override
    public void _case(ClassDeclUnit x) {
        if (x.body != null) {
            rootElements.add(x.body);
        }
    }

    @Override
    public void _case(InterfaceDeclUnit x) {
        if (x.body != null) {
            rootElements.add(x.body);
        }
    }

    // Do nothing for anonymous blocks since that is not applicable for file-based metadata
    @Override
    protected void _default(CompilationUnit x) {}

    public Object[] getRootElements() {
        return rootElements.toArray();
    }
}
