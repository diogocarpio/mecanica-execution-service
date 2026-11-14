package com.f1rsters.execution.validation;

import com.f1rsters.execution.dto.OSEvent;
import com.f1rsters.execution.exception.SagaContractException;
import org.springframework.stereotype.Component;

@Component
public class OSEventValidator {
    public void requireOsId(OSEvent event, String eventType) {
        if (event.osId() == null) {
            throw new SagaContractException("osId obrigatorio no evento " + eventType);
        }
    }
}
