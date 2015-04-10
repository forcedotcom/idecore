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

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.*;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.*;
import com.salesforce.ide.core.services.RetrieveException;
import com.sforce.soap.metadata.StatusCode;
import com.sforce.soap.partner.fault.wsc.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 *
 * @author cwall
 */
public class ForceExceptionUtils {

    private static final Logger logger = Logger.getLogger(ForceExceptionUtils.class);

    private static final String[] RETRYABLE_CODES = new String[] { ExceptionCode.INVALID_SESSION_ID.toString() };
    private static final String NO_EXCEPTION_MESSAGE_MESSAGE = Messages.getString("General.NoExceptionMessage.message");
    private static final String[] STRIPPED_PREFIX_STRINGS =
            new String[] { "AxisFault: null:", "AxisFault:", "null:", "Exception:" };
    private static final String[] STRIPPED_SUFFIX_STRINGS = new String[] { "(UNKNOWN_EXCEPTION)" };
    private static final String PACKAGE_NOT_FOUND = "No package named";

    public static void handleRemoteException(Connection connection, Throwable th)
            throws InsufficientPermissionsException, ForceRemoteException {
        if (isInsufficientPermissionsException(th)) {
            throwNewInsufficientPermissionsException(connection, th);
        } else {
            throw new ForceRemoteException(getStrippedRootCauseMessage(th), connection, th);
        }
    }

    public static void throwNewConnectionException(Connection connection, Throwable th) throws InvalidLoginException,
            ForceConnectionException {
        if (th instanceof LoginFault) {
            throw new InvalidLoginException(getExceptionMessage(th), getExceptionCode(th), connection, th);
        } else if (th instanceof ApiFault) {
            throw new ForceConnectionException(getExceptionMessage(th), getExceptionCode(th), connection, th);
        } else {
            throw new ForceConnectionException(getStrippedRootCauseMessage(th), connection, th);
        }
    }

    public static void handleConnectionException(Connection connection, Throwable th) throws InvalidLoginException,
            ForceConnectionException, InsufficientPermissionsException {
        if (th instanceof LoginFault) {
            throw new InvalidLoginException(getExceptionMessage(th), getExceptionCode(th), connection, th);
        } else if (isInsufficientPermissionsException(th)) {
            throwNewInsufficientPermissionsException(connection, th);
        } else if (th instanceof ApiFault) {
            throw new ForceConnectionException(getExceptionMessage(th), getExceptionCode(th), connection, th);
        } else if (th.getCause() != null && connection.getConnectorConfig() != null) {
            String msg = getConnectionCauseExceptionMessage(connection.getConnectorConfig(), th);
            throw new ForceConnectionException(msg, connection, th);
        } else {
            throw new ForceConnectionException(getStrippedRootCauseMessage(th), connection, th);
        }
    }

    public static String getConnectionCauseExceptionMessage(ConnectorConfig connectorConfig, Throwable th) {
        StringBuffer strBuff = null;
        if (connectorConfig != null && Utils.isNotEmpty(connectorConfig.getServiceEndpoint())) {
            strBuff =
                    new StringBuffer(Messages.getString("General.ConnectionError.Server.message", new String[] { Utils
                            .getServerNameFromUrl(connectorConfig.getServiceEndpoint()) }));
        } else {
            strBuff = new StringBuffer(Messages.getString("General.ConnectionError.message"));
        }
        strBuff.append(":");

        if (th.getCause() instanceof UnknownHostException && connectorConfig != null) {
            return getUnknownHostExceptionMessage(strBuff, connectorConfig, th);
        }
        if(th.getCause() != null) {
		strBuff.append("\n\n").append(th.getCause().getClass().getSimpleName()).append(": ").append(
		    getStrippedRootCauseMessage(th.getCause()));
        }
		return strBuff.toString();
    }

    private static String getUnknownHostExceptionMessage(StringBuffer strBuff, ConnectorConfig connectorConfig,
            Throwable th) {
        if (strBuff == null) {
            strBuff = new StringBuffer();
        }

        strBuff.append("\n\nUnknown host: ").append(getStrippedRootCauseMessage(th.getCause()));

        if (connectorConfig != null && connectorConfig.getProxy() != null) {
            strBuff.append("\n\n").append(Messages.getString("Proxy.CheckSettings.message"));
        }

        return strBuff.toString();
    }

    public static void throwNewInsufficientPermissionsException(Connection connection, Throwable th)
            throws InsufficientPermissionsException {
        if (th instanceof ApiFault && isOrgBasedInsufficientPermissionsException((ApiFault) th)) {
            throw new InsufficientPermissionsException(((ApiFault) th).getExceptionMessage(), connection);
        }

        throw new InsufficientPermissionsException(connection);
    }

