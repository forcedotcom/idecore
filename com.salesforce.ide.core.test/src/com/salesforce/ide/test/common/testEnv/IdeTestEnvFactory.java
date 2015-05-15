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
package com.salesforce.ide.test.common.testEnv;

import org.eclipse.core.runtime.Platform;

import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

public final class IdeTestEnvFactory {

	/**
	 * gets a specific Eclipse Checker based on the Eclipse Version
	 * Note: Caller should check for nullness.
	 * @return version specific checker. Can return null.
	 * @throws IdeTestException
	 */
	public static IIdeTestEclipseVerSpecificChecks getThisEclipseChecker() throws IdeTestException{
		IIdeTestEclipseVerSpecificChecks retObj = null;
		String eclipseVersionString = IdeTestUtil.getCurrentEclipseMajorVersion();
		Float eclipseVersionNumber = Float.valueOf(eclipseVersionString);
		if (eclipseVersionNumber >= 3.5) {
			retObj = Eclipse35SpecificChecks.getInstance();
		} else {
			throw IdeTestException.getWrappedException("Unsupported eclipse version: " + eclipseVersionString);
		}

		return retObj;
	}

	/**
	 * gets a specific OS checker
	 * @return
	 * @throws IdeTestException
	 */
	public static IIdeTestOSSpecificChecks getThisOSChecker() throws IdeTestException {
		IIdeTestOSSpecificChecks retObj = null;
		String currentOS = IdeTestUtil.getCurrentOS();
		if(IdeTestUtil.isEmpty(currentOS)) {
			IdeTestException.wrapAndThrowException("Current OS String can't be empty. Cannot generate concrete class for OS Checker.");
		} else if(currentOS.equalsIgnoreCase(Platform.OS_LINUX)) {
			retObj = LinuxSpecificChecks.getInstance();
		} else if(currentOS.equalsIgnoreCase(Platform.OS_WIN32)) {
			retObj = Win32SpecificChecks.getInstance();
		} else if(currentOS.equalsIgnoreCase(Platform.OS_MACOSX)) {
			retObj = MacSpecificChecks.getInstance();
		} else {
			throw IdeTestException.getWrappedException("Unsupported OS: " + currentOS);
		}
		return retObj;
	}

}
