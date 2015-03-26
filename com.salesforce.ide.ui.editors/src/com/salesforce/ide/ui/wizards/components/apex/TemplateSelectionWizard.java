package com.salesforce.ide.ui.wizards.components.apex;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;

public abstract class TemplateSelectionWizard extends ComponentWizard {

    public TemplateSelectionWizard() {
        setDialogSettings(ForceIdeEditorsPlugin.getDefault().getDialogSettings());
    }

}
