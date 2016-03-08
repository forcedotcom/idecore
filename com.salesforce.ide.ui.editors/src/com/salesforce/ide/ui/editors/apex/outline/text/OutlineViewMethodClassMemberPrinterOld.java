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

import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.FormalParameter;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.TypeRef;
import apex.jorje.services.printers.PrintContext;
import apex.jorje.services.printers.Printer;
import apex.jorje.services.printers.PrinterFactory;

/**
 * Print MethodDecls in the following form: methodName(variable1Type, variable2Type, ...) : returnType
 * 
 * @author nchen
 * 
 */
final class OutlineViewMethodClassMemberPrinterOld implements Printer<MethodMember> {
    private final Printer<Iterable<FormalParameter>> formalParametersPrinter;
    private final Printer<TypeRef> typeRefPrinter;
    private final Printer<Identifier> identifierPrinter;

    public OutlineViewMethodClassMemberPrinterOld(PrinterFactory astPrinterFactory,
            Printer<Iterable<FormalParameter>> formalParametersPrinter) {
        this.formalParametersPrinter = formalParametersPrinter;
        this.typeRefPrinter = astPrinterFactory.typeRefPrinter();
        this.identifierPrinter = astPrinterFactory.identifierPrinter();
    }

    @Override
    public String print(MethodMember x, PrintContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append(identifierPrinter.print(x.methodDecl.name, context) + "("
                + formalParametersPrinter.print(x.methodDecl.formalParameters.values, context) + ")");

        if (x.methodDecl.type.isPresent()) { // If we have a value
            sb.append(" : " + printTypeRef(x.methodDecl.type.get()));
        }

        return sb.toString();
    }

    private final TypeRef.MatchBlockWithDefault<String> optionalTypeMatchBlock =
            new TypeRef.MatchBlockWithDefault<String>() {

				@Override
				protected String _default(TypeRef x) {
					return typeRefPrinter.print(x, new PrintContext());
				}

            };

    private String printTypeRef(TypeRef type) {
        return type.match(optionalTypeMatchBlock);
    }
}
