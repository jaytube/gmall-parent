package com.rtu.gmal.rabbit.controller;


import com.rtu.gmal.rabbit.bean.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    RabbitTemplate template;

    @GetMapping("/order/create")
    public Order createOrder(Long skuId, Integer num) {

        Order order = new Order(123L, 1);
        //void convertAndSend(String exchange, String routingKey, Object message) throws AmqpException
        template.convertAndSend("order-exchange", "create", order);
        template.convertAndSend("user.order.delay.exchange", "order_delay", order);
        return order;
    }
}
