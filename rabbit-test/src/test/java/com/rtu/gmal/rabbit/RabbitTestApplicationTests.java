package com.rtu.gmal.rabbit;

import com.rtu.gmal.rabbit.bean.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitTestApplicationTests {
	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	AmqpAdmin amqpAdmin;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testRabbit() {
		User user = new User();
		user.setUsername("robin tu");
		user.setEmail("rtu@126.com");
		rabbitTemplate.convertAndSend("direct_exchange", "world", user);
		System.out.println("success");
	}

	@Test
	public void exchangeTest() {
		//public DirectExchange(String name, boolean durable, boolean autoDelete)
		Exchange exchange = new DirectExchange("my-exchange", true, false);
		amqpAdmin.declareExchange(exchange);
	}

	@Test
	public void queueTest() {
		//public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete)
		Queue queue = new Queue("my-queue-01", true, false, false);
		amqpAdmin.declareQueue(queue);
	}

	@Test
	public void bindingTest() {
		//public Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
		//			Map<String, Object> arguments) {
		Binding binding = new Binding("my-queue-01", Binding.DestinationType.QUEUE,
				"my-exchange", "hello", null);
		amqpAdmin.declareBinding(binding);
	}

}
