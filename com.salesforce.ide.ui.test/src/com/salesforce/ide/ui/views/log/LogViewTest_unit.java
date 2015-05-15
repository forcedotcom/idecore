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

import static org.mockito.Mockito.mock;

import java.io.File;

import junit.framework.TestCase;

/**
 * Class to unit test Logview 
 * @author bvenkatesan
 *
 */
public class LogViewTest_unit extends TestCase {

	public  void testCheckSpaceInLogEntryBeforeParentheses() throws Exception {
		String mockMessage = "Unable to get component for id";
		File mockFile = mock(File.class);
		LogEntry mockEntry = mock(LogEntry.class);
		
		String TestMessage =  new LogView(mockFile).new LogViewLabelProvider().getExceptionMessage(mockMessage, mockEntry);
		assertTrue(TestMessage.contains(" (Open"));
	}
}
