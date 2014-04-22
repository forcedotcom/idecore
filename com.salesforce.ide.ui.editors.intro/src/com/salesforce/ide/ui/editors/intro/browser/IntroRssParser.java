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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.eclipse.help.internal.util.URLCoder;
import org.w3c.dom.Document;

@SuppressWarnings( { "restriction", "nls" })
public class IntroRssParser {
    public static final String MORE_LINK = "MORE_LINK";
    public static final String MORE_TEXT = "MORE_TEXT";

    public static class RssItem {
        public String title;
        public String link;
        public String description;
    }

    public static List<RssItem> parse(Document doc, int itemCount) throws Exception {
        List<RssItem> list = new ArrayList<RssItem>();

        XPathFactory factory = XPathFactory.newInstance();
        XPath xp = factory.newXPath();

        for (int i = 1; i < itemCount + 1; i++) {
            RssItem item = new RssItem();
            item.title = xp.evaluate("/rss/channel/item[" + i + "]/title", doc);
            item.link = xp.evaluate("/rss/channel/item[" + i + "]/link", doc);
            item.description = xp.evaluate("/rss/channel/item[" + i + "]/description", doc);
            list.add(item);
        }

        return list;
    }

    public static String getRssHtml(Document doc, int itemCount, boolean addMoreLink) {
        StringBuilder content = new StringBuilder();
        try {
            List<RssItem> items = parse(doc, itemCount);

            content.append("<ul>");
            for (RssItem item : items) {
                content.append("<li>");
                content.append("<a href=\"http://org.eclipse.ui.intro/openBrowser?url=" + URLCoder.encode(item.link)
                        + "\">" + stringToHTMLString(item.title) + "</a>");
                content.append("<br/>");
                content.append("<p>");
                content.append(stringToHTMLString(item.description));
                content.append("</p>");
                content.append("</li>");
            }

            if (addMoreLink) {
                content.append("<li>");
                content.append("<a href=\"" + MORE_LINK + "\">" + MORE_TEXT + "</a>");
                content.append("</li>");
            }
            content.append("</ul>");
        } catch (Exception e) {

        }
        return content.toString();
    }

    public static String stringToHTMLString(String string) {
        StringBuffer sb = new StringBuffer(string.length());
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++) {
            c = string.charAt(i);
            if (c == ' ') {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss 
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                } else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            } else {
                lastWasBlankChar = false;
                //
                // HTML Special Chars
                if (c == '"')
                    sb.append("&quot;");
                else if (c == '\'')
                    sb.append("&#39;");
                else if (c == '&')
                    sb.append("&amp;");
                else if (c == '<')
                    sb.append("&lt;");
                else if (c == '>')
                    sb.append("&gt;");
                else if (c == '\n')
                    // Handle Newline
                	sb.append("<br/>");
                else {
                    int ci = 0xffff & c;
                    if (ci < 160)
                        // nothing special only 7 Bit
                        sb.append(c);
                    else {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(Integer.valueOf(ci).toString());
                        sb.append(';');
                    }
                }
            }
        }
        return sb.toString();
    }

}
