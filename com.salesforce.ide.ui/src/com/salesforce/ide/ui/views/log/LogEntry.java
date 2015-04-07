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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.Messages;

/**
 * Models Platform log entry
 * 
 * @author cwall
 * 
 */
public class LogEntry extends PlatformObject implements IWorkbenchAdapter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(Constants.STANDARD_DATE_FORMAT);

    private final List<LogEntry> children = new ArrayList<>();
    private static final int MAX_LABEL_LENGTH = 325;
    public static final int TRACE_SEVERITY = 999;
    public static final String TRACE_SEVERITY_STR = "TRACE";
    public static final String MESSAGE_SEPARATOR = ") -";
    protected Object parent = null;
    private String pluginId = null;
    private int severity = TRACE_SEVERITY;
    private int code;
    private String logDateString = null;
    private Date logDate = null;
    private String message = null;
    private String stack = null;

    //   C O N S T R U C T O R S
    public LogEntry() {}

    public LogEntry(IStatus status) {
        processStatus(status);
    }

    //   M E T H O D S
    @Override
    public Object getParent(Object o) {
        return parent;
    }

    public void setParent(LogEntry parent) {
        this.parent = parent;
    }

    public int getSeverity() {
        return severity;
    }

    public int getCode() {
        return code;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(boolean strip) {
        if (Utils.isEmpty(message) && !message.contains(MESSAGE_SEPARATOR)) {
            return message;
        }

        return message.substring(message.indexOf(MESSAGE_SEPARATOR) + MESSAGE_SEPARATOR.length());
    }

    public String getStack() {
        return stack;
    }

    public String getFormattedDate() {
        if (logDateString == null) {
            logDateString = DATE_FORMAT.format(getDate());
        }

        return logDateString;
    }

    public Date getDate() {
        if (logDate == null) {
            logDate = new Date(0);
        }

        return logDate;
    }

    public String getSeverityText() {
        return getSeverityText(severity);
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object arg0) {
        return null;
    }

    @Override
    public String getLabel(Object obj) {
        return getSeverityText();
    }

    private static String getSeverityText(int severity) {
        switch (severity) {
        case IStatus.ERROR:
            return Messages.ERROR;
        case IStatus.WARNING:
            return Messages.WARNING;
        case IStatus.INFO:
            return Messages.INFO;
        case IStatus.OK:
            return "OK";
        case TRACE_SEVERITY:
            return Messages.TRACE;
        default:
            return "?";
        }
    }

    void processEntry(String line) {
        StringTokenizer stok = new StringTokenizer(line, " ");
        int tokenCount = stok.countTokens();
        boolean noSeverity = stok.countTokens() < 5;

        // no severity means it should be represented as OK
        if (noSeverity) {
            severity = 0;
            code = 0;
        }
        StringBuffer dateBuffer = new StringBuffer();
        for (int i = 0; i < tokenCount; i++) {
            String token = stok.nextToken();
            switch (i) {
            case 0:
                break;
            case 1:
                pluginId = token;
                break;
            case 2:
                if (noSeverity) {
                    if (dateBuffer.length() > 0) {
                        dateBuffer.append(" ");
                    }
                    dateBuffer.append(token);
                } else {
                    severity = parseInteger(token);
                }
                break;
            case 3:
                if (noSeverity) {
                    if (dateBuffer.length() > 0) {
                        dateBuffer.append(" ");
                    }
                    dateBuffer.append(token);
                } else
                    code = parseInteger(token);
                break;
            default:
                if (dateBuffer.length() > 0) {
                    dateBuffer.append(" ");
                }
                dateBuffer.append(token);
            }
        }
        try {
            Date date = DATE_FORMAT.parse(dateBuffer.toString());
            if (date != null) {
                logDate = date;
                logDateString = DATE_FORMAT.format(logDate);
            }
        } catch (ParseException e) { // do nothing
        }
    }

    private static int parseInteger(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    void setStack(String stack) {
        this.stack = stack;
    }

    void setMessage(String message) {
        if (Utils.isNotEmpty(message)) {
            // strip newlines
            message = message.replaceAll(Constants.NEW_LINE, " ");

            // limit message size; full message display not supported at this time; message may be truncated further
            if (message.length() > MAX_LABEL_LENGTH) {
                message = message.substring(0, MAX_LABEL_LENGTH);
            }
            // istatus doesn't understand trace level messages
            if (message.startsWith(TRACE_SEVERITY_STR)) {
                severity = TRACE_SEVERITY;
            }
        }
        this.message = message;
    }

    private void processStatus(IStatus status) {
        pluginId = status.getPlugin();
        severity = status.getSeverity();
        code = status.getCode();
        logDate = new Date();
        logDateString = DATE_FORMAT.format(logDate);
        setMessage(status.getMessage());
        Throwable throwable = status.getException();
        if (throwable != null) {
            try (final QuietCloseable<StringWriter> c0 = QuietCloseable.make(new StringWriter())) {
                final StringWriter swriter = c0.get();

                try (final QuietCloseable<PrintWriter> c = QuietCloseable.make(new PrintWriter(swriter))) {
                    final PrintWriter pwriter = c.get();
    
                    throwable.printStackTrace(pwriter);
                    pwriter.flush();
                }
                stack = swriter.toString();
            }
        }

        IStatus[] schildren = status.getChildren();
        if (schildren.length > 0) {
            for (int i = 0; i < schildren.length; i++) {
                LogEntry child = new LogEntry(schildren[i]);
                addChild(child);
            }
        }
    }

    public void write(PrintWriter writer) {
        writer.println(getSeverityText());
        if (logDate != null)
            writer.println(getDate());

        if (message != null)
            writer.println(getMessage());

        if (stack != null) {
            writer.println();
            writer.println(stack);
        }
    }

    public void addChild(LogEntry child) {
        if (child != null) {
            children.add(0, child);
            child.setParent(this);
        }
    }

    @Override
    public Object[] getChildren(Object parent) {
        return children.toArray();
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

    public int size() {
        return children.size();
    }

    @Override
    public String toString() {
        return getSeverityText();
    }
}
