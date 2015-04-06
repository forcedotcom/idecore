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
package com.salesforce.ide.ui.editors.apex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitAnnotationModelEvent;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider.ProblemAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaDocumentSetupParticipant;
import org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation;
import org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension;
import org.eclipse.jdt.internal.ui.text.spelling.JavaSpellingReconcileStrategy;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.preferences.PreferenceConstants;

@SuppressWarnings( { "restriction" })
public class ApexDocumentProvider extends TextFileDocumentProvider {
    /** Preference key for temporary problems */
    private final static String HANDLE_TEMPORARY_PROBLEMS = PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS;

    /** Internal property changed listener */
    private final IPropertyChangeListener fPropertyListener;

    /** Annotation model listener added to all created CU annotation models */

    public ApexDocumentProvider() {
        IDocumentProvider provider = new TextFileDocumentProvider();
        provider =
                new ForwardingDocumentProvider(IJavaPartitions.JAVA_PARTITIONING, new JavaDocumentSetupParticipant(),
                        provider);
        setParentDocumentProvider(provider);

        fPropertyListener = new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (HANDLE_TEMPORARY_PROBLEMS.equals(event.getProperty()))
                    enableHandlingTemporaryProblems();
            }
        };
        ForceIdeEditorsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyListener);
    }

    /**
     * Switches the state of problem acceptance according to the value in the preference store.
     */
    protected void enableHandlingTemporaryProblems() {
        boolean enable = isHandlingTemporaryProblems();
        for (Iterator<?> iter = getFileInfosIterator(); iter.hasNext();) {
            FileInfo info = (FileInfo) iter.next();
            if (info.fModel instanceof IProblemRequestorExtension) {
                IProblemRequestorExtension extension = (IProblemRequestorExtension) info.fModel;
                extension.setIsHandlingTemporaryProblems(enable);
            }
        }
    }

    /**
     * Returns the preference whether handling temporary problems is enabled.
     * 
     * @return <code>true</code> if temporary problems are handled
     */
    protected boolean isHandlingTemporaryProblems() {
        IPreferenceStore store = ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
        return store.getBoolean(HANDLE_TEMPORARY_PROBLEMS);
    }

    @Override
    protected IAnnotationModel createAnnotationModel(IFile file) {
        return new ApexAnnotationModel(file);
    }

    @Override
    protected FileInfo createFileInfo(Object element) throws CoreException {
        FileInfo info = super.createFileInfo(element);

        IAnnotationModel requestor = info.fModel;
        if (requestor instanceof IProblemRequestorExtension) {
            IProblemRequestorExtension extension = (IProblemRequestorExtension) requestor;
            extension.setIsActive(false);
            extension.setIsHandlingTemporaryProblems(isHandlingTemporaryProblems());
        }

        return info;
    }

    /**
     * Annotation model dealing with java marker annotations and temporary problems. Also acts as problem requester for
     * its compilation unit. Initially inactive. Must explicitly be activated.
     */
    public static class ApexAnnotationModel extends ResourceMarkerAnnotationModel implements IProblemRequestor,
            IProblemRequestorExtension {

        private static class ProblemRequestorState {
            boolean fInsideReportingSequence = false;
            List<IProblem> fReportedProblems;
        }

        private final ThreadLocal<ProblemRequestorState> fProblemRequestorState = new ThreadLocal<>();
        private int fStateCount = 0;

        private final List<Annotation> fGeneratedAnnotations = new ArrayList<>();
        private IProgressMonitor fProgressMonitor;
        private boolean fIsActive = false;
        private boolean fIsHandlingTemporaryProblems;

        private final ReverseMap fReverseMap = new ReverseMap();
        private List<Annotation> fPreviouslyOverlaid = null;
        private List<Annotation> fCurrentlyOverlaid = new ArrayList<>();
        private Thread fActiveThread;

        public ApexAnnotationModel(IResource resource) {
            super(resource);
        }
        
        @Override
        protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
            //            if (JavaMarkerAnnotation.isJavaAnnotation(marker))
            //                return new JavaMarkerAnnotation(marker);
            return new InternalMarkerAnnotation(marker);
        }

        @Override
        protected AnnotationModelEvent createAnnotationModelEvent() {
            return new CompilationUnitAnnotationModelEvent(this, getResource());
        }

        protected Position createPositionFromProblem(IProblem problem) {
            int start = problem.getSourceStart();
            if (start < 0)
                return null;

            int length = problem.getSourceEnd() - problem.getSourceStart() + 1;
            if (length < 0)
                return null;

            return new Position(start, length);
        }

        @Override
        public void beginReporting() {
            ProblemRequestorState state = fProblemRequestorState.get();
            if (state == null)
                internalBeginReporting(false);
        }

        @Override
        public void beginReportingSequence() {
            ProblemRequestorState state = fProblemRequestorState.get();
            if (state == null)
                internalBeginReporting(true);
        }

        /**
         * Sets up the infrastructure necessary for problem reporting.
         * 
         * @param insideReportingSequence
         *            <code>true</code> if this method call is issued from inside a reporting sequence
         */
        private void internalBeginReporting(boolean insideReportingSequence) {
        }

        @Override
        public void acceptProblem(IProblem problem) {
            if (fIsHandlingTemporaryProblems || problem.getID() == JavaSpellingReconcileStrategy.SPELLING_PROBLEM_ID) {
                ProblemRequestorState state =fProblemRequestorState.get();
                if (state != null)
                    state.fReportedProblems.add(problem);
            }
        }

        @Override
        public void endReporting() {
            ProblemRequestorState state = fProblemRequestorState.get();
            if (state != null && !state.fInsideReportingSequence)
                internalEndReporting(state);
        }

        @Override
        public void endReportingSequence() {
            ProblemRequestorState state = fProblemRequestorState.get();
            if (state != null && state.fInsideReportingSequence)
                internalEndReporting(state);
        }

        private void internalEndReporting(ProblemRequestorState state) {
            int stateCount = 0;
            synchronized (getLockObject()) {
                --fStateCount;
                stateCount = fStateCount;
                fProblemRequestorState.set(null);
            }

            if (stateCount == 0)
                reportProblems(state.fReportedProblems);
        }

        /**
         * Signals the end of problem reporting.
         * 
         * @param reportedProblems
         *            the problems to report
         */
        private void reportProblems(List<IProblem> reportedProblems) {
            if (fProgressMonitor != null && fProgressMonitor.isCanceled())
                return;

            boolean temporaryProblemsChanged = false;

            synchronized (getLockObject()) {

                boolean isCanceled = false;

                fPreviouslyOverlaid = fCurrentlyOverlaid;
                fCurrentlyOverlaid = new ArrayList<>();

                if (fGeneratedAnnotations.size() > 0) {
                    temporaryProblemsChanged = true;
                    removeAnnotations(fGeneratedAnnotations, false, true);
                    fGeneratedAnnotations.clear();
                }

                if (reportedProblems != null && reportedProblems.size() > 0) {

                    Iterator<IProblem> e = reportedProblems.iterator();
                    while (e.hasNext()) {

                        if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
                            isCanceled = true;
                            break;
                        }

                        IProblem problem = e.next();
                        Position position = createPositionFromProblem(problem);
                        if (position != null) {

                            try {
                                ProblemAnnotation annotation = new ApexProblemAnnotation(problem);
                                overlayMarkers(position, annotation);
                                addAnnotation(annotation, position, false);
                                fGeneratedAnnotations.add(annotation);

                                temporaryProblemsChanged = true;
                            } catch (BadLocationException x) {
                                // ignore invalid position
                            }
                        }
                    }
                }

                removeMarkerOverlays(isCanceled);
                fPreviouslyOverlaid = null;
            }

            if (temporaryProblemsChanged)
                fireModelChanged();
        }

        private void removeMarkerOverlays(boolean isCanceled) {
            if (isCanceled) {
                fCurrentlyOverlaid.addAll(fPreviouslyOverlaid);
            } else if (fPreviouslyOverlaid != null) {
                Iterator<Annotation> e = fPreviouslyOverlaid.iterator();
                while (e.hasNext()) {
                    JavaMarkerAnnotation annotation = (JavaMarkerAnnotation) e.next();
                    annotation.setOverlay(null);
                }
            }
        }

        /**
         * Overlays value with problem annotation.
         * 
         * @param value
         *            the value
         * @param problemAnnotation
         */
        private void setOverlay(Object value, ProblemAnnotation problemAnnotation) {
            if (value instanceof JavaMarkerAnnotation) {
                JavaMarkerAnnotation annotation = (JavaMarkerAnnotation) value;
                if (annotation.isProblem()) {
                    annotation.setOverlay(problemAnnotation);
                    fPreviouslyOverlaid.remove(annotation);
                    fCurrentlyOverlaid.add(annotation);
                }
            } else {}
        }

        @SuppressWarnings( { "unchecked" })
        private void overlayMarkers(Position position, ProblemAnnotation problemAnnotation) {
            Object value = getAnnotations(position);
            if (value instanceof List) {
                List<Annotation> list = (List<Annotation>) value;
                for (Iterator<Annotation> e = list.iterator(); e.hasNext();)
                    setOverlay(e.next(), problemAnnotation);
            } else {
                setOverlay(value, problemAnnotation);
            }
        }

        /**
         * Tells this annotation model to collect temporary problems from now on.
         */
        private void startCollectingProblems() {
            fGeneratedAnnotations.clear();
        }

        /**
         * Tells this annotation model to no longer collect temporary problems.
         */
        private void stopCollectingProblems() {
            if (fGeneratedAnnotations != null)
                removeAnnotations(fGeneratedAnnotations, true, true);
            fGeneratedAnnotations.clear();
        }

        /*
         * @see IProblemRequestor#isActive()
         */
        @Override
        public synchronized boolean isActive() {
            return fIsActive && fActiveThread == Thread.currentThread();
        }

        /*
         * @see IProblemRequestorExtension#setProgressMonitor(IProgressMonitor)
         */
        @Override
        public void setProgressMonitor(IProgressMonitor monitor) {
            fProgressMonitor = monitor;
        }

        /*
         * @see IProblemRequestorExtension#setIsActive(boolean)
         */
        @Override
        public synchronized void setIsActive(boolean isActive) {
            Assert.isLegal(!isActive || Display.getCurrent() == null); // must not be enabled from UI threads
            fIsActive = isActive;
            if (fIsActive)
                fActiveThread = Thread.currentThread();
            else
                fActiveThread = null;
        }

        /*
         * @see IProblemRequestorExtension#setIsHandlingTemporaryProblems(boolean)
         */
        @Override
        public void setIsHandlingTemporaryProblems(boolean enable) {
            if (fIsHandlingTemporaryProblems != enable) {
                fIsHandlingTemporaryProblems = enable;
                if (fIsHandlingTemporaryProblems)
                    startCollectingProblems();
                else
                    stopCollectingProblems();
            }

        }

        private Object getAnnotations(Position position) {
            synchronized (getLockObject()) {
                return fReverseMap.get(position);
            }
        }

        @Override
        @SuppressWarnings({"unchecked"})
        protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged)
                throws BadLocationException {
            super.addAnnotation(annotation, position, fireModelChanged);

            synchronized (getLockObject()) {
                Object cached = fReverseMap.get(position);
                if (cached == null)
                    fReverseMap.put(position, annotation);
                else if (cached instanceof List) {
                    List<Annotation> list = (List<Annotation>) cached;
                    list.add(annotation);
                } else if (cached instanceof Annotation) {
                    List<Annotation> list = new ArrayList<>(2);
                    list.add((Annotation)cached);
                    list.add(annotation);
                    fReverseMap.put(position, list);
                }
            }
        }

        @Override
        protected void removeAllAnnotations(boolean fireModelChanged) {
            super.removeAllAnnotations(fireModelChanged);
            synchronized (getLockObject()) {
                fReverseMap.clear();
            }
        }

        @Override
        @SuppressWarnings({"unchecked"})
        protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
            Position position = getPosition(annotation);
            synchronized (getLockObject()) {
                Object cached = fReverseMap.get(position);
                if (cached instanceof List) {
                    List<Annotation> list = (List<Annotation>) cached;
                    list.remove(annotation);
                    if (list.size() == 1) {
                        fReverseMap.put(position, list.get(0));
                        list.clear();
                    }
                } else if (cached instanceof Annotation) {
                    fReverseMap.remove(position);
                }
            }
            super.removeAnnotation(annotation, fireModelChanged);
        }

        // WARNING: Needed so we can expose the annotation model to the debugger
        ///////////////////////////////////////////////////////////////
        public void addMyMarkerAnnotation(IMarker marker) {
            synchronized(getLockObject()) {
                addMarkerAnnotation(marker);
                fireModelChanged();
            }
        }
    }

    /**
     * Internal class to avoid API addition for 3.3.2, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=170692
     *
     * @deprecated will be removed during 3.4
     */
    @Deprecated
    private static class InternalMarkerAnnotation extends MarkerAnnotation {

        /**
         * Tells whether this annotation is quick fixable.
         */
        private boolean fIsQuickFixable;
        /**
         * Tells whether the quick fixable state (<code>fIsQuickFixable</code> has been computed.
         */
        private boolean fIsQuickFixableStateSet;

        public InternalMarkerAnnotation(IMarker marker) {
            super(marker);
        }

        /*
         * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#setQuickFixable(boolean)
         */
        @Override
        public void setQuickFixable(boolean state) {
            fIsQuickFixable = state;
            fIsQuickFixableStateSet = true;
        }

        /*
         * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixableStateSet()
         */
        @Override
        public boolean isQuickFixableStateSet() {
            return fIsQuickFixableStateSet;
        }

        /*
         * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixable()
         */
        @Override
        public boolean isQuickFixable() {
            Assert.isTrue(isQuickFixableStateSet());
            return fIsQuickFixable;
        }

    }

    /**
     * Internal structure for mapping positions to some value. The reason for this specific structure is that positions
     * can change over time. Thus a lookup is based on value and not on hash value.
     */
    protected static class ReverseMap {

        static class Entry {
            Position fPosition;
            Object fValue;
        }

        private final List<Entry> fList = new ArrayList<>(2);
        private int fAnchor = 0;

        public ReverseMap() {}

        public Object get(Position position) {

            Entry entry;

            // behind anchor
            int length = fList.size();
            for (int i = fAnchor; i < length; i++) {
                entry = fList.get(i);
                if (entry.fPosition.equals(position)) {
                    fAnchor = i;
                    return entry.fValue;
                }
            }

            // before anchor
            for (int i = 0; i < fAnchor; i++) {
                entry = fList.get(i);
                if (entry.fPosition.equals(position)) {
                    fAnchor = i;
                    return entry.fValue;
                }
            }

            return null;
        }

        private int getIndex(Position position) {
            Entry entry;
            int length = fList.size();
            for (int i = 0; i < length; i++) {
                entry = fList.get(i);
                if (entry.fPosition.equals(position))
                    return i;
            }
            return -1;
        }

        public void put(Position position, Object value) {
            int index = getIndex(position);
            if (index == -1) {
                Entry entry = new Entry();
                entry.fPosition = position;
                entry.fValue = value;
                fList.add(entry);
            } else {
                Entry entry = fList.get(index);
                entry.fValue = value;
            }
        }

        public void remove(Position position) {
            int index = getIndex(position);
            if (index > -1)
                fList.remove(index);
        }

        public void clear() {
            fList.clear();
        }
    }

    public static class ApexProblemAnnotation extends ProblemAnnotation {
        IProblem problem;

        public ApexProblemAnnotation(IProblem problem) {
            super(problem, null);
            this.problem = problem;
        }

    }
}
