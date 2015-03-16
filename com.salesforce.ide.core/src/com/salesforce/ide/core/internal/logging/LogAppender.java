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
package com.salesforce.ide.core.internal.logging;

import java.text.MessageFormat;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

import com.salesforce.ide.core.ForceIdeCorePlugin;

public class LogAppender extends AppenderSkeleton {

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void append(LoggingEvent event) {

        // don't go any further if event is not severe enough.
        if (!isAsSevereAsThreshold(event.getLevel())) {
            return;
        }

        ILog log = getBundleILog();
        if (log == null) {
            return;
        }

        // if throwable information is available, extract it.
        Throwable t = null;
        if (event.getThrowableInformation() != null && layout.ignoresThrowable()) {
            t = event.getThrowableInformation().getThrowable();
        }

        // build an Eclipse Status record, map severity and code from Event.
        Status s = new Status(getSeverity(event), ForceIdeCorePlugin.getPluginId(), getCode(event),
                layout.format(event), t);

        log.log(s);
    }

    /**
     * map LoggingEvent's level to Status severity
     * @param ev
     * @return
     */
    private static int getSeverity(LoggingEvent ev) {
        Level level = ev.getLevel();
        if(level == Level.FATAL || level == Level.ERROR)
            return IStatus.ERROR;
        else if(level == Level.WARN)
            return IStatus.WARNING;
        else if(level == Level.INFO)
            return IStatus.INFO;
        else // debug, trace and custom levels
            return IStatus.OK;
    }

    /**
     * map LoggingEvent to Status code
     * @param ev
     * @return
     */
    private static int getCode(LoggingEvent ev) {
        return 0;
    }

    private ILog getBundleILog() {
        // get the bundle for a plug-in
        Bundle b = Platform.getBundle(ForceIdeCorePlugin.getPluginId());
        if(b == null) {
            String m = MessageFormat.format("Plugin: {0} not found in {1}.",
                    new Object[] { ForceIdeCorePlugin.getPluginId(), this.name });
            this.errorHandler.error(m);
            return null;
        }

        return Platform.getLog(b);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
