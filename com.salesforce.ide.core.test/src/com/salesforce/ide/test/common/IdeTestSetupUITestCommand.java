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
package com.salesforce.ide.test.common;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Command to setup and teardown stuff before and after a ui test. 
 * TODO: contains duplicate private methods. Revise
 * 
 * @author ssasalatti
 */
public class IdeTestSetupUITestCommand implements IdeTestCommand {

    private static final Logger logger = Logger.getLogger(IdeTestSetupUITestCommand.class);

    IdeSetupTest testConfig;

    public IdeTestSetupUITestCommand(IdeSetupTest testConfig) {
        this.testConfig = testConfig;
    }

    public void executeSetup() throws IdeTestException {
        logger.info("Setting up for ui test...");
        
        // check for error dialogs before the test. There shouldn't be any.
        Shell errorShell = doesErrorShellExist();
        if (IdeTestUtil.isNotEmpty(errorShell))
            IdeTestException.wrapAndThrowException("An unexpected error dialog appears to be present( Maybe from a previous test). This can be a problem while running a ui test. Quitting test.");

        logger.info("Setting up for ui test...DONE");
    }

    public void executeTearDown() throws IdeTestException {
        logger.info("Tearing down after ui test...");
        
        try {
			// check for error dialogs. There shouldn't be any if it wasn't expected.
			if (!testConfig.errorDialogExpectedDuringTest()) {
			    Shell errorShell = doesErrorShellExist();
			    
			    if (IdeTestUtil.isNotEmpty(errorShell))
			        IdeTestException.wrapAndThrowException("An unexpected error dialog popped during the test.");
			}
		} finally{
			// tear down all shells except the main workbench.
	        closeAllShellsExceptMainWorkbench();
		}
        
        logger.info("Tearing down after ui test...DONE");
    }

    /**
     * Checks if there's an error shell. if so returns the shell. 
     * 
     * TODO: Is there a better way to assert for an error dialog?
     * 
     * @return shell else null if shell wasn't found or there was no text on the shell.
     */
    private Shell doesErrorShellExist() {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        Object retShell = UIThreadRunnable.syncExec(display, new Result<Object>() {

            public Object run() {
                Shell retShell = null;
                Shell[] shells = display.getShells();
                for (Shell s : shells) {
                    if (IdeTestUtil.isNotEmpty(s.getText()) && s.getText().toLowerCase().contains("error")) {
                        retShell = s;
                        break;
                    }
                }

                return retShell;
            }

        });

        return (IdeTestUtil.isNotEmpty(retShell) && (retShell instanceof Shell)) ? (Shell) retShell : null;
    }

    /**
     * closes all the shells except the main workbench
     * @throws IdeTestException 
     */
    private void closeAllShellsExceptMainWorkbench() throws IdeTestException {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        // get rid of any visible wizards/dialogs except the workbench itself.
        display.syncExec(new Runnable() {

            public void run() {
                Shell[] shells = display.getShells();
                // using the old for style as need to start from the end
                // and work backwards
                // This is still very flaky since this relies on undocumented assumptions
                // I am going to close all shells that are not top-level shells (their parents are null)
                // I will leave top-level shells around. Why shells (plural)? Because there is not necessarily only one top shell in Eclipse 4.x it seems.
                for(Shell shell: shells){
                    if (shell.isDisposed())
                        continue;
                    if (null != shell.getParent()) { // Not a top-level shell, close it
                        shell.close();
                        shell.dispose();
                    }
                }
            }

        });
    }
}
