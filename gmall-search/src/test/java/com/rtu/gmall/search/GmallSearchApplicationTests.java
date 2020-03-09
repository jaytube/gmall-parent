package com.rtu.gmall.search;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchApplicationTests {

	@Autowired
	JestClient jestClient;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testEs() throws Exception {
		Search search = new Search.Builder("").addIndex("product").addType("info").build();
		SearchResult execute = jestClient.execute(search);
		System.out.println(execute.getTotal());
	}

}
