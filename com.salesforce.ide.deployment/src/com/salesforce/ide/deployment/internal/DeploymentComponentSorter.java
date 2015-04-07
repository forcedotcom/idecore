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
package com.salesforce.ide.deployment.internal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.salesforce.ide.core.internal.utils.Utils;

public class DeploymentComponentSorter {
//    private static final Logger logger = Logger.getLogger(DeploymentComponentSorter.class);

    public static final Integer SORT_ACTION = 0;
    public static final Integer SORT_NAME = 1;
    public static final Integer SORT_PACKAGE_NAME = 2;
    public static final Integer SORT_TYPE = 3;
    public static final Integer SORT_FILENAME = 4;

    static final Map<Integer, Comparator<DeploymentComponent>> sorters =
            new HashMap<>();

    static {
        sorters.put(SORT_ACTION, new Comparator<DeploymentComponent>() {
            @Override
            public int compare(DeploymentComponent o1, DeploymentComponent o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1.getDestinationSummary().getIdx() == o2.getDestinationSummary().getIdx()) {
                    // if equal, then secondary sort by name
                    return String.CASE_INSENSITIVE_ORDER.compare(o1.getFileName(), o2.getFileName());
                } else if (o1.getDestinationSummary().getIdx() > o2.getDestinationSummary().getIdx()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        sorters.put(SORT_NAME, new Comparator<DeploymentComponent>() {
            @Override
            public int compare(DeploymentComponent o1, DeploymentComponent o2) {
                return o1 == o2 ? 0 : String.CASE_INSENSITIVE_ORDER.compare(o1.getNameWithFolder(), o2.getNameWithFolder());
            }
        });

        sorters.put(SORT_PACKAGE_NAME, new Comparator<DeploymentComponent>() {
            @Override
            public int compare(DeploymentComponent o1, DeploymentComponent o2) {
                return o1 == o2 ? 0 : o1.getPackageName().compareTo(o2.getPackageName());
            }
        });

        sorters.put(SORT_TYPE, new Comparator<DeploymentComponent>() {
            @Override
            public int compare(DeploymentComponent o1, DeploymentComponent o2) {
                return o1 == o2 ? 0 : o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        sorters.put(SORT_FILENAME, new Comparator<DeploymentComponent>() {
            @Override
            public int compare(DeploymentComponent o1, DeploymentComponent o2) {
                if (o1 == o2) {
                    return 0;
                } else if (Utils.isEmpty(o1.getFileName()) && Utils.isEmpty(o2.getFileName())) {
                    return 0;
                } else if (Utils.isNotEmpty(o1.getFileName()) && Utils.isEmpty(o2.getFileName())) {
                    return -1;
                } else if (Utils.isEmpty(o1.getFileName()) && Utils.isNotEmpty(o2.getFileName())) {
                    return 1;
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1.getFileName(), o2.getFileName());
                }
            }
        });

    }

    public static Comparator<DeploymentComponent> getSorter(Integer sortIdx) {
        Comparator<DeploymentComponent> comparator = sorters.get(sortIdx);
        if (comparator == null) {
            comparator = sorters.get(SORT_ACTION);
        }
        return comparator;
    }
}
