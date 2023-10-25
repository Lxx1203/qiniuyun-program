package top.niumacoder.frameworks.designpattern.chain;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import top.niumacoder.frameworks.bases.ApplicationContextHolder;
import top.niumacoder.frameworks.convention.expection.ServiceException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象责任链上下文
 */
public final class AbstractChainContext<T> implements CommandLineRunner {
    private final Map<String, List<AbstractChainHandler>> abstarctChainHandlerContainer = new HashMap<>();

    /**
     * 责任链组件执行
     * @param mark-责任略组件标识
     * @param requestParam-请求参数
     */
    public void handler(String mark, T requestParam) {
        List<AbstractChainHandler> abstractChainHandlers = abstarctChainHandlerContainer.get(mark);
        if (CollectionUtils.isEmpty(abstractChainHandlers)) {
            throw new ServiceException(String.format("[%s] 当前责任链标识不存在", mark));
        }
        abstractChainHandlers.forEach(each -> each.handler(requestParam));
    }

    /**
     * 初始化责任链容器
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        Map<String, AbstractChainHandler> beans = ApplicationContextHolder.getBeansOfType(AbstractChainHandler.class);
        beans.forEach((beanName, bean) -> {
            List<AbstractChainHandler> abstractChainHandlers = abstarctChainHandlerContainer.get(bean.mark());
            if (CollectionUtils.isEmpty(abstractChainHandlers)) {
                abstractChainHandlers = new ArrayList<>();
            }
            abstractChainHandlers.add(bean);
            List<AbstractChainHandler> actualChainHandlers = abstractChainHandlers.stream().
                    sorted(Comparator.comparing(Ordered::getOrder))
                    .collect(Collectors.toList());
            abstarctChainHandlerContainer.put(bean.mark(), actualChainHandlers);
        });
    }
}
