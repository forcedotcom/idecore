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
package com.salesforce.ide.ui.preferences;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 *
 * Setting UTF-8 encoding charset for Force.com perspective.
 *
 * @author fchang
 */
public class ForceDefaultEncodingPreference implements IStartup {
	public static PerspectiveAdapter perspectivListener;
	public static String encodingOfPrevPerspective;

	@Override
    public void earlyStartup() {
	    encodingOfPrevPerspective = ResourcesPlugin.getEncoding();
	    
		perspectivListener = new PerspectiveAdapter()
		{
			@Override
			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
			    // enforce UTF-8 encoding in force.com perspective
				if(perspective.getId().equals(UIConstants.FORCE_PERSPECTIVE_ID)) {
					encodingOfPrevPerspective = ResourcesPlugin.getEncoding();
                    if (!encodingOfPrevPerspective.equals(Constants.FORCE_DEFAULT_ENCODING_CHARSET)) {
                        getPreferenceStore().setValue(ResourcesPlugin.PREF_ENCODING,
                            Constants.FORCE_DEFAULT_ENCODING_CHARSET);
                    }
				}
			}

			@Override
			public void perspectiveDeactivated(IWorkbenchPage page,
			        IPerspectiveDescriptor perspective) {
			    // restore previous encoding setting when switch out of force.com perspective
			    if (perspective.getId().equals(UIConstants.FORCE_PERSPECTIVE_ID)) {
			        if (!encodingOfPrevPerspective.equals(Constants.FORCE_DEFAULT_ENCODING_CHARSET)) {
			            getPreferenceStore().setValue(ResourcesPlugin.PREF_ENCODING,
			                encodingOfPrevPerspective);
			        }
			    }
			}

            private IPreferenceStore getPreferenceStore() {
                return new ScopedPreferenceStore(InstanceScope.INSTANCE, ResourcesPlugin.PI_RESOURCES);
            }
		};

		UIUtils.addPerspectiveListener(perspectivListener);
	}

}
