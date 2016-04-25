/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.core.remote.tooling.RunTests;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;

import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.HTTPConnection;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.sforce.soap.tooling.sobject.ApexTestSuite;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;

/**
 * Fetch test suites. 
 * 
 * @author jwidjaja
 *
 */
public class SuiteManager {
	
	private static final Logger logger = Logger.getLogger(SuiteManager.class);

	private final String TOOLING_ENDPOINT = "/services/data/";
	private final String QUERY_TEST_SUITES = "SELECT Id, TestSuiteName FROM ApexTestSuite ORDER BY TestSuiteName ASC";
	
	@SuppressWarnings("unused")
	private final IProject project;
	private final ForceProject forceProject;
	private HTTPConnection toolingRESTConnection;
	private ToolingStubExt toolingStubExt;
	
	private List<MySuite> suites;
	
	/**
	 * A holder class to also keep track of
	 * suite selection.
	 *
	 */
	public class MySuite {
		private final ApexTestSuite suite;
		private boolean selected;
		
		public MySuite(ApexTestSuite suite) {
			this.suite = suite;
			this.selected = false;
		}
		
		public String getSuiteId() {
			return suite.getId();
		}
		
		public String getSuiteName() {
			return suite.getTestSuiteName();
		}
		
		public void setSelected(boolean select) {
			this.selected = select;
		}
		
		public boolean isSelected() {
			return this.selected;
		}
	}
	
	public SuiteManager(IProject project) {
		this.project = project;
		this.forceProject = materializeForceProject(project);
		this.suites = Lists.newArrayList();
		initializeConnection(forceProject);
	}
	
	/**
	 * Query for ApexTestSuite in the organization
	 * @return A list of test suites
	 */
	public List<MySuite> fetchSuites() {
		try {
			suites.clear();
			QueryResult qr = toolingStubExt.query(QUERY_TEST_SUITES);
			if (Utils.isNotEmpty(qr) && qr.getSize() > 0) {
				for (SObject sObj : qr.getRecords()) {
					suites.add(new MySuite((ApexTestSuite) sObj));
				}
			} else {
				logger.info("No test suites in org");
			}
		} catch (Exception e) {
			logger.error("Failed to fetch test suites", e);
		}
		
		return suites;
	}
	
	/**
	 * @return A list of previously fetched test suites
	 */
	public List<MySuite> getFetchedSuites() {
		return suites;
	}
	
	/**
	 * @return A list of test suite IDs marked as selected
	 */
	public List<String> getSelectedSuiteIds() {
		List<String> selectedSuiteIds = Lists.newArrayList();
		
		for (MySuite suite : suites) {
			if (suite.isSelected()) {
				selectedSuiteIds.add(suite.getSuiteId());
			}
		}
		
		return selectedSuiteIds;
	}
	
	private ForceProject materializeForceProject(IProject project) {
		if (Utils.isEmpty(project) || !project.exists())
            return null;

        ForceProject forceProject =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(project);
        return forceProject;
	}
	
	private void initializeConnection(ForceProject forceProject) {
		toolingRESTConnection = new HTTPConnection(forceProject, TOOLING_ENDPOINT);
        try {
			toolingRESTConnection.initialize();
			toolingStubExt = ContainerDelegate.getInstance().getFactoryLocator().getToolingFactory().getToolingStubExt(forceProject);
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
	}
}
