package com.example.rules.spi.investigator;

import com.daxtechnologies.database.orm.DatabaseRecordUtilities;
import com.daxtechnologies.database.orm.DetachedDatabaseRecord;
import com.daxtechnologies.record.MappedRecord;
import com.spirent.cem.rules.api.RulesRequest;
import org.springframework.jdbc.core.RowMapper;

/**
 * An implementation of an Investigator, able to extract, wrap and insert MappedRecords populated from SQL results
 *
 * @param <R> the RulesRequest class associated with this processor
 * @param <F> the fact class to extract
 */
public abstract class SqlMappedInvestigator<R extends RulesRequest, F> extends SqlInvestigator<R, F> {

    @Override
    protected RowMapper<F> mapRow() {
        RowMapper<DetachedDatabaseRecord> mapper = DatabaseRecordUtilities.getRowMapper(getSchema());
        return (rs, i) -> wrap(mapper.mapRow(rs, i));
    }

    /**
     * Wrap a MappedRecord extracted by a query
     *
     * @param record the MappedRecord from the database query
     * @return a MappedRecord wrapped into a Fact class usable by rules
     */
    protected abstract <T extends MappedRecord<T, Object>> F wrap(T record);
}
