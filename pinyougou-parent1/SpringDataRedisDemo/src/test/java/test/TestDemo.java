package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:spring/applicationContext-redis.xml")
public class TestDemo {
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Test
	public void setValue() {
		redisTemplate.boundValueOps("name").set("itcast");
	}
	
	@Test
	public void getValue() {
		String string = (String)redisTemplate.boundValueOps("name").get();
		System.out.println(string);
		
	}
	@Test
	public void deleteValue() {
		redisTemplate.delete("name");
	}
}
