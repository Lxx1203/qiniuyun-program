package top.niumacoder.frameworks.designpattern.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import top.niumacoder.frameworks.bases.config.ApplicationBaseAutoConfiguration;
import top.niumacoder.frameworks.designpattern.chain.AbstractChainContext;

/**
 * 设计模式自动装配
 */
@ImportAutoConfiguration(ApplicationBaseAutoConfiguration.class)
public class DesignPatternAutoConfiguration {
    /**
     * 责任链上下文
     * @return
     */
    @Bean
    public AbstractChainContext abstractChainContext() {
        return new AbstractChainContext();
    }
}
