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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.salesforce.ide.ui.editors.intro.IntroPlugin;
import com.salesforce.ide.ui.editors.intro.Messages;
import com.salesforce.ide.ui.internal.ForceImages;

@SuppressWarnings( { "restriction" })
public class IntroEditorInput extends WebBrowserEditorInput {
    private static Logger logger = Logger.getLogger(IntroEditorInput.class);
    private ImageDescriptor image;

    private static final int NUMBER_INTRO_RSS_ITEMS = 4;
    private static final int NUMBER_INTRO_SAMPLE_PROJECTS = 1;

    public IntroEditorInput(int style) {
        this(null, style);
    }

    public IntroEditorInput(URL resolved) {
        this(resolved, 0);
    }

    public IntroEditorInput(URL resolved, int style) {
        super(resolved, style);
        this.setName(Messages.IntroEditor_name);
        this.setToolTipText(Messages.IntroEditor_tooltip);

        image = ForceImages.getDesc(ForceImages.APEX_TITLE_IMAGE);
    }

    public ImageDescriptor getImage() {
        return image;
    }

    public void setImage(ImageDescriptor image) {
        this.image = image;
    }

    public String getScript() {
        StringBuilder script = new StringBuilder();
        try {
            script
                    .append(getScript(
                        "SampleProjectSection", new URL(IntroRssMessages.IntroSampleProjects_url), getSampleProjectCacheUrl(), NUMBER_INTRO_SAMPLE_PROJECTS, IntroRssMessages.IntroSampleProjects_moreProjects_url, IntroRssMessages.IntroSampleProjects_moreProjects_label)); //$NON-NLS-1$
            script
                    .append(getScript(
                        "RssSection", new URL(IntroRssMessages.IntroRss_url), getRssCacheUrl(), NUMBER_INTRO_RSS_ITEMS, IntroRssMessages.IntroRss_morePosts_url, IntroRssMessages.IntroRss_morePosts_label)); //$NON-NLS-1$
        } catch (Exception e) {
            logger.warn(e);
        }

        return script.toString();
    }

    private String getScript(String id, URL rssUrl, URL cacheUrl, int numItems, String moreLink, String moreText) {
        StringBuilder script = new StringBuilder();
        script.append("document.getElementById('"); //$NON-NLS-1$
        script.append(id);
        script.append("').innerHTML = '"); //$NON-NLS-1$

        String rssHtml = getRssHtml(rssUrl, cacheUrl, numItems);

        rssHtml = rssHtml.replace(IntroRssParser.MORE_LINK, moreLink);
        rssHtml = rssHtml.replace(IntroRssParser.MORE_TEXT, moreText);

        script.append(rssHtml);
        script.append("';"); //$NON-NLS-1$
        return script.toString();
    }

    private String getRssHtml(URL rssUrl, URL cacheUrl, int numItems) {
        String content = ""; //$NON-NLS-1$

        try {
            Document doc = getDocument(rssUrl, cacheUrl);
            content = IntroRssParser.getRssHtml(doc, numItems, true);

            if (content != null && content.length() > 0) {
                updateCache(cacheUrl, doc);
            }
        } catch (Exception e) {
            logger.warn(e);
        }

        return content;
    }

    private Document getDocument(URL rssUrl, URL cacheUrl) {
        Document doc = loadDocument(rssUrl);

        if (doc == null) {
            doc = loadDocument(cacheUrl);
        }

        return doc;
    }

    private Document loadDocument(URL rssUrl) {
        Document doc = null;
        DOMParser parser = new DOMParser();
        try {
            InputSource inputSource = new InputSource(rssUrl.openStream());
            parser.parse(inputSource);
            doc = parser.getDocument();
        } catch (Exception e) {
            logger.warn(e);
        }

        return doc;
    }

    private void updateCache(URL cacheUrl, Document doc) {
        try {
            Bundle bundle = IntroPlugin.getDefault().getBundle();
            IPath state = Platform.getStateLocation(bundle);
            IPath path = new Path(cacheUrl.getPath());

            File f = new File(state.toFile(), path.lastSegment());
            f.createNewFile();

            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer xform = tfactory.newTransformer();
            Source src = new DOMSource(doc);

            FileOutputStream stream = new FileOutputStream(f, false);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8"); //$NON-NLS-1$
            Result result = new StreamResult(writer);
            xform.transform(src, result);
            writer.close();

        } catch (Exception e) {
            logger.warn("Unable to save rss feed to cache", e); //$NON-NLS-1$
        }
    }

    private URL getRssCacheUrl() {
        URL url = null;
        try {
            Bundle bundle = IntroPlugin.getDefault().getBundle();

            IPath state = Platform.getStateLocation(bundle);
            File f = new File(state.toFile(), "RssCache.xml"); //$NON-NLS-1$
            if (f.exists()) {
                return f.toURL();
            }

            url = FileLocator.find(bundle, new Path("content/RssCache.xml"), null); //$NON-NLS-1$
            url = FileLocator.toFileURL(url);
        } catch (Exception e) {
            logger.warn("Unable to find RSS cache", e); //$NON-NLS-1$
        }
        return url;
    }

    private URL getSampleProjectCacheUrl() {
        URL url = null;
        try {
            Bundle bundle = IntroPlugin.getDefault().getBundle();

            IPath state = Platform.getStateLocation(bundle);
            File f = new File(state.toFile(), "SpCache.xml"); //$NON-NLS-1$
            if (f.exists()) {
                return f.toURL();
            }

            url = FileLocator.find(bundle, new Path("content/SpCache.xml"), null); //$NON-NLS-1$
            url = FileLocator.toFileURL(url);
        } catch (Exception e) {
            logger.warn("Unable to find Sample Project cache", e); //$NON-NLS-1$
        }
        return url;
    }
}
