package com.rtu.gmal.rabbit.service;

import com.rabbitmq.client.Channel;
import com.rtu.gmal.rabbit.bean.Order;
import com.rtu.gmal.rabbit.bean.User;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserService {

    /*@RabbitListener(queues = {"world"})
    public void receiveUser(Message message, User user) {
        System.out.println(user.getUsername());
    }*/

    @RabbitListener(queues = {"order-queue"})
    public void subStractStock(Message message, Order order, Channel channel) throws IOException {

        System.out.println("substract stock for order: " + order.getSkuId());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
    }

    @RabbitListener(queues = {"user.order.queue"})
    public void closeDeadOrder(Order order, Message message, Channel channel) throws IOException {
        System.out.println("close order: " + order.getSkuId());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
