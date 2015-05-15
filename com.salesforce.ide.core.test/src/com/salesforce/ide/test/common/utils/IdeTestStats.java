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

/**
 * Will contain test statistics.This is a singleton  
 * - orgs created.
 * - projects created.
 * - test count.
 * - test failure count.
 * - test errors.
 * @author ssasalatti
 *
 */
public class IdeTestStats {
	
	private int orgsCreatedCount=0;
	private int projectsCreatedCount=0;
	private int testsCount=0;
	private int failedTestsCount=0;
	
	//data structure for errors.
	//private int testErrors
	
	/**
	 * private constructor.
	 */
	private IdeTestStats(){
		
	}
	private static IdeTestStats instance = null;
	
	public static IdeTestStats getInstance(){
		if(null==instance)
			instance = new IdeTestStats();
		return instance;
	}

	public int getOrgsCreatedCount() {
		return orgsCreatedCount;
	}

	public int getProjectsCreatedCount() {
		return projectsCreatedCount;
	}

	public int getTestsCount() {
		return testsCount;
	}

	public int getFailedTestsCount() {
		return failedTestsCount;
	}
	
	public void incrementOrgsCreatedCount(){
		++orgsCreatedCount;
	}
	public void incrementProjectsCreatedCount(){
		++projectsCreatedCount;
	}
	public void incrementTestsCount(){
		++testsCount;
	}
	public void incrementFailedTestsCount(){
		++failedTestsCount;
	}
	
	@Override
	public String toString(){
		StringBuffer buff = new StringBuffer("\n-------TestStats:---------");
		buff.append("\nOrg Created in Run: "+ orgsCreatedCount);
		buff.append("\nProjects Created in Run: "+ projectsCreatedCount);
		buff.append("\nNumber of Tests Run: "+ testsCount);
		buff.append("\nNumber of Failed Tests: "+ failedTestsCount);
		buff.append("\n------------------------");
		
		return buff.toString();
	}

}
