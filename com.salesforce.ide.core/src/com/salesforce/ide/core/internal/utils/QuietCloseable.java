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
