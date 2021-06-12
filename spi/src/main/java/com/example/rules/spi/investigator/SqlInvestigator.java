package com.example.rules.spi.investigator;

import com.daxtechnologies.annotation.AnnotationUtilities;
import com.daxtechnologies.database.*;
import com.daxtechnologies.database.annotation.DataSource;
import com.daxtechnologies.database.util.SqlBuilder;
import com.daxtechnologies.database.util.SqlUtilities;
import com.daxtechnologies.services.trace.Trace;
import com.daxtechnologies.util.StringUtils;
import com.spirent.cem.rules.api.RulesRequest;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for SQL-based Investigators
 * <br>Implementations should provide one of the following:
 * <li>A {@link RowMapper} to map {@link ResultSet} rows to the fact class</li>
 * <li>A {@link ResultConsumer} to act as a {@link RowCallbackHandler} and process rows one by one</li>
 * <li>A {@link ResultConsumer} to act as a {@link ResultSetExtractor} and process the entire result set</li>
 *
 * @param <R> the RulesRequest class to be processed
 * @param <F> the fact class that is provided
 */
public abstract class SqlInvestigator<R extends RulesRequest, F> extends AbstractInvestigator<R, F> {

    private static final String INITIAL_TRACE = "Initial Result";
    private static final String RESULT_EXTRACTION = "Result extraction";

    /**
     * Override in subclasses to specify the Schema that originates these facts
     * <br>Defaults to using the {@link DataSource} annotation on the fact if it is specified, or the configured OLAP schema if not
     *
     * @return a Schema
     */
    protected Schema getSchema() {
        DataSource annotation = AnnotationUtilities.getAnnotation(getFactClass(), DataSource.class);
        if (annotation != null) {
            String dataSourceId = annotation.value();
            if (StringUtils.isNotEmpty(dataSourceId)) {
                return DatabaseUtilities.getSchema(dataSourceId);
            }
            return DatabaseUtilities.getSchema(annotation.role());
        }
        return DatabaseUtilities.getSchema(Role.OLAP);
    }

    /**
     * Override in subclasses to provide a SqlBuilder to be used to retrieve query results
     * <br>Defaults to using a SQL file in the resources with a snake case version of the fact class's name
     *
     * @return a SqlBuilder
     */
    protected SqlBuilder getSql() {
        String fileName = getFactClass().getSimpleName().replaceAll("(?<=[a-z])(?=[A-Z])", "_").toLowerCase();
        return SqlUtilities.getSqlBuilder(getSchema(), fileName).prepared(true);
    }

    /**
     * Override in subclasses to set parameters in the SqlBuilder
     *
     * @param builder the SqlBuilder whose parameters are to be set
     */
    protected void setParameters(SqlBuilder builder) {
        // Set no parameters by default
    }

    @Override
    @SuppressWarnings("squid:S126")
    protected final void doGather() {
        SqlBuilder sql = Trace.doWithTask("Prepare SQL", () -> {
            SqlBuilder builder = getSql();
            setParameters(builder);
            return builder;
        });

        LOG.debug("Issuing the following SQL for " + getName() + ": " + sql.toString());

        AtomicBoolean initial = new AtomicBoolean(true);
        startTiming(INITIAL_TRACE);

        ResultConsumer extractor = processResults();
        ResultConsumer handler = processRow();
        RowMapper<F> mapper = mapRow();

        Template template = getSchema().getTemplate();
        if (extractor != null) {
            template.query(sql.build(), rs -> {
                initial.set(false);
                endTiming(INITIAL_TRACE);
                startTiming(RESULT_EXTRACTION);
                extractor.accept(rs);
                endTiming(RESULT_EXTRACTION);
                return null;
            }, sql.getArguments());
        } else if (handler != null || mapper != null) {
            template.query(sql.build(), rs -> {
                if (initial.getAndSet(false)) {
                    endTiming(INITIAL_TRACE);
                    startTiming(RESULT_EXTRACTION);
                }
                if (handler != null) {
                    handler.accept(rs);
                } else {
                    insert(mapper.mapRow(rs, rs.getRow()));
                }
            }, sql.getArguments());
            endTiming(RESULT_EXTRACTION);
        }

        if (initial.get()) {
            endTiming(INITIAL_TRACE);
        }
    }

    /**
     * Provides a consumer to process an entire ResultSet, iterating over the rows itself
     *
     * @return a consumer to process an entire ResultSet, or {@code null} to not use
     * @see org.springframework.jdbc.core.ResultSetExtractor
     */
    protected ResultConsumer processResults() {
        return null;
    }

    /**
     * Provides a consumer to process a single row of a ResultSet, with iteration of the rows happening externally
     *
     * @return a consumer to process a single row of a ResultSet, or {@code null} to not use
     * @see org.springframework.jdbc.core.RowCallbackHandler
     */
    protected ResultConsumer processRow() {
        return null;
    }

    /**
     * Provides a RowMapper to map a single row of a ResultSet to a fact object
     * <br>Note that non-null facts generated by this mapper are inserted into the rule session automatically
     *
     * @return a RowMapper to map a single row to a fact object, or {@code null} to not use
     * @see org.springframework.jdbc.core.RowMapper
     */
    protected RowMapper<F> mapRow() {
        return null;
    }

    /**
     * An interface to provide {@link java.util.function.Consumer} functionality with SQLExceptions allowed
     */
    protected interface ResultConsumer {

        /**
         * Performs this operation on the ResultSet.
         *
         * @param rs the ResultSet
         */
        void accept(ResultSet rs) throws SQLException;
    }
}
