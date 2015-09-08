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
package com.salesforce.ide.ui.views;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

import com.salesforce.ide.api.metadata.types.LogCategoryExt;
import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.LoggingInfo.SupportedFeatureEnum;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.LoggingService;

/**
 *
 * @author fchang
 */
public class LoggingComposite extends Composite {
    private static final String INIT_PLUS_PREFIX = "+               ";
    private static final String PLUS_PREFIX = "+   ";

    protected Combo logCategoryCombo = null;
    protected Scale logLevelScale = null;
    protected CLabel logLevelDesc = null;
    protected LoggingService loggingService = null;
    protected IProject project = null;
    protected SupportedFeatureEnum supportedFeatureEnum;

    /**
     * Create the composite
     * @param parent
     * @param style
     * @param supportedFeatureEnum
     * @param controller
     */
    public LoggingComposite(Composite parent, LoggingService loggingService, int style, boolean withGrouping,
            SupportedFeatureEnum supportedFeatureEnum) {
        super(parent, style);
        this.loggingService = loggingService;
        this.supportedFeatureEnum = supportedFeatureEnum;
        setLayout(new GridLayout());
        if (withGrouping) {
            Group group = new Group(this, SWT.NONE);
            String groupingTitle =
                    SupportedFeatureEnum.ExecuteAnonymous == supportedFeatureEnum ? "Execute Anonymous Logging Setting"
                            : "Deploy Logging Setting";
            group.setText(groupingTitle);
            group.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
            group.setLayout(new GridLayout(1, true));
            initialize(group);
        } else {
            initialize(parent);
        }
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    private void initialize(Composite parentComposite) {
        // Create logging composite under parent composite
        Composite loggingComposite = new Composite(parentComposite, SWT.NONE);
        loggingComposite.setLayout(new GridLayout(5, false));
        loggingComposite.setLayoutData(new GridData(SWT.BEGINNING));

        // 1. Log category label
        CLabel logCategoryLabel = new CLabel(loggingComposite, SWT.NONE);
        logCategoryLabel.setText("Log category:");
        logCategoryLabel.setLayoutData(new GridData(SWT.BEGINNING));

        // 2. Log categories list
        logCategoryCombo = new Combo(loggingComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        logCategoryCombo.setEnabled(false);
        populateLogCategory();
        logCategoryCombo.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                String logCategoryText = ((Combo) e.widget).getText();
                handleLogCategoryChange(LogCategoryExt.fromExternalValue(logCategoryText));
            }

            private void handleLogCategoryChange(LogCategoryExt logCategoryExt) {
                // fchang: hook for display diff log category w/o requesting server again. - per bill: not possible
                // w/o refactoring server-side code.
                logLevelScale.setEnabled(true); // enable scale only after logCategory is set.
                LoggingInfo loggingInfo =
                        loggingService.getLoggingInfoByCategory(project, logCategoryExt, supportedFeatureEnum);
                setLoggingInfo(loggingInfo);
            }

            @Override
            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {}
        });

        // 3. Log level label
        CLabel logLevel = new CLabel(loggingComposite, SWT.NONE);
        logLevel.setText("Log level:   --");
        logLevel.setLayoutData(new GridData(SWT.BEGINNING));

        // 4. Log levels scale
        logLevelScale = new Scale(loggingComposite, SWT.NULL);
        logLevelScale.setIncrement(1);
        logLevelScale.setMaximum(7);
        logLevelScale.setEnabled(false);
        logLevelScale.setPageIncrement(1);
        logLevelScale.setLayoutData(new GridData(SWT.BEGINNING));
        logLevelScale.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                LoggingInfo loggingInfo =
                        LoggingInfo.getLoggingInfo(logLevelScale.getSelection(), logCategoryCombo.getText());
                loggingService.setLoggingInfo(project, loggingInfo, supportedFeatureEnum);
                logLevelDesc.setText(PLUS_PREFIX + loggingInfo.getLevelLabelText());
                layout(true, true);
            }
        });

        // 5. Log level description label
        logLevelDesc = new CLabel(loggingComposite, SWT.NONE);
        logLevelDesc.setText(INIT_PLUS_PREFIX);
        GridData logLevelDescGd = new GridData();
        logLevelDesc.setLayoutData(logLevelDescGd);
    }

    private void populateLogCategory() {
        LogCategoryExt[] categories = LoggingInfo.getLogCategories();
        Arrays.sort(categories, new Comparator<LogCategoryExt>() {
            @Override
            public int compare(LogCategoryExt o1, LogCategoryExt o2) {
                return o1.getExternalValue().compareTo(o2.getExternalValue());
            }
        });

        for (int i = 0; i < categories.length; i++) {
            logCategoryCombo.add(categories[i].getExternalValue());
        }
        logCategoryCombo.setVisibleItemCount(LoggingInfo.getLogCategories().length);
    }

    private void setLoggingInfo(LoggingInfo loggingInfo) {
        logCategoryCombo.select(loggingInfo.getCategorySelection());
        logLevelScale.setMaximum(LoggingInfo.getLoggingLevelRange(loggingInfo.getCategoryExt()) - 1);
        logLevelScale.setSelection(loggingInfo.getLevelScaleSelection());
        logLevelDesc.setText(PLUS_PREFIX + loggingInfo.getLevelLabelText());
        layout(true, true);
    }

    protected void setDefaultLoggingInfoSelection(IProject project, SupportedFeatureEnum supportedFeatureEnum) {
        setLoggingInfo(loggingService.getDefaultSelectedLoggingInfo(project, supportedFeatureEnum));
    }

    /**
     * Criteria to enable to pass project. This is done because when composite is init, the project might not be selected yet.
     * @param project
     * @throws Exception
     */
    public void enable(IProject project) {
        if (project == null) {
            enable(false);
        }
        this.project = project;
        enable(true);
    }

    public void enable(boolean enable) {
        if (logCategoryCombo != null) {
            logCategoryCombo.setEnabled(enable);
        }
        if (logLevelScale != null) {
            logLevelScale.setEnabled(enable);
        }
        // set default log category and corresponding level
        if (enable && Utils.isNotEmpty(this.project)) {
            setDefaultLoggingInfoSelection(this.project, supportedFeatureEnum);
        }
    }

}
