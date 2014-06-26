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

import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.TypeRef;
import apex.jorje.data.ast.VariableDecls;
import apex.jorje.services.printers.PrintContext;
import apex.jorje.services.printers.Printer;
import apex.jorje.services.printers.PrinterFactory;

/**
 * Print VariableDecls in the following form: nameOfVariable : typeOfVariable
 * 
 * @author nchen
 */
final class OutlineViewVariableDeclsPrinter implements Printer<VariableDecls> {
    private final Printer<TypeRef> typeRefPrinter;
    private final Printer<Identifier> identifierPrinter;

    public OutlineViewVariableDeclsPrinter(PrinterFactory astPrinterFactory) {
        this.typeRefPrinter = astPrinterFactory.typeRefPrinter();
        this.identifierPrinter = astPrinterFactory.identifierPrinter();
    }

    @Override
    public String print(VariableDecls x, PrintContext context) {
        // This is safe because we ensure that we only have one decl per element
        // @see com.salesforce.ide.ui.editors.apex.outline.BlockMemberFilter
        return identifierPrinter.print(x.decls.get(0).name, context) + " : " + typeRefPrinter.print(x.type, context);
    }
}
