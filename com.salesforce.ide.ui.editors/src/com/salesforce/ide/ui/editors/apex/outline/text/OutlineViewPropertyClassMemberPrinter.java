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
import apex.jorje.services.printers.PrintContext;
import apex.jorje.services.printers.Printer;
import apex.jorje.services.printers.PrinterFactory;

final class OutlineViewPropertyClassMemberPrinter implements Printer<PropertyMember> {

    private final Printer<TypeRef> typeRefPrinter;
    private final Printer<Identifier> identifierPrinter;

    public OutlineViewPropertyClassMemberPrinter(PrinterFactory printerfactory) {
        this.typeRefPrinter = printerfactory.typeRefPrinter();
        this.identifierPrinter = printerfactory.identifierPrinter();
    }

    @Override
    public String print(PropertyMember x, PrintContext context) {
        return identifierPrinter.print(x.propertyDecl.name, context) + " : "
                + typeRefPrinter.print(x.propertyDecl.type, context);
    }

}
