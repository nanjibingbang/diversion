package com.liou.diversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.liou.diversion.container.spring.DiversionBeanDefinitionRegistrar;

/**
 * 接入spring<br>
 * 使用该注解时不要再在spring中手动注册AspectJAwareAdvisorAutoProxyCreator或AnnotationAwareAspectJAutoProxyCreator以免造成二次代理
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Import(DiversionBeanDefinitionRegistrar.class)
public @interface EnableDiversion {

    /**
     * 配置文件地址
     * 
     * @return
     */
    String locations();

    /**
     * 文件编码
     * 
     * @return
     */
    String fileEncoding() default "";
}