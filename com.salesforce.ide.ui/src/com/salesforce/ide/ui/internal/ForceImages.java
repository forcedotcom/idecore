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
package com.salesforce.ide.ui.internal;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;
import com.salesforce.ide.ui.internal.utils.UIConstants;

/**
 *
 * Common images registry
 *
 * @author dcarroll
 */
public class ForceImages {

    public final static String ICONS_PATH = "icons/"; //$NON-NLS-1$
    private static final String EDITOR_ICONS_PATH = ICONS_PATH + "editor/";
    private static final String SCHEMA_BROWSER_ICONS_PATH = ICONS_PATH + "schemaBrowser/";
    public final static String DESC = "_DESC"; //$NON-NLS-1$

    /*
     * Setup a registry of commonly used images
     */
    private static ImageRegistry imageRegistry;

    private static final String NAME_PREFIX = UIConstants.PLUGIN_PREFIX + ".";
    public static final String OVERLAY_DIRTY = "overlays/dirty_ov.gif";
    private static final String PATH_ACTIONS = ICONS_PATH + "actions/"; //$NON-NLS-1$
    private static final String PATH_PACKAGE_MANIFEST = ICONS_PATH + "packagemanifest/"; //$NON-NLS-1$

    public static final ImageDescriptor DESC_DIRTY = create(ICONS_PATH, OVERLAY_DIRTY);

    //displayed in the sync wizard banner
    public static final String WIZBAN_SYNC = "sync/share_wizban.png";
    public static final ImageDescriptor DESC_WIZBAN_SYNC = create(ICONS_PATH, WIZBAN_SYNC);

    // frequently used images
    public static final String APEX_WIZARD_IMAGE = NAME_PREFIX + "APEX_WIZARD_IMAGE";
    public static final String APEX_TITLE_IMAGE = NAME_PREFIX + "APEX_TITLE_IMAGE";

    // apex editor icons
    public static final String PACKAGE_ICON = NAME_PREFIX + "PACKAGE_ICON";
    public static final String CLASS_ICON = NAME_PREFIX + "CLASS_ICON";
    public static final String TRIGGER_ICON = NAME_PREFIX + "TRIGGER_ICON";
    public static final String METHOD_ICON = NAME_PREFIX + "METHOD_ICON";
    public static final String TEST_METHOD_ICON = NAME_PREFIX + "TEST_METHOD_ICON";
    public static final String WEB_METHOD_ICON = NAME_PREFIX + "WEB_METHOD_ICON";
    public static final String PRIVATE_VARIABLE_ICON = NAME_PREFIX + "PRIVATE_VARIABLE_ICON";
    public static final String PUBLIC_VARIABLE_ICON = NAME_PREFIX + "PUBLIC_VARIABLE_ICON";

    // status icons
    public static final String ERROR1_ICON = NAME_PREFIX + "ERROR1_ICON";
    public static final String ERROR_ICON = NAME_PREFIX + "ERROR_ICON";
    public static final String WARNING_ICON = NAME_PREFIX + "WARNING_ICON";
    public static final String INFO_ICON = NAME_PREFIX + "INFO_ICON";
    public static final String OK_ICON = NAME_PREFIX + "OK_ICON";
    public static final String DEBUG_ICON = NAME_PREFIX + "DEBUG_ICON";
    public static final String TRACE_ICON = NAME_PREFIX + "TRACE_ICON";

