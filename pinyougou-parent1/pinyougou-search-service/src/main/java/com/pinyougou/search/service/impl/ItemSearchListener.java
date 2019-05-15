package com.pinyougou.search.service.impl;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemSearchListener implements MessageListener {
	
	@Autowired
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message message) {
		System.out.println("监听接收到消息...");
		TextMessage textMessage =(TextMessage)message;
		try {
			String text = textMessage.getText();
			List<TbItem> list = JSON.parseArray(text,TbItem.class);
			for(TbItem item:list) {
				System.out.println(item.getId()+" "+item.getTitle());
				Map specMap= JSON.parseObject(item.getSpec());//将 spec 字段中的 json字符串转换为 map
				item.setSpecMap(specMap);
			}
			itemSearchService.importList(list);//导入
			System.out.println("成功导入索引库");
		} catch (JMSException e) {
			
			e.printStackTrace();
		}

	}

}
