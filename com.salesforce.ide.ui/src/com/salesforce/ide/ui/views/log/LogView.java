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
package com.salesforce.ide.ui.views.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 * Displays Force.com IDE logs found in the Platform log file
 * 
 * @author cwall
 * 
 */
public class LogView extends ViewPart implements ILogListener {

    private static final Logger logger = Logger.getLogger(LogView.class);

    protected static final String EXCEPTION = "Exception";
    protected static final String OPEN_LOG_EXCEPTION = "(Open log file for full message and/or stacktrace)";
    protected static final String OPEN_LOG_EXCEPTION_FULL = "... " + OPEN_LOG_EXCEPTION;
    protected static final String OPEN_LOG_FULL = "... (Open log file for full message)";
    protected static final int MAX_LABEL_LENGTH = 200;
    protected static final byte SEVERITY = 0x0;
    protected static final byte MESSAGE = 0x1;
    protected static final byte DATE = 0x2;
    protected static final int ASCENDING = 1;
    protected static final int DESCENDING = -1;
    private static final int MAX_LOG_VIEW_ENTRIES = 500;

    private int severityOrder;
    private int messageOrder;
    private int dateOrder;
    private List<LogEntry> logEntries = null;
    private List<LogEntry> batchedLogEntries = null;
    private boolean batchEntries;
    private File logFile = null;
    private Comparator<? extends LogEntry> logComparator = null;
    private boolean firstEvent = true;
    private TreeColumn logSeverityColumn = null;
    private TreeColumn logMessageColumn = null;
    private TreeColumn logDateColumn = null;
    private Tree logTree = null;
    private FilteredTree logFilteredTree = null;
    private final LogViewLabelProvider logViewLabelProvider = new LogViewLabelProvider();
    private Action openLogFileAction = null;
    private Action openLogFolderAction = null;
    private LogView instance = null;

    public LogView() {
    	this( Platform.getLogFileLocation().toFile());
    }
    public LogView(File logFile) {
        logEntries = new ArrayList<>();
        batchedLogEntries = new ArrayList<>();
        this.logFile = logFile;
        // maintain instance of self so that the perspective listener focus on the particular log view instance
        instance = this;
    }

    protected File getLogFile() {
        return logFile;
    }

    public boolean isBatchEntries() {
        return batchEntries;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public List<LogEntry> getBatchedLogEntries() {
        return batchedLogEntries;
    }

    public int getBatchedLogEntriesCount() {
        return Utils.isNotEmpty(batchedLogEntries) ? batchedLogEntries.size() : 0;
    }

    public TreeColumn getLogMessageColumn() {
        return logMessageColumn;
    }

    public TreeColumn getLogDateColumn() {
        return logDateColumn;
    }

    public TreeColumn getLogSeverityColumn() {
        return logSeverityColumn;
    }

    public void setLogSeverityColumn(TreeColumn logSeverityColumn) {
        this.logSeverityColumn = logSeverityColumn;
    }

    public Tree getLogTree() {
        return logTree;
    }

    public FilteredTree getLogFilteredTree() {
        return logFilteredTree;
    }

    public LogViewLabelProvider getLogViewLabelProvider() {
        return logViewLabelProvider;
    }

    public Action getOpenLogFileAction() {
        return openLogFileAction;
    }

    public Action getOpenLogFolderAction() {
        return openLogFolderAction;
    }

    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        readLogFile();
        initViewer(composite);
        initActions();
        initializeViewerSorter();
        getSite().setSelectionProvider(logFilteredTree.getViewer());

        logTree.setToolTipText("View Force.com IDE Log");

        Platform.addLogListener(this);

        // batch entries when view is hidden; display batched entries when view is visible
        getSite().getPage().addPartListener(new org.eclipse.ui.IPartListener2() {
            @Override
            public void partHidden(IWorkbenchPartReference partRef) {
                if (!(partRef instanceof IViewReference)) {
                    return;
                }

                IWorkbenchPart part = partRef.getPart(false);
                if (part == null || !part.equals(instance)) {
                    return;
                }

                batchEntries = true;
            }

            @Override
            public void partVisible(IWorkbenchPartReference partRef) {
                if (!(partRef instanceof IViewReference)) {
                    return;
                }

                IWorkbenchPart part = partRef.getPart(false);
                if (part == null || !part.equals(instance)) {
                    return;
                }

                if (Utils.isNotEmpty(batchedLogEntries)) {
                    displayBatchedEntries();
                }

                batchEntries = false;
            }

            @Override
            public void partActivated(IWorkbenchPartReference partRef) {}

            @Override
            public void partDeactivated(IWorkbenchPartReference partRef) {}

            @Override
            public void partBroughtToTop(IWorkbenchPartReference partRef) {}

            @Override
            public void partInputChanged(IWorkbenchPartReference partRef) {}

            @Override
            public void partOpened(IWorkbenchPartReference partRef) {}

            @Override
            public void partClosed(IWorkbenchPartReference partRef) {}
        });

        UIUtils.setHelpContext(logFilteredTree, this.getClass().getSimpleName());
    }

