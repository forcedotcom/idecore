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

import org.apache.log4j.Logger;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.google.common.annotations.VisibleForTesting;
import com.salesforce.ide.apex.core.tooling.systemcompletions.ApexSystemCompletionsRepository;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;

/**
 * @author nchen
 * 
 */
public abstract class ApexCompletionProcessor implements IContentAssistProcessor {
    protected static final Logger logger = Logger.getLogger(ApexCompletionProcessor.class);

    public static final Point APEX_ICON_SIZE = new Point(15, 15);

    protected StringBuilder errorCollector = new StringBuilder();

    protected ApexCompletionUtils utils;

    protected Completions completions;

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return errorCollector.toString();
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    public Image getImage() {
        return null;
    }

    @VisibleForTesting
    protected ApexCompletionUtils getUtil() {
        return utils != null ? utils : ApexCompletionUtils.INSTANCE;
    }

    @VisibleForTesting
    protected Completions getCompletions() {
        return completions != null ? completions : ApexSystemCompletionsRepository.INSTANCE.getCompletions();
    }
}
