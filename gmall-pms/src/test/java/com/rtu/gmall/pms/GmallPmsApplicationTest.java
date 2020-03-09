package com.rtu.gmall.pms;

import com.rtu.gmall.pms.entity.Brand;
import com.rtu.gmall.pms.entity.Product;
import com.rtu.gmall.pms.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPmsApplicationTest {

	@Autowired
	ProductService productService;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Autowired
	RedisTemplate<Object, Object> redisTemplateObj;

	@Test
	public void contextLoads() {
		Product product = productService.getById(1);
		System.out.println(product.getName());
	}

	@Test
	public void redisTemplate() {
		redisTemplate.opsForValue().set("hello", "world");
		String hello = redisTemplate.opsForValue().get("hello");
		assertThat(hello).isEqualTo("world");
	}

	@Test
	public void redisTemplateObjTest() {
		Brand brand = new Brand();
		brand.setId(1L);
		brand.setBigPic("/image/my.icon");
		redisTemplateObj.opsForValue().set("hello", brand);
		Brand hello = (Brand) redisTemplateObj.opsForValue().get("hello");
	}

	@Test
	public void aopContextTest() {
		//Object o = AopContext.currentProxy();
	}

	@Test
	public void testString() {
		System.out.println(null + " ");
	}
}