    private void initActions() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager toolBarManager = bars.getToolBarManager();

        // init open file and folder actions
        openLogFileAction = new OpenLogFileAction();
        toolBarManager.add(openLogFileAction);
        openLogFolderAction = new OpenLogFolderAction();
        toolBarManager.add(openLogFolderAction);
    }

    private void initViewer(Composite parent) {
        logFilteredTree = new FilteredTree(parent, SWT.FULL_SELECTION, new PatternFilter() {
            @Override
            protected boolean isLeafMatch(Viewer viewer, Object element) {
                if (element instanceof LogEntry) {
                    LogEntry logEntry = (LogEntry) element;
                    String message = logEntry.getMessage();
                    String date = logEntry.getFormattedDate();
                    return wordMatches(message) || wordMatches(date);
                }
                return false;
            }
        }, false);

        logFilteredTree.setInitialText(UIMessages.getString("FilterInitialText"));
        logTree = logFilteredTree.getViewer().getTree();
        logTree.setLinesVisible(true);

        logSeverityColumn = new TreeColumn(logTree, SWT.LEFT);
        logSeverityColumn.setText("Severity");
        logSeverityColumn.setWidth(70);
        logSeverityColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                severityOrder *= -1;
                ViewerComparator comparator = getViewerComparator(SEVERITY);
                logFilteredTree.getViewer().setComparator(comparator);
                setComparator(SEVERITY);
                setColumnSorting(logSeverityColumn, severityOrder);
            }
        });

        logMessageColumn = new TreeColumn(logTree, SWT.LEFT);
        logMessageColumn.setText("Message");
        logMessageColumn.setWidth(500);
        logMessageColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                messageOrder *= -1;
                ViewerComparator comparator = getViewerComparator(MESSAGE);
                logFilteredTree.getViewer().setComparator(comparator);
                setComparator(MESSAGE);
                setColumnSorting(logMessageColumn, messageOrder);
            }
        });

        logDateColumn = new TreeColumn(logTree, SWT.LEFT);
        logDateColumn.setText("Date");
        logDateColumn.setWidth(75);
        logDateColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dateOrder *= -1;
                ViewerComparator comparator = getViewerComparator(DATE);
                logFilteredTree.getViewer().setComparator(comparator);
                setComparator(DATE);
                setColumnSorting(logDateColumn, dateOrder);
            }
        });

        logTree.setHeaderVisible(true);

        logFilteredTree.getViewer().setContentProvider(new LogViewContentProvider(this));
        logFilteredTree.getViewer().setLabelProvider(logViewLabelProvider);
        logFilteredTree.getViewer().setInput(this);
    }

    private void initializeViewerSorter() {
        byte orderType = DATE;
        ViewerComparator comparator = getViewerComparator(orderType);
        logFilteredTree.getViewer().setComparator(comparator);
        if (orderType == MESSAGE) {
            setColumnSorting(logMessageColumn, messageOrder);
        } else if (orderType == DATE) {
            setColumnSorting(logDateColumn, dateOrder);
        }
    }

    private void setColumnSorting(TreeColumn column, int order) {
        logTree.setSortColumn(column);
        logTree.setSortDirection(order == ASCENDING ? SWT.UP : SWT.DOWN);
    }

    @Override
    public void dispose() {
        Platform.removeLogListener(instance);
        logViewLabelProvider.dispose();
        logFilteredTree.dispose();
        super.dispose();
    }

    public LogEntry[] getElements() {
        return logEntries.toArray(new LogEntry[logEntries.size()]);
    }

    private void readLogFile() {
        logEntries.clear();

        try {
            // parse log file and create log entries
            List<LogEntry> result = new ArrayList<>();
            LogReader.parseLogFile(logFile, result);
            logEntries.addAll(result);
            limitEntriesCount();
        } catch (Exception e) {
            logger.error("Unable to read Force.com IDE log file", e);
            Utils.openError(e, true, "Unable to read Force.com IDE log file");
        }
    }

    private void limitEntriesCount() {
        int entriesCount = getEntriesCount();

        if (entriesCount <= MAX_LOG_VIEW_ENTRIES) {
            return;
        }

        logEntries.subList(0, logEntries.size() - MAX_LOG_VIEW_ENTRIES).clear();
    }

    private int getEntriesCount() {
        return logEntries.size();
    }

    @Override
    public void logging(IStatus status, String plugin) {
        // skip non-Force.com log events
        if (Utils.isEmpty(status.getPlugin()) || !status.getPlugin().contains(Constants.FORCE_PLUGIN_PREFIX)) {
            return;
        }

        if (batchEntries) {
            LogEntry entry = new LogEntry(status);
            batchedLogEntries.add(entry);
            return;
        }

        if (firstEvent) {
            readLogFile();
            asyncRefresh(true);
            firstEvent = false;
        } else {
            LogEntry entry = new LogEntry(status);
            if (!batchedLogEntries.isEmpty()) {
                batchedLogEntries.add(entry);
                displayBatchedEntries();
            } else {
                pushEntry(entry);
            }
        }
    }

    private void displayBatchedEntries() {
        Job job = new Job("Batching Force.com IDE log entries...") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (int i = 0; i < batchedLogEntries.size(); i++) {
                    if (!monitor.isCanceled()) {
                        LogEntry entry = batchedLogEntries.get(i);
                        pushEntry(entry);
                        batchedLogEntries.remove(i);
                    }
                }

                if (batchedLogEntries.size() > 0) {
                    batchedLogEntries.clear();
                }

                asyncRefresh(true);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private synchronized void pushEntry(LogEntry entry) {
        logEntries.addAll(Collections.singletonList(entry));
        limitEntriesCount();
        asyncRefresh(true);
    }

    private void asyncRefresh(final boolean activate) {
        if (logTree.isDisposed()) {
            return;
        }

        Display display = logTree.getDisplay();
        if (display != null) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!logTree.isDisposed()) {
                        TreeViewer viewer = logFilteredTree.getViewer();
                        viewer.refresh();
                    }
                }
            });
        }
    }

    @Override
    public void setFocus() {
        if (logFilteredTree != null && !logFilteredTree.isDisposed()) {
            logFilteredTree.setFocus();
        }
    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        dateOrder = DESCENDING;
        messageOrder = DESCENDING;
        severityOrder = DESCENDING;
        setComparator(DATE);
    }

    public Comparator<? extends LogEntry> getComparator() {
        return logComparator;
    }

    private void setComparator(byte sortType) {
        if (sortType == DATE) {
            logComparator = new Comparator<LogEntry>() {
                @Override
                public int compare(LogEntry entry1, LogEntry entry2) {
                    long date1 = (entry1).getDate().getTime();
                    long date2 = (entry2).getDate().getTime();

                    if (date1 == date2) {
                        int result = logEntries.indexOf(entry2) - logEntries.indexOf(entry1);
                        if (dateOrder == DESCENDING) {
                            result *= DESCENDING;
                        }
                        return result;
                    }
                    if (dateOrder == DESCENDING) {
                        return date1 > date2 ? DESCENDING : ASCENDING;
                    }

                    return date1 < date2 ? DESCENDING : ASCENDING;
                }
            };
        } else if (sortType == MESSAGE) {
            logComparator = new Comparator<LogEntry>() {
                @Override
                public int compare(LogEntry entry1, LogEntry entry2) {
                    return String.CASE_INSENSITIVE_ORDER.compare(entry1.getMessage(true), entry2.getMessage(true))
                            * messageOrder;
                }
            };
        } else {
            logComparator = new Comparator<LogEntry>() {
                @Override
                public int compare(LogEntry entry1, LogEntry entry2) {
                    if (entry1.getSeverity() == entry2.getSeverity()) {
                        return 0;
                    } else if (entry1.getSeverity() > entry2.getSeverity()) {
                        return 1 * severityOrder;
                    } else {
                        return -1 * severityOrder;
                    }
                }
            };
        }
    }

    private ViewerComparator getViewerComparator(byte sortType) {
        if (sortType == SEVERITY) {
            return new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
                        LogEntry entry1 = (LogEntry) e1;
                        LogEntry entry2 = (LogEntry) e2;
                        if (entry1.getSeverity() == entry2.getSeverity()) {
                            return 0;
                        } else if (entry1.getSeverity() > entry2.getSeverity()) {
                            return 1 * severityOrder;
                        } else {
                            return -1 * severityOrder;
                        }
                    }
                    return 0;
                }
            };
        } else if (sortType == MESSAGE) {
            return new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
                        LogEntry entry1 = (LogEntry) e1;
                        LogEntry entry2 = (LogEntry) e2;
                        return String.CASE_INSENSITIVE_ORDER.compare(entry1.getMessage(true), entry2.getMessage(true))
                                * messageOrder;
                    }
                    return 0;
                }
            };
        } else {
            return new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    long date1 = 0;
                    long date2 = 0;
                    if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
                        date1 = ((LogEntry) e1).getDate().getTime();
                        date2 = ((LogEntry) e2).getDate().getTime();
                    }

                    if (date1 == date2) {
                        int result = logEntries.indexOf(e2) - logEntries.indexOf(e1);
                        if (dateOrder == DESCENDING)
                            result *= DESCENDING;
                        return result;
                    }
                    if (dateOrder == DESCENDING)
                        return date1 > date2 ? DESCENDING : ASCENDING;
                    return date1 < date2 ? DESCENDING : ASCENDING;
                }
            };
        }
    }

    public void sortByDateDescending() {
        setColumnSorting(logDateColumn, DESCENDING);
    }

    // content and label providers
    class LogViewContentProvider implements ITreeContentProvider {
        private LogView logView = null;

        public LogViewContentProvider(LogView logView) {
            this.logView = logView;
        }

        @Override
        public void dispose() {}

        @Override
        public Object[] getChildren(Object element) {
            return ((LogEntry) element).getChildren(element);
        }

        @Override
        public Object[] getElements(Object element) {
            return logView.getElements();
        }

        @Override
        public Object getParent(Object element) {
            return ((LogEntry) element).getParent(element);
        }

        @Override
        public boolean hasChildren(Object element) {
            return ((LogEntry) element).getChildren(element).length > 0;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

        public boolean isDeleted(Object element) {
            return false;
        }
    }

    class LogViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider {
       
        

        public LogViewLabelProvider() {}

        @Override
        public void dispose() {
            super.dispose();
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
        	LogEntry entry = (LogEntry) element;
            if (columnIndex == 0) {
                switch (entry.getSeverity()) {
                case IStatus.INFO:
                    return ForceImages.get(ForceImages.INFO_ICON);
                case IStatus.OK:
                    return ForceImages.get(ForceImages.DEBUG_ICON);
                case IStatus.WARNING:
                    return ForceImages.get(ForceImages.WARNING_ICON);
                case LogEntry.TRACE_SEVERITY:
                    return ForceImages.get(ForceImages.TRACE_ICON);
                default:
                    return ForceImages.get(ForceImages.ERROR_ICON);
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
        if (!(element instanceof LogEntry)) {
                return "Log statement not found";
            }

            LogEntry entry = (LogEntry) element;
            switch (columnIndex) {
            case 1:
                String message = entry.getMessage(true);
                if (Utils.isNotEmpty(message)) {
                    message = message.trim();
                    if (entry.getSeverity() == IStatus.WARNING || entry.getSeverity() == IStatus.ERROR) {
                        return getExceptionMessage(message, entry);
                    } else if (message.length() > MAX_LABEL_LENGTH) {
                        return getTruncatedMessage(message, OPEN_LOG_FULL);
                    } else {
                        return message;
                    }
                }
				return "No message found";
            case 2:
                return new SimpleDateFormat(Constants.STANDARD_DATE_FORMAT).format(entry.getDate());
            default:
                return Constants.EMPTY_STRING;
            }
        }

        public String getExceptionMessage(String message, LogEntry entry) {
            if (Utils.isNotEmpty(entry.getStack()) && entry.getStack().contains(EXCEPTION)) {
                String exception =
                        entry.getStack().substring(0, entry.getStack().indexOf(EXCEPTION) + EXCEPTION.length());
                if (exception.contains(Constants.DOT)) {
                    exception = exception.substring(exception.lastIndexOf(Constants.DOT) + 1);
                }
                StringBuffer sb = new StringBuffer("(");
                sb.append(exception).append(") ");
                message = sb.toString() + message;
            }

            if (message.length() > MAX_LABEL_LENGTH
                    || (message.length() + OPEN_LOG_EXCEPTION_FULL.length()) > MAX_LABEL_LENGTH) {
                message = getTruncatedMessage(message, OPEN_LOG_EXCEPTION_FULL);
            } else {
                message += " "+OPEN_LOG_EXCEPTION;
            }

            return message;
        }

        private String getTruncatedMessage(String message, String truncateStr) {
            if (Utils.isEmpty(message) || message.length() < MAX_LABEL_LENGTH) {
                return message;
            }
			StringBuffer sb = new StringBuffer(message.substring(0, MAX_LABEL_LENGTH - truncateStr.length()));
			sb.append(truncateStr);
			return sb.toString();
        }

        @Override
        public Font getFont(Object element, int columnIndex) {
            return null;
        }
    }
}
