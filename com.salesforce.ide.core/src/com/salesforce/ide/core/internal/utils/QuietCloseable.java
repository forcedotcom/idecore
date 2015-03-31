/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.internal.utils;

public final class QuietCloseable<T extends AutoCloseable> implements AutoCloseable {
    private final T resource;

    public QuietCloseable(final T resource){
        this.resource = resource;
    }

    public T get() {
        return this.resource;
    }

    @Override
    public void close() {
        try {
            resource.close();
        } catch (final Exception e) {
            // suppress exception
        }
    }  

    public static final <U extends AutoCloseable> QuietCloseable<U> make(final U closable) {
        return new QuietCloseable<>(closable);
    }
}
