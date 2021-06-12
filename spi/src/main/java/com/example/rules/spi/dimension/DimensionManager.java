package com.example.rules.spi.dimension;

import com.daxtechnologies.annotation.AnnotationUtilities;
import com.daxtechnologies.configuration.Configuration;
import com.daxtechnologies.configuration.ConfigurationFactory;
import com.daxtechnologies.database.DatabaseUtilities;
import com.daxtechnologies.database.annotation.PrimaryKey;
import com.daxtechnologies.database.orm.activerecord.ActiveRecord;
import com.daxtechnologies.database.orm.activerecord.ActiveRecords;
import com.daxtechnologies.database.transaction.TransactionOptions;
import com.daxtechnologies.oam.ILogger;
import com.daxtechnologies.oam.TheLogger;
import com.daxtechnologies.record.filter.Expression;
import com.daxtechnologies.record.filter.ExpressionFactory;
import com.daxtechnologies.services.cache.*;
import com.daxtechnologies.services.worker.AbstractWorker;
import com.daxtechnologies.services.worker.WorkerService;
import com.daxtechnologies.util.ArgumentUtilities;
import com.daxtechnologies.util.ObjectUtilities;
import com.spirent.cem.rules.api.ErrorNumbers;
import com.spirent.cem.rules.api.RulesException;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.spirent.cem.rules.api.ErrorNumbers.INVALID_FACT_TYPE;

public class DimensionManager {

    private static final ILogger LOG = TheLogger.getInstance(DimensionManager.class);

    private final AtomicInteger cacheId = new AtomicInteger();

    private final int concurrency;
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends ActiveRecord>, Cache<Object, ? extends ActiveRecord>> recordCaches = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends ActiveRecord>, Expression> filters = new HashMap<>();

    private final Configuration config = ConfigurationFactory.getInstance().getConfiguration().subset("rules");

    public DimensionManager() {
        concurrency = config.getInt("cache.concurrency", 4);
    }

    /**
     * Registers an ActiveRecord type as a dimension with a given capacity and warm up filter
     *
     * @param recordType the ActiveRecord type
     * @param filter     an Expression used to filter entries during warm up
     * @param <R>        the ActiveRecord type
     */
    public synchronized <R extends ActiveRecord<R>> void registerDimension(Class<R> recordType, Expression filter) {
        CacheBuilder<Object, R> builder = Cache.create("rules-" + recordType.getSimpleName() + "-" + cacheId.getAndIncrement(), recordType)
                .name("Rules dimension " + recordType.getSimpleName())
                .type(Cache.Type.MEMORY)
                .warmUpStrategy(WarmUpStrategy.LAZY)
                .selfPopulating(true)
                .concurrencyLevel(concurrency);

        com.daxtechnologies.services.cache.annotation.Cache annotation = recordType.getAnnotation(com.daxtechnologies.services.cache.annotation.Cache.class);

        long capacity = config.getLong("dimension." + recordType.getSimpleName() + ".capacity",
                annotation != null ? annotation.capacity() : -1);
        if (capacity > 0) {
            builder.capacity(capacity);
        }

        builder.rememberMisses(annotation == null || annotation.rememberMisses() != MissStrategy.OFF);

        Cache<Object, R> cache = builder.build();

        cache.addCacheContentProvider(new ContentProvider<>(recordType));

        CacheService.getInstance().registerCache(cache);

        if (filter != null) {
            filters.put(recordType, filter);
        }

        recordCaches.put(recordType, cache);
    }

    /**
     * Finds a dimension entry for a given ActiveRecord type and key
     *
     * @param recordType the ActiveRecord type
     * @param key        the record key
     * @param <R>        the ActiveRecord type
     * @return an ActiveRecord for the key, or {@code null} if none is found
     */
    public <R extends ActiveRecord<R>> R resolveDimension(Class<R> recordType, Object key) {
        ArgumentUtilities.validateIfNotNull(recordType);
        if (key == null) {
            return null;
        }
        return getRecordCache(recordType).find(key);
    }

