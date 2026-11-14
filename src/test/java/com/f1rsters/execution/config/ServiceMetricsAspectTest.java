package com.f1rsters.execution.config;

import com.newrelic.api.agent.NewRelic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceMetricsAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private Object target;

    @Test
    void recordServiceMetricsIncrementsSuccessCounter() throws Throwable {
        ServiceMetricsAspect aspect = new ServiceMetricsAspect();
        
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.proceed()).thenReturn("result");
        
        try (MockedStatic<NewRelic> mockedNewRelic = mockStatic(NewRelic.class)) {
            aspect.recordServiceMetrics(joinPoint);
            
            mockedNewRelic.verify(() -> NewRelic.incrementCounter(anyString()), times(2));
            mockedNewRelic.verify(() -> NewRelic.incrementCounter(contains("Success")));
            mockedNewRelic.verify(() -> NewRelic.recordResponseTimeMetric(anyString(), anyLong()));
        }
    }

    @Test
    void recordServiceMetricsIncrementsErrorCounterOnException() throws Throwable {
        ServiceMetricsAspect aspect = new ServiceMetricsAspect();
        
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        RuntimeException error = new RuntimeException("Test error");
        when(joinPoint.proceed()).thenThrow(error);
        
        try (MockedStatic<NewRelic> mockedNewRelic = mockStatic(NewRelic.class)) {
            assertThrows(RuntimeException.class, () -> aspect.recordServiceMetrics(joinPoint));
            
            mockedNewRelic.verify(() -> NewRelic.incrementCounter(anyString()), times(2));
            mockedNewRelic.verify(() -> NewRelic.incrementCounter(contains("Errors")));
            mockedNewRelic.verify(() -> NewRelic.noticeError(error));
            mockedNewRelic.verify(() -> NewRelic.recordResponseTimeMetric(anyString(), anyLong()));
        }
    }

}
