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
package com.salesforce.ide.core.internal.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 *
 * Test context object: includes test context enum and corresponding runnables for specific test context.
 *
 * @author fchang
 */
public class TestContext {
    private static final Logger logger = Logger.getLogger(TestContext.class);

    public enum TestContextEnum {
        NONE, SAVE_TO_SERVER, DEPLOY_TO_SERVER
    };

    private static Runnable closeDialogRunnables = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted while waiting to close dialog", e);
            }
            // getting all the window shells
            Shell shells[] = Display.getDefault().getShells();
            // find the message dialog and close it.
            for (Shell shell : shells) {
                if (shell.getData() instanceof Dialog) {
                    shell.close();
                }
            }

        }
    };

    private static Runnable yesToQestionDialogRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted while waiting to close dialog", e);
            }

            // getting all the window shells
            Shell shells[] = Display.getDefault().getShells();

            // find the message dialog and close it.
            for (Shell shell : shells) {
                if (shell.getData() instanceof MessageDialog) {
                    Button defaultButton = shell.getDefaultButton(); //default button is Yes
                    Event e = new Event();
                    e.type = SWT.Selection;
                    e.display = defaultButton.getDisplay();
                    defaultButton.notifyListeners(SWT.Selection, e);
                }
            }

        }
    };

    private final TestContextEnum testContextEnum;
    private final Runnable[] ayncRunnables;

    private TestContext(TestContextEnum testContextEnum, Runnable[] ayncRunnables) {
        this.testContextEnum = testContextEnum;
        this.ayncRunnables = ayncRunnables;
    }

    // Add additional runnables to Runnable[] to represent/test different result/flow.
    // For example, in deploy to server flow, i want to "say yes to first question dialog" then "say no to second question dialog" then "close the dialog"
    public final static TestContext DEPLOY_TO_SERVER =
            new TestContext(TestContextEnum.DEPLOY_TO_SERVER, new Runnable[] { closeDialogRunnables });
    public final static TestContext SAVE_TO_SERVER =
            new TestContext(TestContextEnum.SAVE_TO_SERVER, new Runnable[] { yesToQestionDialogRunnable });
    public final static TestContext NONE = new TestContext(TestContextEnum.NONE, null);

    private static Map<TestContextEnum, TestContext> testCtxMap = new HashMap<>();
    static {
        testCtxMap.put(TestContextEnum.DEPLOY_TO_SERVER, DEPLOY_TO_SERVER);
        testCtxMap.put(TestContextEnum.SAVE_TO_SERVER, SAVE_TO_SERVER);
        testCtxMap.put(TestContextEnum.NONE, NONE);
    }

    public static TestContext getTestContextBy(TestContextEnum testContextEnum) {
        return testCtxMap.get(testContextEnum);
    }

    public TestContextEnum getTestContextEnum() {
        return this.testContextEnum;
    }

    public void execAsyncRunnables() {
        if (Utils.isEmpty(this.ayncRunnables))
            return;
        for (Runnable ayncRunnable : this.ayncRunnables) {
            Display.getDefault().asyncExec(ayncRunnable);
        }
    }

}
