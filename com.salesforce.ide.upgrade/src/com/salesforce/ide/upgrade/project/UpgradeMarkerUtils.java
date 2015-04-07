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
package com.salesforce.ide.upgrade.project;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.upgrade.ForceIdeUpgradePlugin;

/**
 * Common methods for adding and removing upgrade markers
 * 
 * @author cwall
 */
public class UpgradeMarkerUtils {

    private static final Logger logger = Logger.getLogger(MarkerUtils.class);

    // dirty upgrade marker
    public static final String MARKER_DIRTY = ForceIdeUpgradePlugin.getPluginId() + ".dirty";

    // project upgrade
    public static final String MARKER_UPGRADE = ForceIdeUpgradePlugin.getPluginId() + ".inactive";

    public static void applyDirty(IResource resource) {
        if (resource == null) {
            logger.warn("Unable to apply dirty marker to resource - resource is null");
            return;
        }

        MarkerUtils.getInstance().applyDirty(resource, MARKER_DIRTY,
            Messages.getString("Markers.OnlySavedLocally.message"));
    }

    public static void clearDirty(IResource resource) {
        MarkerUtils.getInstance().clearDirty(resource, MARKER_DIRTY);
        MarkerUtils.getInstance().clearDirty(resource);
    }

    public static final void applyUpgradeRequiredMarker(IResource resource, String msg) {
        if (resource == null) {
            logger.warn("Unable to apply upgrade required warning marker to resource - resource is null");
            return;
        }

        if (MarkerUtils.getInstance().hasMarker(resource, MARKER_UPGRADE)) {
            return;
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IMarker.MESSAGE, msg);
        attributes.put(IMarker.LINE_NUMBER, new Integer(1));
        attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
        attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

        MarkerUtils.getInstance().createMarker(resource, attributes, MARKER_UPGRADE);
    }

    public static void clearUpgradeRequiredMarker(IResource resource, boolean rescursively) {
        if (rescursively) {
            MarkerUtils.getInstance().clearMarkers(resource, null, MARKER_UPGRADE, IResource.DEPTH_INFINITE);
        } else {
            MarkerUtils.getInstance().clearMarkers(resource, null, MARKER_UPGRADE);
        }
    }

    public static void clearAllUpgradeMarkers(IResource resource) {
        clearUpgradeRequiredMarker(resource, false);
        MarkerUtils.getInstance().clearSaveMarkers(resource);
        clearDirty(resource);
    }
}
