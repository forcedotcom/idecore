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

import java.util.LinkedList;
import java.util.Queue;

import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Can be used to collect IdeTestExceptions. 
 * Specifically used in Teardown when the framework tries to gracefully teardown and multiple exceptions can occur at multiple points.
 * @see IdeTestCommandInvoker 
 * @author ssasalatti
 */
public class IdeTestExceptionCollector {
    Queue<Throwable> exceptionQueue = new LinkedList<Throwable>();

	public Queue<Throwable> getExceptionQueue() {
		return exceptionQueue;
	}
    
	/**
	 * adds the exception to the exception queue that can be used later.
	 * @param e
	 */
	public void collectException(Throwable e){
		exceptionQueue.offer(e);
	}
	
	/**
	 * @return true if the exception queue is empty
	 */
	public boolean isExceptionQueueEmpty(){
		return exceptionQueue.isEmpty();
	}
	
	/**
	 * @return the size of the exception queue
	 */
	public int getExceptionQueueSize(){
		return exceptionQueue.size();
	}
	
	/**
	 * Collates all the messages from the exceptions in the order of occurance and returns a single message
	 * @return
	 */
	public String getCollatedExceptionMessages(){
		if(IdeTestUtil.isEmpty(exceptionQueue))
			return null;
		StringBuffer buff = new StringBuffer();
        Throwable[] tempArray = exceptionQueue.toArray(new Throwable[exceptionQueue.size()]);
		for(int i=0;i<tempArray.length;i++){
			buff.append("\n\nException :").append(i).append(":").append(tempArray[i].getMessage());
			buff.append("\nLocation :");
			StackTraceElement[] stackTraceElements = tempArray[i].getStackTrace();
			if(IdeTestUtil.isNotEmpty(stackTraceElements)){
				StackTraceElement stackTraceElement = stackTraceElements[0]; //add the location which is at the top of the stack.
				buff.append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName()).append("():Line Number:").append(stackTraceElement.getLineNumber());
			}else
				buff.append("could not determine location");
			
		}
		return buff.toString();
	}
	
	/**
	 * @return a new exception with the message as the collated message from all the exceptions.
	 */
	public Throwable getCollatedException(){
		return new Throwable(getCollatedExceptionMessages());
	}
	
	
}
