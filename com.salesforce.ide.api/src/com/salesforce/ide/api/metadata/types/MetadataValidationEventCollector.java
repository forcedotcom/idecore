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
package com.salesforce.ide.api.metadata.types;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;

import org.apache.log4j.Logger;

public class MetadataValidationEventCollector extends ValidationEventCollector {

    private static final Logger logger = Logger.getLogger(MetadataValidationEventCollector.class);

    private boolean failOnValidateError = false;

    public MetadataValidationEventCollector() {
        super();
    }

    public MetadataValidationEventCollector(boolean failOnValidateError) {
        super();
        this.failOnValidateError = failOnValidateError;
    }

    public boolean hasValidationIssues() {
        return hasFatals() || hasErrors() || hasWarnings();
    }

    public boolean hasFatals() {
        return hasSeverity(ValidationEvent.FATAL_ERROR);
    }

    public boolean hasErrors() {
        return hasSeverity(ValidationEvent.ERROR);
    }

    public boolean hasWarnings() {
        return hasSeverity(ValidationEvent.WARNING);
    }

    public boolean hasSeverity(int severity) {
        if (!hasEvents()) {
            return false;
        }

        for (ValidationEvent validationEvent : getEvents()) {
            if (validationEvent.getSeverity() == severity) {
                return true;
            }
        }

        return false;
    }

    public List<String> getValidationMessages() {
        if (!hasEvents()) {
            return null;
        }

		List<String> messages = new ArrayList<>(getEvents().length);
		for (ValidationEvent validationEvent : getEvents()) {
		    messages.add(getSeverity(validationEvent.getSeverity()) + ": " + validationEvent.getMessage());
		}
		return messages;
    }

    public void logValidationMessages(String name) {
        List<String> validationMessages = getValidationMessages();
        if (validationMessages == null || validationMessages.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info(name + " has no validation issues");
            }
        } else {
            StringBuffer strBuff =
                    new StringBuffer("Found following validation issues [" + validationMessages.size() + "] for "
                            + name);
            int cnt = 0;
            for (String validationMessage : validationMessages) {
                strBuff.append("\n (").append(++cnt).append(") ").append(validationMessage);
            }
            logger.warn(strBuff.toString());
        }
    }

    private static String getSeverity(int severity) {
        switch (severity) {
        case ValidationEvent.FATAL_ERROR:
            return "FATAL";
        case ValidationEvent.ERROR:
            return "FATAL";
        case ValidationEvent.WARNING:
            return "WARNING";
        default:
            return "";
        }
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        // failOnValidateError determines whether super class determines recovery or this impl
        // when false, we always recover from parse errors/failures
        if (failOnValidateError) {
            return super.handleEvent(event);
        }
		super.handleEvent(event);
		return true;
    }
}