    // schema browser
    public static final String IMAGE_NOT_CHECKED = NAME_PREFIX + "IMAGE_NOT_CHECKED";
    public static final String IMAGE_CHECKED = NAME_PREFIX + "IMAGE_CHECKED";
    public static final String IMAGE_ARROW_DOWN = NAME_PREFIX + "IMAGE_ARROW_DOWN";
    public static final String IMAGE_ARROW_UP = NAME_PREFIX + "IMAGE_ARROW_UP";
    public static final String IMAGE_BLANK = NAME_PREFIX + "IMAGE_BLANK";
    public static final String SCHEMA_FILTERABLE = NAME_PREFIX + "SCHEMA_FILTERABLE";
    public static final String SCHEMA_SELECTABLE = NAME_PREFIX + "SCHEMA_SELECTABLE";
    public static final String SCHEMA_REPLICATEABLE = NAME_PREFIX + "SCHEMA_REPLICATEABLE";
    public static final String SCHEMA_NILLABLE = NAME_PREFIX + "SCHEMA_NILLABLE";
    public static final String SCHEMA_SEARCHABLE = NAME_PREFIX + "SCHEMA_SEARCHABLE";
    public static final String SCHEMA_RETRIEVEABLE = NAME_PREFIX + "SCHEMA_RETRIEVEABLE";
    public static final String SCHEMA_CREATABLE = NAME_PREFIX + "SCHEMA_CREATABLE";
    public static final String SCHEMA_ACTIVATEABLE = NAME_PREFIX + "SCHEMA_ACTIVATEABLE";
    public static final String SCHEMA_FIELD = NAME_PREFIX + "SCHEMA_FIELD";
    public static final String SCHEMA_FIELDS = NAME_PREFIX + "SCHEMA_FIELDS";
    public static final String SCHEMA_ENTITY_ACCESS = NAME_PREFIX + "SCHEMA_ENTITY_ACCESS";
    public static final String SCHEMA_DELETE = NAME_PREFIX + "SCHEMA_DELETE";
    public static final String SCHEMA_ID_LIST = NAME_PREFIX + "SCHEMA_ID_LIST";
    public static final String SCHEMA_INSERT = NAME_PREFIX + "SCHEMA_INSERT";
    public static final String SCHEMA_QUERY = NAME_PREFIX + "SCHEMA_QUERY";
    public static final String SCHEMA_SEARCH = NAME_PREFIX + "SCHEMA_SEARCH";
    public static final String SCHEMA_UPDATE = NAME_PREFIX + "SCHEMA_UPDATE";
    public static final String SCHEMA_ATTRIBUTE = NAME_PREFIX + "SCHEMA_ATTRIBUTE";
    public static final String SCHEMA_FILTER = NAME_PREFIX + "SCHEMA_FILTER";
    public static final String SCHEMA_FIELD_ACCESS = NAME_PREFIX + "SCHEMA_FIELD_ACCESS";
    public static final String SCHEMA_SELECT = NAME_PREFIX + "SCHEMA_SELECT";
    public static final String SCHEMA_PICKLIST_ITEM = NAME_PREFIX + "SCHEMA_PICKLIST_ITEM";
    public static final String SCHEMA_UPDATEABLE = NAME_PREFIX + "SCHEMA_UPDATEABLE";
    public static final String SCHEMA_DELETEABLE = NAME_PREFIX + "SCHEMA_DELETEABLE";
    public static final String SCHEMA_QUERYABLE = NAME_PREFIX + "SCHEMA_QUERYABLE";
    public static final String SCHEMA_CUSTOM_FIELD = NAME_PREFIX + "SCHEMA_CUSTOM_FIELD";
    public static final String SCHEMA_CUSTOMTABLE = NAME_PREFIX + "SCHEMA_CUSTOMTABLE";
    public static final String SCHEMA_REQUIRED = NAME_PREFIX + "SCHEMA_REQUIRED";
    public static final String SCHEMA_EMPTY_LIST = NAME_PREFIX + "SCHEMA_EMPTY_LIST";

    // deploy
    public static final String IMAGE_ADD = NAME_PREFIX + "IMAGE_ADD";
    public static final String IMAGE_DELETE = NAME_PREFIX + "IMAGE_DELETE";
    public static final String IMAGE_OVERWRITE = NAME_PREFIX + "IMAGE_OVERWRITE";
    public static final String IMAGE_NO_CHANGE = NAME_PREFIX + "IMAGE_NO_CHANGE";
    public static final String IMAGE_SUCCESS = NAME_PREFIX + "IMAGE_SUCCESS";
    public static final String IMAGE_FAILURE = NAME_PREFIX + "IMAGE_FAILURE";
    public static final String IMAGE_WARNING = NAME_PREFIX + "IMAGE_WARNING";
    public static final String IMAGE_CONFIRM = NAME_PREFIX + "IMAGE_CONFIRM";

