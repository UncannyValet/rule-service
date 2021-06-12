package com.example.rules.spi.investigator;

import com.daxtechnologies.dynarecord.DynaRecord;
import com.daxtechnologies.dynarecord.DynaRecordClass;
import com.spirent.cem.rules.api.RulesException;
import com.spirent.cem.rules.api.RulesRequest;
import org.apache.commons.beanutils.ResultSetDynaClass;

import static com.spirent.cem.rules.api.ErrorNumbers.FACT_INSTANTIATION;

/**
 * An implementation of an Investigator, able to extract and insert DynaRecords populated from SQL results
 *
 * @param <R> the RulesRequest class associated with this processor
 * @param <F> the DynaRecord class to extract
 */
public abstract class SqlDynaRecordInvestigator<R extends RulesRequest, F extends DynaRecord<F>> extends SqlInvestigator<R, F> {

    @Override
    protected ResultConsumer processResults() {
        return rs -> {
            Class<F> factClass = getFactClass();
            ResultSetDynaClass rsc = new ResultSetDynaClass(rs);
            DynaRecordClass<F> dc = new DynaRecordClass<>(factClass, rsc.getDynaProperties());
            rsc.iterator().forEachRemaining(row -> {
                F fact;
                try {
                    fact = dc.copy(row);
                } catch (RuntimeException e) {
                    throw new RulesException(e, FACT_INSTANTIATION, factClass.getSimpleName());
                }
                insert(fact);
            });
        };
    }
}
