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
package com.salesforce.ide.ui.editors.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;

/**
 * @author ataylor
 */
public class EditorUtils {
    static Set<String> activitySet = new HashSet<>();
    static IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
    static IActivityManager activityManager = workbenchActivitySupport.getActivityManager();

    static {
        activitySet.add("com.salesforce.ide.activities.activity.contextualLaunch");
    }

    /**
     * Add list of capabilities back to IDE
     */
    @SuppressWarnings( { "unchecked" })
    public static void addCapabilites() {
        Set<String> enabledActivityIds = new HashSet<>(activityManager.getEnabledActivityIds());
        boolean update = false;

        for (String activity : activitySet) {
            update |= enabledActivityIds.add(activity);
        }

        update(enabledActivityIds, update);
    }

    /**
     * Remove list of capabilities from IDE
     */
    @SuppressWarnings( { "unchecked" })
    public static void removeCapabilites() {
        Set<String> enabledActivityIds = new HashSet<>(activityManager.getEnabledActivityIds());
        boolean update = false;

        for (String activity : activitySet) {
            update |= enabledActivityIds.remove(activity);
        }

        update(enabledActivityIds, update);
    }

    /**
     * Get project from current active editor.
     * @return current project
     */
    public static IProject getCurrentProjectFromActiveEditor() {
        IProject project = null;
        IWorkbenchWindow workbenchWindow = ForceIdeEditorsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow.getActivePage().getActiveEditor() instanceof BaseMultiPageEditorPart) {
            BaseMultiPageEditorPart bpe = (BaseMultiPageEditorPart)workbenchWindow.getActivePage().getActiveEditor();
            project = bpe.getProject();
        }
        return project;
    }
    
    private static void update(Set<String> enabledActivityIds, boolean update) {
        if (update) {
            workbenchActivitySupport.setEnabledActivityIds(enabledActivityIds);
        }
    }
}
