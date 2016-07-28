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
package com.salesforce.ide.core.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Common methods for adding and removing dirty markers
 * 
 * TODO: There is a weird error with Eclipse itself, where if we specify both line number and column number, it only
 * respects the column number. For now, disable column location in all apply*Marker methods.
 * 
 * @author cwall
 */
public class MarkerUtils {
    
    private static final Logger logger = Logger.getLogger(MarkerUtils.class);
    
    private static final String MARKER_ATTR_OBJECT_TYPE = "ComponentType";
    
    // Superclass for all apex markers
    public static final String MARKER_PROBLEM = ForceIdeCorePlugin.getPluginId() + ".problem";
    
    // Dirty sync marker
    public static final String MARKER_DIRTY = ForceIdeCorePlugin.getPluginId() + ".dirty";
    
    // apex code compile error marker
    public static final String MARKER_COMPILE_ERROR = ForceIdeCorePlugin.getPluginId() + ".compileError";
    
    // save error marker
    public static final String MARKER_SAVE_ERROR = ForceIdeCorePlugin.getPluginId() + ".saveError";
    
    // retrieve error marker
    public static final String MARKER_RETRIEVE_ERROR = ForceIdeCorePlugin.getPluginId() + ".retrieveError";
    
    // run test failure marker
    public static final String MARKER_RUN_TEST_FAILURE = ForceIdeCorePlugin.getPluginId() + ".runTestFailure";
    
    // code coverage warning marker
    public static final String MARKER_CODE_COVERAGE_WARNING = ForceIdeCorePlugin.getPluginId() + ".codeCoverageWarning";
    
    // inactive marker
    public static final String MARKER_INACTIVE = ForceIdeCorePlugin.getPluginId() + ".inactive";
    
    private static MarkerUtils instance = null;
    
    protected MarkerUtils() {}
    
    public static MarkerUtils getInstance() {
        if (instance == null) {
            instance = new MarkerUtils();
        }
        return instance;
    }
    
    /**
     * Set a dirty resource marker on the resource
     * 
     * @param res
     */
    public void applyDirty(IResource[] resources) {
        if (Utils.isEmpty(resources)) {
            logger.warn("Unable to apply dirty marker to resources - resources is null or empty");
            return;
        }
        
        for (IResource resource : resources) {
            applyDirty(resource);
        }
    }
    
    public void applyDirty(IResource resource) {
        if (resource == null) {
            logger.warn("Unable to apply dirty marker to resource - resource is null");
            return;
        }
        
        applyDirty(resource, Messages.getString("Markers.OnlySavedLocally.message"));
    }
    
    /**
     * Set a dirty resource marker on the resource
     * 
     * @param resource
     */
    public void applyDirty(IResource resource, String msg) {
        applyDirty(resource, MarkerUtils.MARKER_DIRTY, msg);
    }
    
