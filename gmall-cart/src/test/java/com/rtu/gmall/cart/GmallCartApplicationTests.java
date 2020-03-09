package com.rtu.gmall.cart;

import com.rtu.gmall.cart.vo.CartItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallCartApplicationTests {

	@Autowired
	RedissonClient redisson;


	@Test
	public void testRedissonCollection() {
		RMap<Object, Object> cart = redisson.getMap("cart");
		CartItem item = new CartItem();
		item.setPrice(new BigDecimal(12.33));
		item.setSkuId(123L);
		cart.put("1", item);
	}

	@Test
	public void contextLoads() {
	}

}
