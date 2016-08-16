/*******************************************************************************
* Copyright (c) 2016 Salesforce.com, inc..
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors:
*     Salesforce.com, inc. - initial API and implementation
*******************************************************************************/
package com.salesforce.ide.ui.wizards.components.lightning;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.salesforce.ide.ui.wizards.components.apex.CodeWizardComposite;

/**
 * @author nchen
 *         
 */
public class AuraDefinitionBundleWizardComposite extends CodeWizardComposite {
    private Combo typeCombo;
    
    public AuraDefinitionBundleWizardComposite(
        Composite parent,
        int style,
        String displayName,
        Set<String> supportedApiVesions) {
        super(parent, style, displayName);
        initialize(supportedApiVesions);
    }
    
    protected void initialize(Set<String> supportedApiVersions) {
        setLayout(new GridLayout());
        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Group grpProperties = createPropertiesGroup(this);
        createNameText(grpProperties);
        createApiVersionCombo(grpProperties, supportedApiVersions);
        createCombo("Type: ", grpProperties, getSupportedTopLevelBundleTypes());
        initSize();
    }
    
    private Set<String> getSupportedTopLevelBundleTypes() {
        return Arrays.stream(LightningBundleType.values())
            .map(e -> e.humanReadableName)
            .collect(Collectors.toCollection(TreeSet::new));
    }
    
    protected final void createCombo(String text, Group containingGroup, Set<String> bundleTypes) {
        Label lblApiVersion = new Label(containingGroup, SWT.NONE);
        lblApiVersion.setText(text);
        lblApiVersion.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        typeCombo = new Combo(containingGroup, SWT.BORDER | SWT.READ_ONLY);
        typeCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));
        for (String bundleType : bundleTypes) {
            typeCombo.add(bundleType);
        }
        typeCombo.select(1); // Select Component as the default
    }
    
    public LightningBundleType getSelectedBundleType() {
        String selection = typeCombo.getText();
        return Arrays.stream(LightningBundleType.values())
            .filter(l -> l.humanReadableName.equals(selection))
            .findFirst().map(b -> b)
            .orElse(LightningBundleType.COMPONENT);
    }
}