    /**
     * Set a dirty resource marker on the resource
     * 
     * @param resource
     */
    public void applyDirty(IResource resource, String markerId, String msg) {
        if (isDirty(resource, markerId)) {
            return;
        }
        Map<String, Object> attributes = new HashMap<>(4);
        
        attributes.put(IMarker.MESSAGE, msg);
        // marker line numbers are 1-based: we set the marker arbitrarily on the first line
        attributes.put(IMarker.LINE_NUMBER, new Integer(1));
        attributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_WARNING));
        attributes.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
        
        createMarker(resource, attributes, markerId);
    }
    
    /**
     * 
     * @param resource
     * @return true if the resource is dirty, false otherwise
     */
    public boolean isDirty(IResource resource) {
        return isDirty(resource, MARKER_DIRTY);
    }
    
    public boolean isDirty(IResource resource, String markerId) {
        IMarker[] markers = getMarkers(resource, markerId);
        if (markers.length == 0) {
            return false;
        }
        return true;
    }
    
    public boolean hasMarker(IResource resource, String markerId) {
        IMarker[] markers = getMarkers(resource, markerId);
        if (Utils.isEmpty(markers)) {
            return false;
        }
        for (IMarker marker : markers) {
            if (String.valueOf(marker.getId()).equals(markerId)) {
                return true;
            }
        }
        return true;
    }
    
    public void applyCompileErrorMarker(IResource resource, String msg) {
        applyCompileMarker(resource, 1, 1, 2, msg, IMarker.SEVERITY_ERROR);
    }
    
    public void applyCompileErrorMarker(IResource resource, int line, int charStart, int charEnd, String msg) {
        applyCompileMarker(resource, line, charStart, charEnd, msg, IMarker.SEVERITY_ERROR);
    }
    
    public void applyCompileWarningMarker(IResource resource, int line, int charStart, int charEnd, String msg) {
        applyCompileMarker(resource, line, charStart, charEnd, msg, IMarker.SEVERITY_WARNING);
    }
    
    public void applyCompileMarker(IResource resource, int line, int charStart, int charEnd, String msg, int severity) {
        if (resource == null) {
            logger.warn(
                "Unable to apply compile " 
                    + (severity == IMarker.SEVERITY_ERROR ? "error" : "warning")
                    + " marker to resource - resource is null");
            return;
        }
        
        String message = Messages.getString("Markers.CompilationPrefix.message") + " " + msg;
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IMarker.MESSAGE, message);
        attributes.put(IMarker.LINE_NUMBER, line);
        attributes.put(IMarker.SEVERITY, severity);
        attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        
        createMarker(resource, attributes, MarkerUtils.MARKER_COMPILE_ERROR);
    }
    
    public void applySaveErrorMarker(IResource resource, String msg) {
        applySaveErrorMarker(resource, 1, 1, 2, msg);
    }
    
    public void applySaveErrorMarker(IResource resource, Integer line, Integer charStart, Integer charEnd, String msg) {
        applySaveMarker(
            resource,
            (line != null ? line : 0),
            (charStart != null ? charStart : 0),
            (charEnd != null ? charEnd : 0),
            msg,
            IMarker.SEVERITY_ERROR);
    }
    
    public void applySaveWarningMarker(IResource resource, String msg) {
        applySaveWarningMarker(resource, 1, 1, 2, msg);
    }
    
    public void applySaveWarningMarker(
        IResource resource,
        Integer line,
        Integer charStart,
        Integer charEnd,
        String msg) {
        applySaveMarker(
            resource,
            (line != null ? line : 0),
            (charStart != null ? charStart : 0),
            (charEnd != null ? charEnd : 0),
            msg,
            IMarker.SEVERITY_WARNING);
    }
    
    private void applySaveMarker(IResource resource, int line, int charStart, int charEnd, String msg, int severity) {
        if (resource == null) {
            logger.warn(
                "Unable to apply save " 
                + (severity == IMarker.SEVERITY_ERROR ? "error" : "warning")
                + " marker to resource - resource is null");
            return;
        }
        
        String message = Messages.getString(
            severity == IMarker.SEVERITY_ERROR
                ? "Markers.SavePrefix.Error.message"
                : "Markers.SavePrefix.Warning.message")
            + " " + msg;
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IMarker.MESSAGE, message);
        attributes.put(IMarker.LINE_NUMBER, line);
        attributes.put(IMarker.SEVERITY, severity);
        attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        
        if (logger.isInfoEnabled()) {
            logger.info(
                "Applying save " 
                + (severity == IMarker.SEVERITY_ERROR ? "error" : "warning") 
                + " marker: '"
                + resource.getProjectRelativePath().toPortableString() 
                + "': '" 
                + message 
                + "'");
        }
        createMarker(resource, attributes, MarkerUtils.MARKER_SAVE_ERROR);
    }
    
    public void applyRetrieveErrorMarker(IResource resource, String[] componentTypes, String msg) {
        for (String componentType : componentTypes) {
            applyRetrieveErrorMarker(resource, 1, 1, 2, componentType, msg);
        }
    }
    
    public void applyRetrieveErrorMarker(IResource resource, String msg) {
        applyRetrieveErrorMarker(resource, 1, 1, 2, null, msg);
    }
    
    public void applyRetrieveErrorMarker(
        IResource resource,
        Integer line,
        Integer charStart,
        Integer charEnd,
        String componentType,
        String msg) {
        applyRetrieveMarker(
            resource,
            (line != null ? line : 0),
            (charStart != null ? charStart : 0),
            (charEnd != null ? charEnd : 0),
            componentType,
            msg,
            IMarker.SEVERITY_ERROR);
    }
    
    public void applyRetrieveWarningMarker(IResource resource, String[] componentTypes, String msg) {
        for (String componentType : componentTypes) {
            applyRetrieveWarningMarker(resource, 1, 1, 2, componentType, msg);
        }
    }
    
    public void applyRetrieveWarningMarker(IResource resource, String msg) {
        applyRetrieveWarningMarker(resource, 1, 1, 2, null, msg);
    }
    
    public void applyRetrieveWarningMarker(
        IResource resource,
        Integer line,
        Integer charStart,
        Integer charEnd,
        String componentType,
        String msg) {
        applyRetrieveMarker(
            resource,
            (line != null ? line : 0),
            (charStart != null ? charStart : 0),
            (charEnd != null ? charEnd : 0),
            componentType,
            msg,
            IMarker.SEVERITY_WARNING);
    }
    
    private void applyRetrieveMarker(
        IResource resource,
        int line,
        int charStart,
        int charEnd,
        String componentType,
        String msg,
        int severity) {
        if (resource == null) {
            logger.warn(
                "Unable to apply retrieve " 
                + (severity == IMarker.SEVERITY_ERROR ? "error" : "warning")
                + " marker to resource - resource is null");
            return;
        }
        
        String message = Messages.getString("Markers.RetrievePrefix.message") + " " + msg;
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IMarker.MESSAGE, message);
        attributes.put(IMarker.LINE_NUMBER, line);
        attributes.put(IMarker.SEVERITY, severity);
        attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        if (Utils.isEmpty(componentType) && msg.matches("^..*type '..*' not found")) {
            // get object type by parsing error message, ex: Entity of type 'Layout' named 'Account-kk' not found
            // this is for scenario where markers generated by refreshing from
            // server on package, src, project levels, invocation will not associated with any object type
            // consequently, when we refresh from server on component folder level afterwards, the markers won't get
            // removed properly base on object type attribute on marker.
            
            // FIXME - Too much assumption on this solution
            int firstSingleQuote = msg.indexOf("'");
            int secondSingleQuote = msg.indexOf("'", firstSingleQuote + 1);
            componentType = msg.substring(firstSingleQuote + 1, secondSingleQuote);
        }
        
        if (Utils.isNotEmpty(componentType)) {
            attributes.put(MARKER_ATTR_OBJECT_TYPE, componentType);
        }
        
        createMarker(resource, attributes, MarkerUtils.MARKER_RETRIEVE_ERROR);
    }
    
    public void applyRunTestFailureMarker(IResource resource, String msg) {
        applyRunTestFailureMarker(resource, 1, 1, 2, msg);
    }
    
    public void applyRunTestFailureMarker(IResource resource, int line, int charStart, int charEnd, String msg) {
        if (resource == null) {
            logger.warn("Unable to apply run test failure marker to resource - resource is null");
            return;
        }
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IMarker.MESSAGE, msg);
        attributes.put(IMarker.LINE_NUMBER, line);
        attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        
        if (logger.isInfoEnabled()) {
            logger.info(
                "Run test failure marker on resource '" 
                + resource.getProjectRelativePath().toPortableString() 
                + "': '"
                + msg 
                + "'");
        }
        createMarker(resource, attributes, MarkerUtils.MARKER_RUN_TEST_FAILURE);
    }
    
    public void applyCodeCoverageWarningMarker(IResource resource, String msg) {
        applyCodeCoverageWarningMarker(resource, 1, 1, 2, msg);
    }
    
    public void applyCodeCoverageWarningMarker(IResource resource, int line, int charStart, int charEnd, String msg) {
        if (resource == null) {
            logger.warn("Unable to apply code coverage warning marker to resource - resource is null");
            return;
        }
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IMarker.MESSAGE, msg);
        attributes.put(IMarker.LINE_NUMBER, line);
        attributes.put(IMarker.CHAR_START, charStart);
        attributes.put(IMarker.CHAR_END, charEnd);
        
        if (logger.isInfoEnabled()) {
            logger.info(
                "Code coverage warning marker on resource '" 
                + resource.getProjectRelativePath().toPortableString()
                + "': '" 
                + msg 
                + "'");
        }
        createMarker(resource, attributes, MarkerUtils.MARKER_CODE_COVERAGE_WARNING);
    }
    
    /*
     * All apex markers
     */
    public void clearAll(IResource[] resources) {
        if (Utils.isNotEmpty(resources)) {
            for (int i = 0; i < resources.length; i++) {
                clearMarkers(resources[i], null, MarkerUtils.MARKER_PROBLEM);
            }
        }
    }
    
    public void clearAll(List<IResource> resources) {
        if (Utils.isNotEmpty(resources)) {
            for (IResource resource : resources) {
                clearMarkers(resource, null, MarkerUtils.MARKER_PROBLEM);
            }
        }
    }
    
    /**
     * Clear all apex resource markers on the resource
     * 
     * @param resource
     */
    public void clearAll(IResource resource) {
        clearMarkers(resource, null, MarkerUtils.MARKER_PROBLEM);
    }
    
    public void clearAllRecursively(IResource resource) {
        clearMarkers(resource, null, MarkerUtils.MARKER_PROBLEM, IResource.DEPTH_INFINITE);
    }
    
    public void clearDirty(IResource[] resources) {
        if (Utils.isEmpty(resources)) {
            logger.warn("Unable to clear dirty markers on resources - resources is null or empty");
            return;
        }
        
        for (IResource resource : resources) {
            clearDirty(resource);
        }
    }
    
    public void clearDirty(List<IResource> resources) {
        clearDirty(resources.toArray(new IResource[0]));
    }
    
    /**
     * Clear dirty resource markers on the resource
     * 
     * @param resource
     */
    public void clearDirty(IResource resource) {
        clearDirty(resource, MarkerUtils.MARKER_DIRTY);
    }
    
    public void clearDirty(IResource resource, String markerId) {
        clearMarkers(resource, null, markerId);
    }
    
    public void createMarker(final IResource resource, final Map<String, Object> attributes, final String markerType) {
        if (resource == null || !resource.exists()) {
            return;
        }
        
        try {
            IWorkspaceRunnable r = new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    // apply marker
                    IMarker marker = resource.createMarker(markerType);
                    marker.setAttributes(attributes);
                }
            };
            ISchedulingRule sr = getRule(resource);
            resource.getWorkspace().run(r, sr, IWorkspace.AVOID_UPDATE, null);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to apply marker to resource: " + logMessage);
        }
    }
    
    private static ISchedulingRule getRule(IResource resource) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
        ISchedulingRule rule = ruleFactory.markerRule(resource.getProject());
        return rule;
    }
    
    public IMarker[] getMarkers(IResource resource, String marker) {
        return getMarkers(resource, marker, IResource.DEPTH_ZERO);
    }
    
    protected IMarker[] getMarkers(IResource resource, String marker, int depth) {
        IMarker[] markers = new IMarker[0];
        if (resource == null || !resource.exists()) {
            return markers;
        }
        
        try {
            markers = resource.findMarkers(marker, true, depth);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn(logMessage);
        }
        return markers;
    }
    
    public void clearMarkers(IResource resource, String componentType, String marker) {
        clearMarkers(resource, componentType, marker, IResource.DEPTH_ZERO);
    }
    
    public void clearMarkers(IResource resource, String componentType, String marker, int depth) {
        if (resource == null || !resource.exists()) {
            return;
        }
        
        IMarker[] markers = getMarkers(resource, marker, depth);
        try {
            for (IMarker element : markers) {
                if (Utils.isEmpty(componentType)
                    || componentType.equals(element.getAttribute(MARKER_ATTR_OBJECT_TYPE))) {
                    element.delete();
                }
                
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to clear marker: " + logMessage);
        }
    }
    
    /*
     * Compile errors
     */
    public void clearCompileMarkers(IResource[] resources) {
        if (Utils.isEmpty(resources)) {
            logger.warn("Unable to clear compile markers on resources - resources is null or empty");
            return;
        }
        
        for (IResource resource : resources) {
            clearCompileMarkers(resource);
        }
    }
    
    public void clearCompileMarkers(IResource resource) {
        clearMarkers(resource, null, MarkerUtils.MARKER_COMPILE_ERROR);
    }
    
    public void clearSaveMarkers(IResource[] resources) {
        if (Utils.isEmpty(resources)) {
            logger.warn("Unable to clear save markers on resources - resources is null or empty ");
            return;
        }
        
        for (IResource resource : resources) {
            clearSaveMarkers(resource);
        }
    }
    
    public void clearSaveMarkers(IResource resource) {
        clearMarkers(resource, null, MarkerUtils.MARKER_SAVE_ERROR);
    }
    
    public void clearRetrieveMarkers(List<IResource> resources) {
        if (Utils.isEmpty(resources)) {
            logger.warn("Unable to clear retrieve markers on resources - resources is null or empty ");
            return;
        }
        
        for (IResource resource : resources) {
            clearRetrieveMarkers(resource, null);
        }
    }
    
    public void clearRetrieveMarkers(IResource resource, String[] componentTypes) {
        if (Utils.isNotEmpty(componentTypes)) {
            for (String componentType : componentTypes) {
                clearMarkers(resource, componentType, MarkerUtils.MARKER_RETRIEVE_ERROR);
            }
        }
    }
    
    public void clearRunTestFailureMarkers(List<IResource> resources) {
        if (Utils.isEmpty(resources)) {
            if (logger.isInfoEnabled()) {
                logger.info("Unable to clear run tests markers on resources - resources is null or empty ");
            }
            return;
        }
        
        for (IResource resource : resources) {
            clearRunTestFailureMarkers(resource);
        }
    }
    
    public void clearRunTestFailureMarkers(IResource resource) {
        clearMarkers(resource, null, MarkerUtils.MARKER_RUN_TEST_FAILURE);
    }
    
    public void clearCodeCoverageWarningMarkers(List<IResource> resources) {
        if (Utils.isEmpty(resources)) {
            if (logger.isInfoEnabled()) {
                logger.info("Unable to clear coverage warning markers on resources - resources is null or empty ");
            }
            return;
        }
        
        for (IResource resource : resources) {
            clearRunTestFailureMarkers(resource);
        }
    }
    
    public void clearCodeCoverageWarningMarkers(IResource resource) {
        clearMarkers(resource, null, MarkerUtils.MARKER_CODE_COVERAGE_WARNING, IResource.DEPTH_INFINITE);
    }
    
    public void clearCodeCoverageWarningMarkersFor(IResource resource) {
        clearMarkers(resource, null, MarkerUtils.MARKER_CODE_COVERAGE_WARNING, IResource.DEPTH_ZERO);
    }
}
