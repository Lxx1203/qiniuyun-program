package top.niumacoder.frameworks.designpattern.chain;

import org.springframework.core.Ordered;

/**
 * 抽象责任链组件
 * @param <T>
 */
public interface AbstractChainHandler <T> extends Ordered {
    /**
     * 责任链执行逻辑
     * @param requestParam-责任链入参
     */
    void handler(T requestParam);

    /**
     * @return-责任链标识
     */
    String mark();
}
