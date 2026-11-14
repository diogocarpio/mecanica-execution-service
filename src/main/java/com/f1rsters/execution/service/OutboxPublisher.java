package com.f1rsters.execution.service;

import com.f1rsters.execution.domain.OutboxEvent;

public interface OutboxPublisher {
    void publish(OutboxEvent event);
}
