/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.internal.apex.completions;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.mockito.MockitoAnnotations;

import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * @author nchen
 * 
 */
public class CompletionsTestUtils {
    public static CompletionsTestUtils INSTANCE = new CompletionsTestUtils();

    private CompletionsTestUtils() {}

    public Completions createTestCompletions() throws Exception {
        MockitoAnnotations.initMocks(this);
        JAXBContext jc = JAXBContext.newInstance(Completions.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        InputStream stream = IdeTestUtil.getFullUrlEntry("/filemetadata/completions/completions.xml").openStream();
        return (Completions) unmarshaller.unmarshal(stream);
    }

}
