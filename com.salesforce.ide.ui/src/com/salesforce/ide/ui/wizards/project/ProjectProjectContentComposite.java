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
package com.salesforce.ide.ui.wizards.project;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.ui.internal.composite.BaseComposite;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 * FXIME
 * 
 * @author cwall
 */
public class ProjectProjectContentComposite extends BaseComposite {

    static final Logger logger = Logger.getLogger(ProjectProjectContentComposite.class);

    protected ProjectProjectContentPage projectProjectContentPage = null;
    protected Composite parent = null;
    protected Group grpComponentTypes = null;
    protected Button btnAll = null;
    protected Button btnAllDevCode = null;
    protected Button btnCustomComponents = null;
    protected Button btnCustomComponentsOpen = null;
    protected Button btnSpecificPackage = null;
    protected Combo cmbPackageName = null;
    protected Button btnNone = null;
    protected StyledText txtContentSummary = null;
    protected Label lblIntroContentSummary = null;
    protected GridData gdaContentSummary = null;
    protected ProjectCustomComponentsDialog customComponentsDialog = null;
    protected ProjectModel projectModel = null;
    protected Label lblPackageContentMessage = null;

    //   C O N S T R U C T O R
    public ProjectProjectContentComposite(Composite parent, int style) {
        super(parent, style);
    }

    public ProjectProjectContentComposite(Composite parent, int style,
            ProjectProjectContentPage projectProjectContentPage, ProjectModel projectModel) {
        super(parent, style);
        this.parent = parent;
        this.projectProjectContentPage = projectProjectContentPage;
        this.projectModel = projectModel;
        initialize();
    }

    //   M E T H O D S
    @Override
    public Composite getParent() {
        return this.parent;
    }

