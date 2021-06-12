package com.example.rules.spi.investigator;

import com.daxtechnologies.annotation.AnnotationUtilities;
import com.daxtechnologies.database.annotation.Column;
import com.daxtechnologies.database.annotation.PrimaryKey;
import com.daxtechnologies.database.orm.activerecord.ActiveRecord;
import com.daxtechnologies.util.ClassUtils;
import com.spirent.cem.rules.api.RulesException;
import com.spirent.cem.rules.api.RulesRequest;
import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static com.spirent.cem.rules.api.ErrorNumbers.*;

/**
 * An implementation of an Investigator that extracts Facts from the rows returned by a SQL query
 *
 * @param <R> the RulesRequest class associated with this processor
 * @param <F> the Fact to insert, with fields populated from the query annotated with @Column
 */
public abstract class SqlBeanInvestigator<R extends RulesRequest, F> extends SqlInvestigator<R, F> {

    private static final Map<Class<?>, ResultExtractor> extractors = new HashMap<>();

    private final Map<String, Field> fieldMap = new HashMap<>();
    private final Map<String, ActiveRecordExtractor> activeRecordExtractors = new HashMap<>();
    private final FieldExtractor adapter = new FieldExtractor();

    static {
        extractors.put(String.class, ResultSet::getString);
        extractors.put(Integer.class, ResultSet::getInt);
        extractors.put(int.class, ResultSet::getInt);
        extractors.put(Long.class, ResultSet::getLong);
        extractors.put(long.class, ResultSet::getLong);
        extractors.put(Float.class, ResultSet::getFloat);
        extractors.put(float.class, ResultSet::getFloat);
        extractors.put(Double.class, ResultSet::getDouble);
        extractors.put(double.class, ResultSet::getDouble);
        extractors.put(Boolean.class, ResultSet::getBoolean);
        extractors.put(boolean.class, ResultSet::getBoolean);
        extractors.put(Timestamp.class, ResultSet::getTimestamp);
        extractors.put(DateTime.class, (rs, column) -> rs.getTimestamp(column) != null ? new DateTime(rs.getTimestamp(column)) : null);
    }

    @Override
    public void doInitialize(Object... objects) {
        super.doInitialize(objects);

        // Map @Column annotated fields to their column name and record class if present
        AnnotationUtilities.getAnnotatedFields(getFactClass(), Column.class).forEach(field -> {
            String columnName = field.getAnnotation(Column.class).name();
            fieldMap.put(columnName, field);
            Class<?> fieldClass = field.getType();
            if (ActiveRecord.class.isAssignableFrom(fieldClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends ActiveRecord<?>> recordClass = (Class<? extends ActiveRecord<?>>)fieldClass;
                activeRecordExtractors.put(columnName, new ActiveRecordExtractor(recordClass));
            }
        });
    }

    @Override
    protected RowMapper<F> mapRow() {
        return (rs, i) -> {
            F fact;
            try {
                fact = ClassUtils.newInstance(getFactClass());
            } catch (ClassUtils.UninstantiableClassException e) {
                throw new RulesException(e, FACT_INSTANTIATION, getFactClass().getSimpleName());
            }
            fieldMap.forEach((column, field) -> adapter.extract(rs, fact, column, field));
            return fact;
        };
    }

    private interface ResultExtractor {
        Object extract(ResultSet rs, String column) throws SQLException;
    }

    private class FieldExtractor {
        void extract(ResultSet rs, F fact, String column, Field field) {
            try {
                Class<?> type = field.getType();
                ActiveRecordExtractor activeRecordExtractor = activeRecordExtractors.get(column);
                Object object;
                if (activeRecordExtractor != null) {
                    // Special treatment for active record fields - resolve them from the database by primary key
                    object = activeRecordExtractor.extractFromKey(rs, column);
                } else {
                    ResultExtractor extractor = extractors.get(type);
                    if (extractor == null) {
                        throw new RulesException(NO_SQL_EXTRACTOR, type.getSimpleName());
                    }
                    object = extractor.extract(rs, column);
                    if (!type.isPrimitive() && rs.wasNull()) {
                        // Special case to enable Number objects to be null (ResultSet.getNumber returns 0's from nulls)
                        object = null;
                    }
                }
                PropertyUtils.setProperty(fact, field.getName(), object);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SQLException | RuntimeException e) {
                throw new RulesException(e, SQL_EXTRACT_FAILURE, getFactClass().getSimpleName());
            }
        }
    }

    private class ActiveRecordExtractor {

        private final Class<? extends ActiveRecord<?>> recordClass;
        private final Class<?> keyType;

        private ActiveRecordExtractor(Class<? extends ActiveRecord<?>> recordClass) {
            this.recordClass = recordClass;
            List<Field> primaryKeys = new ArrayList<>();
            AnnotationUtilities.getAnnotatedFields(recordClass, PrimaryKey.class).forEach(primaryKeys::add);
            if (primaryKeys.size() != 1) {
                throw new RulesException(PROCESSOR_INSTANTIATION_FAILURE, getName());
            }
            this.keyType = primaryKeys.get(0).getType();
        }

        private Object extractFromKey(ResultSet rs, String column) throws SQLException {
            ResultExtractor extractor = extractors.get(keyType);
            if (extractor == null) {
                throw new RulesException(NO_SQL_EXTRACTOR, keyType.getSimpleName());
            }
            Object key = extractor.extract(rs, column);
            if (rs.wasNull()) {
                // Do not resolve nulls into numbers; they'll just resolve to 0 and result in cache misses
                return null;
            }

            return resolveDimension(recordClass, key);
        }

        @SuppressWarnings("unchecked")
        private <RC extends ActiveRecord<RC>> RC resolveDimension(Class<? extends ActiveRecord<?>> recordClass, Object key) {
            return getContext().resolveDimension((Class<RC>)recordClass, key);
        }
    }
}
