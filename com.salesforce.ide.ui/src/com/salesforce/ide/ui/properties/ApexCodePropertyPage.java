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
package com.salesforce.ide.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.views.LoggingComposite;

public class ApexCodePropertyPage extends BasePropertyPage {

    protected RunTestLoggingPropertyComposite runTestLoggingPropertyComposite;
    private LoggingInfo[] cachedExeAnonymousLoggingSetting;
    public LoggingInfo[] getCachedExeAnonymousLoggingSetting() {
        return cachedExeAnonymousLoggingSetting;
    }

    public LoggingInfo[] getCachedRunTestLoggingSetting() {
        return cachedRunTestLoggingSetting;
    }

    private LoggingInfo[] cachedRunTestLoggingSetting;

    public ApexCodePropertyPage() {
        super();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        getDefaultsButton().setEnabled(false);

        UIUtils.setHelpContext(getControl(), this.getClass().getSimpleName());
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite propertyComposite = new Composite(parent, SWT.NONE);
        propertyComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 1;
        propertyComposite.setLayout(gridLayout1);

        // run test logging setting: new logging framework
        cachedRunTestLoggingSetting =
                getLoggingService().getAllLoggingInfo(getProject(), LoggingInfo.SupportedFeatureEnum.RunTest);
        LoggingComposite runTestLoggingComposite =
                new LoggingComposite(propertyComposite, getLoggingService(), SWT.NONE, true,
                        LoggingInfo.SupportedFeatureEnum.RunTest);
        runTestLoggingComposite.enable(getProject());

        // execute anonymous logging setting: new logging framework
        cachedExeAnonymousLoggingSetting =
                getLoggingService().getAllLoggingInfo(getProject(), LoggingInfo.SupportedFeatureEnum.ExecuteAnonymous);
        LoggingComposite loggingComposite =
                new LoggingComposite(propertyComposite, getLoggingService(), SWT.NONE, true,
                        LoggingInfo.SupportedFeatureEnum.ExecuteAnonymous);
        loggingComposite.enable(getProject());

        UIUtils.setHelpContext(propertyComposite, this.getClass().getSimpleName());

        return propertyComposite;
    }

    protected IProject getProject() {
        return (IProject) getElement();
    }

    @Override
    protected void performApply() {
        performOk();
    }

    @Override
    public boolean performCancel() {
        getLoggingService().setAllLoggingInfo(getProject(), cachedRunTestLoggingSetting,
            LoggingInfo.SupportedFeatureEnum.RunTest);
        getLoggingService().setAllLoggingInfo(getProject(), cachedExeAnonymousLoggingSetting,
            LoggingInfo.SupportedFeatureEnum.ExecuteAnonymous);
        return super.performCancel();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        return true;
    }
}
