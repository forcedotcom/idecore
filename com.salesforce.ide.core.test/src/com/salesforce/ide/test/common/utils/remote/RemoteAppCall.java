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
package com.salesforce.ide.test.common.utils.remote;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Class to make http requests to the test servlet running on app server.
 * 
 * @author agupta
 * @author ssasalatti
 */
public class RemoteAppCall {

    private static final Logger logger = Logger.getLogger(RemoteAppCall.class);

    private static final String OPERATION_KEY = "OPERATION";
    private static final String APP_SERVER_CONFIG_URL = "<ToBeContinued>";

    private static URL url = null;

    /**
     * Class to capture the request that needs to be made to the app server. Move this class out as being inner class if
     * this class expands with time, though it is not expected to happen
     */
    public static final class RequestObject implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -3429091222831856818L;
        private RemoteOperationEnum operation;
        private String orgId;
        private Properties params;
        private byte[] zipBytesForPackageToInstall;

        public byte[] getZipBytesForPackageToInstall() {
            return zipBytesForPackageToInstall;
        }

        public RequestObject(String orgId, RemoteOperationEnum oper, Properties params) {
            this.operation = oper;
            this.params = params;
        }

        public RequestObject(String orgId, RemoteOperationEnum oper, Properties params, byte[] zipBytes) {
            this.operation = oper;
            this.params = params;
            this.zipBytesForPackageToInstall = zipBytes;
        }

        String getOperation() {
            return this.operation.toString();
        }

        Properties getParameters() {
            return this.params;
        }

        String getOrgId() {
            return orgId;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("\n");
            sb.append("Operation").append(operation.toString()).append("\n");
            if (null != orgId)
                sb.append("OrgId").append(orgId.toString()).append("\n");
            if (null != params)
                sb.append("Properties").append(params.toString()).append("\n");
            return sb.toString();
        }
    }

    /**
     * Method to make requests to the app server.
     * 
     * @throws IdeTestException
     */
    public static Properties sendRequest(RequestObject rObject) throws IdeTestException {

        HttpURLConnection conn = null;
        Properties result = new Properties();
        try {
            if (null == url) {

                String remoteServerLocation =
                        (new StringBuffer(IdeTestUtil.getAppServerToHitForTests(true)).append(APP_SERVER_CONFIG_URL))
                                .toString();
                url = new URL(remoteServerLocation);
            }

            logger.info("Opening URL connection: " + url.toString());

            // Open a Http Connection to app server and set the input and output streams to true
            conn = (HttpURLConnection) url.openConnection();
            // probably excessive, but we're getting a lot of timeouts
            conn.setConnectTimeout(360000); // 6 min
            conn.setReadTimeout(360000); // 6 min
            conn.setDoOutput(true);
            conn.setDoInput(true);
            StringBuffer strBuffer = new StringBuffer(OPERATION_KEY + "=" + rObject.getOperation());

            // Setting the Org Id of the org on which operation needs to be performed
            if (rObject.getOrgId() != null) {
                strBuffer.append("&").append(RemoteOperationEnum.OperationKey.ORG_ID).append("=").append(
                    rObject.getOrgId());
            }

            // Setting up any further required parameters
            if (rObject.getParameters() != null) {
                Enumeration<?> properties = rObject.getParameters().propertyNames();
                while (properties.hasMoreElements()) {
                    String property = (String) properties.nextElement();
                    strBuffer.append("&").append(property).append("=").append(
                        rObject.getParameters().getProperty(property));
                }
            }

            logger.info("Sending query string: " + strBuffer.toString());

            // Setting first the operation that needs to be performed
            conn.getOutputStream().write(strBuffer.toString().getBytes());

            InputStream ris = null;
            boolean isError = false;
            //Obtaining server response
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                ris = conn.getInputStream();
            else {
                isError = true;
                ris = conn.getErrorStream();
            }

            result.load(ris);

            if (isError) {
                StringBuilder stringBuilder =
                        new StringBuilder("Error while communicating with ").append(url.toString()).append(". ");
                stringBuilder.append(conn.getResponseMessage()).append(". ");
                stringBuilder
                        .append((IdeTestUtil.isNotEmpty(result.getProperty("Error"))
                                ? result.getProperty("Error")
                                : "UNKNOWN ERROR WHILE EXECUTING REMOTE REQUEST. Try bouncing the app server and try again.  Response Code: "
                                        + conn.getResponseCode()));
                String msg = stringBuilder.toString();
                throw IdeTestException.getWrappedException(msg);
            }
        } catch (MalformedURLException e) {
            throw IdeTestException.getWrappedException("Malformed URL exception in sending request to server", e);
        } catch (IOException e) {
            throw IdeTestException.getWrappedException("I/O Exception while communication with server", e);
        } finally {
            if (null != conn)
                conn.disconnect();
        }

        return result;
    }

  
}
