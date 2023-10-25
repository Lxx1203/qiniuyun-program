package top.niumacoder.frameworks.cache.core;

/**
 * 缓存加载器
 *
 * @param <T>
 */
@FunctionalInterface
public interface CacheLoader<T> {
    /**
     * 加载缓存
     */
    T load();
}
