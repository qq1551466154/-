package Testdemo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.pojo.TbItem;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-solr.xml")
public class TestTemplate {
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Test
	public void TestAdd() {
		TbItem tbItem = new TbItem();
		tbItem.setId(1L);
		tbItem.setTitle("华为mate9");
		tbItem.setBrand("华为");
		tbItem.setCategory("手机");
		tbItem.setSeller("华为旗舰店");
		tbItem.setPrice(new BigDecimal(3000.0));
		solrTemplate.saveBean(tbItem);
		solrTemplate.commit();
	}
	
	@Test
	public void testFindOne() {
		TbItem tbItem = solrTemplate.getById(1, TbItem.class);
		System.out.println(tbItem.getTitle());
	}
	
	@Test
	public void testDelete1() {
		solrTemplate.deleteById("1");
		solrTemplate.commit();
	}
	
	@Test
	public void testAddList() {
		List<TbItem> list = new ArrayList();
		
		for (int i = 0; i < 100; i++) {
			TbItem tbItem = new TbItem();
			tbItem.setId(i+1L);
			tbItem.setTitle("华为mate"+i);
			tbItem.setBrand("华为");
			tbItem.setCategory("手机");
			tbItem.setSeller("华为"+i+"号旗舰店");
			tbItem.setPrice(new BigDecimal(3000.0+i));
			tbItem.setGoodsId(1L);
			list.add(tbItem);
		}
		solrTemplate.saveBean(list);
		solrTemplate.commit();
	}
	
	@Test
	public void testPageQuery() {

		Query query = new SimpleQuery("*:*");
		query.setOffset(1);//开始索引（默认0）
		query.setRows(20);//每页记录数
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		System.out.println("总记录数："+page.getTotalElements());
		List<TbItem> content = page.getContent();
		showList(content);
		
		
	}
	
	private void showList(List<TbItem> list) {
		for(TbItem item:list) {
			System.out.println(item.getTitle()+item.getPrice());
		}
	}
	
	
	@Test
	public void testPageQueryMutil() {
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_title").contains("2");
		criteria = criteria.and("item_title").contains("5");
		query.addCriteria(criteria);
		
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		System.out.println("总记录数："+page.getTotalElements());
		List<TbItem> list = page.getContent();
		showList(list);
	}
	
	
	@Test
	public void testDelete() {
		Query query = new SimpleQuery("*:*");
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
	
}
