package com.simple.simplerpc.consumer;

import com.simple.simplerpc.annotation.RpcConsumer;
import com.simple.simplerpc.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author simple
 */
@Slf4j
public class Consumer implements BeanFactoryPostProcessor, BeanClassLoaderAware, ApplicationContextAware {

    private ClassLoader classLoader;

    private ApplicationContext applicationContext;

    /**
     * linkedHashMap保证有序
     */
    private Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap<>();


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //遍历容器里的所有bean
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = definition.getBeanClassName();

            if(beanClassName != null) {
                //使用反射获取 bean 的 class 对象，注意 classloader 是容器加载 bean 的 classloader
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseField);
            }
        }

        //将 BeanDefinition 注入到容器中
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry)beanFactory;
        this.beanDefinitions.forEach((beanName, beanDefinition) -> {
            if (applicationContext.containsBean(beanName)) {
                throw new IllegalArgumentException("Spring context already has a bean named: " + beanName);
            }

            registry.registerBeanDefinition(beanName, beanDefinition);
        });
    }

    /**
     * 1.解析 class 的字段
     * 2.找到被 @RpcConsumer 注解的类，构造 BeanDefinition
     */
    private void parseField(Field field) {
        RpcConsumer annotation = AnnotationUtils.getAnnotation(field, RpcConsumer.class);
        if (annotation == null) {
            return;
        }

        //构造 Bean 的参数
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerFactoryBean.class);
        builder.setInitMethodName(Constants.INIT_METHOD);
        builder.addPropertyValue("interfaceClass", field.getType());
        builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
        builder.addPropertyValue("registryType", annotation.registryType());
        builder.addPropertyValue("registryAddress", annotation.registryAddress());

        BeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinitions.put(field.getName(), beanDefinition);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
