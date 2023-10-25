package top.niumacoder.frameworks.cache.core;

/**
 * 缓存过滤
 * @param <T>
 */
@FunctionalInterface
public interface CacheGetFilter<T> {
    /**
     * 缓存过滤
     * @param param-输出参数
     * @return 如果参数匹配返回true,否则返回false
     */
    boolean filter(T param);
}
