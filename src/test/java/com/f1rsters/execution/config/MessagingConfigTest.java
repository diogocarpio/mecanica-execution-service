package com.f1rsters.execution.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MessagingConfigTest {

    @Test
    void configConstantsAreCorrectlyDefined() {
        assertEquals("os.exchange", MessagingConfig.EXCHANGE);
        assertEquals("os.exchange.dlx", MessagingConfig.DLX);
        assertEquals("execution.os.approved.queue", MessagingConfig.APPROVED_QUEUE);
        assertEquals("execution.os.cancelled.queue", MessagingConfig.CANCEL_QUEUE);
        assertEquals("execution.os.approved.dlq", MessagingConfig.APPROVED_DLQ);
        assertEquals("execution.os.cancelled.dlq", MessagingConfig.CANCEL_DLQ);
        assertEquals("execution.completed.queue", MessagingConfig.COMPLETED_QUEUE);
        assertEquals("execution.status.updated.queue", MessagingConfig.STATUS_QUEUE);
    }

    @Test
    void osExchangeBeanCreatesTopicExchange() {
        MessagingConfig config = new MessagingConfig();
        TopicExchange exchange = config.osExchange();
        
        assertNotNull(exchange);
        assertEquals(MessagingConfig.EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void deadLetterExchangeBeanCreatesDirectExchange() {
        MessagingConfig config = new MessagingConfig();
        DirectExchange exchange = config.deadLetterExchange();
        
        assertNotNull(exchange);
        assertEquals(MessagingConfig.DLX, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void cancelQueueBeanHasDeadLetterConfiguration() {
        MessagingConfig config = new MessagingConfig();
        Queue queue = config.cancelQueue();
        
        assertNotNull(queue);
        assertEquals(MessagingConfig.CANCEL_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void approvedQueueBeanHasDeadLetterConfiguration() {
        MessagingConfig config = new MessagingConfig();
        Queue queue = config.approvedQueue();
        
        assertNotNull(queue);
        assertEquals(MessagingConfig.APPROVED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void completedQueueBeanIsDurable() {
        MessagingConfig config = new MessagingConfig();
        Queue queue = config.completedQueue();
        
        assertNotNull(queue);
        assertEquals(MessagingConfig.COMPLETED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void statusQueueBeanIsDurable() {
        MessagingConfig config = new MessagingConfig();
        Queue queue = config.statusQueue();
        
        assertNotNull(queue);
        assertEquals(MessagingConfig.STATUS_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void approvedDlqBeanIsDurable() {
        MessagingConfig config = new MessagingConfig();
        Queue queue = config.approvedDlq();
        
        assertNotNull(queue);
        assertEquals(MessagingConfig.APPROVED_DLQ, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void cancelDlqBeanIsDurable() {
        MessagingConfig config = new MessagingConfig();
        Queue queue = config.cancelDlq();
        
        assertNotNull(queue);
        assertEquals(MessagingConfig.CANCEL_DLQ, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void approvedBindingCreatesCorrectBinding() {
        MessagingConfig config = new MessagingConfig();
        Queue approvedQueue = config.approvedQueue();
        TopicExchange osExchange = config.osExchange();
        
        Binding binding = config.approvedBinding(approvedQueue, osExchange);
        
        assertNotNull(binding);
        assertEquals(MessagingConfig.APPROVED_QUEUE, binding.getDestination());
        assertEquals("os.approved", binding.getRoutingKey());
    }

    @Test
    void cancelBindingCreatesCorrectBinding() {
        MessagingConfig config = new MessagingConfig();
        Queue cancelQueue = config.cancelQueue();
        TopicExchange osExchange = config.osExchange();
        
        Binding binding = config.cancelBinding(cancelQueue, osExchange);
        
        assertNotNull(binding);
        assertEquals(MessagingConfig.CANCEL_QUEUE, binding.getDestination());
        assertEquals("os.cancelled", binding.getRoutingKey());
    }

    @Test
    void approvedDlqBindingCreatesCorrectBinding() {
        MessagingConfig config = new MessagingConfig();
        Queue approvedDlq = config.approvedDlq();
        DirectExchange deadLetterExchange = config.deadLetterExchange();
        
        Binding binding = config.approvedDlqBinding(approvedDlq, deadLetterExchange);
        
        assertNotNull(binding);
        assertEquals(MessagingConfig.APPROVED_DLQ, binding.getDestination());
        assertEquals(MessagingConfig.APPROVED_QUEUE, binding.getRoutingKey());
    }

    @Test
    void cancelDlqBindingCreatesCorrectBinding() {
        MessagingConfig config = new MessagingConfig();
        Queue cancelDlq = config.cancelDlq();
        DirectExchange deadLetterExchange = config.deadLetterExchange();
        
        Binding binding = config.cancelDlqBinding(cancelDlq, deadLetterExchange);
        
        assertNotNull(binding);
        assertEquals(MessagingConfig.CANCEL_DLQ, binding.getDestination());
        assertEquals(MessagingConfig.CANCEL_QUEUE, binding.getRoutingKey());
    }

    @Test
    void rabbitTemplateBeanUsesJacksonConverter() {
        MessagingConfig config = new MessagingConfig();
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        
        RabbitTemplate template = config.rabbitTemplate(connectionFactory);
        
        assertNotNull(template);
        assertTrue(template.getMessageConverter() instanceof Jackson2JsonMessageConverter);
    }
}
