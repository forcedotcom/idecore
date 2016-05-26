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
package com.salesforce.ide.ui.perspective;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.ui.internal.utils.UIConstants;

/**
 *
 * @author cwall
 */
public class ForcePerspectiveFactory implements IPerspectiveFactory {
    
    private static final Logger logger = Logger.getLogger(ForcePerspectiveFactory.class);
    
    public static final String ID = Constants.FORCE_PLUGIN_PREFIX + ".perspective";
    
    /**
     * Constructs a new Default layout engine.
     */
    public ForcePerspectiveFactory() {
        super();
    }
    
    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        
        IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea);
        folder.addView(JavaUI.ID_PACKAGES);
        
        IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.7, editorArea);
        outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        outputfolder.addView(UIConstants.RUN_TEST_VIEW_ID);
        outputfolder.addView(UIConstants.DEBUG_LOG_VIEW_ID);
        outputfolder.addView(ISynchronizeView.VIEW_ID);
        
        layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float) 0.75, editorArea);
        
        // views
        layout.addShowViewShortcut(UIConstants.RUN_TEST_VIEW_ID);
        layout.addShowViewShortcut(UIConstants.DEBUG_LOG_VIEW_ID);
        layout.addShowViewShortcut(UIConstants.IDE_LOG_VIEW_ID);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
        
        // new wizards
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.eclipse.ui.newWizards");
        for (IExtension extension : extensionPoint.getExtensions()) {
            for (IConfigurationElement element : extension.getConfigurationElements()) {
                if (element.getName().equals("wizard")) {
                    if (element.getAttribute("id").startsWith(UIConstants.PLUGIN_PREFIX + ".wizards.create")
                        && element.getAttribute("category").equals(UIConstants.PLUGIN_PREFIX + ".wizards")) {
                        layout.addNewWizardShortcut(element.getAttribute("id"));
                        logger.debug("Added wizard '" + element.getAttribute("id") + "' as new wizard shortuct");
                    }
                }
            }
        }
        
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
    }
}
