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
package com.salesforce.ide.core.internal.aspects;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.springframework.core.Ordered;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.RetrieveRequest;

public class ServicePreProcessorAspect implements Ordered {

    private static final Logger logger = Logger.getLogger(ServicePreProcessorAspect.class);

    protected int order = 1;

    //   M E T H O D S
    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void preProcessRetrieve(RetrieveRequest retrieveRequest) {
        sameNamedPackageCheck(retrieveRequest);
    }

    // checks if request contains > 1 package of the same name
    private void sameNamedPackageCheck(RetrieveRequest retrieveRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking for same named package in request...");
        }

        // ensure viability of request
        if (retrieveRequest == null || Utils.isEmpty(retrieveRequest.getPackageNames())
                || retrieveRequest.getPackageNames().length < 2) {
            return;
        }

        // look for and record same named packages
        Set<String> packageNameSet = new HashSet<>();
        Set<String> sameNamedPackageSet = new HashSet<>();
        for (String packageName : retrieveRequest.getPackageNames()) {
            if (!packageNameSet.add(packageName)) {
                sameNamedPackageSet.add(packageName);
                packageNameSet.remove(packageName);
            }
        }

        if (Utils.isEmpty(sameNamedPackageSet)) {
            // no same named packages in request
            return;
        }

        // handle same named package
        StringBuffer strBuff = new StringBuffer();
        for (String packageNameDuplicate : sameNamedPackageSet) {
            strBuff.append(" '").append(packageNameDuplicate).append("'");
        }
        logger.warn("Found packages of the same name:" + strBuff.toString());

        String message = Messages.getString("Retrieve.SameNamedPackages.error", new Object[] { strBuff.toString() });
        ConfirmRunnable confirm = new ConfirmRunnable("Same Named Package Request", message);
        try {
            Display.getDefault().syncExec(confirm);
        } catch (SWTException e) {
            Display.getDefault().asyncExec(confirm);
        }

        // should allow for 'cancel', but, for now, we'll just show a warning.

        if (Utils.isNotEmpty(packageNameSet)) {
            retrieveRequest.setPackageNames(packageNameSet.toArray(new String[packageNameSet.size()]));
        } else {
            retrieveRequest.setPackageNames(null);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Retrieve package request list augmented - same named packages removed");
        }

        logRetrieve(retrieveRequest);
    }

    private class ConfirmRunnable implements Runnable {
        private String title = null;
        private String message = null;
        public ConfirmRunnable(String title, String message) {
            this.title = title;
            this.message = message;
        }

        @Override
        public void run() {
            Utils.openWarn(title, message);
        }
    }

    private static void logRetrieve(RetrieveRequest retrieveRequest) {
        if (logger.isDebugEnabled()) {
            StringBuffer strBuff = new StringBuffer();
            boolean defaultPackage = (retrieveRequest.getUnpackaged() != null ? true : false);
            String[] packageNames = retrieveRequest.getPackageNames();
            strBuff.append("Updated retrieve request of the following");
            if (Utils.isNotEmpty(packageNames)) {
                strBuff.append(" [");
                strBuff.append(defaultPackage ? packageNames.length + 1 : packageNames.length);
                strBuff.append("] packages: ");
                for (String packageName : packageNames) {
                    strBuff.append("'");
                    strBuff.append(packageName);
                    strBuff.append("' ");
                }
            } else {
                strBuff.append(" packages: (no named packages) ");
            }

            if (defaultPackage) {
                strBuff.append("'");
                strBuff.append(Constants.DEFAULT_PACKAGED_NAME);
                strBuff.append("' ");
            }

            String[] componentNames = retrieveRequest.getSpecificFiles();
            int componentCnt = 0;
            if (Utils.isNotEmpty(componentNames)) {
                strBuff.append("\nRequesting retrieval of the following ");
                strBuff.append("[");
                strBuff.append(componentNames.length);
                strBuff.append("] components: ");
                for (String componentName : componentNames) {
                    strBuff.append("\n (");
                    strBuff.append(++componentCnt);
                    strBuff.append(") ");
                    strBuff.append(componentName);
                }
            }

            strBuff.append("\nsingle package = " + retrieveRequest.isSinglePackage());

            logger.debug(strBuff.toString());
        } else {
            if (logger.isInfoEnabled()) {
                String[] packageNames = retrieveRequest.getPackageNames();
                int packageCount = 0;
                if (Utils.isNotEmpty(packageNames)) {
                    packageCount = packageNames.length;
                }
                packageCount = packageCount + (retrieveRequest.getUnpackaged() != null ? 1 : 0);
                logger.info("Updated retrieving [" + packageCount + "] packages");
            }
        }
    }
}
