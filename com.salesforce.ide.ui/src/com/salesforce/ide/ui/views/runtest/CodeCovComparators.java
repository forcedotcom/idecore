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

import java.util.Comparator;

public class CodeCovComparators {
	public static Comparator<CodeCovResult> CLASSNAME_ASC = createSorterWithDirection(Messages.View_CodeCoverageClass, -1);
	public static Comparator<CodeCovResult> PERCENT_ASC = createSorterWithDirection(Messages.View_CodeCoveragePercent, -1);
	public static Comparator<CodeCovResult> LINES_ASC = createSorterWithDirection(Messages.View_CodeCoverageLines, -1);
	public static Comparator<CodeCovResult> CLASSNAME_DESC = createSorterWithDirection(Messages.View_CodeCoverageClass, 1);
	public static Comparator<CodeCovResult> PERCENT_DESC = createSorterWithDirection(Messages.View_CodeCoveragePercent, 1);
	public static Comparator<CodeCovResult> LINES_DESC = createSorterWithDirection(Messages.View_CodeCoverageLines, 1);
	
	private static Comparator<CodeCovResult> createSorterWithDirection(final String type, final int direction) {
		return new Comparator<CodeCovResult>() {
			@Override
			public int compare(CodeCovResult o1, CodeCovResult o2) {
				if (o1.getClassOrTriggerName().equals(Messages.View_CodeCoverageOverall)) return -1;
				if (o2.getClassOrTriggerName().equals(Messages.View_CodeCoverageOverall)) return 1;
				
				int compareDir = -1;
				if (type.equals(Messages.View_CodeCoveragePercent)) {
					compareDir = o1.getPercent().compareTo(o2.getPercent());
				} else if (type.equals(Messages.View_CodeCoverageLines)) {
					compareDir = o1.getLinesTotal().compareTo(o2.getLinesTotal());
				} else {
					compareDir = o1.getClassOrTriggerName().compareTo(o2.getClassOrTriggerName());
				}
				
				if (direction > 0) {
					compareDir *= -1;
				}
				
				return compareDir;
			}
    	};
	}
}