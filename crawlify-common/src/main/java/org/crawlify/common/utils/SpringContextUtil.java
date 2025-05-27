package org.crawlify.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文工具类，用于在非 Spring Bean 中获取容器中的 Bean。
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取 Spring 容器中的 Bean（通过类型）
     *
     * @param beanClass Bean 的 Class 类型
     * @return 实例化后的 Bean
     * @throws IllegalStateException 如果 Spring 上下文未初始化
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring context has not been initialized.");
        }
        return applicationContext.getBean(beanClass);
    }

    /**
     * 获取 Spring 容器中的 Bean（通过名称）
     *
     * @param name Bean 的名称
     * @return 实例化后的 Bean
     * @throws IllegalStateException 如果 Spring 上下文未初始化
     */
    public static Object getBean(String name) {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring context has not been initialized.");
        }
        return applicationContext.getBean(name);
    }

    /**
     * 获取 Spring 容器中的 Bean（通过名称 + 类型）
     *
     * @param name Bean 的名称
     * @param beanClass Bean 的 Class 类型
     * @return 实例化后的 Bean
     * @throws IllegalStateException 如果 Spring 上下文未初始化
     */
    public static <T> T getBean(String name, Class<T> beanClass) {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring context has not been initialized.");
        }
        return applicationContext.getBean(name, beanClass);
    }
}
