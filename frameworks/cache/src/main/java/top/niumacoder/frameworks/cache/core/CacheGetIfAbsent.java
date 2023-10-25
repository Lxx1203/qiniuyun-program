package top.niumacoder.frameworks.cache.core;

/**
 * 缓存查询为空
 * @param <T>
 */
@FunctionalInterface
public interface CacheGetIfAbsent<T> {
    /**
     * 如果查询为空，执行以下逻辑
     * @param param
     */
    void execute(T param);
}
