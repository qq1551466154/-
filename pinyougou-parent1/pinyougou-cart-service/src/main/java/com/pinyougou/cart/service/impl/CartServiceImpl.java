package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

import javassist.expr.NewArray;

@Service
@Transactional
public class CartServiceImpl implements CartService {
	
	@Autowired
	private TbItemMapper itemMapper;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
			
		//1.根据商品SKUid查询SKU商品
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item==null) {
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效");
		}
		//2.获取商家id
		String sellerId = item.getSellerId();
		
		//3.根据商家id判断购物车是否存在该商家的购物车
		Cart cart = searchCartBySellerId(cartList,sellerId);
		//4.如果购物车列表中不存在该商家的购物车
		if(cart==null) {
			//5.新建购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(cart.getSellerName());
			TbOrderItem orderItem = createOrderItem(item,num);
			List orderItemList = new ArrayList();
			orderItemList.add(orderItem);
			cart.setOrderItemsList(orderItemList);
			//6.将新建的购物车添加到购物车列表
			cartList.add(cart);
		}else {
			//7.如果购物车存在该商品的购物车
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemsList(), itemId);
			//判断购物车明细列表是否存在该商品
			if(orderItem==null) {
				orderItem = createOrderItem(item, num);
				cart.getOrderItemsList().add(orderItem);
			}else {
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
				//如果数量操作后小于等于0，则移除
				if(orderItem.getNum()<=0) {
					cart.getOrderItemsList().remove(orderItem);//移除购物车明细
					
				}
				if(cart.getOrderItemsList().size()==0) {
					cartList.remove(cart);
				}
			}
		}
		
		
		
		
		
		
		
		
		
		//如果没有就添加一个购物陈明细
		System.out.println(cart.getOrderItemsList());
		//如果有，在原购物陈明细中添加数量，改变金额
		return cartList;
	}
	/**
	 * 根据商家id查询购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for(Cart cart:cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	/**
	 * 创建订单明细
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		if(num<=0) {
			throw new RuntimeException("数量非法");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;
		
	}
	/**
	 * 根据商家明细id查询
	 * @param orderItems
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItems,Long itemId) {
		for(TbOrderItem orderItem:orderItems) {
			if(orderItem.getItemId().longValue()==itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 从redis中查询数据
	 */
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取数据："+username);
		List<Cart> cartList=(List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null) {
			cartList=new ArrayList();
		}
		return cartList;
	}
	/**
	 * 保存数据到redis中
	 */
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis中存储数据："+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
		
		
		
	}
	/**
	 * 合并本地购物车和育成购物车
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.out.println("合并购物车");
		for(Cart cart:cartList2) {
			for(TbOrderItem orderItem:cart.getOrderItemsList()) {
				cartList1=addGoodsToCartList(cartList1,orderItem.getItemId() , orderItem.getNum());
			}
		}
		return cartList1;
	}

}
