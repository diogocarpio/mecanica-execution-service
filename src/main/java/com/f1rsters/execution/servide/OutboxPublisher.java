package com.f1rsters.execution.servide;

import com.f1rsters.execution.domain.OutboxEvent;

public interface OutboxPublisher {
    void publish(OutboxEvent event);
}
