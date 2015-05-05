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

package com.salesforce.ide.core.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.base.Function;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.Messages;

/**
 * <p>
 * A light wrapper around the Job concurrency infrastructure in Eclipse to give us some niceties of promises, e.g.,
 * onSucess and onFailure.
 * </p>
 * <p>
 * In the cases where concurrency is not necessary, you can either do a job.join on the job or just invoke the run
 * method directly. This is useful for testing where we don't want to deal with asynchrony.
 * </p>
 * 
 * @author nchen
 * 
 */
public abstract class PromiseableJob<T> extends Job {
    // Provide default handler for onSuccess and onFailure when we don't care
    protected Function<T, IStatus> onSuccess = new Function<T, IStatus>() {
        @Override
        public IStatus apply(T arg0) {
            return Status.OK_STATUS;
        }
    };

    protected Function<Throwable, IStatus> onFailure = new Function<Throwable, IStatus>() {

        @Override
        public IStatus apply(Throwable throwable) {
            return new Status(Status.ERROR, ForceIdeCorePlugin.PLUGIN_ID,
                    Messages.PromiseableJob_GenericError, throwable);
        }
    };

    private T answer;

    public PromiseableJob(String name) {
        super(name);
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        try {
            setAnswer(execute(monitor));
            return getOnSuccess().apply(getAnswer());
        } catch (Throwable t) {
            return getOnFailure().apply(t);
        }
    }

    protected abstract T execute(IProgressMonitor monitor) throws Throwable;

    public Function<T, IStatus> getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(Function<T, IStatus> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public Function<Throwable, IStatus> getOnFailure() {
        return onFailure;
    }

    public void setOnFailure(Function<Throwable, IStatus> onFailure) {
        this.onFailure = onFailure;
    }

    // TODO: Maybe invoking getAnswer should schedule/block until the answer is available (just like Future does in Java)
    public T getAnswer() {
        return answer;
    }

    private void setAnswer(T answer) {
        this.answer = answer;
    }
}
