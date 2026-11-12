package com.f1rsters.execution.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
@ConditionalOnProperty(name = "BROKER_ENABLED", havingValue = "true")
public class MessagingConfig {
    public static final String EXCHANGE = "os.exchange";
    public static final String DLX = "os.exchange.dlx";
    public static final String APPROVED_QUEUE = "execution.os.approved.queue";
    public static final String CANCEL_QUEUE = "execution.os.cancelled.queue";
    public static final String APPROVED_DLQ = "execution.os.approved.dlq";
    public static final String CANCEL_DLQ = "execution.os.cancelled.dlq";
    public static final String COMPLETED_QUEUE = "execution.completed.queue";
    public static final String STATUS_QUEUE = "execution.status.updated.queue";

    @Bean
    TopicExchange osExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue cancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(CANCEL_QUEUE)
                .build();
    }

    @Bean
    Queue approvedQueue() {
        return QueueBuilder.durable(APPROVED_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(APPROVED_QUEUE)
                .build();
    }

    @Bean
    Queue completedQueue() {
        return QueueBuilder.durable(COMPLETED_QUEUE).build();
    }

    @Bean
    Queue statusQueue() {
        return QueueBuilder.durable(STATUS_QUEUE).build();
    }

    @Bean
    Binding approvedBinding(@Qualifier("approvedQueue") Queue approvedQueue, TopicExchange osExchange) {
        return BindingBuilder.bind(approvedQueue).to(osExchange).with("os.approved");
    }

    @Bean
    Binding cancelBinding(@Qualifier("cancelQueue") Queue cancelQueue, TopicExchange osExchange) {
        return BindingBuilder.bind(cancelQueue).to(osExchange).with("os.cancelled");
    }

    @Bean
    Queue approvedDlq() {
        return QueueBuilder.durable(APPROVED_DLQ).build();
    }

    @Bean
    Queue cancelDlq() {
        return QueueBuilder.durable(CANCEL_DLQ).build();
    }

    @Bean
    Binding approvedDlqBinding(@Qualifier("approvedDlq") Queue approvedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(approvedDlq).to(deadLetterExchange).with(APPROVED_QUEUE);
    }

    @Bean
    Binding cancelDlqBinding(@Qualifier("cancelDlq") Queue cancelDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(cancelDlq).to(deadLetterExchange).with(CANCEL_QUEUE);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter());
        return template;
    }
}
