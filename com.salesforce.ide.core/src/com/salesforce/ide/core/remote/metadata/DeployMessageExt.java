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
import com.sforce.soap.metadata.DeployMessage;

public class DeployMessageExt implements IMessageExt {

    private static final Logger logger = Logger.getLogger(DeployMessageExt.class);

    public static final int SORT_FILENAME = 0;
    public static final int SORT_RESULT = 1;

    protected int sortOrder = SORT_FILENAME;

    public int getSortOrder() {
        return sortOrder;
    }

    private final Comparator<DeployMessage> messageComparator = new Comparator<DeployMessage>() {
        @Override
        public int compare(DeployMessage o1, DeployMessage o2) {
            switch (sortOrder) {
            case SORT_RESULT:
                return compareResult(o1, o2);
            default:
                return compareFileName(o1, o2);
            }
        }

        private int compareFileName(DeployMessage o1, DeployMessage o2) {
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

        private int compareResult(DeployMessage o1, DeployMessage o2) {
            if (o1 == o2) {
                return 0;
            } else if (o1.isSuccess() && !o2.isSuccess()) {
                return 1;
            } else if (!o1.isSuccess() && o2.isSuccess()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private DeployMessage[] messages = null;

    //   C O N S T R U C T O R
    public DeployMessageExt(DeployMessage[] messages) {
        this.messages = messages;
        sort(SORT_FILENAME);
    }

    //   M E T H O D S
    public DeployMessage[] getMessages() {
        return messages;
    }

    public void setMessages(DeployMessage[] messages) {
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
                StringBuffer strBuff = new StringBuffer(" (");
                strBuff.append(i + 1).append(") ");
                if (!messages[i].isSuccess()) {
                    strBuff.append(" **");
                }
                strBuff.append(messages[i].getFileName()).append(": ").append(
                    messages[i].isSuccess() ? "successful" : Utils.isNotEmpty(messages[i].getProblem()) ? messages[i]
                            .getProblem() : "n/a");
                messageStrings[i] = strBuff.toString();
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

    public void sort(int sortOrder) {
        this.sortOrder = sortOrder;
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
            strBuff.append("No deploy messages found");
        } else {
            sort(DeployMessageExt.SORT_RESULT);
            strBuff.append("Got the following deploy messages [" + messages.length + "]:");
            int msgCnt = 0;
            for (DeployMessage deployMessage : messages) {
                String prefix = deployMessage.isSuccess() ? "" : "(F)";
                strBuff.append("\n (").append(++msgCnt).append(") ").append(prefix).append(" filename = ").append(
                    deployMessage.getFileName()).append(", result = ").append(
                    deployMessage.isSuccess() ? "SUCCESS" : "FAILED").append(", affect = ").append(
                    deployMessage.isChanged() ? "changed" : deployMessage.isCreated() ? "created" : deployMessage
                            .isDeleted() ? "deleted" : "none");
                if (!deployMessage.isSuccess()) {
                    strBuff.append(", problem = ").append(deployMessage.getProblem()).append(", line# = ").append(
                        deployMessage.getLineNumber()).append(", column# = ").append(deployMessage.getColumnNumber());
                }
                strBuff.append(", id = ").append(deployMessage.getId()).append(", fullname = ").append(
                    deployMessage.getFullName());
            }
        }

        if (write && logger.isInfoEnabled()) {
            logger.info(strBuff.toString());
        }
    }
}