    private void initialize() {
        setLayout(new GridLayout(6, false));

        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                layout(true);
            }
        });

        // ALL CONTENT - sfdc only
        if (Utils.isInternalMode()) {
            Label lblIndent = new Label(this, SWT.NONE);
            lblIndent.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
            // REVIEWME: extra spaces are for better pixel indent; there might be a better way
            lblIndent.setText(" ");
            btnAll = new Button(this, SWT.RADIO);
            btnAll.setText(UIMessages.getString("ProjectCreateWizard.ProjectContent.AllContent.label"));
            btnAll.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 5, 0));
            btnAll.setData(ProjectController.ALL_CONTENT);
            btnAll.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    if (btnAll.getSelection()) {
                        toggleButtons();
                        setLblIntroContentSummaryTxt("");
                        showContentSummary(false);
                    }
                    validateUserInput();
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    widgetDefaultSelected(e);
                }
            });
        }

        // ALL APEX CONTENT
        Label lblIndent1 = new Label(this, SWT.NONE);
        lblIndent1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        // REVIEWME: extra spaces are for better pixel indent; there might be a better way
        lblIndent1.setText(" ");
        btnAllDevCode = new Button(this, SWT.RADIO | SWT.WRAP);
        btnAllDevCode.setText(UIMessages.getString("ProjectCreateWizard.ProjectContent.AllApexContent.label"));
        btnAllDevCode.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 5, 0));
        btnAllDevCode.setData(ProjectController.ALL_DEV_CODE_CONTENT);
        btnAllDevCode.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (btnAllDevCode.getSelection()) {
                    toggleButtons();
                    showContentSummary(false);
                    projectProjectContentPage.setContentSummaryText();
                }
                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        // CUSTOM COMPONENTS
        Label lblIndent2 = new Label(this, SWT.NONE);
        lblIndent2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        // REVIEWME: extra spaces are for better pixel indent; there might be a better way
        lblIndent2.setText(" ");
        btnCustomComponents = new Button(this, SWT.RADIO);
        // extra spaces are for better pixel spacing between colon and drop-down
        btnCustomComponents.setText(UIMessages.getString("ProjectCreateWizard.ProjectContent.CustomComponents.label")
                + "  ");
        btnCustomComponents.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 0));
        btnCustomComponents.setData(ProjectController.CUSTOM_COMPONENTS);
        btnCustomComponents.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (btnCustomComponents.getSelection()) {
                    toggleButtons();
                    showContentSummary(true);
                    projectProjectContentPage.setContentSummaryText();
                }
                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        createProjectManifestEditorBtn(UIMessages
                .getString("ProjectCreateWizard.ProjectContent.CustomComponentsOpen.label"));

        btnCustomComponents.setEnabled(false);
        btnCustomComponentsOpen.setEnabled(false);
        @SuppressWarnings("unused")
        Label filler22 = new Label(this, SWT.NONE);

        // SPECIFIC PACKAGE
        Label lblIndent3 = new Label(this, SWT.NONE);
        lblIndent3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        // REVIEWME: extra spaces are for better pixel indent; there might be a better way
        lblIndent3.setText(" ");
        btnSpecificPackage = new Button(this, SWT.RADIO);
        // extra spaces are for better pixel spacing between colon and drop-down
        btnSpecificPackage.setText(UIMessages.getString("ProjectCreateWizard.ProjectContent.SpecificPackage.label")
                + "  ");
        btnSpecificPackage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        btnSpecificPackage.setData(ProjectController.SPECIFIC_PACKAGE);
        btnSpecificPackage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (btnSpecificPackage.getSelection()) {
                    // enable drop-down
                    cmbPackageName.setEnabled(btnSpecificPackage.getSelection());
                    toggleButtons();
                    setLblIntroContentSummaryTxt(UIMessages
                            .getString("ProjectCreateWizard.ProjectContent.SpecificPackage.IntroContentSummary.label"));
                    showContentSummary(false);
                }
                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
        cmbPackageName = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
        cmbPackageName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 0));
        cmbPackageName.setEnabled(false);
        cmbPackageName.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // set content summary text
                projectProjectContentPage.setContentSummaryText();

                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        Label filler5 = new Label(this, SWT.NONE);
        filler5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0));

        // NONE
        Label lblIndent4 = new Label(this, SWT.NONE);
        lblIndent4.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        // REVIEWME: extra spaces are for better pixel indent; there might be a better way
        lblIndent4.setText(" ");
        btnNone = new Button(this, SWT.RADIO);
        btnNone.setText(UIMessages.getString("ProjectCreateWizard.ProjectContent.None.label"));
        btnNone.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 5, 0));
        btnNone.setData(ProjectController.NONE);
        btnNone.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (btnNone.getSelection()) {
                    toggleButtons();
                    projectProjectContentPage.setContentSummaryText();
                }
                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        Label filler3 = new Label(this, SWT.NONE);
        filler3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 0));

        // CONTENT SUMMARY
        lblIntroContentSummary = new Label(this, SWT.NONE);
        lblIntroContentSummary.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 6, 0));
        lblIntroContentSummary.setText(UIMessages
                .getString("ProjectCreateWizard.ProjectContent.IntroContentSummary.label"));

        contentSummaryTextArea(this);
    }

    protected void contentSummaryTextArea(Composite composite) {
        gdaContentSummary = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
        gdaContentSummary.horizontalSpan = 6;
        gdaContentSummary.heightHint = UIUtils.convertHeightInCharsToPixels(composite, 15);

        txtContentSummary = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
        txtContentSummary.setText(UIMessages
                .getString("ProjectCreateWizard.ProjectContent.ContentSummary.NoContent.message"));
        txtContentSummary.setLayoutData(gdaContentSummary);
        txtContentSummary.setBackground(parent.getBackground());
    }

    protected void createProjectManifestEditorBtn(String btnText) {
        btnCustomComponentsOpen = new Button(this, SWT.PUSH);
        btnCustomComponentsOpen.setText(btnText);
        btnCustomComponentsOpen.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 0));
        btnCustomComponentsOpen.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                try {
                    customComponentsDialog =
                            new ProjectCustomComponentsDialog(getShell(), projectModel.getPackageManifestModel(),
                                    projectModel.getConnection());
                    customComponentsDialog.open();
                } catch (Exception ex) {
                    logger.error("Unable to open custom component selection dialog", ex);
                    Utils.openError(ex, true, "Unable to open custom component selection dialog");
                    updateContentSummaryText();
                    return;
                }

                // show content
                updateContentSummaryText();
                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

    protected void updateContentSummaryText() {
        projectProjectContentPage.setContentSummaryText();
    }

    public void showContentSummary(boolean show) {
        gdaContentSummary.exclude = !show;
        txtContentSummary.setVisible(show);
    }

    protected void toggleButtons() {
        cmbPackageName.setEnabled(btnSpecificPackage.getSelection());
        btnCustomComponentsOpen.setEnabled(btnCustomComponents.getSelection());
    }

    // Monitors user input and reports messages.
    @Override
    public void validateUserInput() {
        projectProjectContentPage.validateUserInput();
    }

    public ProjectProjectContentPage getProjectProjectContentPage() {
        return projectProjectContentPage;
    }

    public void setProjectProjectContentPage(ProjectProjectContentPage projectProjectContentPage) {
        this.projectProjectContentPage = projectProjectContentPage;
    }

    public String getCmbPackageNameString() {
        return isEmpty(getText(cmbPackageName)) ? Constants.EMPTY_STRING : getText(cmbPackageName);
    }

    public Button getBtnAllApex() {
        return btnAllDevCode;
    }

    public boolean isAll() {
        return btnAll != null ? btnAll.getSelection() : false;
    }

    public boolean isAllDevCode() {
        return btnAllDevCode != null ? btnAllDevCode.getSelection() : false;
    }

    public Button getBtnSpecificPackage() {
        return btnSpecificPackage;
    }

    public boolean isSpecificPackage() {
        return btnSpecificPackage != null ? btnSpecificPackage.getSelection() : false;
    }

    public Combo getCmbPackageName() {
        return cmbPackageName;
    }

    public String getCmbPackageNameText() {
        return getText(cmbPackageName);
    }

    public void disableCmbPackageName() {
        btnSpecificPackage.setEnabled(false);
        cmbPackageName.setEnabled(false);
    }

    public Button getBtnCustomComponents() {
        return btnCustomComponents;
    }

    public boolean isCustomComponents() {
        return btnCustomComponents != null ? btnCustomComponents.getSelection() : false;
    }

    public Button getBtnNone() {
        return btnNone;
    }

    public boolean isNone() {
        return btnNone != null ? btnNone.getSelection() : false;
    }

    public Label getLblIntroContentSummary() {
        return lblIntroContentSummary;
    }

    public void setLblIntroContentSummaryTxt(String label) {
        if (lblIntroContentSummary != null) {
            lblIntroContentSummary.setText(label);
            layout(true, true);
        }
    }

    public StyledText getTxtContentSummary() {
        return txtContentSummary;
    }

    public void setTxtContentSummary(StyledText txtContentSummary) {
        this.txtContentSummary = txtContentSummary;
    }

    public void setTxtContentSummaryTxt(String label) {
        if (txtContentSummary != null) {
            txtContentSummary.setText(label);
            txtContentSummary.update();
        }
    }

    public void setTxtContentSummaryTxt(String label, StyleRange[] ranges) {
        if (txtContentSummary != null) {
            txtContentSummary.setText(label);
            if (Utils.isNotEmpty(ranges)) {
                txtContentSummary.setStyleRanges(ranges);
            }
            txtContentSummary.update();
        }
    }

    public int getContentSelection() {
        if (isAllDevCode()) {
            return Integer.parseInt(btnAllDevCode.getData().toString());
        } else if (isSpecificPackage()) {
            return Integer.parseInt(btnSpecificPackage.getData().toString());
        } else if (isCustomComponents()) {
            return Integer.parseInt(btnCustomComponents.getData().toString());
        } else if (isNone()) {
            return Integer.parseInt(btnNone.getData().toString());
        } else if (isAll()) {
            return Integer.parseInt(btnAll.getData().toString());
        } else {
            return ProjectController.ALL_DEV_CODE_CONTENT;
        }
    }

    public void disableServerContentOptions() {
        if (btnAllDevCode != null) {
            btnAllDevCode.setSelection(false);
            btnAllDevCode.setEnabled(false);
        }

        if (btnSpecificPackage != null) {
            btnSpecificPackage.setSelection(false);
            btnSpecificPackage.setEnabled(false);
        }

        if (btnCustomComponents != null) {
            btnCustomComponents.setSelection(false);
            btnCustomComponents.setEnabled(false);
        }

        if (btnNone != null) {
            btnNone.setSelection(true);
            btnNone.setEnabled(true);
        }
    }

    public void defaultServerContentOptions() {
        if (btnAllDevCode != null) {
            btnAllDevCode.setEnabled(true);
            btnAllDevCode.setSelection(true);
        }

        if (btnSpecificPackage != null) {
            btnSpecificPackage.setSelection(false);
            btnSpecificPackage.setEnabled(true);
        }

        if (btnCustomComponents != null) {
            btnCustomComponents.setSelection(false);
            btnCustomComponents.setEnabled(true);
        }

        if (btnNone != null) {
            btnNone.setEnabled(true);
            btnNone.setSelection(false);
        }
    }

    public Button getBtnCustomComponentsOpen() {
        return btnCustomComponentsOpen;
    }
}
