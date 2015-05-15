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
package com.salesforce.ide.core.remote.registries;

import java.util.Collections;
import java.util.Hashtable;
import java.util.SortedSet;

import com.salesforce.ide.core.remote.Connection;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;

import junit.framework.TestCase;
import static org.mockito.Mockito.*;

public class DescribeObjectRegistryTest_unit extends TestCase {
	public void testGetCachedWorkflowableDescribeTypes_excludesKnowledgeTypes()
			throws Exception {
		final String someProjectName = "foo";
		DescribeObjectRegistry registry = spy(new DescribeObjectRegistry());
		doReturn(Collections.emptyList()).when(registry)
				.getWorkflowableObjectNames();
		final Hashtable<String, DescribeSObjectResult> hashtable = new Hashtable<String, DescribeSObjectResult>();
		DescribeSObjectResult value = mock(DescribeSObjectResult.class);
		when(value.getName()).thenReturn("obj__kav");
		when(value.isCustom()).thenReturn(true);
		hashtable.put(someProjectName, value);
		doReturn(hashtable).when(registry).getDescribeCacheForProject(
				eq(someProjectName));
		Connection connection = mock(Connection.class);
		final SortedSet<String> result = registry
				.getCachedWorkflowableDescribeTypes(connection,
						someProjectName, false);
		assertNotNull(result);
		verify(registry, times(0)).loadDescribeCaches(eq(connection),
				eq(someProjectName));
		assertEquals(0, result.size());
		assertFalse(result.contains("obj__kav"));
	}
}
