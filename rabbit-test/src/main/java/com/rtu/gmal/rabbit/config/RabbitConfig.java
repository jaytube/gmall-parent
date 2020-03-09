package com.rtu.gmal.rabbit.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue orderQueue() {
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete)
        return new Queue("order-queue", true, false, false);
    }

    @Bean
    public Exchange orderExchange() {
        return new DirectExchange("order-exchange", true, false);
    }

    @Bean
    public Binding orderBinding() {
        return new Binding("order-queue", Binding.DestinationType.QUEUE,
                "order-exchange", "create", null);
    }
}
