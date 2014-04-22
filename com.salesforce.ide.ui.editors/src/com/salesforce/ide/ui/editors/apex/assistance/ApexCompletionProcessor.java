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
package com.salesforce.ide.ui.editors.apex.assistance;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Example Java completion processor.
 */
public class ApexCompletionProcessor implements IContentAssistProcessor {
    //private static final Logger logger = Logger.getLogger(ApexCompletionProcessor.class);

    protected static ICompletionProposal[] currentList;
    private static String fileName = null;
    protected IProject project;
    private ContentAssistant assistant;
    
    /**
     * Simple content assist tip closer. The tip is valid in a range of 5 characters around its popup location.
     */
    protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

        protected int fInstallOffset;

        /*
         * @see IContextInformationValidator#isContextInformationValid(int)
         */
        public boolean isContextInformationValid(int offset) {
            return true; // Math.abs(fInstallOffset - offset) < 5;
        }

        /*
         * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
         */
        public void install(IContextInformation info, ITextViewer viewer, int offset) {
            fInstallOffset = offset;
        }

        /*
         * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int,
         *      TextPresentation)
         */
        public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
            return false;
        }
    }

    protected IContextInformationValidator contextValidator = new Validator();

    //  C O N S T R U C T O R S
    public ApexCompletionProcessor() {}

    //   M E T H O D S
    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public void setContentAssistant(ContentAssistant assistant) {
        this.assistant = assistant;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
    	return new ICompletionProposal[0];
    }
    
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, SWT.WRAP, presenter);
            }
        };
    }

    static final DefaultInformationControl.IInformationPresenter presenter =
            new DefaultInformationControl.IInformationPresenter() {
                public String updatePresentation(Display display, String infoText, TextPresentation presentation,
                        int maxWidth, int maxHeight) {
                    int start = -1;

                    // Loop over all characters of information text
                    for (int i = 0; i < infoText.length(); i++) {
                        switch (infoText.charAt(i)) {
                        case '<':
                            // Remember start of tag
                            start = i;
                            break;
                        case '>':
                            if (start >= 0) {
                                // We have found a tag and create a new style range
                                StyleRange range = new StyleRange(start, i - start + 1, null, null, SWT.BOLD);
                                // Add this style range to the presentation
                                presentation.addStyleRange(range);
                                // Reset tag start indicator
                                start = -1;
                            }
                            break;
                        }
                    }

                    // Return the information text

                    return infoText;
                }
            };

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
        /*
         * IContextInformation[] result= new IContextInformation[5]; for (int i= 0; i < result.length; i++) result[i]=
         * new ContextInformation(
         * MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"), new
         * Object[] { Integer.valueOf(i), Integer.valueOf(documentOffset) }), //$NON-NLS-1$
         * MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"), new
         * Object[] { Integer.valueOf(i), Integer.valueOf(documentOffset - 5), Integer.valueOf(documentOffset + 5)})); //$NON-NLS-1$
         */
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '.', '(' };
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return new char[] { '#' };
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformationValidator getContextInformationValidator() {
        return contextValidator;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public String getErrorMessage() {
        return null;
    }

    /**
     * Select the word at the current selection location. Return <code>true</code> if successful, <code>false</code>
     * otherwise.
     * 
     * @return <code>true</code> if a word can be found at the current selection location, <code>false</code> otherwise
     */
    protected String matchWord(IDocument doc, int offset) {
        try {
            int pos = offset - 2;
            char c;
            while (pos >= 0) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                --pos;
            }

            int fStartPos = ++pos;
            pos = offset - 1;

            // situation when looking for subkeyword in which case we don't need to remove tailing . as looking for keyword, so ++ position
            if (".".equals(doc.get(fStartPos - 1, 1))) {
                pos++;
            }

            String returnValue = doc.get(fStartPos, pos - fStartPos);
            return returnValue;

        } catch (BadLocationException x) {}

        return "";
    }

    public void clearState() {
        currentList = null;
    }

    public static IPartListener2 getPartListener() {
        return new IPartListener2() {
            public void partActivated(IWorkbenchPartReference partRef) {
                // TODO: export string
                if (partRef.getId().equals("com.salesforce.ide.ui.editors.apex")) {
                    ApexCompletionProcessor.setFileName(partRef.getPartName());
                }
            }

            public void partBroughtToTop(IWorkbenchPartReference partRef) {}

            public void partClosed(IWorkbenchPartReference partRef) {}

            public void partDeactivated(IWorkbenchPartReference partRef) {}

            public void partHidden(IWorkbenchPartReference partRef) {}

            public void partInputChanged(IWorkbenchPartReference partRef) {}

            public void partOpened(IWorkbenchPartReference partRef) {}

            public void partVisible(IWorkbenchPartReference partRef) {}

        };
    }

    protected static void setFileName(String fileName) {
        ApexCompletionProcessor.fileName = fileName;
    }

}
