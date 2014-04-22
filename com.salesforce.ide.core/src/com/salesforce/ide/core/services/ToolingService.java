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
package com.salesforce.ide.core.services;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;

/**
 * Encapsulates services that are common to the Tooling API.
 * 
 * @author nchen
 * 
 */
public class ToolingService extends BaseService {

    /**
     * Checks if the Tooling API supports this component as a valid container member. This will need to be revised as we
     * support more things, e.g., Aura components.
     * 
     * @param cmp
     *            Component to check
     * @return
     */
    public boolean checkIfCanCreateContainerMember(Component cmp, ComponentList cmps) {
        // These names come from plugin.properties in com.salesforce.ide.core
        return componentDelegate(cmp, cmps, new ComponentHandler<Boolean>() {

            @Override
            public Boolean handleApexClass(Component cmp, ComponentList cmps) {
                return true;
            }

            @Override
            public Boolean handleApexTrigger(Component cmp, ComponentList cmps) {
                return true;
            }

            @Override
            public Boolean handleApexPage(Component cmp, ComponentList cmps) {
                return true;
            }

            @Override
            public Boolean handleApexComponent(Component cmp, ComponentList cmps) {
                return true;
            }

            @Override
            public Boolean handleUnknownCase(Component cmp, ComponentList cmps) {
                return false;
            }
        });

    }

    public <T> T componentDelegate(Component cmp, ComponentList cmps, ComponentHandler<T> handler) {
        // TODO: String comparisons are brittle but there doesn't appear to be another (safely typed) way to get the underlying type
        // Isolate the string delegate to this portion of the code so it is easy to change
        String type = cmp.getComponentType();
        if (type.equalsIgnoreCase("ApexClass")) {
            return handler.handleApexClass(cmp, cmps);
        } else if (type.equalsIgnoreCase("ApexTrigger")) {
            return handler.handleApexTrigger(cmp, cmps);
        } else if (type.equalsIgnoreCase("ApexPage")) {
            return handler.handleApexPage(cmp, cmps);
        } else if (type.equalsIgnoreCase("ApexComponent")) {
            return handler.handleApexComponent(cmp, cmps);
        } else {
            return handler.handleUnknownCase(cmp, cmps);
        }
    }

    /**
     * <p>
     * A handler for the different types of components. There are two parameters passed to each handler: the current
     * component to handle (of the right type for that handler) and the list of components that were deployed together
     * with the component, i.e., when a user does a "Save All", we hand in the different components.
     * </p>
     * <p>
     * This is useful when we need to find the corresponding metadata (-meta.xml) file for components that need them.
     * However, we want to keep the interface flexible enough for those components that do not require a metadata type.
     * So we delegate to the handler on what to do with the list of components to be deployed.
     * </p>
     * 
     * @author nchen
     * 
     * @param <T>
     *            The type to return upon handling the different metadata types.
     */
    public static interface ComponentHandler<T> {
        public T handleApexClass(Component cmp, ComponentList cmps);

        public T handleApexTrigger(Component cmp, ComponentList cmps);

        public T handleApexPage(Component cmp, ComponentList cmps);

        public T handleApexComponent(Component cmp, ComponentList cmps);

        public T handleUnknownCase(Component cmp, ComponentList cmps);
    }
}