    // log viewer
    public static final String OPEN_FILE_ICON = NAME_PREFIX + "OPEN_FILE_ICON";
    public static final String OPEN_FOLDER_ICON = NAME_PREFIX + "OPEN_FOLDER_ICON";

    // package manifest editor
    public static final String COLLAPSE_ALL = NAME_PREFIX + "COLLAPSE_ALL";
    public static final String EXPAND_ALL = NAME_PREFIX + "EXPAND_ALL";

    public static final String REFRESH_ENABLED = NAME_PREFIX + "REFRESH_ENABLED";
    public static final String REFRESH_DISABLED = NAME_PREFIX + "REFRESH_DISABLED";

    public static final String CUSTOMOBJECT_NODE = NAME_PREFIX + "CUSTOMOBJECT_NODE";

    // Apex lang accessor modifier icons
    public static final String APEX_GLOBAL_CLASS = NAME_PREFIX + "GLOBAL_CLASS";
    public static final String APEX_GLOBAL_FIELD = NAME_PREFIX + "GLOBAL_FIELD";
    public static final String APEX_GLOBAL_METHOD = NAME_PREFIX + "GLOBAL_METHOD";
    public static final String APEX_WEBSERVICE_METHOD = NAME_PREFIX + "WEBSERVICE_METHOD";
    public static final String APEX_WITHSHARING_ACCESSOR_OVERLAY = NAME_PREFIX + "WITHSHARING_ACCESSOR";
    public static final String APEX_WITHOUTSHARING_ACCESSOR_OVERLAY = NAME_PREFIX + "WITHOUTSHARING_ACCESSOR";
    public static final String APEX_TEST_METHOD = NAME_PREFIX + "TEST_METHOD";
    public static final String APEX_SYS_CLS_OVERLAY = NAME_PREFIX + "APEX_SYS_CLS";
    public static final String APEX_WEBSERVICE_ACCESSOR_OVERLAY = NAME_PREFIX + "WEBSERVICE_ACCESSOR";

    public static final String APEX_VIRTUAL_OVERLAY = NAME_PREFIX + "VIRTUAL";
    public static final String APEX_TRANSIENT_OVERLAY = NAME_PREFIX + "TRANSIENT";


    public static final String APEX_TRIGGER = NAME_PREFIX + "APEX_TRIGGER";
    public static final String SCHEMA_OBJECT = NAME_PREFIX + "SCHEMA_OBJECT";
    public static final String JDT_CLASS = NAME_PREFIX + "JDT_CLASS";
    public static final String JDT_METHOD = NAME_PREFIX + "JDT_METHOD";
    public static final String JDT_PUB_METHOD = NAME_PREFIX + "JDT_PUB_METHOD";
    public static final String JDT_FIELD = NAME_PREFIX + "JDT_FIELD";
    public static final String JDT_LOCAL_VAR = NAME_PREFIX + "JDT_LOCAL_VAR";




    /**
     * Get an Image for a given key
     *
     * @param key
     * @return
     */
    public static Image get(String key) {
        init();
        Image image = imageRegistry.get(key);
        if (Utils.isEmpty(image)) {
            ImageDescriptor descriptor = getDesc(key);
            image = descriptor.createImage();
            imageRegistry.put(key, image);
        }
        return image;
    }

    /**
     * Using decorated Image descriptor to create image instead of default descriptor.
     * @param key
     * @param accessorFlags
     * @param decoratedImageDescriptor
     * @return
     */
    public static Image get(String key, int accessorFlags, ApexElementImageDescriptor decoratedImageDescriptor) {
        init();
        // internal key combine type key, accessor and adornment flags.
        String internalKey = key + "_" + accessorFlags + "_" + decoratedImageDescriptor.getAdronments();
        Image image = imageRegistry.get(internalKey);
        if (Utils.isEmpty(image)) {
            image = decoratedImageDescriptor.createImage();
            imageRegistry.put(internalKey, image);
        }
        return image;
    }

