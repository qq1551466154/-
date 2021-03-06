package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.TermsOptions.Sort;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap();
		// 关键字空格处理
		String keyword = (String) searchMap.get("keywords");
		searchMap.put("keywords",keyword.replace(" ", ""));
		
		// 1.查询列表
		map.putAll(searchList(searchMap));
		// 2.分组查询 商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);

		// 3.查询品牌和规格列表
		String category = (String) searchMap.get("category");
		if (!category.equals("")) {
			map.putAll(searchBrandAndSpecList(category));
		} else {
			if (categoryList.size() > 0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
			System.out.println(map);
		return map;
	}

	// 查询列表
	private Map searchList(Map searchMap) {
		Map map = new HashMap();
		// 高亮选项初始化
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");// 高亮域
		highlightOptions.setSimplePrefix("<em style='color:red'>");// 前缀
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);// 为查询对象设置高亮选项

		// 1.1 关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		// 1.2 按商品分类过滤
		if (!"".equals(searchMap.get("category"))) {// 如果用户选择了分类
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

			query.addFilterQuery(filterQuery);
		}

		// 1.3 按品牌过滤
		if (!"".equals(searchMap.get("brand"))) {// 如果用户选择了品牌
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

			query.addFilterQuery(filterQuery);
		}
		// 1.4 按规格过滤
		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

				query.addFilterQuery(filterQuery);

			}

		}

		// 1.5按价格过滤
		if (!"".equals(searchMap.get("price"))) {
			String[] price = ((String) searchMap.get("price")).split("-");
			if (!price[0].equals("0")) {// 如果区点起点不等于0
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery fQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(fQuery);

			}
			if (!price[1].equals("*")) {// 如果区终点不等于*
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		// 1.6分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo");
		if (pageNo == null) {
			pageNo = 1;// 默认第一页
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");// 每页总记录数
		if (pageSize == null) {
			pageSize = 20;// 默认20
		}

		query.setOffset((pageNo - 1) * pageSize);// 从第几条记录开始查询
		query.setRows(pageSize);
		
		//1.7排序
		String sortValue = (String)searchMap.get("sort");//ASC DESC
		String sortFiled = (String)searchMap.get("sortFiled");//排序字段
		
		if(sortValue!=null&&!"".equals(sortValue)) {
			if(sortValue.equals("ASC")) {
				org.springframework.data.domain.Sort sort = new org.springframework.data.domain.Sort(org.springframework.data.domain.Sort.Direction.ASC, "item_"+sortFiled);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")) {
				org.springframework.data.domain.Sort sort = new org.springframework.data.domain.Sort(org.springframework.data.domain.Sort.Direction.DESC, "item_"+sortFiled);
				query.addSort(sort);
			}
		}

		// *********** 获取高亮结果集 ***********
		// 高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		// 高亮入口集合(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : entryList) {
			// 获取高亮列表(高亮域的个数)
			List<Highlight> highlightList = entry.getHighlights();
			/*
			 * for(Highlight h:highlightList){ List<String> sns =
			 * h.getSnipplets();//每个域有可能存储多值 System.out.println(sns); }
			 */
			if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());// 返回总页数
		map.put("total", page.getTotalElements());// 返回总记录数
		return map;

	}

	/**
	 * 分组查询（查询商品分类列表）
	 * 
	 * @return
	 */
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList();

		Query query = new SimpleQuery("*:*");
		// 根据关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));// where ...
		query.addCriteria(criteria);
		// 设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category"); // group by ...
		query.setGroupOptions(groupOptions);
		// 获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		// 获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		// 获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		// 获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();

		for (GroupEntry<TbItem> entry : entryList) {
			list.add(entry.getGroupValue()); // 将分组的结果添加到返回值中
		}
		return list;

	}

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 根据商品分类名称查询品牌和规格列表
	 * 
	 * @param category 商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		// 1.根据商品分类名称得到模板ID
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId != null) {
			// 2.根据模板ID获取品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			System.out.println("品牌列表条数：" + brandList.size());

			// 3.根据模板ID获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
			System.out.println("规格列表条数：" + specList.size());
		}

		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}

	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品ID"+goodsIdList);
		Query query = new SimpleQuery("*:*" );
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}

}
