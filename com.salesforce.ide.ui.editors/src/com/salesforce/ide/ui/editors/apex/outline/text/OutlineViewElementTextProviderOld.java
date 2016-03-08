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

import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;
import apex.jorje.services.printers.ListPrinter;
import apex.jorje.services.printers.PrintContext;
import apex.jorje.services.printers.Printer;
import apex.jorje.services.printers.PrinterFactory;
import apex.jorje.services.printers.PrinterUtil;

import com.salesforce.ide.ui.editors.apex.outline.ApexLabelProviderOld;
import com.salesforce.ide.ui.editors.apex.outline.IOutlineViewElementHandlerOld;

/**
 * Provides the text labeling for the outline view elements.
 * 
 * This class is shared among different outline views since all the functionality do not mutate state. Therefore, if you
 * modify this class, it is imperative that you preserve this invariant or else race conditions and data races can
 * occur. This is also why it has been marked final.
 * 
 * @author nchen
 * 
 */
public final class OutlineViewElementTextProviderOld implements IOutlineViewElementHandlerOld<String> {
    private static final PrinterFactory printerFactory = PrinterUtil.get().getFactory();
    private static Printer<Identifier> identifierPrinter = printerFactory.identifierPrinter();
    private static OutlineViewVariableDeclsPrinterOld variableDeclsPrinter = new OutlineViewVariableDeclsPrinterOld(
            printerFactory);
    private static OutlineViewFormalParameterPrinterOld formalParameterPrinter = new OutlineViewFormalParameterPrinterOld(
            printerFactory);
    private static OutlineViewMethodClassMemberPrinterOld methodMemberPrinter = new OutlineViewMethodClassMemberPrinterOld(
            printerFactory, ListPrinter.create(formalParameterPrinter, ", ", "", ""));
    private static OutlineViewPropertyClassMemberPrinterOld propertyMemberPrinter =
            new OutlineViewPropertyClassMemberPrinterOld(printerFactory);

    private static PrintContext defaultPrintContext() {
        return new PrintContext();
    }

    @Override
    public String handle(TriggerDeclUnit element) {
        return identifierPrinter.print(element.name, defaultPrintContext());
    }

    @Override
    public String handle(ClassDecl element) {
        return identifierPrinter.print(element.name, defaultPrintContext());
    }

    @Override
    public String handle(InterfaceDecl element) {
        return identifierPrinter.print(element.name, defaultPrintContext());
    }

    @Override
    public String handle(EnumDecl element) {
        return identifierPrinter.print(element.name, defaultPrintContext());
    }

    @Override
    public String handle(InnerClassMember element) {
        return identifierPrinter.print(element.body.name, defaultPrintContext());
    }

    @Override
    public String handle(InnerInterfaceMember element) {
        return identifierPrinter.print(element.body.name, defaultPrintContext());
    }

    @Override
    public String handle(InnerEnumMember element) {
        return identifierPrinter.print(element.body.name, defaultPrintContext());
    }

    @Override
    public String handle(StmntBlockMember element) {
        return ApexLabelProviderOld.ELLIPSIS_SUFFIX;
    }

    @Override
    public String handle(StaticStmntBlockMember element) {
        return ApexLabelProviderOld.ELLIPSIS_SUFFIX;
    }

    @Override
    public String handle(FieldMember element) {
        return variableDeclsPrinter.print(element.variableDecls, defaultPrintContext());
    }

    @Override
    public String handle(MethodMember element) {
        return methodMemberPrinter.print(element, defaultPrintContext());
    }

    @Override
    public String handle(PropertyMember element) {
        return propertyMemberPrinter.print(element, defaultPrintContext());
    }

    @Override
    public String handle(Identifier element) {
        return identifierPrinter.print(element, defaultPrintContext());
    }
}
