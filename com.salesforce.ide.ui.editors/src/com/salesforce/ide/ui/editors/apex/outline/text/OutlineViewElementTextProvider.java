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
package com.salesforce.ide.ui.editors.apex.outline.text;

import java.util.stream.Collectors;

import com.salesforce.ide.ui.editors.apex.outline.IOutlineViewElementHandler;

import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.member.Field;
import apex.jorje.semantic.ast.member.Method;
import apex.jorje.semantic.ast.member.Property;
import apex.jorje.semantic.bcl.AsmMethod;
import apex.jorje.semantic.symbol.member.method.MethodInfo;
import apex.jorje.semantic.symbol.member.variable.FieldInfo;
import apex.jorje.semantic.symbol.type.TypeInfo;
import apex.jorje.semantic.symbol.type.UnitType;

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
public final class OutlineViewElementTextProvider implements IOutlineViewElementHandler<String> {
    
    @Override
    public String handle(UserClass userClass) {
        return TypeInfoPrinter.print(userClass.getDefiningType());
    }
    
    @Override
    public String handle(UserInterface userInterface) {
        return TypeInfoPrinter.print(userInterface.getDefiningType());
    }
    
    @Override
    public String handle(UserTrigger userTrigger) {
        return TypeInfoPrinter.print(userTrigger.getDefiningType());
    }
    
    @Override
    public String handle(UserEnum userEnum) {
        return TypeInfoPrinter.print(userEnum.getDefiningType());
    }
    
    @Override
    public String handle(Method method) {
        return MethodInfoPrinter.print(method.getMethodInfo());
    }
    
    @Override
    public String handle(Property property) {
        return PropertyPrinter.print(property);
    }
    
    @Override
    public String handle(Field field) {
        return FieldPrinter.print(field);
    }
    
    // PRINTERS
    ///////////
    
    public static final class MethodInfoPrinter {
        public static String print(MethodInfo methodInfo) {
            StringBuilder sb = new StringBuilder();
            if (isImplicitInit(methodInfo)) {
                sb.append(TypeInfoPrinter.print(methodInfo.getDefiningType()));
                sb.append("(");
                sb.append(
                    methodInfo.getParameters().stream()
                    .map(p -> TypeInfoPrinter.print(p.getType()))
                    .collect(Collectors.joining(", ")));
                sb.append(")");
            } else {
                sb.append(methodInfo.getCanonicalName());
                sb.append("(");
                sb.append(
                    methodInfo.getParameters().stream()
                    .map(p -> TypeInfoPrinter.print(p.getType()))
                    .collect(Collectors.joining(", ")));
                sb.append(")");
                sb.append(" : ");
                sb.append(TypeInfoPrinter.print(methodInfo.getReturnType()));
            }
            return sb.toString();
        }
        
        static boolean isImplicitInit(MethodInfo methodInfo) {
            String canonicalName = methodInfo.getCanonicalName();
            return canonicalName.equals(AsmMethod.INIT);
        }
        
    }
    
    public static final class TypeInfoPrinter {
        public static String print(TypeInfo typeInfo) {
            String fullyQualifiedApexName = typeInfo.getApexName();
            String[] split = fullyQualifiedApexName.split("\\.");
            return split[split.length -1];
        }
    }
    
    public static final class FieldPrinter {
        public static String print(Field field) {
            StringBuilder sb = new StringBuilder();
            FieldInfo fieldInfo = field.getFieldInfo();
            if (fieldInfo.getDefiningType().getUnitType() == UnitType.ENUM) {
                sb.append(fieldInfo.getName());
            } else {
                sb.append(fieldInfo.getName());
                sb.append(" : ");
                sb.append(TypeInfoPrinter.print(fieldInfo.getType()));
            }
            return sb.toString();
        }
    }
    
    public static final class PropertyPrinter {
        public static String print(Property property) {
            StringBuilder sb = new StringBuilder();
            FieldInfo fieldInfo = property.getFieldInfo();
            sb.append(fieldInfo.getName());
            sb.append(" : ");
            sb.append(TypeInfoPrinter.print(fieldInfo.getType()));
            return sb.toString();
        }
    }
}
