/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.apex.outline;

import org.apache.log4j.Logger;

import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.member.Field;
import apex.jorje.semantic.ast.member.Method;
import apex.jorje.semantic.ast.member.Property;

/**
 * This class provides some form of type sanity for the outline view. The outline view in Eclipse is dispatched based on
 * Object as its type signature, forcing us to do instanceof tests to define behavior. This class contains all the
 * expected types and will dispatch to surrogate methods that must be implemented.
 * 
 * Ideally, I would like a visitor, but the Eclipse outline view, does not expose it as such, possibly, because we want
 * to be reactive and only dispatch when the user displays a part of the outline and not visit the whole tree at once.
 * 
 * @author nchen
 */
public class OutlineViewDispatcher<T> {
    private static final Logger logger = Logger.getLogger(OutlineViewDispatcher.class);
    
    private IOutlineViewElementHandler<T> handler;
    
    public OutlineViewDispatcher(IOutlineViewElementHandler<T> handler) {
        this.handler = handler;
    }
    
    public T dispatch(Object element) {
        if (element instanceof UserClass) {
            return handler.handle((UserClass) element);
        } else if (element instanceof UserInterface) {
            return handler.handle((UserInterface) element);
        } else if (element instanceof UserTrigger) {
            return handler.handle((UserTrigger) element);
        } else if (element instanceof UserEnum) {
            return handler.handle((UserEnum) element);
        } else if (element instanceof Method) {
            return handler.handle((Method) element);
        } else if (element instanceof Property) {
            return handler.handle((Property) element);
        } else if (element instanceof Field) {
            return handler.handle((Field) element);
        } else {
            return handleUnknownElementType(element);
        }
    }
    
    protected T handleUnknownElementType(Object element) {
        logger.debug("Encountered an unexpected element in the outline view: " + element);
        return null;
    }
}
