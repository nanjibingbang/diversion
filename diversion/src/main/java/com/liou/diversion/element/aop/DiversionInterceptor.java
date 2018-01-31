package com.liou.diversion.element.aop;

import com.liou.diversion.element.Element;
import com.liou.diversion.element.execute.DiversionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * diversion切入点
 *
 * @author liou
 */
@Aspect
public class DiversionInterceptor {

    private DiversionService diversionService;

    /**
     * spring中指定order,按优先级从高到低<br/>
     * 1.继承org.springframework.core.Ordered接口<br/>
     * 2.通过org.springframework.core.annotation.Order注解指定<br/>
     * 3.通过javax.annotation.Priority注解指定(spring version4.1)<br/>
     * 4.(默认)最低优先级（org.springframework.core.Ordered.LOWEST_PRECEDENCE）
     */
    @Around("@annotation(diversion)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, Diversion diversion) throws Throwable {
        Object proceed = proceedingJoinPoint.proceed();
        if (proceed == null) {
            MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
            Object[] args = proceedingJoinPoint.getArgs();
            Element element = new Element(diversion.targetClass(), diversion.targetMethod(), args);
            Object result = diversionService.receiveElement(element);
            return signature.getReturnType().cast(result);
        }
        return proceed;
    }

    public void setDiversionService(DiversionService diversionService) {
        this.diversionService = diversionService;
    }

}