    public static void throwNewConnectionException(String message) throws ForceConnectionException {
        throw new ForceConnectionException(message);
    }

    public static Throwable getRootCause(Throwable th) {
        Throwable rootCause = ExceptionUtils.getRootCause(th);
        return (rootCause != null ? rootCause : th);
    }

    public static String getRootCauseMessage(Throwable th) {
        return getRootCauseMessage(th, true);
    }

    public static String getRootCauseMessage(Throwable th, boolean exceptionClassPrefix) {
        if (th instanceof ForceConnectionException) {
            return ((ForceConnectionException) th).getExceptionMessage();
        }

        Throwable rootCauseTh = ForceExceptionUtils.getRootCause(th);
        Throwable finalThrow = (rootCauseTh != null ? rootCauseTh : th);
        String message = NO_EXCEPTION_MESSAGE_MESSAGE;
        if (Utils.isNotEmpty(finalThrow.getMessage())) {
            message =
                    (exceptionClassPrefix ? finalThrow.getClass().getSimpleName() + ": " : "")
                            + finalThrow.getMessage();
        } else if (Utils.isNotEmpty(getExceptionMessage(finalThrow))) {
            message = getExceptionMessage(finalThrow);
        }
        return message;
    }

    public static String getStrippedRootCauseMessage(Throwable th) {
        String rootCauseMessage = getRootCauseMessage(th);
        return getStrippedExceptionMessage(rootCauseMessage);
    }

    public static String getExceptionCode(Throwable th) {
        return th instanceof ApiFault ? ((ApiFault) th).getExceptionCode().name() : "";
    }

    public static String getExceptionMessage(Throwable th) {
        if (th instanceof ApiFault) {
            return ((ApiFault) th).getExceptionMessage();
        } else if (th instanceof ForceException) {
            return ((ForceException) th).getExceptionMessage();
        } else {
            return th.getMessage();
        }
    }

    public static String getRootExceptionMessage(Throwable th) {
        Throwable root = getRootCause(th);
        return root instanceof ApiFault ? ((ApiFault) root).getExceptionMessage() : getStrippedRootCauseMessage(root);
    }

    public static void throwNewCoreException(Throwable th) throws CoreException {
        IStatus status =
                new MultiStatus(ForceIdeCorePlugin.getPluginId(), Constants.ERROR_CODE__44, getRootCauseMessage(th),
                        ForceExceptionUtils.getRootCause(th));
        Utils.addTraceToStatus((MultiStatus) status, th.getStackTrace(), IStatus.ERROR);
        throw new CoreException(status);
    }

    public static void throwNewForceProjectException(String string) throws ForceProjectException {
        throw new ForceProjectException(string);
    }

    public static void throwNewForceProjectException(Throwable th, String string) throws ForceProjectException {
        Throwable rootCause = ForceExceptionUtils.getRootCause(th);
        throw new ForceProjectException(rootCause, string);
    }

    public static String getStrippedExceptionMessage(String exceptionMessage) {
        if (Utils.isEmpty(exceptionMessage)) {
            return NO_EXCEPTION_MESSAGE_MESSAGE;
        }

        for (String prefix : STRIPPED_PREFIX_STRINGS) {
            if (exceptionMessage.contains(prefix)) {
                exceptionMessage = exceptionMessage.substring(exceptionMessage.indexOf(prefix) + prefix.length());
                break;
            }
        }

        for (String suffix : STRIPPED_SUFFIX_STRINGS) {
            if (exceptionMessage.endsWith(suffix)) {
                exceptionMessage = exceptionMessage.substring(0, exceptionMessage.indexOf(suffix));
                break;
            }
        }

        return exceptionMessage != null ? exceptionMessage.trim() : exceptionMessage;
    }

    public static boolean isRetryableException(Exception ex) {
        Throwable th = ForceExceptionUtils.getRootCause(ex);
        String message = ForceExceptionUtils.getRootCauseMessage(ex);
        if (th instanceof java.net.SocketException) {
            if (Utils.isNotEmpty(message) && message.contains("Connection reset")) {
                return true;
            }
        } else if (th instanceof ApiFault) {
            String code = getExceptionCode(th);
            return evaluateExceptionCode(code);
        } else if (th instanceof java.rmi.RemoteException) {
            return evaluateExceptionCodeMessage(message);
        } else if (ex instanceof ForceConnectionException) {
            String code = ((ForceConnectionException) ex).getExceptionCode();
            return evaluateExceptionCode(code);
        }
        return false;
    }

