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
package com.salesforce.ide.ui.editors.apex.outline.text;

import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.TypeRef;
import apex.jorje.services.printers.Printer;
import apex.jorje.services.printers.StandardPrinterFactory;

final class OutlineViewPropertyClassMemberPrinter implements Printer<PropertyMember> {

    private Printer<TypeRef> typeRefPrinter;
    private Printer<Identifier> identifierPrinter;

    public OutlineViewPropertyClassMemberPrinter(StandardPrinterFactory printerFactory) {
        this.typeRefPrinter = printerFactory.typeRefPrinter();
        this.identifierPrinter = printerFactory.identifierPrinter();
    }

    @Override
    public String print(PropertyMember x) {
        return identifierPrinter.print(x.propertyDecl.name) + " : " + typeRefPrinter.print(x.propertyDecl.type);
    }

}
