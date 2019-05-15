package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;
import com.sun.tools.classfile.StackMapTable_attribute.verification_type_info;

@Component
public class SolrUtil {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private SolrTemplate solrTemplate;

	/**
	 * 导入商品数据
	 */
	public void importItemData() {
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");

		List<TbItem> list = itemMapper.selectByExample(example);
		System.out.println("===商品列表===");
		for (TbItem item : list) {

			Map specMap = JSON.parseObject(item.getSpec(), Map.class);// 将spec字段中的json字符串串转化为map
			item.setSpecMap(specMap);
			System.out.println(item.getTitle());
		}
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		System.out.println("===结束===");
	}

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
		solrUtil.importItemData();

	}
}