    /**
     * Warms up all registered dimensions, replacing entries as needed
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void warmUp() {
        recordCaches.keySet().forEach(recordType -> {
            warmUp(recordType);
            if (((AbstractCache<?, ?>)getRecordCache(recordType)).isRememberMissed()) {
                WorkerService.getInstance().scheduleAtFixedRate(new MissCleanupWorker(recordType), 10, 10, TimeUnit.MINUTES);
            }
        });
    }

    /**
     * Warms up the dimension for a given ActiveRecord type, replacing entries as needed
     *
     * @param recordType the ActiveRecord type
     * @param <R>        the ActiveRecord type
     */
    public <R extends ActiveRecord<R>> void warmUp(Class<R> recordType) {
        CacheService.getInstance().scheduleWarmUpWorker(new WarmupWorker<>(recordType));
    }

    private <R extends ActiveRecord<R>> Cache<Object, R> getRecordCache(Class<R> recordType) {
        @SuppressWarnings("unchecked")
        Cache<Object, R> cache = (Cache<Object, R>)recordCaches.get(recordType);
        if (cache == null) {
            throw new RulesException(ErrorNumbers.INVALID_DIMENSION, recordType.getName());
        }
        return cache;
    }

    /**
     * Clears cached lookup misses for a given record type
     *
     * @param recordType the ActiveRecord type
     * @param <R>        the ActiveRecord type
     */
    <R extends ActiveRecord<R>> void clearMisses(Class<R> recordType) {
        Cache<Object, R> recordCache = getRecordCache(recordType);
        recordCache.getEntries().stream()
                .filter(entry -> CacheUtilities.CACHE_MISS_VALUE == entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .forEach(recordCache::remove);
    }

    /**
     * Content provider to resolve ActiveRecords, using {@link ActiveRecords#find(Class, Object)}
     *
     * @param <R> the ActiveRecord type
     */
    private static class ContentProvider<R extends ActiveRecord<R>> implements CacheContentProvider<Object, R> {

        private final Class<R> recordType;

        ContentProvider(Class<R> recordType) {
            this.recordType = recordType;
        }

        @Override
        public R resolveContent(Object key, CacheContext<Object, R> context) {
            return ActiveRecords.find(recordType, key);
        }
    }

    /**
     * Worker to warm up the cache for a given record type
     *
     * @param <R> the ActiveRecord type
     */
    class WarmupWorker<R extends ActiveRecord<R>> extends AbstractCacheWarmUpWorker<Object, R> {

        private final Class<R> recordType;
        private final Expression filter;
        private Field pkField;
        private final int fetchSize;

        WarmupWorker(Class<R> recordType) {
            super(getRecordCache(recordType), false);

            this.recordType = recordType;
            this.filter = ObjectUtilities.defaultIfNull(filters.get(recordType), ExpressionFactory.newEmptyExpression());
            for (Field field : AnnotationUtilities.getAnnotatedFields(recordType, PrimaryKey.class)) {
                pkField = field;
            }

            fetchSize = config.getInt("dimension." + recordType.getSimpleName() + ".fetch.size", 2000);
        }

        @Override
        protected int warmUpCache() {
            return DatabaseUtilities.doInTransaction((tr, template) -> {
                int count = 0;
                try {
                    for (R record : ActiveRecords.withFetchSize(fetchSize, () -> ActiveRecords.where(recordType, filter))) {
                        cache.put(PropertyUtils.getProperty(record, pkField.getName()), record);
                        ++count;
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | RuntimeException e) {
                    throw new RulesException(e, INVALID_FACT_TYPE, recordType.getSimpleName(), e.getMessage());
                }
                return count;
            }, TransactionOptions.OLAP_NEW_TRANSACTION);
        }
    }

    /**
     * Worker to remove all cached lookup misses for a given record type
     */
    class MissCleanupWorker<R extends ActiveRecord<R>> extends AbstractWorker {

        private final Class<R> recordType;

        MissCleanupWorker(Class<R> recordType) {
            this.recordType = recordType;
        }

        @Override
        protected void doRun() {
            try {
                clearMisses(recordType);
            } catch (RuntimeException e) {
                LOG.error("Failed to purge cache misses for record type " + recordType.getName(), e);
            }
        }
    }
}
