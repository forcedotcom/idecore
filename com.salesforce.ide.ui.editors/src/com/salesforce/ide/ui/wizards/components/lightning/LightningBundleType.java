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
package com.salesforce.ide.ui.wizards.components.lightning;

import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.APP;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.AURADOC;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.CMP;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.CONTROLLER;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.CSS;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.DESIGN;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.EVT;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.HELPER;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.INTF;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.RENDERER;
import static com.salesforce.ide.ui.wizards.components.lightning.LightningElement.SVG;

import com.google.common.collect.ImmutableList;

/**
 * Represents the top-level bundle types that can be created.
 * 
 * @author nchen
 *         
 */
public enum LightningBundleType {
    
    APPLICATION(
        "Lightning Application",
        APP,
        ImmutableList.of(CONTROLLER, HELPER, RENDERER, CSS),
        ImmutableList.of(CONTROLLER, HELPER, RENDERER, CSS, DESIGN, SVG, AURADOC)),
    COMPONENT(
        "Lightning Component",
        CMP,
        ImmutableList.of(CONTROLLER, HELPER, RENDERER, CSS),
        ImmutableList.of(CONTROLLER, HELPER, RENDERER, CSS, DESIGN, SVG, AURADOC)),
    INTERFACE("Lightning Interface", INTF, ImmutableList.of(), ImmutableList.of()),
    EVENT("Lightning Event", EVT, ImmutableList.of(), ImmutableList.of()),
    TOKENS("Lightning Tokens", LightningElement.TOKENS, ImmutableList.of(), ImmutableList.of());
    
    public final String humanReadableName;
    public final LightningElement primaryElement;
    public final ImmutableList<LightningElement> secondaryElementsToInclude; // Usually you want a few other files by default.
    public final ImmutableList<LightningElement> allowableSecondaryElements; // This is the full list of allowable elements, it excludes the primary element since that must already be added at inception time.
    
    LightningBundleType(
        String humanReadableName,
        LightningElement primaryElement,
        ImmutableList<LightningElement> elementsToInclude,
        ImmutableList<LightningElement> allowableElements) {
        this.primaryElement = primaryElement;
        this.humanReadableName = humanReadableName;
        this.secondaryElementsToInclude = elementsToInclude;
        this.allowableSecondaryElements = allowableElements;
    }
}