    private static boolean evaluateExceptionCode(String code) {
        if (Utils.isEmpty(code)) {
            return false;
        }

        for (String retryableCode : RETRYABLE_CODES) {
            if (code.equals(retryableCode)) {
                return true;
            }
        }

        return false;
    }

    private static boolean evaluateExceptionCodeMessage(String message) {
        if (Utils.isEmpty(message)) {
            return false;
        }

        for (String retryableCode : RETRYABLE_CODES) {
            if (Utils.isNotEmpty(message) && message.contains(retryableCode)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isReadOnlyException(CoreException e) {
        String message = getRootCauseMessage(e);

        //TODO this will only work for languages where error message appears in english -- need a better way
        if (Utils.isNotEmpty(message) && message.matches("ResourceException:.*read-only.*")) { //$NON-NLS-1$
            if (logger.isDebugEnabled()) {
                logger.debug("Encountered read-only exception");
            }
            return true;
        }

        return false;
    }

    public static boolean isPackageNotFoundException(RetrieveException ex) {
        String message = getRootCauseMessage(ex);
        if (Utils.isNotEmpty(message) && message.contains(PACKAGE_NOT_FOUND)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encountered 'No package found' exception");
            }
            return true;
        }

        return false;
    }

    public static boolean isInsufficientPermissionsException(Throwable th) {
        Throwable cause = ForceExceptionUtils.getRootCause(th);
        if (th instanceof InsufficientPermissionsException || cause instanceof InsufficientPermissionsException) {
            return true;
        } else if (cause instanceof ApiFault) {
            ApiFault apiFaultCause = (ApiFault) cause;
            if (Utils.isEqual(StatusCode.INVALID_ACCESS_LEVEL.toString(), apiFaultCause.getExceptionCode().toString())
                    || isOrgBasedInsufficientPermissionsException(apiFaultCause)) {
                return true;
            } else if (Utils.isEqual(StatusCode.INVALID_TYPE.toString(), apiFaultCause.getExceptionCode().toString())
                    && Utils.isNotEmpty(apiFaultCause.getExceptionMessage())
                    && (apiFaultCause.getExceptionMessage().contains("'" + SoqlEnum.INSTALL_PACKAGE_OBJECT + "'")
                            || apiFaultCause.getExceptionMessage().contains("'" + SoqlEnum.DEVELOPMENT_PACKAGE_OBJECT+ "'"))) {
                return true;
            }
        }
        else if (Utils.isNotEmpty(cause.getMessage())
                && cause.getMessage().startsWith(FaultCode.INSUFFICIENT_ACCESS.toString())) {
            return true;
        }

        return false;
    }

    // org-based determination... vs. user-based
    private static boolean isOrgBasedInsufficientPermissionsException(ApiFault apiFaultCause) {
        return Utils.isEqual(FaultCode.API_DISABLED_FOR_ORG.toString(), apiFaultCause.getExceptionCode().toString())
                || Utils.isEqual(FaultCode.API_CURRENTLY_DISABLED.toString(), apiFaultCause.getExceptionCode()
                        .toString());
    }

    public static boolean isApiUnknownException(ConnectionException e) {
        Throwable cause = ForceExceptionUtils.getRootCause(e);
        if (cause instanceof ApiFault) {
            ApiFault apiFaultCause = (ApiFault) cause;
            if (Utils.isEqual(StatusCode.UNKNOWN_EXCEPTION.toString(), apiFaultCause.getExceptionCode().toString())) {
                return true;
            }
        }

        return false;
    }

    public static void throwNewApiUnknownException(Connection connection, ConnectionException e)
            throws ForceRemoteException {
        Throwable cause = ForceExceptionUtils.getRootCause(e);
        if (cause instanceof ApiFault) {
            ApiFault apiFaultCause = (ApiFault) cause;
            String message = getStrippedExceptionMessage(apiFaultCause.getExceptionMessage());
            message = Messages.getString("General.ApiUnknownException.message", new String[] { message });
            throw new ForceRemoteException(message, connection);
        }
		throw new ForceRemoteException(e, connection);
    }

    public static boolean isReadTimeoutException(ConnectionException e) {
        return ForceExceptionUtils.getRootCause(e) instanceof SocketTimeoutException ? true : false;
    }

	public static void throwTranslatedException(ConnectionException e, Connection connection) throws ForceRemoteException {
        if (isInsufficientPermissionsException(e)) {
            throwNewInsufficientPermissionsException(connection, e);
        } else if (isApiUnknownException(e)) {
            throwNewApiUnknownException(connection, e);
        } else {
            throw new ForceRemoteException(e, connection);
        }
    }
}
