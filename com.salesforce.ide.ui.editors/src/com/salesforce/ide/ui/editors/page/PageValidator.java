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
package com.salesforce.ide.ui.editors.page;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.wst.html.internal.validation.HTMLSourceValidator;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

@SuppressWarnings( { "restriction", "unchecked" })
public class PageValidator implements ISourceValidator, IValidator {
	HTMLSourceValidator validator = new HTMLSourceValidator();    
	
	public HTMLSourceValidator getValidator() {
		return validator;
	}

	@Override
    public void cleanup(IReporter reporter) {
		validator.cleanup(reporter);
	}

	@Override
    public void validate(IValidationContext helper, IReporter reporter)
			throws ValidationException {
		validator.validate(helper, reporter);
		removeApexValidationErrors(reporter);
	}
	
	@Override
    public void validate(IRegion dirtyRegion, IValidationContext helper,
			IReporter reporter) {
		validator.validate(dirtyRegion, helper, reporter);
		removeApexValidationErrors(reporter);
	}

	@Override
    public void connect(IDocument document) {
		validator.connect(document);
	}

	@Override
    public void disconnect(IDocument document) {
		validator.disconnect(document);
	}

	/**
	 * Remove all the apex: and c: validation error messages
	 * @param reporter
	 */
    void removeApexValidationErrors(IReporter reporter) {
        
        final List<IMessage> messages = reporter.getMessages();
		final List<IMessage> copy = new ArrayList<>(messages);

		for (Object o : messages) {
			final IMessage msg = (IMessage) o;
			if ((msg.getText().contains("apex:"))||(msg.getText().contains("c:"))) {
				copy.remove(o); 
			}
		}

		reporter.removeAllMessages(validator);

		for (Object o : copy) {
			reporter.addMessage(validator, (IMessage) o);
		}
    }
}
