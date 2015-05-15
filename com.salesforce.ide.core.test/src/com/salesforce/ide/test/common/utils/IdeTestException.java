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
package com.salesforce.ide.test.common.utils;

import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Ide test exception. Exceptions in the test framework should be wrapped with this exception. I have removed two
 * constructors to enforce the user to enter a message while creating the exception. Let's keep it that way.
 * 
 * @author ssasalatti
 * 
 */
public class IdeTestException extends Exception {

    /**
     * wraps the exception with an IdeTestException and returns it. Use this to get a wrapped exception. Note: Caller
     * still needs to throw it.
     * 
     * @param message
     * @param cause
     * @return
     */
    public static IdeTestException getWrappedException(String message, Throwable cause) {
        return new IdeTestException(getExceptionLocation(message), cause);
    }

    /**
     * returns an IdeTestException with the given message. Use this to get a wrapped exception. Note: Caller still needs
     * to throw it.
     * 
     * @param message
     * @return
     */
    public static IdeTestException getWrappedException(String message) {
        return new IdeTestException(getExceptionLocation(message));

    }

    /**
     * get the exception location
     * @param message
     * @return
     */
    private static String getExceptionLocation(String message) {
        StackTraceElement ste = getStackTraceElementRoot();
        message += "\n\nLocation:  " + ste.getClassName() + "." + ste.getMethodName() + "():" + ste.getLineNumber();
        return message;
    }

    /**
     * tries to get the exception root if it's already wrapped in an ideTestException.
     * @return
     */
    private static StackTraceElement getStackTraceElementRoot() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (Utils.isNotEmpty(stackTraceElements)) {
            for (int i = 0; i < stackTraceElements.length; i++) {
                if (stackTraceElements[i].getClassName().contains("IdeTestException")
                        && (i + 1) < stackTraceElements.length && stackTraceElements[i + 1] != null
                        && !stackTraceElements[i + 1].getClassName().contains("IdeTestException")) {
                    return stackTraceElements[i + 1];
                }

            }
        }

        return Thread.currentThread().getStackTrace()[3];
    }

    /**
     * creates an IdeTestException with the message and throws it.
     * 
     * @param message
     * @throws IdeTestException
     */
    public static void wrapAndThrowException(String message) throws IdeTestException {
        throw getWrappedException(message);
    }

    /**
     * wraps a given exception with a message and throws an IdeTestException.
     * 
     * @param message
     * @throws IdeTestException
     */
    public static void wrapAndThrowException(String message, Throwable cause) throws IdeTestException {
        throw getWrappedException(message, cause);
    }

    private IdeTestException(String message, Throwable cause) {
        super(message, cause);

    }

    private IdeTestException(String displayMessage) {
        super(displayMessage);
    }

    private static final long serialVersionUID = 1L;

}
