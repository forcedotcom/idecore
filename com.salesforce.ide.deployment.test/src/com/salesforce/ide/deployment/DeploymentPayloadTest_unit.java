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

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSet;
import com.salesforce.ide.deployment.internal.DeploymentPayload;
import com.salesforce.ide.test.common.NoOrgSetupTest;

@SuppressWarnings("deprecation")
public class DeploymentPayloadTest_unit extends NoOrgSetupTest {

    private static final Logger logger = Logger.getLogger(DeploymentPayloadTest_unit.class);

    public void testDeploymentPayloadTest_basics() {
        logStart("testDeploymentPayloadTest_basics");
        try {
            ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
            assertNotNull("Project package should not be null", projectPackageList);

            DeploymentComponentSet deploymentComponentSet = new DeploymentComponentSet();
            ComponentList componentList = projectPackageList.getAllComponents();
            int numOfComponents = componentList.size();
            assertTrue("Component list should not be null or empty", Utils.isNotEmpty(componentList));
            for (Component component : componentList) {
                deploymentComponentSet.add(new DeploymentComponent(component));
            }
            assertTrue("Expected " + numOfComponents + " number of components",
                numOfComponents == deploymentComponentSet.getComponents().size());

            DeploymentComponent deploymentComponent = deploymentComponentSet.first();
            DeploymentPayload deploymentPayload = new DeploymentPayload();
            deploymentPayload.addAll(deploymentComponentSet);
            assertTrue("DeploymentComponent should be removed", deploymentPayload.remove(deploymentComponent));
            deploymentPayload.add(deploymentComponent);
            assertNotNull("DeploymentComponent for type should not be null", deploymentPayload
                    .getDeploymentComponentsByType(deploymentComponent.getType()));
            assertNotNull("DeploymentComponent for filename should not be null", deploymentPayload
                    .getDeploymentComponentByFilename(deploymentComponent.getFileName()));
            deploymentComponent.setDeploy(false);
            assertTrue("Expected " + (numOfComponents - 1) + " number of components",
                (numOfComponents - 1) == deploymentPayload.getDeploySelectedCount());
            assertTrue("Expected > 3 number of components", deploymentPayload.getDeploymentComponentsByType(
                Constants.APEX_CLASS).size() > 3);
            assertTrue("Expected > 3 number of components",
                deploymentPayload.getDeploySelectedComponents(true).size() > 3);
            logger.info(deploymentPayload.getDeploymentPlanSummary());

        } catch (Exception e) {
            handleFailure("Unable to test deployment payload basics", e);
        } finally {
            logEnd("testDeploymentPayloadTest_basics");
        }
    }
}
