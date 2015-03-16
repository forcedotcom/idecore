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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.project.ProjectProjectContentComposite;

/**
 *
 * Composite includes project content property specific controls
 *
 * @author fchang
 */
public class ProjectContentPropertyComposite extends ProjectProjectContentComposite {
    static final Logger logger = Logger.getLogger(ProjectContentPropertyComposite.class);
    protected ProjectContentPropertyPage projectContentPropertyPage = null;
    protected Package packageManifest = null;
    
    public ProjectContentPropertyComposite(Composite parent, int style,
            ProjectContentPropertyPage projectContentPropertyPage, Package packageManifest) {
        super(parent, style);
        this.parent = parent;
        this.projectContentPropertyPage = projectContentPropertyPage;
        this.projectModel = projectContentPropertyPage.getProjectController().getProjectModel();
        this.packageManifest = packageManifest;
        initialize();
    }

    @Override
    protected void updateContentSummaryText() {
        projectContentPropertyPage.setContentSummaryText(true);
    }

    private void initialize() {
        setLayout(new GridLayout(6, false));
        String packgeName = Utils.isEmpty(packageManifest) ? null : packageManifest.getFullName();

        // package label (packaged project only)
        if (Utils.isNotEmpty(packgeName)) {
            Label lblPackageContent = new Label(this, SWT.NONE);
            lblPackageContent.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 6, 0));
            lblPackageContent.setText(UIMessages
                    .getString("ProjectProperties.ProjectContent.IntroPackageContent.label")
                    + " " + packgeName);
        }

        // content summary label
        lblIntroContentSummary = new Label(this, SWT.NONE);
        lblIntroContentSummary.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));
        lblIntroContentSummary.setText(UIMessages
                .getString("ProjectProperties.ProjectContent.IntroContentSummary.label"));

        // change button (non-package project only)
        if (Utils.isEmpty(packgeName)) {
            createProjectManifestEditorBtn(UIMessages
                    .getString("ProjectProperties.ProjectContent.CustomComponentsChange.label"));
            btnCustomComponentsOpen.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    projectContentPropertyPage.setUpdated(true);
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    widgetDefaultSelected(e);
                }
            });
        }

        // content summary
        contentSummaryTextArea(this);

        // message (packaged project only)
        if (Utils.isNotEmpty(packgeName)) {
            lblPackageContentMessage = new Label(this, SWT.WRAP);
            GridData dataSelectContents = new GridData(GridData.FILL_HORIZONTAL);
            dataSelectContents.widthHint = 1;
            dataSelectContents.heightHint = 40;
            lblPackageContentMessage.setLayoutData(dataSelectContents);
            lblPackageContentMessage.setText(UIMessages
                    .getString("ProjectProperties.ProjectContent.IntroPackageContent.message"));
        }
    }

    @Override
    public void validateUserInput() {}

}
