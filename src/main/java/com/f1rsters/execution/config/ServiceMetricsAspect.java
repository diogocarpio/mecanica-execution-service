package com.f1rsters.execution.config;

import com.newrelic.api.agent.NewRelic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Aspect
@Component
@EnableAspectJAutoProxy
public class ServiceMetricsAspect {

    @Around("@within(" + "org.springframework.stereotype.Service" + ") && execution(public * *(..))")
    public Object recordServiceMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String metricName = "Custom/Service/"
                + joinPoint.getTarget().getClass().getSimpleName()
                + "/" + joinPoint.getSignature().getName();
        long startedAt = System.nanoTime();

        NewRelic.incrementCounter(metricName + "/Calls");
        try {
            Object result = joinPoint.proceed();
            NewRelic.incrementCounter(metricName + "/Success");
            return result;
        } catch (Throwable error) {
            NewRelic.incrementCounter(metricName + "/Errors");
            NewRelic.noticeError(error);
            throw error;
        } finally {
            NewRelic.recordResponseTimeMetric(metricName + "/Duration",
                    (System.nanoTime() - startedAt) / 1_000_000);
        }
    }
}
