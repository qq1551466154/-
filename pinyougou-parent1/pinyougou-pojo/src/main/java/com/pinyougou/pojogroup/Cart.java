package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbOrderItem;

public class Cart implements Serializable{

	private String sellerId;//商家id
	
	private String sellerName;//商家名称
	
	private List<TbOrderItem> orderItemsList;//购物车明细

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

	public List<TbOrderItem> getOrderItemsList() {
		return orderItemsList;
	}

	public void setOrderItemsList(List<TbOrderItem> orderItemsList) {
		this.orderItemsList = orderItemsList;
	}
	
	
}
