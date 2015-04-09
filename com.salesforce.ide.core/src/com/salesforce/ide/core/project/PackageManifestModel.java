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
package com.salesforce.ide.core.project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;

public class PackageManifestModel {

    private static final Logger logger = Logger.getLogger(PackageManifestModel.class);

    protected IProject project;
    protected FileMetadataExt fileMetadatExt;
    protected Document manifestDocument;
    protected Document manifestCache;
    protected Package packageManifest;
    protected IFile packageManifestFile;
    protected Map<String, Boolean> nodeAllSelection;

    public PackageManifestModel() {
        setNewManifestDocumentInstance();
    }

    public void setNewManifestDocumentInstance() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            manifestDocument = builder.newDocument();
        } catch (Exception e) {
            logger.error("Unable to create new document instance", e);
        }
    }

    public PackageManifestModel(Document manifestDocument) {
        this.manifestDocument = manifestDocument;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public Package getPackageManifest() {
        return packageManifest;
    }

    public void setPackageManifest(Package packageManifest) {
        this.packageManifest = packageManifest;
    }

    public IFile getPackageManifestFile() {
        return packageManifestFile;
    }

    public void setPackageManifestFile(IFile packageManifestFile) {
        this.packageManifestFile = packageManifestFile;
    }

    public FileMetadataExt getFileMetadatExt() {
        return fileMetadatExt;
    }

    public void setFileMetadatExt(FileMetadataExt fileMetadatExt) {
        this.fileMetadatExt = fileMetadatExt;
    }

    public Document getManifestDocument() {
        return manifestDocument;
    }

    public void setManifestDocument(Document manifestDocument) {
        this.manifestDocument = manifestDocument;
    }

    public Document getManifestCache() {
        return manifestCache;
    }

    public void setManifestCache(Document manifestCache) {
        this.manifestCache = manifestCache;
    }

    public Map<String, Boolean> getNodeAllSelectionMap() {
        return nodeAllSelection;
    }

    public void setNodeAllSelectionMap(Map<String, Boolean> nodeAllSelection) {
        this.nodeAllSelection = nodeAllSelection;
    }

    public boolean isNodeAllSelected(String nodeName) {
        if (Utils.isEmpty(nodeAllSelection) || !nodeAllSelection.containsKey(nodeName)) {
            return false;
        }
		Boolean allSelected = nodeAllSelection.get(nodeName);
		return allSelected != null ? allSelected.booleanValue() : false;
    }

    public void addNodeAllSelection(String nodeName, boolean allSelected) {
        if (nodeAllSelection == null) {
            nodeAllSelection = new HashMap<>();
        }
        nodeAllSelection.put(nodeName, new Boolean(allSelected));
    }

    public void addPackageManifest(Package updatedPackageManifest) {
        if (updatedPackageManifest == null || Utils.isEmpty(updatedPackageManifest.getTypes())) {
            return;
        }

        // for each type stanza, check for wildcard and if found add cache names to type
        for (PackageTypeMembers updatedPackageComponentType : updatedPackageManifest.getTypes()) {
            if (Utils.isEmpty(updatedPackageComponentType.getMembers())) {
                continue;
            }

            PackageTypeMembers manifestComponentType =
                    getPackageTypeMembers(updatedPackageComponentType, packageManifest);
            if (manifestComponentType != null) {
                // augment project manifest content for type w/ cache content
                Set<String> tmpProjectManifestComponentMembers = new HashSet<>();
                tmpProjectManifestComponentMembers.addAll(manifestComponentType.getMembers());
                tmpProjectManifestComponentMembers.addAll(updatedPackageComponentType.getMembers());
                manifestComponentType.getMembers().clear();
                manifestComponentType.getMembers().addAll(tmpProjectManifestComponentMembers);
            } else {
                packageManifest.getTypes().add(updatedPackageComponentType);
            }
        }
    }

    private static PackageTypeMembers getPackageTypeMembers(PackageTypeMembers projectPackageComponentType,
            Package cachePackageManifest) {
        for (PackageTypeMembers cachePackageComponentType : cachePackageManifest.getTypes()) {
            if (projectPackageComponentType.getName().equals(cachePackageComponentType.getName())) {
                return cachePackageComponentType;
            }
        }
        return null;

    }
}
