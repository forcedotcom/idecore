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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.RunTestsResult;

public class DeployMessageExtractorTest_unit extends TestCase {

    public void testDeploySuccessIsFound() {
        DeployResultExt resultHandler = mock(DeployResultExt.class);
        DeployMessageExt messageHandler = mock(DeployMessageExt.class);

        DeployMessage message = new DeployMessage();
        message.setSuccess(true);

        when(resultHandler.getMessageCount()).thenReturn(1);
        when(resultHandler.getMessageHandler()).thenReturn(messageHandler);
        when(messageHandler.getMessages()).thenReturn(new DeployMessage[] { message });

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);

        assertEquals(1, extractor.getDeploySuccesses().size());
    }

    public void testDeployWarningIsNotSuccess() {
        String messageText = "warning message";

        DeployResultExt resultHandler = mock(DeployResultExt.class);
        DeployMessageExt messageHandler = mock(DeployMessageExt.class);

        DeployMessage message = new DeployMessage();
        message.setProblem(messageText);
        message.setSuccess(true);

        when(resultHandler.getMessageCount()).thenReturn(1);
        when(resultHandler.getMessageHandler()).thenReturn(messageHandler);
        when(messageHandler.getMessages()).thenReturn(new DeployMessage[] { message });

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);

        assertEquals(0, extractor.getDeploySuccesses().size());
    }

    public void testDeployFailureIsFound() {
        DeployResultExt resultHandler = mock(DeployResultExt.class);
        DeployMessageExt messageHandler = mock(DeployMessageExt.class);
        DeployMessage message = mock(DeployMessage.class);

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);
        assertEquals(0, extractor.getDeployFailures().size());

        when(resultHandler.getMessageCount()).thenReturn(1);
        when(resultHandler.getMessageHandler()).thenReturn(messageHandler);
        when(messageHandler.getMessages()).thenReturn(new DeployMessage[] { message });
        when(message.isSuccess()).thenReturn(false);

        assertEquals(1, extractor.getDeployFailures().size());
    }

    public void testDeploySuccessIsNotFailure() {
        DeployResultExt resultHandler = mock(DeployResultExt.class);
        DeployMessageExt messageHandler = mock(DeployMessageExt.class);
        DeployMessage message = mock(DeployMessage.class);

        when(resultHandler.getMessageCount()).thenReturn(1);
        when(resultHandler.getMessageHandler()).thenReturn(messageHandler);
        when(messageHandler.getMessages()).thenReturn(new DeployMessage[] { message });
        when(message.isSuccess()).thenReturn(true);

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);

        assertEquals(0, extractor.getDeployFailures().size());
    }

    public void testDeployWarningIsFound() {
        String messageText = "warning message";

        DeployResultExt resultHandler = mock(DeployResultExt.class);
        DeployMessageExt messageHandler = mock(DeployMessageExt.class);

        DeployMessage message = new DeployMessage();
        message.setProblem(messageText);
        message.setSuccess(true);

        when(resultHandler.getMessageCount()).thenReturn(1);
        when(resultHandler.getMessageHandler()).thenReturn(messageHandler);
        when(messageHandler.getMessages()).thenReturn(new DeployMessage[] { message });

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);

        assertEquals(1, extractor.getDeployWarnings().size());
        assertEquals(messageText, extractor.getDeployWarnings().iterator().next().getProblem());
    }

    public void testDeployFailureIsNotWarning() {
        String messageText = "failure message";

        DeployResultExt resultHandler = mock(DeployResultExt.class);
        DeployMessageExt messageHandler = mock(DeployMessageExt.class);

        DeployMessage message = new DeployMessage();
        message.setProblem(messageText);

        when(resultHandler.getMessageCount()).thenReturn(1);
        when(resultHandler.getMessageHandler()).thenReturn(messageHandler);
        when(messageHandler.getMessages()).thenReturn(new DeployMessage[] { message });

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);

        assertEquals(0, extractor.getDeployWarnings().size());
    }

    public void testDeploySuccessIsNotWarning() {
        DeployResultExt resultHandler = mock(DeployResultExt.class);
        DeployMessageExt messageHandler = mock(DeployMessageExt.class);

        DeployMessage message = new DeployMessage();
        message.setSuccess(true);

        when(resultHandler.getMessageCount()).thenReturn(1);
        when(resultHandler.getMessageHandler()).thenReturn(messageHandler);
        when(messageHandler.getMessages()).thenReturn(new DeployMessage[] { message });

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);

        assertEquals(0, extractor.getDeployWarnings().size());
    }

    public void testTestFailures() {
        DeployResultExt resultHandler = mock(DeployResultExt.class);
        RunTestsResult testResult = mock(RunTestsResult.class);
        RunTestFailure testFailure = mock(RunTestFailure.class);

        DeployMessage message = new DeployMessage();
        message.setSuccess(true);

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);
        assertEquals(0, extractor.getTestFailures().size());

        when(resultHandler.getRunTestsResult()).thenReturn(testResult);
        when(testResult.getFailures()).thenReturn(new RunTestFailure[] { testFailure });

        assertEquals(1, extractor.getTestFailures().size());
    }

    public void testTestWarnings() {
        DeployResultExt resultHandler = mock(DeployResultExt.class);
        RunTestsResult testResult = mock(RunTestsResult.class);
        CodeCoverageWarning testWarning = mock(CodeCoverageWarning.class);

        DeployMessage message = new DeployMessage();
        message.setSuccess(true);

        DeployMessageExtractor extractor = new DeployMessageExtractor(resultHandler);
        assertEquals(0, extractor.getTestWarnings().size());

        when(resultHandler.getRunTestsResult()).thenReturn(testResult);
        when(testResult.getCodeCoverageWarnings()).thenReturn(new CodeCoverageWarning[] { testWarning });

        assertEquals(1, extractor.getTestWarnings().size());
    }
}
