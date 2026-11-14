package com.f1rsters.execution.listener;

import com.f1rsters.execution.domain.OutboxEvent;
import com.f1rsters.execution.repository.OutboxEventRepository;
import com.f1rsters.execution.service.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherJobTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private OutboxPublisher publisher;

    @Test
    void publishPendingProcessesAllUnpublishedEvents() {
        OutboxPublisherJob job = new OutboxPublisherJob(repository, publisher);
        
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        
        OutboxEvent event1 = new OutboxEvent(id1, "event.type.1", "{}", OffsetDateTime.now());
        OutboxEvent event2 = new OutboxEvent(id2, "event.type.2", "{}", OffsetDateTime.now());
        
        when(repository.findTop100ByPublishedFalseOrderByOccurredAtAsc())
                .thenReturn(List.of(event1, event2));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        job.publishPending();
        
        verify(publisher).publish(event1);
        verify(publisher).publish(event2);
        verify(repository, times(2)).save(any());
        assertTrue(event1.isPublished());
        assertTrue(event2.isPublished());
    }

    @Test
    void publishPendingHandlesEmptyList() {
        OutboxPublisherJob job = new OutboxPublisherJob(repository, publisher);
        
        when(repository.findTop100ByPublishedFalseOrderByOccurredAtAsc())
                .thenReturn(List.of());
        
        job.publishPending();
        
        verify(publisher, never()).publish(any());
        verify(repository, never()).save(any());
    }

    @Test
    void publishPendingMarksEventsAsPublished() {
        OutboxPublisherJob job = new OutboxPublisherJob(repository, publisher);
        
        UUID id = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(id, "event.type", "{}", OffsetDateTime.now());
        
        assertFalse(event.isPublished());
        
        when(repository.findTop100ByPublishedFalseOrderByOccurredAtAsc())
                .thenReturn(List.of(event));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        job.publishPending();
        
        verify(repository).save(event);
        assertTrue(event.isPublished());
    }
}
