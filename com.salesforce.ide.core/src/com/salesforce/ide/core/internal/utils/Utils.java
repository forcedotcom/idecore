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
package com.salesforce.ide.core.internal.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.eclipse.core.internal.resources.OS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;
import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.TestContext.TestContextEnum;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.services.PackageDeployService;
import com.salesforce.ide.core.services.PackageRetrieveService;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ManageableState;
import com.sforce.soap.partner.fault.wsc.ApiFault;

/**
 * Common utilities methods
 */
@SuppressWarnings("restriction")
public class Utils {
    private static final ImmutableList<String> SPECIAL_NOUNS = ImmutableList.of("Apex", "Visualforce", "Lightning", "Salesforce", "Salesforce.com");

	private static final Logger logger = Logger.getLogger(Utils.class);

	public static final String DIALOG_TITLE_ERROR = "Error";
	public static final String DIALOG_TITLE_WARNING = "Warning";
	public static final String TEST_CONTEXT_PROP = "TestContext";

	public static boolean isEmpty(Object obj) {
		return null == obj;
	}

	public static boolean isNotEmpty(Object obj) {
		return null != obj;
	}

	public static boolean isEmpty(Object[] objs) {
		return objs == null || objs.length == 0;
	}

	public static boolean isNotEmpty(Object[] objs) {
	    return objs != null && 0 < objs.length;
	}

	public static boolean isEmpty(byte[] objs) {
		return objs == null || objs.length == 0;
	}

	public static boolean isNotEmpty(byte[] objs) {
		return objs != null && 0 < objs.length;
	}

	public static boolean isEmpty(Collection<?> col) {
		return col == null || col.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> col) {
	    return col != null && !col.isEmpty();
	}

	public static boolean isEmpty(List<?> col) {
		return col == null || col.isEmpty();
	}

