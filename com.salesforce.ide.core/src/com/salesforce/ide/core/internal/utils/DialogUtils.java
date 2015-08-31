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
package com.salesforce.ide.core.internal.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.ForceException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.services.RetrieveException;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;

public class DialogUtils {

    private static final Logger logger = Logger.getLogger(DialogUtils.class);

    public static final String CONTINUE_LABEL = Messages.getString("Continue");
    public static final int CONTINUE_ID = 1025;
    public static final int FIRST_BUTTON = 0;

    private static DialogUtils instance = null;

    protected DialogUtils() {}

    public static DialogUtils getInstance() {
        if (instance == null) {
            instance = new DialogUtils();
        }
        return instance;

    }

    public int retryAbortMessage(String title, String message) {
        // retry is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR, new String[] {
                    IDialogConstants.RETRY_LABEL, IDialogConstants.ABORT_LABEL }, IDialogConstants.RETRY_ID);
        return dialog.open();
    }

    public int retryAbortCancelMessage(String title, String message) {
        // retry is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR, new String[] {
                    IDialogConstants.RETRY_LABEL, IDialogConstants.ABORT_LABEL, IDialogConstants.CANCEL_LABEL },
                    IDialogConstants.RETRY_ID);
        return dialog.open();
    }

    public int retryOkCancelMessage(String title, String message) {
        // retry is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR, new String[] {
                    IDialogConstants.RETRY_LABEL, IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
                    IDialogConstants.RETRY_ID);
        return dialog.open();
    }

    public int retryOkMessage(String title, String message) {
        // retry is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR, new String[] {
                    IDialogConstants.RETRY_LABEL, IDialogConstants.OK_LABEL }, IDialogConstants.RETRY_ID);
        return dialog.open();
    }

    public int okMessage(String title, String message) {
        return okMessage(title, message, MessageDialog.ERROR);
    }

    public int okMessage(String title, String message, int severity) {
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, severity,
                    new String[] { IDialogConstants.OK_LABEL }, IDialogConstants.OK_ID);
        return dialog.open();
    }

    public int closeMessage(String title, String message) {
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, MessageDialog.INFORMATION,
                    new String[] { IDialogConstants.CLOSE_LABEL }, IDialogConstants.CLOSE_ID);
        return dialog.open();
    }

    public int yesNoMessage(String title, String message, int severity) {
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, severity, new String[] {
                    IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, IDialogConstants.NO_ID);
        return dialog.open();
    }

    public int continueMessage(String title, String message, int severity) {
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, severity, new String[] { CONTINUE_LABEL }, 1);
        return dialog.open();
    }

    public int cancelMessage(String title, String message, int severity) {
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, severity,
                    new String[] { IDialogConstants.CANCEL_LABEL }, IDialogConstants.CANCEL_ID);
        return dialog.open();
    }

    public int abortOkMessage(String title, String message) {
        return abortOkMessage(title, message, MessageDialog.ERROR);
    }

    public int abortOkMessage(String title, String message, int severity) {
        // ok is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, severity, new String[] {
                    IDialogConstants.ABORT_LABEL, IDialogConstants.OK_LABEL }, IDialogConstants.OK_ID);
        return dialog.open();
    }

    public int abortContinueMessage(String title, String message, int severity) {
        // ok is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, severity, new String[] {
                    IDialogConstants.ABORT_LABEL, CONTINUE_LABEL }, 1);
        return dialog.open();
    }
    
    public int cancelContinueMessage(String title, String message, int severity) {
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, severity, new String[] {
                    IDialogConstants.CANCEL_LABEL, CONTINUE_LABEL }, 1);
        return dialog.open();
    }

    public int abortMessage(String title, String message) {
        // ok is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR,
                    new String[] { IDialogConstants.ABORT_LABEL }, IDialogConstants.ABORT_ID);
        return dialog.open();
    }

    public boolean retryConnection(ForceException ex, IProgressMonitor monitor) {
        String exceptionMessage = ForceExceptionUtils.getExceptionMessage(ex);
        logger.error("Unable to create or get connection", ex);
        StringBuffer strBuff = new StringBuffer(Messages.getString("General.ConnectionError.message"));
        strBuff.append(":\n\n").append(exceptionMessage).append("\n\n")
        .append(Messages.getString("General.ConnectionErrorRetry.message"));
        int action = retryAbortMessage("Connection Error", strBuff.toString());
        if (action == FIRST_BUTTON) {
            if (logger.isInfoEnabled()) {
                logger.info("Retrying connection");
            }
            return true;
        }
        logger.warn("Abort create or get connection");
        return false;
    }

    public void presentFetchExceptionDialog(Exception ex, IProgressMonitor monitor) throws Exception {
        if (ex instanceof RetrieveException || ex instanceof ServiceException || ex instanceof FactoryException
                || ex instanceof InvocationTargetException) {
            String exceptionMessage = ForceExceptionUtils.getExceptionMessage(ex);
            exceptionMessage = ForceExceptionUtils.getStrippedExceptionMessage(exceptionMessage);
            StringBuffer strBuff = new StringBuffer(Messages.getString("General.FetchError.message"));
            strBuff.append(":\n\n").append(exceptionMessage).append("\n\n")
            .append(Messages.getString("General.AbortOrContinue.message"));
            int action = abortContinueMessage("Fetch Error", strBuff.toString(), MessageDialog.ERROR);
            if (action == FIRST_BUTTON) {
                logger.info("Abort remote component fetch and project creation");
                throw new ForceProjectException(ex, "Unable to fetch components");
            }
        } else {
            throw ex;
        }
    }

    public void presentInsufficientPermissionsDialog(InsufficientPermissionsException ex) {
        logger.warn("Insufficient permissions encountered for user '" + ex.getConnection().getUsername() + "'");
        okMessage("Insufficient Permissions", ex.getExceptionMessage(), MessageDialog.WARNING);
    }

    public void invalidLoginDialog(String exceptionMessage) {
        invalidLoginDialog(exceptionMessage, null, true);
    }

    public void invalidLoginDialog(String exceptionMessage, String projectName, boolean showProjectProperties) {
        List<String> params = new ArrayList<>();

        params.add(projectName == null ? "" : Messages.getString(
            "General.InvalidLogin.UpdateCredentials.Parms.project", new String[] { projectName }));
        params.add(exceptionMessage);
        params.add(!showProjectProperties ? "" : Messages.getString("General.InvalidLogin.UpdateCredentials.message"));

        MessageDialog.openWarning(getShell(), "Invalid Login",
            Messages.getString("General.InvalidLogin.UpdateCredentials.Parms.message", params.toArray()));
    }

    public boolean presentCycleLimitExceptionDialog(ServiceTimeoutException ex, IProgressMonitor monitor) {
        String exceptionMessage = ForceExceptionUtils.getExceptionMessage(ex);
        final StringBuffer strBuff = new StringBuffer(Messages.getString("General.FetchCycleLimitReached.message"));
        strBuff.append(":\n\n").append(exceptionMessage).append("\n\n")
        .append(Messages.getString("General.FetchTimeoutRetry.message"));

        // must execute in separate ui thread as operation may be executed outside of a ui thread (invalid thread access)
        int action = (new Runnable() {
            int action = -1;

            @Override
            public void run() {
                action = abortContinueMessage("Fetch Cycle Limit Reached", strBuff.toString(), MessageDialog.WARNING);
            }

            public int getAction() {
                Display.getDefault().syncExec(this);
                return action;
            }
        }).getAction();

        //int action = abortContinueMessage("Fetch Cycle Limit Reached", strBuff.toString(), MessageDialog.WARNING);
        if (action == FIRST_BUTTON) {
            logger.warn("Abort remote component fetching");
            return false;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Continuing remote component fetching");
        }
        return true;
    }

    public static final Shell getShell() {
        return Display.getDefault().getActiveShell();
    }
}
