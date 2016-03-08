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

import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.member.Field;
import apex.jorje.semantic.ast.member.Method;
import apex.jorje.semantic.ast.member.Property;

/**
 * The handler that is called and passed the opportunity to act on each element in the outline view.
 * 
 * @author nchen
 *         
 * @param <T>
 *            The return type for handling each type of element
 */
public interface IOutlineViewElementHandler<T> {
    
    T handle(UserClass userClass);
    
    T handle(UserInterface userInterface);
    
    T handle(UserTrigger userTrigger);
    
    T handle(UserEnum userEnum);
    
    T handle(Method method);
    
    T handle(Property property);
    
    T handle(Field field);
}