	public static boolean isNotEmpty(List<?> col) {
		return col != null && !col.isEmpty();
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isNotEmpty(Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}

	public static boolean isWorkspaceCaseSensitive() {
		return Platform.OS_MACOSX.equals(Platform.getOS()) ? false
				: new java.io.File("a").compareTo(new java.io.File("A")) != 0;
	}

	public static void openForcePerspective() {
		IWorkbench workbench = ForceIdeCorePlugin.getDefault().getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IAdaptable input;

		if (page != null) {
			input = page.getInput();
		} else {
			input = ResourcesPlugin.getWorkspace().getRoot();
		}

		try {
			workbench.showPerspective(Constants.FORCE_PLUGIN_PREFIX
					+ ".perspective", window, input);
		} catch (WorkbenchException e) {
			logger.error("Unable to open Force.com Perspective", e);
		}
	}

	public static Shell getShell() {
		Shell shell = PlatformUI.getWorkbench().getWorkbenchWindows()[0]
				.getShell();
		return shell;
	}

	public static void openError(Exception e, String message, String details) {
		IStatus status = new Status(IStatus.ERROR, ForceIdeCorePlugin
				.getPluginId(), Constants.ERROR_CODE__44, details, e);
		ErrorDialog.openError(getShell(), DIALOG_TITLE_ERROR, message, status);
	}

	public static void openError(Throwable pThrowable,
			boolean includeStackTrace, String messageSummary) {
		IStatus status = null;
		messageSummary = Utils.isEmpty(messageSummary) ? ForceExceptionUtils
				.getRootExceptionMessage(pThrowable) : messageSummary;
		if (includeStackTrace && logger.isDebugEnabled()) {
			status = new MultiStatus(ForceIdeCorePlugin.getPluginId(),
					Constants.ERROR_CODE__44, getStatusMessage(pThrowable),
					ForceExceptionUtils.getRootCause(pThrowable));
			addTraceToStatus((MultiStatus) status, pThrowable.getStackTrace(),
					IStatus.ERROR);
		} else {
			status = new Status(IStatus.ERROR,
					ForceIdeCorePlugin.getPluginId(), Constants.ERROR_CODE__44,
					getStatusMessage(pThrowable), null);
		}
		openErrorDialog(DIALOG_TITLE_ERROR, messageSummary, status);
	}

	public static void openError(Shell shell, Throwable pThrowable,
			boolean includeStackTrace, String messageSummary) {
		IStatus status = null;
		messageSummary = Utils.isEmpty(messageSummary) ? ForceExceptionUtils
				.getRootCause(pThrowable).getMessage() : messageSummary;
		if (includeStackTrace && logger.isDebugEnabled()) {
			status = new MultiStatus(ForceIdeCorePlugin.getPluginId(),
					Constants.ERROR_CODE__44, getStatusMessage(pThrowable),
					ForceExceptionUtils.getRootCause(pThrowable));
			addTraceToStatus((MultiStatus) status, pThrowable.getStackTrace(),
					IStatus.ERROR);
		} else {
			status = new Status(IStatus.ERROR,
					ForceIdeCorePlugin.getPluginId(), Constants.ERROR_CODE__44,
					getStatusMessage(pThrowable), null);
		}
		openErrorDialog(shell, DIALOG_TITLE_ERROR, messageSummary, status);
	}

	public static void openWarning(Throwable pThrowable,
			boolean includeStackTrace, String messageSummary) {
		IStatus status = null;
		if (includeStackTrace && logger.isDebugEnabled()) {
			status = new MultiStatus(ForceIdeCorePlugin.getPluginId(),
					Constants.ERROR_CODE__44, getStatusMessage(pThrowable),
					ForceExceptionUtils.getRootCause(pThrowable));
			addTraceToStatus((MultiStatus) status, pThrowable.getStackTrace(),
					IStatus.WARNING);
		} else {
			status = new Status(IStatus.WARNING, ForceIdeCorePlugin
					.getPluginId(), Constants.ERROR_CODE__44,
					getStatusMessage(pThrowable), null);
		}
		openErrorDialog(DIALOG_TITLE_WARNING, messageSummary, status);
	}

	private static String getStatusMessage(Throwable th) {
		if (th == null) {
			return null;
		}

		if (th instanceof ApiFault) {
			ApiFault apiFault = (ApiFault) th;
			return apiFault.getExceptionCode().name() + " - "
					+ apiFault.getExceptionMessage();
		} else if (th.getCause() instanceof ApiFault) {
			ApiFault apiFault = (ApiFault) th.getCause();
			return apiFault.getExceptionCode().name() + " - "
					+ apiFault.getExceptionMessage();
		} else {
			return ForceExceptionUtils.getStrippedRootCauseMessage(th);
		}
	}

	private static void openErrorDialog(String type, String message,
			IStatus status) {
		Shell newShell = getShell();
		if (newShell == null) {
			newShell = new Shell();
		}
		ErrorDialog.openError(newShell, type, message, status);
	}

	private static void openErrorDialog(Shell shell, String type,
			String message, IStatus status) {
		ErrorDialog.openError(shell, type, message, status);
	}

	public static void addTraceToStatus(MultiStatus multiStatus,
			StackTraceElement[] trace, int errorCode) {
		for (int i = 1; i < trace.length; i++) {
			IStatus stat = new Status(errorCode, ForceIdeCorePlugin
					.getPluginId(), Constants.ERROR_CODE__44, trace[i]
					.getClassName()
					+ "."
					+ trace[i].getMethodName()
					+ " ("
					+ trace[i].getFileName()
					+ " "
					+ trace[i].getLineNumber()
					+ ")", null);
			multiStatus.add(stat);
		}
	}

	public static String generateCoreExceptionLog(CoreException ex) {
		if(ex==null){
			throw new IllegalArgumentException();
		}

		StringBuffer strBuff = new StringBuffer(ex.getMessage());
		if (ex.getStatus() != null
				&& ex.getStatus().isMultiStatus()
				&& isNotEmpty(((MultiStatus) ex.getStatus()).getChildren())) {
			strBuff.append(" Cause(s):\n");
			IStatus[] stats = ((MultiStatus) ex.getStatus()).getChildren();
			int cnt = 0;
			for (IStatus status : stats) {
				strBuff.append(" (").append(++cnt).append(") ").append(
						status.getMessage());
				if (status.getException() != null) {
					strBuff.append("\n    Root Exception: ").append(
							status.getException().getMessage());
				}
			}

		} else if (ex.getStatus() != null) {
			strBuff.append(" Cause:\n");
			strBuff.append(" (1) ").append(ex.getStatus().getMessage());
			if (ex.getStatus().getException() != null) {
				strBuff.append("\n    Root Exception: ").append(
						ex.getStatus().getException().getMessage());
			}

		}
		return strBuff.toString();
	}

	public static boolean openConfirm(String pTitle, String pMessage) {
		return MessageDialog.openConfirm(getShell(), pTitle, pMessage);
	}

	public static boolean openConfirm(Shell shell, String pTitle,
			String pMessage) {
		return MessageDialog.openConfirm(shell, pTitle, pMessage);
	}

	public static boolean openQuestion(String pTitle, String pMessage) {
		return MessageDialog.openQuestion(getShell(), pTitle, pMessage);
	}

	public static boolean openQuestion(Shell shell, String pTitle,
			String pMessage) {
		return MessageDialog.openQuestion(shell, pTitle, pMessage);
	}

	public static void openError(String pTitle, String pMessage) {
		MessageDialog.openError(getShell(), pTitle, pMessage);
	}

	public static void openInfo(String pTitle, String pMessage) {
		MessageDialog.openInformation(getShell(), pTitle, pMessage);
	}

	public static void openWarn(String pTitle, String pMessage) {
		MessageDialog.openWarning(getShell(), pTitle, pMessage);
	}

	public static void openWarn(Shell shell, String pTitle, String pMessage) {
		MessageDialog.openWarning(shell, pTitle, pMessage);
	}

	public static void openDialog(IProject project, WizardDialog dialog) {
		TestContext testContext = TestContext
				.getTestContextBy(getTestContextEnum(project));
		testContext.execAsyncRunnables();
		dialog.open();
	}

	public static boolean openQuestion(IProject project, Shell shell,
			String title, String message) {
		TestContext testContext = TestContext
				.getTestContextBy(getTestContextEnum(project));
		testContext.execAsyncRunnables();
		return openQuestion(shell, title, message);
	}

	public static IResource getCurrentSelectionResource() {
		IWorkbenchWindow workbenchWindow = ForceIdeCorePlugin.getDefault()
				.getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow == null) {
			return null;
		}

		ISelection selection = workbenchWindow.getSelectionService()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object firstSelected = ((IStructuredSelection) selection)
					.getFirstElement();
			if (firstSelected instanceof IResource) {
				return (IResource) firstSelected;
			}
		}
		return null;
	}

	public static IStatus getStatus(Exception e) {
		String msg = e.getMessage();
		if (e instanceof CoreException) {
			CoreException ce = (CoreException) e;
			IStatus status = ce.getStatus();
			return status;
		}
		IStatus status = new Status(IStatus.ERROR, ForceIdeCorePlugin
				.getPluginId(), IStatus.OK, msg, null);
		return status;
	}

	public static String getDisplayDate(Calendar cal) {
		if (cal == null) {
			return "n/a";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(
				"E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		return formatter.format(cal.getTime());
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	public static boolean isValidFullUrl(String endpoint) {
		if (Utils.isEmpty(endpoint)) {
			return false;
		}

		try {
			new URL(endpoint);
		} catch (MalformedURLException e) {
			return false;
		}

		return true;
	}

	public static boolean validateDomainName(String domainName) {
		String oneAlpha = "(.)*((\\p{Alpha})|[-])(.)*";
		String domainIdentifier = "((\\p{Alnum})([-]|(\\p{Alnum}))*(\\p{Alnum}))|(\\p{Alnum})";
		String domainNameRule = "(" + domainIdentifier + ")((\\.)("
				+ domainIdentifier + "))*";
		if (domainName == null || domainName.length() > 63) {
			return false;
		}
		return domainName.matches(domainNameRule)
				&& domainName.matches(oneAlpha);
	}

	public static String getServerNameFromUrl(String url) {
		if (Utils.isEmpty(url)) {
			return url;
		}

		String protocol = (url.startsWith(Constants.HTTPS) ? Constants.HTTPS
				: Constants.HTTP)
				+ "://";
		return url.substring(url.indexOf(protocol) + protocol.length(), url
				.indexOf("/", protocol.length() + 1));
	}

	public static boolean isInternalMode() {
		String mode = System.getProperty(Constants.SYS_SETTING_SFDC_INTERNAL);
		return Utils.isNotEmpty(mode)
				&& Constants.SYS_SETTING_SFDC_INTERNAL_VALUE.equals(mode) ? true : false;
	}

    public static boolean isXForceProxy() {
        String forceProxy = System.getProperty(Constants.SYS_SETTING_X_FORCE_PROXY);
        return Utils.isNotEmpty(forceProxy) && Constants.SYS_SETTING_SFDC_INTERNAL_VALUE.equals(forceProxy) ? true : false;
    }

    public static int getApexManifestTimeoutMS() {
        String apexManifestTimeoutMS = System.getProperty(Constants.SYS_SETTING_APEX_MANIFEST_TIMEOUT);
        if (Utils.isNotEmpty(apexManifestTimeoutMS)) {
            try {
                return Integer.parseInt(apexManifestTimeoutMS);
            } catch (Exception e) {
                return Constants.APEX_MANIFEST_TIMEOUT_IN_MS_DEFAULT;
            }
        } else {
            return Constants.APEX_MANIFEST_TIMEOUT_IN_MS_DEFAULT;
        }
    }

	public static String getDefaultSystemApiVersion() {
		String apiVersion = System
				.getProperty(Constants.SYS_SETTING_DEFAULT_API_VERSION);
		return Utils.isNotEmpty(apiVersion) ? apiVersion : Constants.EMPTY_STRING;
	}

	public static String getPollLimit() {
		String pollLimit = System
				.getProperty(Constants.SYS_SETTING_POLL_LIMIT_MILLIS);
		return Utils.isNotEmpty(pollLimit) ? pollLimit : Constants.EMPTY_STRING;
	}

	public static String getLocaleFormattedDateTime(long datetime) {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.MEDIUM, Locale.getDefault()).format(
				new Date(datetime));
	}

	public static String getCurrentFormattedDateTime() {
		return (new SimpleDateFormat(Constants.STANDARD_DATE_FORMAT))
				.format(new Date());
	}

	public static String getFormattedTimestamp(File file) {
		return file != null && file.exists() ? getLocaleFormattedDateTime(file.lastModified()) : null;
	}

	public static File getCacheFile(IProject project) {
		try {
			Bundle bundle = ForceIdeCorePlugin.getDefault().getBundle();
			IPath state = null;
			if (project != null) {
				state = project.getWorkingLocation(ForceIdeCorePlugin
						.getPluginId());
			} else {
				state = Platform.getStateLocation(bundle);
			}

			return new File(state.toFile(), Constants.CACHE_FILENAME);
		} catch (Exception e) {
			logger.warn("Unable to get cache file: " + e.getMessage());
		}
		return null;
	}

	public static URL getCacheUrl(IProject project) {
		File file = Utils.getCacheFile(project);
		if (file != null) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Manifest cache file (does"
							+ (file.exists() ? " " : " not ") + "exist): "
							+ file.toURI().toURL().toExternalForm());
				}

				return file.toURI().toURL();
			} catch (MalformedURLException e) {
				logger.warn("Unable to get url from file: " + e.getMessage());
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Manifest cache file: n/a");
		}

		return null;
	}

	public static boolean isDebugMode() {
		String debug = System.getProperty(Constants.SYS_SETTING_DEBUG);
		return logger != null && logger.isDebugEnabled()
				|| Utils.isNotEmpty(debug)
				&& Constants.SYS_SETTING_DEBUG_VALUE.equals(debug);
	}

	public static boolean isManifestListenerEnabled() {
		String listener = System
				.getProperty(Constants.SYS_SETTING_MANIFEST_LISTENER);
		return Utils.isNotEmpty(listener) ? Boolean.parseBoolean(listener)
				: true;
	}

	public static boolean hasDefaultProperties() {
		String propFilePath = System
				.getProperty(Constants.SYS_SETTING_PROPERTIES);
		return Utils.isNotEmpty(propFilePath)
				&& (new java.io.File(propFilePath)).exists();
	}

	public static Properties getDefaultProperties() {
		Properties props = null;
		String propFilePath = System
				.getProperty(Constants.SYS_SETTING_PROPERTIES);
		if (Utils.isEmpty(propFilePath)) {
			return props;
		}

		java.io.File propFile = new java.io.File(propFilePath);
		if (!propFile.exists()) {
			return props;
		}

		try (final QuietCloseable<FileInputStream> c = QuietCloseable.make(new FileInputStream(propFile))) {
		    final FileInputStream fis = c.get();
			if (fis.available() > 0) {
				logger.debug("Loading properties found in prop file '"
						+ propFilePath + "'");
				props = new Properties();
				props.load(fis);
			} else {
				logger.debug("No content found in prop file '" + propFilePath
						+ "'");
			}
		} catch (Exception e) {
			logger.warn("Unable to load prop file '" + propFilePath + "'", e);
		}

		return props;
	}

	/**
	 * remove service level version number from bundle version which composed by
	 * <major_version_#>.<minor_version_#>.<service_level_version_#>.qualifier
	 *
	 * @param bundleVersion
	 * @return
	 */
	public static String removeServiceLevelFromPluginVersion(
			String bundleVersion) {
		String[] subBundleVersion = bundleVersion.split("\\.");
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < subBundleVersion.length; i++) {
			if (i == 2) {
				continue; // skip service level version
			}
			strBuffer.append(subBundleVersion[i]).append(".");
		}
		String newBundleVersion = strBuffer.toString();
		return newBundleVersion.substring(0, newBundleVersion.length() - 1);
	}

	public static void logStats() {
		OperationStats[] operationsStats = new OperationStats[2];
		OperationStats aggregatedStats = new OperationStats(
				OperationStats.AGGREGATED_OPERATIONS);

		int idx = 0;
		if (PackageDeployService.getOperationStats() != null) {
			PackageDeployService.getOperationStats().logStats();
			operationsStats[idx] = PackageDeployService.getOperationStats();
		}

		if (PackageRetrieveService.getOperationStats() != null) {
			PackageRetrieveService.getOperationStats().logStats();
			operationsStats[++idx] = PackageRetrieveService.getOperationStats();
		}

		if (isNotEmpty(operationsStats)) {
			aggregatedStats.aggregateStats(operationsStats);
			aggregatedStats.logStats();
		}

		ForceIdeCorePlugin.logStats();

	}

	public static String timeoutToSecs(String timeout) {
		return timeoutToSecs(Long.parseLong(timeout));
	}

	public static String timeoutToSecs(long timeout) {
		return timeout < 1 ? "0 secs" : timeout / Constants.SECONDS_TO_MILISECONDS + " secs";
	}

	// REVIEWME: should this be moved to ProjectService?
	public static TestContextEnum getTestContextEnum(IProject project) {
		if (project == null) {
			return TestContextEnum.NONE;
		}
		IEclipsePreferences node = getPreferences(project);
		return node != null ? TestContextEnum.valueOf(node.get(TEST_CONTEXT_PROP,
				TestContextEnum.NONE.toString())) : TestContextEnum.NONE;
	}

	public static IEclipsePreferences getPreferences(IProject project) {
		if (project == null) {
			return null;
		}
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences node = projectScope
				.getNode(Constants.PLUGIN_PREFIX);
		return node;
	}

	public static Method getGetter(Class<?> clazz, String field) {
		if (clazz == null) {
			return null;
		}

		Method[] methods = clazz.getDeclaredMethods();
		if (isEmpty(methods)) {
			return null;
		}

		for (Method method : methods) {
			if (Utils.isNotEmpty(field) && method.getName().startsWith("get"+field) && method.getGenericParameterTypes().length==0) {
				return method;
			}
		}

		return null;
	}

	public static List<String> getProperties(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}

		List<String> properties = new ArrayList<>();
		Method[] methods = clazz.getDeclaredMethods();
		if (isNotEmpty(methods)) {
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				if (method.getName().startsWith("get")
						&& method.getGenericParameterTypes().length == 0) {
					properties.add(method.getName().substring(3));
				}
			}
		}

		return properties;
	}

	public static Object getPropertyValue(Object obj, String propertyName)
			throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (obj == null || propertyName == null) {
			return null;
		}

		Object propertyValue = null;
		Method getterMethod = getGetterMethod(obj.getClass(), propertyName);
		if (getterMethod != null) {
			Object[] args = null;
			propertyValue = getterMethod.invoke(obj, args);
		}
		return propertyValue;
	}

	public static Method getGetterMethod(Class<?> clazz, String methodNameWithoutGetPrefix) {
		Method getterMethod = null;
		Method[] methods = clazz.getDeclaredMethods();
		if (isNotEmpty(methods)) {
			for (Method method : methods) {
				if (method.getName().equals("get" + methodNameWithoutGetPrefix) && method.getGenericParameterTypes().length==0) {
					getterMethod = method;
					break;
				}
			}
		}
		return getterMethod;
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends MetadataExt> getMetadataClassForComponentType(
			String componentType) throws ClassNotFoundException {
		if (Utils.isEmpty(componentType)) {
			return null;
		}

		return (Class<? extends MetadataExt>) Class
				.forName(Constants.COMPONENT_TYPE_API_CLASS_PACKAGE + "."
						+ componentType);
	}

	public static void saveDocument(Document doc, Bundle bundle, String fileName)
			throws IOException, TransformerException {
		IPath state = Platform.getStateLocation(bundle);
		saveDocument(doc, state.toPortableString() + File.separator + fileName);
	}

	public static void saveDocument(Document doc, String fullPath)
			throws IOException, TransformerException {
		File f = new File(URLDecoder.decode(fullPath, "UTF-8"));
		f.createNewFile();

		TransformerFactory tfactory = TransformerFactory.newInstance();
		tfactory.setAttribute("indent-number", new Integer(2));
		Transformer xform = tfactory.newTransformer();
		xform.setOutputProperty(OutputKeys.INDENT, "yes");
		// xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
		// "4");
		Source src = new DOMSource(doc);

        try (final QuietCloseable<OutputStreamWriter> c = QuietCloseable.make(new OutputStreamWriter(new FileOutputStream(f, false), Constants.FORCE_DEFAULT_ENCODING_CHARSET))) {
            final OutputStreamWriter writer = c.get();
    		Result result = new StreamResult(writer);
    		xform.transform(src, result);
        }
	}

	public static Document loadDocument(URL fileUrl) {
		Document doc = null;
		DOMParser parser = new DOMParser();

		if (logger.isDebugEnabled()) {
			logger.debug("Loading document from " + fileUrl.toExternalForm());
		}

		try {
			InputSource inputSource = new InputSource(fileUrl.openStream());
			parser.parse(inputSource);
			doc = parser.getDocument();
		} catch (Exception e) {
			logger.warn("Unable to load document: " + e.getMessage());
		}

		return doc;
	}

	public static void adjustResourceReadOnly(IResource resource,
			boolean readyOnly, boolean recursive) {
		if (resource == null || !resource.exists()) {
			return;
		}

		// no need to set read-only if resource is already set to desired
		// read-only setting
		if (resource.getResourceAttributes() != null
				&& resource.getResourceAttributes().isReadOnly() != readyOnly) {
			ResourceAttributes resourceAttributes = new ResourceAttributes();
			resourceAttributes.setReadOnly(readyOnly);
			try {
				resource.setResourceAttributes(resourceAttributes);
				if (logger.isDebugEnabled()) {
					logger.debug("Set resource '"
							+ resource.getProjectRelativePath()
									.toPortableString() + "' read-only="
							+ readyOnly);
				}
			} catch (CoreException e) {
				String logMessage = Utils.generateCoreExceptionLog(e);
				logger.warn("Unable to set read-only attribute on file "
						+ resource.getName() + ": " + logMessage);
			}
		}

		if (recursive && resource instanceof IContainer) {
			try {
				IResource[] children = ((IContainer) resource).members();
				if (Utils.isNotEmpty(children)) {
					for (IResource childResource : children) {
						adjustResourceReadOnly(childResource, readyOnly,
								recursive);
					}
				}
			} catch (CoreException e) {
				String logMessage = Utils.generateCoreExceptionLog(e);
				logger.warn("Unable to get children for folder "
						+ resource.getName() + ": " + logMessage);
			}
		}
	}

	// !!! ADD NEW METHODS ABOVE THE FOLLOWING STRING UTILS SECTION !!!

	// (S T A R T) M O V E T O S T R I N G U T I L S
	private final static String[] IDE_INVALID_CHARS = new String[] { ">", "<",
			",", ":", ";", "/", "\\" };
	private final static String[] HOST_PORT_INVALID_CHARS = new String[] { ">",
			"<", ",", ";", "/", "\\", " " };

	public static boolean isEqual(String str, String str2,boolean isCaseSensitive) {
		return isNotEmpty(str) && isNotEmpty(str2) ? isCaseSensitive ? str.equals(str2) : str.equalsIgnoreCase(str2) : false;
	}

	public static boolean isEqual(String str, String compareStr) {
		return isNotEmpty(str) && isNotEmpty(compareStr)
				&& str.equals(compareStr);
	}

	public static boolean isNotEqual(String str, String str2) {
		return !isEqual(str, str2, true);
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return str != null && 0 < str.length();
	}

	public static boolean startsWith(String str, String suffix) {
		return isNotEmpty(str) && isNotEmpty(suffix) && str.startsWith(suffix);
	}

	public static boolean endsWith(String str, String suffix) {
		return isNotEmpty(str) && isNotEmpty(suffix) && str.endsWith(suffix);
	}

	public static boolean contains(String str, String contains) {
		return isNotEmpty(str) && isNotEmpty(contains)
				&& str.contains(contains);
	}

	public static InputStream openContentStream(String contents) {
		return new ByteArrayInputStream(contents.getBytes());
	}

	public static String getContentString(IFile file) throws IOException,
			CoreException {
		String contentStr = null;
		if (file != null && file.exists()) {
			StringBuffer strBuff = new StringBuffer();

	        try (final QuietCloseable<BufferedReader> c = QuietCloseable.make(new BufferedReader(new InputStreamReader(file.getContents(), Constants.UTF_8)))) {
	            final BufferedReader reader = c.get();

                String line = reader.readLine();
                if (line != null) {
                    strBuff.append(line);
                }
                while ((line = reader.readLine()) != null) {
                    strBuff.append(Constants.NEW_LINE);
                    strBuff.append(line);
                }
			} catch (IOException e) {
				logger.error("Unable to load body from file " + file.getName(), e);
				throw e;
			} catch (CoreException e) {
				throw e;
	        }

			if (logger.isDebugEnabled()) {
				logger.debug("Loaded body size ["
						+ strBuff.toString().getBytes().length
						+ "] bytes from file '" + file.getName() + "'");
			}

			contentStr = strBuff.toString();
		}

		return contentStr;
	}

	public static String getStringFromStream(InputStream is, long length)
			throws IOException {
		return getStringFromBytes(getBytesFromStream(is, length));
	}

	public static byte[] getBytesFromStream(InputStream is, long length) throws IOException {
	    try (final QuietCloseable<InputStream> c0 = QuietCloseable.make(is)) {
	        final InputStream in = c0.get();

	        try (final QuietCloseable<ByteArrayOutputStream> c = QuietCloseable.make(new ByteArrayOutputStream())) {
	            final ByteArrayOutputStream out = c.get();
	            byte[] buffer = new byte[1024];
	            int len;
	    
	            while ((len = in.read(buffer)) >= 0) {
	                out.write(buffer, 0, len);
	            }

	            return out.toByteArray();
	        }
	    }
	}

	public static String getStringFromBytes(byte[] bytes) {
		return new String(bytes);
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		if (file == null || !file.exists()) {
			return null;
		}

		return getBytesFromStream(new FileInputStream(file), file.length());
	}

	public static byte[] getBytesFromFile(IFile file) throws IOException,
			CoreException {
		if (file == null || !file.exists()) {
			return null;
		}

		return getBytesFromStream(file.getContents(), 0);
	}

	public static String trim(String str) {
		return Utils.isNotEmpty(str) ? str.trim() : str;
	}

	public static String replaceSpaceWithUnderscore(String str) {
		return str.replaceAll(" ", "_");
	}

	public static String stripExtension(Object obj) {
		if (obj == null) {
			return null;
		}

		String tmpName = null;
		if (obj instanceof Component) {
			tmpName = ((Component) obj).getName();
		} else if (obj instanceof IFile) {
			tmpName = ((IFile) obj).getName();
		} else if (obj instanceof String) {
			tmpName = (String) obj;
		}

		if (null == tmpName) return null;

		if (tmpName.contains(".")) {
			tmpName = tmpName.substring(0, tmpName.indexOf("."));
		}

		// strip "-meta" typically found on folder metadata files
		if (tmpName.endsWith("-meta")) {
			tmpName = tmpName.substring(0, tmpName.indexOf("-meta"));
		}

		return tmpName;
	}

	public static String stripSourceFolder(String filePath) {
		if (isEmpty(filePath)) {
			return filePath;
		}
		String[] folderPrefixes = new String[] {
				Constants.SOURCE_FOLDER_NAME + "/",
				Constants.REFERENCED_PACKAGE_FOLDER_NAME + "/" };

		for (String folderPrefix : folderPrefixes) {
			if (filePath.startsWith(folderPrefix)) {
				return filePath.substring(filePath.indexOf(folderPrefix)
						+ folderPrefix.length());
			}
		}

		return filePath;
	}

	public static String stripNamespace(String str, String namespace) {
		if (isEmpty(namespace) || isEmpty(str)
				|| !str.startsWith(namespace + Constants.NAMESPACE_SEPARATOR)) {

			return str;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Remove prepended namespace '" + namespace
					+ "' from '" + str + "'");
		}

		return str.substring(namespace.length()
				+ Constants.NAMESPACE_SEPARATOR.length());
	}

	public static String getNameFromFilePath(String filePath) {
		if (isNotEmpty(filePath) && filePath.contains("/")) {
			int idx = filePath.lastIndexOf("/") + 1;
			filePath = filePath.substring(idx);
			if (isNotEmpty(filePath) && filePath.contains(".")) {
				filePath = stripExtension(filePath);
			}
		}
		return filePath;
	}

	public static String getPlural(String str) {
		if (isEmpty(str)) {
			return str;
		}

		if (str.endsWith("x") || str.endsWith("ss")) {
			return str + "es";
		} else if (str.endsWith("s")) {
			return str;
		} else {
			return str + "s";
		}
	}

	public static String stripUnsupportedChars(String str) {
		if (isEmpty(str)) {
			return str;
		}
		return str.replaceAll(":", "");
	}

	public static boolean isAlphaNumericValid(String str) {
		if (isEmpty(str)) {
			return true;
		}
		String regex = "(\\w+)";
		return str.matches(regex);
	}

	/**
	 * Metadata names are (where needed) encoded such that they are reasonable
	 * file names & do not contain '.'. The basic encoding is URL encoding,
	 * excluding '+' (spaces are preserved), but also escaping '.' to '%2E' and
	 * '__' to '%5F%5F'
	 */
	public static String encode(String name) {
		if (isEmpty(name)) {
			return name;
		}

		try {
			String replaceStr = URLEncoder.encode(name,
					Constants.FORCE_DEFAULT_ENCODING_CHARSET);
			replaceStr = replaceStr.replace('+', ' ');
			// Javadoc of URLEncoder.encode() - The special characters ".", "-",
			// "*", and "_" remain the same.
			// replaceStr = replaceStr.replace(".", "%2E");
			// replaceStr = replaceStr.replace("__", "%5F%5F");

			if (logger.isDebugEnabled() && !name.equals(replaceStr)) {
				logger.debug("Encoded name '" + name + "' to '" + replaceStr
						+ "'");
			}

			return replaceStr;
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(uee);
		}
	}

	public static boolean containsInvalidChars(String str) {
		if (isEmpty(str)) {
			return false;
		}

		for (String invalidChar : getInvalidChars()) {
			if (str.contains(invalidChar)) {
				return true;
			}
		}

		return false;
	}

	protected static String[] getInvalidChars() {
		Set<String> invalidCharSet = new HashSet<>();
		// invalid resource chars provided by eclipse platform
		for (char osInvalidChar : OS.INVALID_RESOURCE_CHARACTERS) {
			invalidCharSet.add(String.valueOf(osInvalidChar));
		}
		// invalid chars defined by IDE
		for (String ideInvalidChar : IDE_INVALID_CHARS) {
			invalidCharSet.add(ideInvalidChar);
		}
		return invalidCharSet.toArray(new String[invalidCharSet.size()]);
	}

	public static boolean containsInvalidHostPortChars(String str) {
		if (isEmpty(str)) {
			return false;
		}

		for (String invalidChar : HOST_PORT_INVALID_CHARS) {
			if (str.contains(invalidChar)) {
				return true;
			}
		}

		return false;
	}

	public static String stripNonAlphaNumericChars(String str) {
		if (isEmpty(str)) {
			return str;
		}
		str = str.replaceAll("[^a-zA-Z0-9 _]", ""); // clean up nonAlphaNumeric
		// char in front/trail of _.
		// Ex. -__a__b__c__- would
		// become __a__b__c__
		str = str.replaceAll("^_*", ""); // strips all starting "_". Ex
		// __a__b__c__ would become
		// a__b__c__
		str = str.replaceAll("_*$", ""); // strips all trailing "_". Ex
		// a__b__c__ would become a__b__c
		str = str.replaceAll("_+", "_"); // replaces all leftover multiple
		// occurances of "_" with 1 "_". Ex.
		// a__b__c would become a_b_c
		return str;
	}

	public static boolean containsNonAlphaNumericChars(String str) {
		if (isEmpty(str)) {
			return false;
		}

		Pattern p = Pattern.compile("[^a-zA-Z0-9]");
		return p.matcher(str).matches();
	}

	public static String generateNameFromLabel(String str) {
		if (isEmpty(str)) {
			return str;
		}

		if (startsWithNumeric(str)) {
			str = "X" + str;
		}

		str = stripNonAlphaNumericChars(str);
		return replaceSpaceWithUnderscore(str);
	}

	public static boolean startsWithNumeric(String str) {
		if (isEmpty(str)) {
			return false;
		}

		Pattern p = Pattern.compile("[0-9].*");
		return p.matcher(str).matches();
	}

    public static String capitalizeFirstLetter(String name) {
        return isNotEmpty(name) 
            ? Character.toUpperCase(name.charAt(0)) + name.toLowerCase().substring(1) 
            : name;
    }
    
	/**
	 *
	 * @param name
	 *            - string need to cap first letter and letter after specific
	 *            token
	 * @param token
	 *            - token
	 * @param escape
	 *            - does this token needs escape?
	 * @return
	 */
	public static String capFirstLetterAndLetterAfterToken(String name,
			String token, boolean escape) {
		if (name.indexOf(token.toUpperCase()) == -1
				&& name.indexOf(token.toLowerCase()) == -1) {
			return capitalizeFirstLetter(name);
		}
		String[] segments = name.toLowerCase().split(
				escape ? "\\" + token : token.toLowerCase()); // escaping .
		StringBuffer sb = new StringBuffer();
		for (String segment : segments) {
			sb = sb.append(capitalizeFirstLetter(segment)).append(
					token.toLowerCase());
		}
		return sb.substring(0, sb.length() - 1);
	}

	public static boolean firstLetterCapitalized(String name) {
		return isNotEmpty(name) ? Character.isUpperCase(name.charAt(0)) : false;
	}

	public static String camelCaseToSpaces(String str) {
		if (isEmpty(str)) {
			return str;
		}

		StringBuffer result = new StringBuffer();
		char prevChar = ' ';

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isUpperCase(c) && !Character.isUpperCase(prevChar)) {
				result.append(' ');
				result.append(Character.toUpperCase(c));
			} else {
				result.append(c);
			}

			prevChar = c;
		}

		return result.toString();
	}

	/**
	 * Return null if filePath doesn't have file extension but this method will
	 * fail: 1) if filePath param doesn't have valid file extension and having .
	 * in filePath 2) if filePath is pointing to folder metadata file See
	 * testcase: UtilsTest_unit.testGetExtensionFromFilePath() for usage
	 */
	public static String getExtensionFromFilePath(String filePath) {
		if (Utils.isEmpty(filePath)) {
			logger.error("Filepath cannot be null");
			throw new IllegalArgumentException("Filepath cannot be null");
		}

		String fileExtension = null;
		String[] splitFilePath = filePath.split("\\.");
		if (splitFilePath.length == 1) {
			return null; // no extension found!
		}
		if (filePath.endsWith(Constants.DEFAULT_METADATA_FILE_EXTENSION)) {
			// if it's metadata file, file extension should be
			// <component-file-extension>-meta.xml
			fileExtension = splitFilePath[splitFilePath.length - 2]
					+ Constants.DOT + splitFilePath[splitFilePath.length - 1];
		} else {
			// if it's regular component file, file extension should very last
			// string append after .
			fileExtension = splitFilePath[splitFilePath.length - 1];
		}
		return fileExtension;

	}

	public static boolean isSkipCompatibilityCheck() {
		String mode = System
				.getProperty(Constants.SYS_SETTING_SKIP_COMPATIBILITY_CHECK);
		return Utils.isNotEmpty(mode)
				&& Constants.SYS_SETTING_SKIP_COMPATIBILITY_CHECK_VALUE
						.equals(mode) ? true : false;
	}

	public static boolean isUpgradeEnabled() {
		String mode = System.getProperty(Constants.SYS_SETTING_UPGRADE_ENABLE);
		return Utils.isEmpty(mode) || isEqual("true", mode) ? true : false;
	}

	/**
	 * if the input array contains any packaged components, returns a new
	 * FileProperties array which lacks any packaged components otherwise just
	 * returns the input array
	 */

	public static FileProperties[] removePackagedFiles(FileProperties[] props,
			String organizationNamespace) {
		if (Utils.isEmpty(props)) {
			logger
					.debug("Input file properties is empty. Skip remove packaged file check.");
			return props;
		}

		List<FileProperties> newProps = new ArrayList<>();

		for (FileProperties prop : props) {
			if (prop.getManageableState() != ManageableState.installed
					&& (Utils.isEmpty(prop.getNamespacePrefix()) || prop
							.getNamespacePrefix().equals(organizationNamespace))) {
				newProps.add(prop);
			}

			else if (logger.isDebugEnabled()) {
				logger.debug(prop.getFullName()
						+ " removed from FileProperties"); //$NON-NLS-1$
			}
		}

		if (newProps.size() != props.length) {
			return newProps.toArray(new FileProperties[newProps.size()]);
		}

		return props;
	}

	public static String replaceColonToSurroundingGenericBlock(String type) {
		return type.indexOf(":") > -1 ? type.replace(":", "<") + ">" : type; // replace
		// List:@KeyType
		// to
		// List<@KeyType>
	}

	public static String recursiveReplaceAll(String str, String regex,
			String replacement) {
		String replaced = str.replaceAll(regex, replacement);
		boolean flag = true;
		while (flag) {
			String replaced_again = replaced.replaceAll(regex, replacement);
			if (replaced.equalsIgnoreCase(replaced_again)) {
				flag = false;
			} else {
				replaced = replaced_again;
			}
		}
		return replaced;
	}

	// Lower cases everything except special nouns (e.g, Apex, Lightning, VisualForce)
    public static String sentenceCase(String plural) {
        // Type inference has problems with this so explicitly specify the function parameters
        return Arrays.stream(plural.split("\\s+")).map(new Function<String, String>() {
            @Override
            public String apply(String w) {
                return SPECIAL_NOUNS.contains(w) 
                    ? w
                    : w.toLowerCase();
            }
        }).collect(Collectors.joining(" "));
    }
}
