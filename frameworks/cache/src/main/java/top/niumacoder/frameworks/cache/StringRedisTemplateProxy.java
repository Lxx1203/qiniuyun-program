package top.niumacoder.frameworks.cache;

import com.alibaba.fastjson2.JSON;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.niumacoder.frameworks.cache.config.RedisDistributedProperties;
import top.niumacoder.frameworks.cache.core.CacheGetFilter;
import top.niumacoder.frameworks.cache.core.CacheGetIfAbsent;
import top.niumacoder.frameworks.cache.core.CacheLoader;
import top.niumacoder.frameworks.cache.toolkit.CacheUtil;
import top.niumacoder.frameworks.cache.toolkit.FastJson2Util;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 分布式缓存 Redis代理
 */
@AllArgsConstructor
public class StringRedisTemplateProxy implements DistributedCache {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisDistributedProperties redisDistributedProperties;
    private final RedissonClient redissonClient;

    private static final String SAFE_GET_DISTRIBUTED_LOCK_KEY_PREFIX = "safe_get_distributed_lock_get:";

    @Override
    public <T> T get(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (String.class.isAssignableFrom(clazz)) {
            return (T) value;
        }
        return JSON.parseObject(value, FastJson2Util.buildType(clazz));
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, redisDistributedProperties.getValueTimeout());
    }


    @Override
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    @Override
    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }


    @Override
    public <T> T get(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout) {
        return get(key, clazz, cacheLoader, timeout, redisDistributedProperties.getValueTimeUnit());
    }

    @Override
    public <T> T get(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit) {
        T result = get(key, clazz);
        if (!CacheUtil.isNullOrBlank(result)) {
            return result;
        }
        return loadAndSet(key, cacheLoader, timeout, timeUnit, false, null);
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout) {
        return safeGet(key, clazz, cacheLoader, timeout, redisDistributedProperties.getValueTimeUnit());
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit) {
        return safeGet(key, clazz, cacheLoader, timeout, timeUnit, null);
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout,
                         RBloomFilter<String> bloomFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, bloomFilter, null, null);
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout,
                         TimeUnit timeUnit, RBloomFilter<String> bloomFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, timeUnit, bloomFilter, null, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout,
                         RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheCheckFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, redisDistributedProperties.getValueTimeUnit(), bloomFilter, cacheCheckFilter, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit,
                         RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheCheckFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, timeUnit, bloomFilter, cacheCheckFilter, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout,
                         RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheGetFilter,
                         CacheGetIfAbsent<String> cacheGetIfAbsent) {
        return safeGet(key, clazz, cacheLoader, timeout, redisDistributedProperties.getValueTimeUnit(), bloomFilter, cacheGetFilter, cacheGetIfAbsent);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit,
                         RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheCheckFilter, CacheGetIfAbsent<String> cacheGetIfAbsent) {
        T result = get(key, clazz);
        if (!CacheUtil.isNullOrBlank(result)
                || Optional.ofNullable(cacheCheckFilter).map(each -> each.filter(key)).orElse(null)
                || Optional.ofNullable(bloomFilter).map(each -> !each.contains(key)).orElse(null)) {
            return result;
        }
        RLock lock = redissonClient.getLock(SAFE_GET_DISTRIBUTED_LOCK_KEY_PREFIX + key);
        lock.lock();
        try {
            if (CacheUtil.isNullOrBlank(result = get(key, clazz))) {
                if (CacheUtil.isNullOrBlank(result = loadAndSet(key, cacheLoader, timeout, timeUnit, true, bloomFilter))) {
                    Optional.ofNullable(cacheGetIfAbsent).ifPresent(each -> each.execute(key));
                }
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public void put(String key, Object value, long timeout) {
        put(key, value, timeout, redisDistributedProperties.getValueTimeUnit());
    }

    @Override
    public void put(String key, Object value, long timeout, TimeUnit timeUnit) {
        String s = value instanceof String ? String(value) : JSON.toJSONString(value);
        stringRedisTemplate.opsForValue().set(key, s, timeout, timeUnit);
    }

    @Override
    public void safePut(String key, Object value, long timeout, RBloomFilter<String> bloomFilter) {
        safePut(key, value, timeout, redisDistributedProperties.getValueTimeUnit(), null);
    }

    @Override
    public void safePut(String key, Object value, long timeout, TimeUnit timeUnit, RBloomFilter<String> bloomFilter) {
        put(key, value, timeout, timeUnit);
        if (bloomFilter != null) {
            bloomFilter.add(key);
        }
    }


    @Override
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }


    @Override
    public Object getInstance() {
        return stringRedisTemplate;
    }


    @Override
    public Long countExistingKeys(@NotNull String... keys) {
        return stringRedisTemplate.countExistingKeys(List.of(keys));
    }

    private <T> T loadAndSet(String key, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit, boolean safeFlag, RBloomFilter<String> bloomFilter) {
        T result = cacheLoader.load();
        if (CacheUtil.isNullOrBlank(result)) {
            return result;
        }
        if (safeFlag) {
            safePut(key, result, timeout, timeUnit, bloomFilter);
        } else {
            put(key, result, timeout, timeUnit);
        }
        return result;
    }
}
