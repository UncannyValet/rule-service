package com.example.rules.spi.investigator;

import com.daxtechnologies.database.orm.activerecord.ActiveRecord;
import com.daxtechnologies.database.orm.activerecord.ActiveRecords;
import com.daxtechnologies.record.filter.Expression;
import com.daxtechnologies.record.filter.ExpressionFactory;
import com.spirent.cem.rules.api.RulesException;
import com.spirent.cem.rules.api.RulesRequest;

import static com.spirent.cem.rules.api.ErrorNumbers.INVALID_FACT_TYPE;

/**
 * An implementation of an Investigator, able to extract and insert ActiveRecords
 *
 * @param <R> the RulesRequest class associated with this processor
 * @param <F> the ActiveRecord class to extract
 */
public abstract class ActiveRecordInvestigator<R extends RulesRequest, F extends ActiveRecord<F>> extends AbstractInvestigator<R, F> {

    @Override
    public void doInitialize(Object... objects) {
        super.doInitialize(objects);

        // Verify that fact class is an active record
        if (!ActiveRecord.class.isAssignableFrom(getFactClass())) {
            throw new RulesException(INVALID_FACT_TYPE, getFactClass().getSimpleName(), getClass().getSimpleName() + " requires fact to be an ActiveRecord");
        }
    }

    /**
     * Override in subclasses to provide an Expression for filtering the records
     *
     * @return an Expression used to filter the records
     */
    protected Expression getExpression() {
        // Default includes all records
        return ExpressionFactory.newEmptyExpression();
    }

    @Override
    protected final void doGather() {
        ActiveRecords.where(getFactClass(), getExpression()).forEach(this::insert);
    }
}
