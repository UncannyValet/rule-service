package com.example.rules.spi.investigator;

import com.daxtechnologies.util.ObjectUtilities;
import com.spirent.cem.rules.spi.Context;

/**
 * <p>This class provides a skeletal implementation of the {@link FactFunction}
 * interface, to minimize the effort required to implement this interface</p>
 */
public abstract class AbstractFactFunction<F> implements FactFunction<F> {

    private Context context;

    protected FactFunction<F> next;

    @Override
    public final void initialize(Object... objects) {
        context = ObjectUtilities.findInstance(objects, Context.class);

        doInitialize();
    }

    protected void doInitialize() {
    }

    @Override
    public void release() {
    }

    protected final Context getContext() {
        return context;
    }

    @Override
    public void andThen(FactFunction<F> next) {
        this.next = next;
    }
}
