package com.rtu.gmal.rabbit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DeadLetterRabbitConfig {

    @Bean
    public Exchange deadExchange() {
        return new DirectExchange("user.order.delay.exchange");
    }

    @Bean
    public Queue deadQueue() {
        Map<String, Object> args = new HashMap() {{
           put("x-message-ttl", 10*1000);
           put("x-dead-letter-exchange", "user.order.exchange");
           put("x-dead-letter-routing-key", "order");
        }};
        return new Queue("user.order.delay.queue", true, false, false, args);
    }

    @Bean
    public Binding deadBinding() {
        return new Binding("user.order.delay.queue", Binding.DestinationType.QUEUE,
                "user.order.delay.exchange", "order_delay", null);
    }

    @Bean
    public Exchange deadOrderExchange() {
        return new DirectExchange("user.order.exchange");
    }

    @Bean
    public Queue deadOrderQueue() {
        return new Queue("user.order.queue");
    }

    @Bean
    public Binding deadOrderBinding() {
        return new Binding("user.order.queue", Binding.DestinationType.QUEUE,
                "user.order.exchange", "order", null);
    }
}
