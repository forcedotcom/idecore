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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.FileProperties;

public class FileMetadataExt {

	private static final Logger logger = Logger
			.getLogger(FileMetadataExt.class);

	protected static final Comparator<FileProperties> SORT_BY_TYPE = new Comparator<FileProperties>() {
		@Override
        public int compare(FileProperties o1, FileProperties o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getType(), o2
					.getType());
		}

	};

	private FileProperties[] fileProperties = null;
	private boolean subset = false;

	public FileMetadataExt() {
	}

	public FileMetadataExt(FileProperties... fileProperties) {
		this.fileProperties = fileProperties;
	}

	public void setFileProperties(FileProperties[] fileProperties) {
		this.fileProperties = fileProperties;
	}

	public FileProperties[] getFileProperties() {
		return fileProperties;
	}

	public boolean isSubset() {
		return subset;
	}

	public void setSubset(boolean subset) {
		this.subset = subset;
	}

	public void addFileProperties(FileProperties[] tmpFileProperties) {
		if (Utils.isNotEmpty(fileProperties)) {
			Set<FileProperties> newfileProperties = new HashSet<>();

			for (FileProperties fileProperty : fileProperties) {
				newfileProperties.add(fileProperty);
			}

			for (FileProperties tmpFileProperty : tmpFileProperties) {
				newfileProperties.add(tmpFileProperty);
			}

			this.fileProperties = newfileProperties
					.toArray(new FileProperties[newfileProperties.size()]);
		} else {
			this.fileProperties = tmpFileProperties;
		}
	}

	public int getFilePropertiesCount() {
		return Utils.isNotEmpty(fileProperties) ? fileProperties.length : 0;
	}

	public boolean hasFileProperties() {
		return Utils.isNotEmpty(fileProperties);
	}

	public FileProperties getFilePropertiesByFilePath(String filePath) {
		if (!hasFileProperties() || Utils.isEmpty(filePath)) {
			return null;
		}

		for (FileProperties tmpFileProperties : fileProperties) {
			if (tmpFileProperties.getFileName().equals(filePath)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found FileProperties for '" + filePath + "'");
				}
				return tmpFileProperties;
			}
		}

		return null;
	}

	public FileProperties[] getFilePropertiesByDirectories(String[] directories) {
		if (!hasFileProperties() || Utils.isEmpty(directories)) {
			return null;
		}

		List<FileProperties> filePropertiesList = new ArrayList<>();
		for (FileProperties tmpFileProperties : fileProperties) {
			for (String directory : directories) {
				if (tmpFileProperties.getFileName().startsWith(directory)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found FileProperties for directory '"
								+ directory + "'");
					}
					filePropertiesList.add(tmpFileProperties);
					break;
				}
			}
		}

		return filePropertiesList.toArray(new FileProperties[filePropertiesList
				.size()]);
	}

	public FileProperties[] getFilePropertiesByExtensions(String[] extensions) {
		if (!hasFileProperties() || Utils.isEmpty(extensions)) {
			return null;
		}

		List<FileProperties> filePropertiesList = new ArrayList<>();
		for (FileProperties tmpFileProperties : fileProperties) {
			for (String extension : extensions) {
				if (tmpFileProperties.getFileName().endsWith(extension)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found FileProperties for extension '"
								+ extension + "'");
					}
					filePropertiesList.add(tmpFileProperties);
					break;
				}
			}
		}

		return filePropertiesList.toArray(new FileProperties[filePropertiesList
				.size()]);
	}

	/**
	 * Get FileProperties for given component types.
	 *
	 * @param componentTypes
	 *            Include "StandardObject" type if StandardObject objects are
	 *            desired. "CustomObjects" types includes only CustomObjects
	 *            objects.
	 * @return
	 */
	public FileProperties[] getFilePropertiesByComponentTypes(
			String[] componentTypes) {
		if (!hasFileProperties() || Utils.isEmpty(componentTypes)) {
			return null;
		}

		List<FileProperties> filePropertiesList = new ArrayList<>();
		for (FileProperties tmpFileProperties : fileProperties) {
			for (String componentType : componentTypes) {
				if (tmpFileProperties.getType().equals(componentType)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found FileProperties for type '"
								+ componentType + "'");
					}
					filePropertiesList.add(tmpFileProperties);
					break;
				}
			}
		}

		return filePropertiesList.toArray(new FileProperties[filePropertiesList
				.size()]);
	}

	public FileProperties[] getFilePropertiesByComponentType(
			String componentType) {
		if (!hasFileProperties() || Utils.isEmpty(componentType)) {
			return null;
		}

		List<FileProperties> filePropertiesList = new ArrayList<>();
		for (FileProperties tmpFileProperties : fileProperties) {
			if (tmpFileProperties.getType().equals(componentType)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found FileProperties for type '"
							+ componentType + "'");
				}
				filePropertiesList.add(tmpFileProperties);
			}
		}

		return filePropertiesList.toArray(new FileProperties[filePropertiesList
				.size()]);
	}

	public List<String> getComponentNamesByComponentType(String componentType) {
		if (!hasFileProperties() || Utils.isEmpty(componentType)) {
			return null;
		}

		List<String> componentNames = new ArrayList<>();
		for (FileProperties fileProp : fileProperties) {
			if (fileProp.getType().equals(componentType)
					&& !(CustomObjectNameResolver.getCheckerForStandardObject()).check(fileProp.getFullName(), componentType)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found '" + fileProp.getFullName()
							+ "' for type '" + componentType + "'");
				}
				componentNames.add(fileProp.getFullName());
			}
		}

		return componentNames;
	}

	public FileProperties[] getFilePropertiesById(String[] ids) {
		if (!hasFileProperties() || Utils.isEmpty(ids)) {
			return null;
		}

		List<FileProperties> filePropertiesList = new ArrayList<>();
		for (FileProperties tmpFileProperties : fileProperties) {
			for (String id : ids) {
				if (tmpFileProperties.getId().equals(id)) {
					if (logger.isDebugEnabled()) {
						logger
								.debug("Found FileProperties for id '" + id
										+ "'");
					}
					filePropertiesList.add(tmpFileProperties);
					break;
				}
			}
		}

		return filePropertiesList.toArray(new FileProperties[filePropertiesList
				.size()]);
	}

	/**
	 * Get component types represent by FilePropeties.
	 *
	 * Will return StandardObject from non-CustomObject FileProperties.
	 *
	 * @return component types
	 */
	public Set<String> getComponentTypes() {
		if (!hasFileProperties()) {
			return null;
		}

		sort(SORT_BY_TYPE);
		Set<String> componentTypes = new HashSet<>();
		for (FileProperties tmpFileProperties : fileProperties) {
			String componentType = tmpFileProperties.getType();

			if (CustomObjectNameResolver.getCheckerForStandardObject().check(tmpFileProperties.getFullName(), componentType))
			{
				componentType = Constants.STANDARD_OBJECT;
			}

			boolean added = componentTypes.add(componentType);
			if (added && logger.isDebugEnabled()) {
				logger.debug("Add list for component type '" + componentType
						+ "'");
			}
		}

		return new TreeSet<>(componentTypes);
	}

	/**
	 * Get mapping of all component types and their associated FileProperties
	 *
	 * Will return StandardObject from non-CustomObject FileProperties.
	 *
	 * @return component type to FileProperties map
	 */
	public Map<String, List<FileProperties>> getFilePropertiesMap() {
		return getFilePropertiesMap(null);
	}

	/**
	 * Get mapping of component types and their associated FileProperties for
	 * given components.
	 *
	 * @param additionalComponentTypes
	 *            Include "StandardObject" type if StandardObject objects are
	 *            desired. "CustomObjects" types includes only CustomObjects
	 *            objects.
	 * @return component type to FileProperties map
	 */
	public Map<String, List<FileProperties>> getFilePropertiesMap(
			List<String> additionalComponentTypes) {
		Map<String, List<FileProperties>> filePropertiesMap = new HashMap<>();
		if (!hasFileProperties()) {
			return filePropertiesMap;
		}

        List<String> localComponentTypes = null;
        if (additionalComponentTypes != null) {
            localComponentTypes = new ArrayList<>(additionalComponentTypes);
        }

		sort(SORT_BY_TYPE);

		String componentType = null;
		for (FileProperties fp : fileProperties) {
			componentType = fp.getType();

			if (null != localComponentTypes) {
                localComponentTypes.remove(componentType);
			}

			if (CustomObjectNameResolver.getCheckerForStandardObject().check(fp.getFullName(), fp.getType())) {
				componentType = Constants.STANDARD_OBJECT;
			}

	        List<FileProperties> filePropertiesList = filePropertiesMap.get(componentType);
            if (null == filePropertiesList) {
				filePropertiesList = new ArrayList<>();
				filePropertiesMap.put(componentType, filePropertiesList);
			}

			filePropertiesList.add(fp);
		}

		if (null != localComponentTypes) {
			if (!filePropertiesMap.containsKey(Constants.CUSTOM_OBJECT)) {
				localComponentTypes.add(Constants.CUSTOM_OBJECT);
			}

			if (!filePropertiesMap.containsKey(Constants.STANDARD_OBJECT)) {
				localComponentTypes.add(Constants.STANDARD_OBJECT);
			}

			for (String tmpComponentType : localComponentTypes) {
				filePropertiesMap.put(tmpComponentType,
						new ArrayList<FileProperties>());
				if (logger.isDebugEnabled()) {
					logger.debug("Add empty list for component type '"
							+ tmpComponentType + "'");
				}
			}
		}
		logMap(filePropertiesMap);
		return filePropertiesMap;
	}

	private static void logMap(Map<String, List<FileProperties>> filePropertiesMap) {
		if (logger.isDebugEnabled()) {
			TreeSet<String> tmpComponentTypes = new TreeSet<>(
					String.CASE_INSENSITIVE_ORDER);
			tmpComponentTypes.addAll(filePropertiesMap.keySet());
			StringBuffer strBuff = new StringBuffer(
					"Got the following file properties:");
			int totalComponentCnt = 0;
			for (String tmpComponentType : tmpComponentTypes) {
				strBuff.append("\n").append(tmpComponentType);
				int componentCnt = 0;
				List<FileProperties> tmpFilePropertiesList = filePropertiesMap
						.get(tmpComponentType);
				if (Utils.isNotEmpty(tmpFilePropertiesList)) {
					for (FileProperties tmpFileProperties : tmpFilePropertiesList) {
						String fullName = Utils.isNotEmpty(tmpFileProperties
								.getFullName()) ? tmpFileProperties
								.getFullName() : "n/a";
						String id = Utils.isNotEmpty(tmpFileProperties.getId()) ? tmpFileProperties
								.getId()
								: "n/a";
						strBuff.append("\n (").append(++componentCnt).append(
								") ").append(fullName).append(",  ").append(
								tmpFileProperties.getFileName()).append(",  ")
								.append(id);
					}
				} else {
					strBuff.append("\n n/a");
				}
				totalComponentCnt += componentCnt;
			}
			strBuff.append("\nTotal components: " + totalComponentCnt);
			logger.info(strBuff.toString());
		}
	}

	public void sort() {
		sort(new Comparator<FileProperties>() {
			@Override
            public int compare(FileProperties o1, FileProperties o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getFileName(),
						o2.getFileName());
			}

		});
	}

	public void sort(Comparator<FileProperties> sortcomparator) {
		if (!hasFileProperties()) {
			return;
		}
		Arrays.sort(fileProperties, sortcomparator);
	}

	@Override
	public String toString() {
		StringBuffer strBuff = new StringBuffer(
				"Retrieved the following FileProperties [");
		strBuff.append(getFilePropertiesCount());
		strBuff.append("]:");

		sort();

		if (Utils.isNotEmpty(fileProperties)) {
			int cnt = 0;
			for (FileProperties tmpFileProperties : fileProperties) {
				String fullName = Utils.isNotEmpty(tmpFileProperties
						.getFullName()) ? tmpFileProperties.getFullName()
						: "n/a";
				String id = Utils.isNotEmpty(tmpFileProperties.getId()) ? tmpFileProperties
						.getId()
						: "n/a";
				strBuff.append("\n (").append(++cnt).append(") ").append(
						tmpFileProperties.getFileName()).append(",  ").append(
						fullName).append(",  ").append(
						tmpFileProperties.getType()).append(",  ").append(id);
			}
		}
		return strBuff.toString();
	}
}
