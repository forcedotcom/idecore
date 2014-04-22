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
package com.salesforce.ide.ui.editors.intro.browser;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.browser.WebBrowserEditor;

import com.salesforce.ide.ui.internal.utils.UIUtils;

@SuppressWarnings( { "restriction" })
public class IntroEditor extends WebBrowserEditor {
    private static Logger logger = Logger.getLogger(IntroEditor.class);
    public static final String ID = "com.salesforce.ide.ui.editors.intro"; //$NON-NLS-1$
    private IntroEditorInput browserInput;
    protected Browser browser = null;

    public IntroEditor() {}

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        browser = webBrowser.getBrowser();
        if (browser != null) {
            browser.addLocationListener(new IntroLocationListener());

            browser.addProgressListener(new ProgressAdapter() {
                @Override
                public void completed(ProgressEvent event) {
                    executeScript();
                }
            });

            browser.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    event.doit = false;
                }
            });

            browser.addStatusTextListener(new StatusTextListener() {
                public void changed(StatusTextEvent event) {
                    String url = event.text;
                    if (url == null || url.length() == 0)
                        return;

                    url = IntroEditorURL.getStatusText(url);
                    if (url != null) {
                        event.text = url;
                        getActionBars().getStatusLineManager().setMessage(url);
                    }
                }
            });
        }

        UIUtils.setHelpContext(webBrowser, this.getClass().getSimpleName());
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        if (input instanceof IntroEditorInput) {
            browserInput = (IntroEditorInput) input;
            if (browserInput.getImage() != null) {
                Image oldImage = image;
                image = browserInput.getImage().createImage();
                setTitleImage(image);
                if (oldImage != null && !oldImage.isDisposed())
                    oldImage.dispose();
            }

        }
    }

    public void executeScript() {
        if (browserInput != null && browser != null) {
            boolean succeeded = browser.execute(browserInput.getScript());
            if (!succeeded) {
                logger.debug("Failed to load RSS"); //$NON-NLS-1$
            }
        }
    }

    public void setURL(String url) {
        if (browser != null)
            browser.setUrl(url);
    }

    private static class IntroLocationListener extends LocationAdapter {
        @Override
        public void changing(LocationEvent event) {
            String url = event.location;
            if (url == null)
                return;

            if (IntroEditorURL.parseUrl(url)) {
                event.doit = false;
            }
        }
    }

    public Browser getBrowser() {
        return browser;
    }
}
