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
package com.salesforce.ide.deployment;

import java.util.List;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;

public class PackageManifest {

	private final Package metadata;

	public PackageManifest(Package metadata) {
		this.metadata = metadata;
	}

	public boolean contains(String typeName, String componentName) {
		List<PackageTypeMembers> types = metadata.getTypes();
		for (PackageTypeMembers typeMetadata : types) {
			String thisTypeName = typeMetadata.getName();
			if (typeName.equals(thisTypeName)) {
				List<String> members = typeMetadata.getMembers();
				return members.contains(componentName) || members.contains("*");
			}
		}
		return false;
	}
	


}
