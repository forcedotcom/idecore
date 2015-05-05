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
package com.salesforce.ide.ui.editors.apex.preferences;

@SuppressWarnings({ "nls" })
public class PreferenceConstants {
    public final static String EDITOR_EVALUTE_TEMPORARY_PROBLEMS = "EDITOR_EVALUTE_TEMPORARY_PROBLEMS";

    /**
     * A named preference that controls whether bracket matching highlighting is turned on or off.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$

    /**
     * A named preference that holds the color used to highlight matching brackets.
     * <p>
     * Value is of type <code>String</code>. A RGB color value encoded as a string using class
     * <code>PreferenceConverter</code>
     * </p>
     * 
     * @see org.eclipse.jface.resource.StringConverter
     * @see org.eclipse.jface.preference.PreferenceConverter
     */
    public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$

    /**
     * A named preference that holds the java compiler version support.
     * <p>
     * Value is of type <code>String</code>.
     * </p>
     */
    public final static String COMPILER_SOURCE = "compilerSource"; //$NON-NLS-1$

    /**
     * A named preference that controls whether the 'close strings' feature is enabled.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String EDITOR_CLOSE_STRINGS = "closeStrings"; //$NON-NLS-1$

    /**
     * A named preference that controls whether the 'close brackets' feature is enabled.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String EDITOR_CLOSE_BRACKETS = "closeBrackets"; //$NON-NLS-1$

    /**
     * A named preference that controls whether the 'close braces' feature is enabled.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String EDITOR_CLOSE_BRACES = "closeBraces"; //$NON-NLS-1$

    /**
     * Controls whether we are going to parse with the new compiler. This affects the following:
     * <ul>
     * <li>Error Reporting</li>
     * <li>Outline View</li>
     * </ul>
     * We set this as a preference because the new compiler is still in beta mode so errors could happen.
     */
    public final static String EDITOR_PARSE_WITH_NEW_COMPILER = "parseWithNewCompiler"; //$NON-NLS-1$

    /**
     * Controls whether we are going to display auto-completion
     */
    public final static String EDITOR_AUTOCOMPLETION = "editorAutoCompletion"; //$NON-NLS-1$
}
