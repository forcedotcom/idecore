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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.QuietCloseable;

/**
 * Parse Platform log file
 * 
 * @author cwall
 * 
 */
public class LogReader {

    public static final long MAX_FILE_LENGTH = 1024 * 1024;
    private static final int ENTRY_STATE = 0;
    private static final int SUBENTRY_STATE = 1;
    private static final int MESSAGE_STATE = 2;
    private static final int STACK_STATE = 3;
    private static final int TEXT_STATE = 4;
    private static final int UNKNOWN_STATE = 5;

    public static void parseLogFile(File file, List<LogEntry> entries) throws UnsupportedEncodingException, IOException {
        if (file == null || !file.exists()) {
            return;
        }

        try (final QuietCloseable<BufferedReader> c = QuietCloseable.make(new BufferedReader(new InputStreamReader(new TailInputStream(file, MAX_FILE_LENGTH), Constants.FORCE_DEFAULT_ENCODING_CHARSET)))) {
            final BufferedReader reader = c.get();

            ArrayList<LogEntry> parents = new ArrayList<>();
            LogEntry current = null;
            int writerState = UNKNOWN_STATE;
            StringWriter swriter = null;
            PrintWriter writer = null;
            int state = UNKNOWN_STATE;

            try {
                boolean found = false;
                for (;;) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();

                    if (line.startsWith("!SESSION")) {
                        continue;
                    } else if (line.startsWith("!ENTRY")) {
                        state = ENTRY_STATE;
                        found = line.contains(Constants.FORCE_PLUGIN_PREFIX);
                        if (!found) {
                            continue;
                        }
                    } else if (line.startsWith("!SUBENTRY")) {
                        state = SUBENTRY_STATE;
                    } else if (line.startsWith("!MESSAGE")) {
                        state = MESSAGE_STATE;
                    } else if (line.startsWith("!STACK")) {
                        state = STACK_STATE;
                    } else {
                        state = TEXT_STATE;
                    }

                    if (state == TEXT_STATE && found) {
                        if (writer != null) {
                            writer.println(line);
                        }
                        continue;
                    }

                    if (writer != null && found) {
                        setLogData(current, writerState, swriter);
                        writerState = UNKNOWN_STATE;
                        swriter = null;
                        writer.close();
                        writer = null;
                    }

                    if (state == STACK_STATE && found) {
                        swriter = new StringWriter();
                        writer = new PrintWriter(swriter, true);
                        writerState = STACK_STATE;
                    } else if (state == ENTRY_STATE && found) {
                        LogEntry entry = new LogEntry();
                        entry.processEntry(line);
                        setNewLogParent(parents, entry, 0);
                        current = entry;
                        entries.add(entry);
                    } else if (state == MESSAGE_STATE && found) {
                        swriter = new StringWriter();
                        writer = new PrintWriter(swriter, true);
                        String message = Constants.EMPTY_STRING;
                        if (line.length() > 8) {
                            message = line.substring(9).trim();
                        }

                        message = message.trim();
                        if (current != null) {
                            current.setMessage(message);
                        }
                        writerState = MESSAGE_STATE;
                    }
                }

                if (swriter != null && current != null && writerState == STACK_STATE && found) {
                    writerState = UNKNOWN_STATE;
                    current.setStack(swriter.toString());
                }
            } finally {
                if (writer != null) {
                    setLogData(current, writerState, swriter);
                    writer.close();
                }
            }
        }
    }

    private static void setLogData(LogEntry current, int writerState, StringWriter swriter) {
        if (writerState == STACK_STATE && current != null) {
            current.setStack(swriter.toString());
        } else if (writerState == MESSAGE_STATE && current != null) {
            StringBuffer sb = new StringBuffer(current.getMessage());
            sb.append(swriter.toString());
            current.setMessage(sb.toString().trim());
        }
    }

    private static void setNewLogParent(ArrayList<LogEntry> parents, LogEntry entry, int depth) {
        if (depth + 1 > parents.size()) {
            parents.add(entry);
        } else {
            parents.set(depth, entry);
        }
    }
}
