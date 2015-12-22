/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.ui.views.runtest;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexCodeLocation;

public class CodeCovResult {
	private final String classOrTriggerName;
	private final ApexCodeLocation loc;
	private final Integer percent;
	private final Integer linesCovered;
	private final Integer linesTotal;
	private final Integer linesNotCovered;
	
	public CodeCovResult(String classOrTriggerName, ApexCodeLocation loc, Integer percent, Integer linesCovered, Integer linesTotal) {
		this.classOrTriggerName = classOrTriggerName;
		this.loc = loc;
		this.percent = percent;
		this.linesCovered = linesCovered;
		this.linesTotal = linesTotal;
		this.linesNotCovered = (Utils.isNotEmpty(linesTotal) && Utils.isNotEmpty(linesCovered)) ? linesTotal - linesCovered : 0;
	}
	
	public String getClassOrTriggerName() {
		return this.classOrTriggerName;
	}
	
	public ApexCodeLocation getLoc() {
		return this.loc;
	}
	
	public Integer getPercent() {
		return this.percent;
	}
	
	public Integer getLinesCovered() {
		return this.linesCovered;
	}
	
	public Integer getLinesTotal() {
		return this.linesTotal;
	}
	
	public Integer getLinesNotCovered() {
		return this.linesNotCovered;
	}
}