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
package com.salesforce.ide.ui.wizards.components;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public interface IComponentWizardComposite {

    Text getTxtName();

    String getComponentName();

    String getNameString();

    String getLabelString();

    void setComponentWizardPage(IComponentWizardPage page);

    Combo getCmbTemplateNames();

    void setEnabled(boolean b);

}
