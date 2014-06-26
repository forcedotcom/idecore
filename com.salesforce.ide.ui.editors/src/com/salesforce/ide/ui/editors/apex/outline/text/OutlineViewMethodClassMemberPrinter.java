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

import apex.jorje.data.Optional;
import apex.jorje.data.Optional.None;
import apex.jorje.data.Optional.Some;
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
final class OutlineViewMethodClassMemberPrinter implements Printer<MethodMember> {
    private final Printer<Iterable<FormalParameter>> formalParametersPrinter;
    private final Printer<TypeRef> typeRefPrinter;
    private final Printer<Identifier> identifierPrinter;

    public OutlineViewMethodClassMemberPrinter(PrinterFactory astPrinterFactory,
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

        if (x.methodDecl.type instanceof Optional.Some) { // If we have a value
            sb.append(" : " + printTypeRef(x.methodDecl.type));
        }

        return sb.toString();
    }

    private final Optional.MatchBlock<TypeRef, String> optionalTypeMatchBlock =
            new Optional.MatchBlock<TypeRef, String>() {

                @Override
                public String _case(Some<TypeRef> x) {
                    return typeRefPrinter.print(x.value, new PrintContext());
                }

                @Override
                public String _case(None<TypeRef> x) {
                    return "";
                }
            };

    private String printTypeRef(Optional<TypeRef> type) {
        return type.match(optionalTypeMatchBlock);
    }
}
