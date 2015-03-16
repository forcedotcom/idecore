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
package com.salesforce.ide.core.remote.metadata;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.RetrieveMessage;

public class RetrieveMessageExt implements IMessageExt {

    private static final Logger logger = Logger.getLogger(RetrieveMessageExt.class);

    private static Comparator<RetrieveMessage> messageComparator = new Comparator<RetrieveMessage>() {
        @Override
        public int compare(RetrieveMessage o1, RetrieveMessage o2) {
            if (o1 == o2) {
                return 0;
            } else if (Utils.isEmpty(o1.getFileName()) && Utils.isEmpty(o2.getFileName())) {
                return 0;
            } else if (Utils.isNotEmpty(o1.getFileName()) && Utils.isEmpty(o2.getFileName())) {
                return -1;
            } else if (Utils.isEmpty(o1.getFileName()) && Utils.isNotEmpty(o2.getFileName())) {
                return 1;
            } else {
                return o1.getFileName().compareTo(o2.getFileName());
            }
        }
    };

    private RetrieveMessage[] messages = null;

    //  C O N S T R U C T O R
    public RetrieveMessageExt(RetrieveMessage[] messages) {
        this.messages = messages;
        sort();
    }

    //  M E T H O D S
    public RetrieveMessage[] getMessages() {
        return messages;
    }

    public void setMessages(RetrieveMessage[] messages) {
        this.messages = messages;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.handlers.IMessageHandler#getMessageStrings()
    */
    @Override
    public String[] getMessageStrings() {
        String[] messageStrings = null;
        if (Utils.isNotEmpty(messages)) {
            messageStrings = new String[messages.length];
            for (int i = 0; i < messages.length; i++) {
                messageStrings[i] = messages[i].getProblem();
            }
        }
        return messageStrings;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.handlers.IMessageHandler#getFileNames()
    */
    @Override
    public String[] getFileNames() {
        String[] fileNamesStrings = null;
        if (Utils.isNotEmpty(messages)) {
            fileNamesStrings = new String[messages.length];
            for (int i = 0; i < messages.length; i++) {
                fileNamesStrings[i] = messages[i].getFileName();
            }
        }
        return fileNamesStrings;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.handlers.IMessageHandler#getDisplayMessages()
    */
    @Override
    public String[] getDisplayMessages() {
        String[] displayMessages = null;
        if (Utils.isNotEmpty(messages)) {
            displayMessages = new String[messages.length];
            for (int i = 0; i < messages.length; i++) {
                displayMessages[i] = messages[i].getFileName() + ": " + messages[i].getProblem();
            }
        }
        return displayMessages;
    }

    @Override
    public int getMessageCount() {
        return messages != null ? messages.length : 0;
    }

    public void sort() {
        if (Utils.isNotEmpty(messages)) {
            Arrays.sort(messages, messageComparator);
        }
    }

    public void logMessage() {
        logMessage(new StringBuffer(), true);
    }

    public void logMessage(StringBuffer strBuff, boolean write) {
        if (strBuff == null) {
            strBuff = new StringBuffer();
        }

        if (Utils.isEmpty(messages)) {
            strBuff.append("No retrieve messages found");
        } else {
            strBuff.append("Got the following retrieve messages [" + messages.length + "]:");
            int msgCnt = 0;
            for (RetrieveMessage deployMessage : messages) {
                strBuff.append("\n (")
                    .append(++msgCnt)
                    .append(") ")
                    .append("filename = ")
                    .append(deployMessage.getFileName())
                    .append(", problem = ")
                    .append(deployMessage.getProblem());
            }
        }

        if (write && logger.isInfoEnabled()) {
            logger.info(strBuff.toString());
        }
    }
}