    /**
     * Get an ImageDescriptor for a given key
     *
     * @param key
     * @return
     */
    public static ImageDescriptor getDesc(String key) {
        init();
        return imageRegistry.getDescriptor(key + DESC);
    }

    private static void cacheImageDescriptor(String key, ImageDescriptor descriptor) {
        imageRegistry.put(key + DESC, descriptor);
    }

    private static URL getImageURL(String prefix, String name) {
        Path path = new Path(prefix + name);
        return FileLocator.find(ForceIdeUIPlugin.getDefault().getBundle(), path, null);
    }

    private static ImageDescriptor create(String prefix, String name) {
        ImageDescriptor desc = ImageDescriptor.createFromURL(getImageURL(prefix, name));
        return desc;
    }

    public static ImageRegistry getImageRegistry() {
        return imageRegistry;
    }

    public static void setImageRegistry(ImageRegistry imageRegistry) {
        ForceImages.imageRegistry = imageRegistry;
    }

    public static void dispose() {
        if (imageRegistry != null) {
            imageRegistry.dispose();
        }
    }

    private static void init() {
        if (imageRegistry != null) {
            return;
        }
        imageRegistry = JFaceResources.getImageRegistry();

        cacheImageDescriptor(APEX_WIZARD_IMAGE, create(ICONS_PATH, "appExDev.png"));
        cacheImageDescriptor(APEX_TITLE_IMAGE, create(ICONS_PATH, "appExDevIco16.png"));

        cacheImageDescriptor(PACKAGE_ICON, create(ICONS_PATH, "package.gif"));
        cacheImageDescriptor(CLASS_ICON, create(ICONS_PATH, "class.gif"));
        cacheImageDescriptor(TRIGGER_ICON, create(ICONS_PATH, "trigger.gif"));
        cacheImageDescriptor(METHOD_ICON, create(ICONS_PATH, "attributeItem.gif"));
        cacheImageDescriptor(TEST_METHOD_ICON, create(ICONS_PATH, "attributeItemTest.gif"));
        cacheImageDescriptor(WEB_METHOD_ICON, create(ICONS_PATH, "attributeItemWeb.gif"));
        cacheImageDescriptor(PRIVATE_VARIABLE_ICON, create(ICONS_PATH, "attributeItemPrivate.gif"));
        cacheImageDescriptor(PUBLIC_VARIABLE_ICON, create(ICONS_PATH, "attributeItemPublic.gif"));

        cacheImageDescriptor(ERROR1_ICON, create(ICONS_PATH, "error1.png"));

        cacheImageDescriptor(IMAGE_NOT_CHECKED, create(ICONS_PATH, "echeck.png"));
        cacheImageDescriptor(IMAGE_CHECKED, create(ICONS_PATH, "check.png"));
        cacheImageDescriptor(IMAGE_ARROW_DOWN, create(ICONS_PATH, "asc.png"));
        cacheImageDescriptor(IMAGE_ARROW_UP, create(ICONS_PATH, "desc.png"));
        cacheImageDescriptor(IMAGE_BLANK, create(ICONS_PATH, "blank.png"));

        cacheImageDescriptor(IMAGE_DELETE, create(ICONS_PATH, "delete.gif"));
        cacheImageDescriptor(IMAGE_ADD, create(ICONS_PATH, "add_other.gif"));
        cacheImageDescriptor(IMAGE_NO_CHANGE, create(ICONS_PATH, "no_change.gif"));
        cacheImageDescriptor(IMAGE_OVERWRITE, create(ICONS_PATH, "overwrite.gif"));

        cacheImageDescriptor(IMAGE_SUCCESS, create(ICONS_PATH, "success16.png"));
        cacheImageDescriptor(IMAGE_FAILURE, create(ICONS_PATH, "failure16.png"));
        cacheImageDescriptor(IMAGE_WARNING, create(ICONS_PATH, "warning16.png"));
        cacheImageDescriptor(IMAGE_CONFIRM, create(ICONS_PATH, "confirm16.png"));

        cacheImageDescriptor(ERROR_ICON, create(ICONS_PATH, "error.gif"));
        cacheImageDescriptor(WARNING_ICON, create(ICONS_PATH, "warning.gif"));
        cacheImageDescriptor(INFO_ICON, create(ICONS_PATH, "info.gif"));
        cacheImageDescriptor(OK_ICON, create(ICONS_PATH, "ok.gif"));
        cacheImageDescriptor(DEBUG_ICON, create(ICONS_PATH, "debug.gif"));
        cacheImageDescriptor(TRACE_ICON, create(ICONS_PATH, "trace.gif"));

        cacheImageDescriptor(OPEN_FILE_ICON, create(ICONS_PATH, "open_file.gif"));
        cacheImageDescriptor(OPEN_FOLDER_ICON, create(ICONS_PATH, "folder.gif"));

        cacheImageDescriptor(COLLAPSE_ALL, create(PATH_ACTIONS, "collapseall.gif"));
        cacheImageDescriptor(EXPAND_ALL, create(PATH_ACTIONS, "expandall.gif"));

        cacheImageDescriptor(REFRESH_ENABLED, create(PATH_ACTIONS, "refresh_hover.gif"));
        cacheImageDescriptor(REFRESH_DISABLED, create(PATH_ACTIONS, "refresh_normal.gif"));

        cacheImageDescriptor(CUSTOMOBJECT_NODE, create(PATH_PACKAGE_MANIFEST, "property.gif"));

        cacheImageDescriptor(APEX_GLOBAL_CLASS, create(EDITOR_ICONS_PATH, "classGlobal.png"));
        cacheImageDescriptor(APEX_GLOBAL_FIELD, create(EDITOR_ICONS_PATH, "fieldGlobal.png"));
        cacheImageDescriptor(APEX_GLOBAL_METHOD, create(EDITOR_ICONS_PATH, "methodGlobal.png"));
        cacheImageDescriptor(APEX_WEBSERVICE_METHOD, create(EDITOR_ICONS_PATH, "methodWebService.png"));
        cacheImageDescriptor(APEX_WITHSHARING_ACCESSOR_OVERLAY, create(EDITOR_ICONS_PATH, "withsharing_overlay.png"));
        cacheImageDescriptor(APEX_WITHOUTSHARING_ACCESSOR_OVERLAY, create(EDITOR_ICONS_PATH, "withoutsharing_overlay.png"));
        cacheImageDescriptor(APEX_TEST_METHOD, create(EDITOR_ICONS_PATH, "test.png"));
        cacheImageDescriptor(APEX_SYS_CLS_OVERLAY, create(EDITOR_ICONS_PATH, "forcedotcom_overlay.png"));
        cacheImageDescriptor(APEX_WEBSERVICE_ACCESSOR_OVERLAY, create(EDITOR_ICONS_PATH, "webservice_overlay.png"));
        cacheImageDescriptor(APEX_TRIGGER, create(EDITOR_ICONS_PATH, "trigger.png"));
        cacheImageDescriptor(SCHEMA_OBJECT, create(EDITOR_ICONS_PATH, "object.png"));

        cacheImageDescriptor(APEX_VIRTUAL_OVERLAY, create(EDITOR_ICONS_PATH, "volatile_co.gif"));
        cacheImageDescriptor(APEX_TRANSIENT_OVERLAY, create(EDITOR_ICONS_PATH, "transient_co.gif"));

        // cache image desc from Schema Browser
        cacheImageDescriptor(SCHEMA_FILTERABLE, create(SCHEMA_BROWSER_ICONS_PATH, "filterable.gif"));
        cacheImageDescriptor(SCHEMA_SELECTABLE, create(SCHEMA_BROWSER_ICONS_PATH, "selectable.gif"));
        cacheImageDescriptor(SCHEMA_REPLICATEABLE, create(SCHEMA_BROWSER_ICONS_PATH, "replicateable.gif"));
        cacheImageDescriptor(SCHEMA_NILLABLE, create(SCHEMA_BROWSER_ICONS_PATH, "nillable.gif"));
        cacheImageDescriptor(SCHEMA_SEARCHABLE, create(SCHEMA_BROWSER_ICONS_PATH, "searchable.gif"));
        cacheImageDescriptor(SCHEMA_RETRIEVEABLE, create(SCHEMA_BROWSER_ICONS_PATH, "retrieveable.gif"));
        cacheImageDescriptor(SCHEMA_CREATABLE, create(SCHEMA_BROWSER_ICONS_PATH, "createable.gif"));
        cacheImageDescriptor(SCHEMA_ACTIVATEABLE, create(SCHEMA_BROWSER_ICONS_PATH, "activateable.gif"));
        cacheImageDescriptor(SCHEMA_FIELD, create(SCHEMA_BROWSER_ICONS_PATH, "field.gif"));
        cacheImageDescriptor(SCHEMA_FIELDS, create(SCHEMA_BROWSER_ICONS_PATH, "fields.gif"));
        cacheImageDescriptor(SCHEMA_ENTITY_ACCESS, create(SCHEMA_BROWSER_ICONS_PATH, "entityAccess.gif"));
        cacheImageDescriptor(SCHEMA_DELETE, create(SCHEMA_BROWSER_ICONS_PATH, "delete.gif"));
        cacheImageDescriptor(SCHEMA_ID_LIST, create(SCHEMA_BROWSER_ICONS_PATH, "idList.gif"));
        cacheImageDescriptor(SCHEMA_INSERT, create(SCHEMA_BROWSER_ICONS_PATH, "insert.gif"));
        cacheImageDescriptor(SCHEMA_QUERY, create(SCHEMA_BROWSER_ICONS_PATH, "query.gif"));
        cacheImageDescriptor(SCHEMA_SEARCH, create(SCHEMA_BROWSER_ICONS_PATH, "search.gif"));
        cacheImageDescriptor(SCHEMA_UPDATE, create(SCHEMA_BROWSER_ICONS_PATH, "update.gif"));
        cacheImageDescriptor(SCHEMA_ATTRIBUTE, create(SCHEMA_BROWSER_ICONS_PATH, "attribute.gif"));
        cacheImageDescriptor(SCHEMA_FILTER, create(SCHEMA_BROWSER_ICONS_PATH, "filter.gif"));
        cacheImageDescriptor(SCHEMA_FIELD_ACCESS, create(SCHEMA_BROWSER_ICONS_PATH, "fieldAccess.gif"));
        cacheImageDescriptor(SCHEMA_SELECT, create(SCHEMA_BROWSER_ICONS_PATH, "select.gif"));
        cacheImageDescriptor(SCHEMA_PICKLIST_ITEM, create(SCHEMA_BROWSER_ICONS_PATH, "pickListItem.gif"));

        cacheImageDescriptor(SCHEMA_UPDATEABLE, create(SCHEMA_BROWSER_ICONS_PATH, "updateable.gif"));
        cacheImageDescriptor(SCHEMA_DELETEABLE, create(SCHEMA_BROWSER_ICONS_PATH, "deleteable.gif"));
        cacheImageDescriptor(SCHEMA_QUERYABLE, create(SCHEMA_BROWSER_ICONS_PATH, "queryable.gif"));
        cacheImageDescriptor(SCHEMA_CUSTOM_FIELD, create(SCHEMA_BROWSER_ICONS_PATH, "customfield.gif"));
        cacheImageDescriptor(SCHEMA_CUSTOMTABLE, create(SCHEMA_BROWSER_ICONS_PATH, "customtable.gif"));
        cacheImageDescriptor(SCHEMA_REQUIRED, create(SCHEMA_BROWSER_ICONS_PATH, "required.gif"));
        cacheImageDescriptor(SCHEMA_EMPTY_LIST, create(SCHEMA_BROWSER_ICONS_PATH, "emptylist.gif"));


    }
}
