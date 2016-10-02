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
package com.salesforce.ide.ui.editors.lightning.misc;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import com.salesforce.ide.ui.wizards.components.lightning.LightningBundleType;
import com.salesforce.ide.ui.wizards.components.lightning.LightningElement;

import junit.framework.TestCase;

/**
 * Tests for the specifications of what is included by default and allowe in a lightning bundle.
 * 
 * @author nchen
 * 
 */
public class LightningBundlesDefaultTest_unit extends TestCase {
	public void testLightningApplication() {
		LightningBundleType app = LightningBundleType.APPLICATION;

		assertThat(app.humanReadableName, is("Lightning Application"));
		assertThat(app.primaryElement, is(APP));
		assertThat(app.secondaryElementsToInclude, contains(CONTROLLER, HELPER, RENDERER, CSS));
		assertThat(app.allowableSecondaryElements, contains(CONTROLLER, HELPER, RENDERER, CSS, DESIGN, SVG, AURADOC));
	}

	public void testLightningComponent() {
		LightningBundleType cmp = LightningBundleType.COMPONENT;

		assertThat(cmp.humanReadableName, is("Lightning Component"));
		assertThat(cmp.primaryElement, is(CMP));
		assertThat(cmp.secondaryElementsToInclude, contains(CONTROLLER, HELPER, RENDERER, CSS));
		assertThat(cmp.allowableSecondaryElements, contains(CONTROLLER, HELPER, RENDERER, CSS, DESIGN, SVG, AURADOC));

	}

	public void testLightningInterface() {
		LightningBundleType intf = LightningBundleType.INTERFACE;

		assertThat(intf.humanReadableName, is("Lightning Interface"));
		assertThat(intf.primaryElement, is(INTF));
		assertThat(intf.secondaryElementsToInclude, is(empty()));
		assertThat(intf.allowableSecondaryElements, is(empty()));
	}

	public void testLightningEvent() {
		LightningBundleType evt = LightningBundleType.EVENT;

		assertThat(evt.humanReadableName, is("Lightning Event"));
		assertThat(evt.primaryElement, is(EVT));
		assertThat(evt.secondaryElementsToInclude, is(empty()));
		assertThat(evt.allowableSecondaryElements, is(empty()));
	}

	public void testLightningTokens() {
		LightningBundleType tokens = LightningBundleType.TOKENS;

		assertThat(tokens.humanReadableName, is("Lightning Tokens"));
		assertThat(tokens.primaryElement, is(LightningElement.TOKENS));
		assertThat(tokens.secondaryElementsToInclude, is(empty()));
		assertThat(tokens.allowableSecondaryElements, is(empty()));
	}

}
