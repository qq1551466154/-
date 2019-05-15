package test;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:spring/applicationContext-redis.xml")
public class TestSetValue {
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Test
	public void setValue() {
		redisTemplate.boundSetOps("nameset").add("曹操");
		redisTemplate.boundSetOps("nameset").add("刘备");
		redisTemplate.boundSetOps("nameset").add("刘权");
	}
	
	@Test
	public void getValue() {
		Set members =  redisTemplate.boundSetOps("nameset").members();
		System.out.println(members);
	}
	
	@Test
	public void deleteValue() {
		redisTemplate.boundSetOps("nameset").remove("孙权");
		
	}
	
	@Test
	public void deleteAllValue() {
		redisTemplate.delete("nameset");
	}
	
	
	
	
}
