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

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import com.salesforce.ide.test.common.utils.IdeTestConstants;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.salesforce.ide.test.common.utils.PackageTypeEnum;

/**
 * This is the test setup command invoker. It processes the annotations and sets the right command to be executed i.e. -
 * org with data and proj - org without data and proj - org - nothing. The callee can then invoke execute setup or
 * teardown on this.
 * 
 * It contains a commandQueue that pretty much works like a fifo queue. However, there are methods to add commands in
 * specific places in the queue.The advantage of having a queue is that one can now add/remove as many setup and
 * teardown steps. You could either queue up multiple commands or create a special one( in instances where you need to
 * do more things between 2 commands) and queue that. the execution path for teardown is reverse of that used for setup.
 * 
 * @author ssasalatti
 * @see IdeTestSetupWithOrgWithMetaDataNoProjectCommand (Ties 2 commands together with more work between them)
 * @see IdeTestSetupLocalOrgCommand (Single command)
 * 
 */
public class IdeTestCommandInvoker {
    Queue<IdeTestCommand> commandQueue = new LinkedList<IdeTestCommand>();

    public IdeTestCommandInvoker(IdeSetupTest testConfig) throws IdeTestException {

        int orgBit = testConfig.needOrg() ? 4 : 0;
        int dataBit = testConfig.needMoreMetadataDataInOrg() ? 2 : 0;
        int projBit = testConfig.needProject() ? 1 : 0;

        int configBit = orgBit | dataBit | projBit;
        switch (configBit) {
        case (1):
            System.setProperty(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX, "ArbitraryVal");
            String preCannedProjectPath = testConfig.importProjectFromPath();
            if (IdeTestUtil.isNotEmpty(preCannedProjectPath)) {
                insertCommandIntoQueue(new IdeTestSetupImportProjectCommand(testConfig));
                break;
            }
			throw IdeTestException
			        .getWrappedException("TestSetupConfiguration not allowed:Cannot create project if you don't set needOrg=true");
        case (2):
            throw IdeTestException
                    .getWrappedException("TestSetupConfiguration not allowed:Cannot load data to org if you don't set needOrg=true");
        case (3):
            throw IdeTestException
                    .getWrappedException("TestSetupConfiguration not allowed:Cannot load data to org or create Project if you don't set needOrg=true");
        case (4): {
            // create org only.
            insertCommandIntoQueue(IdeTestOrgFactory.getTestSetupOrgCommand(testConfig));
            break;
        }
        case (5): {
            // create org and a project only. no metadata uploaded to org before
            // project create.
            insertCommandIntoQueue(IdeTestOrgFactory.getTestSetupOrgCommand(testConfig));
            insertCommandIntoQueue(new IdeTestSetupCreateProjectCommand(testConfig));
            break;
        }
        case (6): {
            // create org and add metadata
            insertOrgCreationCommandIntoQueue(testConfig);
            break;
        }
        case (7): {
            insertOrgCreationCommandIntoQueue(testConfig);
            insertCommandIntoQueue(new IdeTestSetupCreateProjectCommand(testConfig));
            break;
        }
        default:
            //dummy command.
            insertCommandIntoQueue(new IdeTestNoOrgNoDataNoProjectCommand());
        }
    }

    private void insertOrgCreationCommandIntoQueue(IdeSetupTest testConfig) {
        PackageTypeEnum addMetadataAsPkgType = testConfig.addMetadataDataAsPackage();
        if (addMetadataAsPkgType.equals(PackageTypeEnum.MANAGED_INSTALLED_PKG)
                || addMetadataAsPkgType.equals(PackageTypeEnum.UNMANAGED_INSTALLED_PKG)) {
            insertCommandIntoQueue(new IdeTestSetupWithOrgWithMetadataAsInstalledPkgNoProjectCommand(testConfig));
        } else {
            insertCommandIntoQueue(new IdeTestSetupWithOrgWithMetaDataNoProjectCommand(testConfig));
        }
    }

    public IdeTestCommandInvoker(IdeTestCommand command) {
        insertCommandIntoQueue(command);
    }

    /**
     * inserts a command into the queue only if it already hasn't been.follows the fifo rule. i.e inserts to the end of
     * the queue. if InsertAtBeginning is true, then command is ineserted at the beginning of the queue.
     * 
     * @param command
     * @param insertAtBeginning
     */
    public void insertCommandIntoQueue(IdeTestCommand command, boolean insertAtBeginning) {
        // insert only unique commands.
        if (!commandQueue.contains(command))
            if (insertAtBeginning)
                ((LinkedList<IdeTestCommand>) commandQueue).addFirst(command);
            else
                commandQueue.offer(command);
    }

    /**
     * inserts a command into the queue only if it already hasn't been.follows the fifo rule. i.e inserts to the end of
     * the queue.
     * 
     * @param command
     */
    public void insertCommandIntoQueue(IdeTestCommand command) {
        insertCommandIntoQueue(command, false);

    }

    /**
     * inserts a command into the queue only if it already hasn't been. inserts into a specific position. Use if you
     * want to override the queue insertion rules.
     * 
     * @param command
     */
    public void insertCommandIntoQueueAtPosition(IdeTestCommand command, int position) {
        // insert only unique commands.
        if (!commandQueue.contains(command))
            ((LinkedList<IdeTestCommand>) commandQueue).add(position, command);
    }

    /**
     * invoke the setup before the test.
     * 
     * @throws IdeTestException
     */
    public void invokeExecuteSetup() throws IdeTestException {
        IdeTestCommand[] tempArray = commandQueue.toArray(new IdeTestCommand[commandQueue.size()]);
        for (IdeTestCommand itc : tempArray) {
            itc.executeSetup();
        }
    }

    /**
     * invoke teardown after the test
     * 
     * @throws IdeTestException
     */
    public void invokeExecuteTeardown() throws IdeTestException {
        Collections.reverse((LinkedList<IdeTestCommand>) commandQueue);
        IdeTestCommand[] reverseArray = commandQueue.toArray(new IdeTestCommand[commandQueue.size()]);
        //do a graceful teardown. invoke teardown of all commands and collect exceptions in each.
        IdeTestExceptionCollector exceptionCollector = new IdeTestExceptionCollector();
        for (IdeTestCommand itc : reverseArray) {
            try {
                itc.executeTearDown();
            } catch (Exception e) {
                exceptionCollector.collectException(e);
            }
        }

        //if all was ok, the exception queue should be empty.
        if (!exceptionCollector.isExceptionQueueEmpty() && exceptionCollector.getExceptionQueueSize() > 1) {
			IdeTestException.wrapAndThrowException("Multiple Exceptions were encountered during Teardown.",
                exceptionCollector.getCollatedException());
		}
        if (!exceptionCollector.isExceptionQueueEmpty() && exceptionCollector.getExceptionQueueSize() == 1) {
            Throwable exception = exceptionCollector.getExceptionQueue().element();
            if (exception instanceof IdeTestException) {
				throw (IdeTestException) exception;
			}
			IdeTestException.getWrappedException(exception.getMessage(), exception);
        }

    }
}
