package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Reference(timeout = 6000)
	private CartService cartService;

	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//获取登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		String cartListString = util.CookieUtil.getCookieValue(request, "cart", "UTF-8");
		if(cartListString==null||cartListString.equals("")) {
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		if(username.equals("anonymousUser")) {//如果未登录，就读取本地购物车
			
			System.out.println("从本地cookie查找数据");
			return cartList_cookie;
		}else {
			//如果已经登录，
			List<Cart> listFromRedis = cartService.findCartListFromRedis(username);
			System.out.println("从redis中查找数据");
			if(cartList_cookie.size()>0) {//如果本地存在购物车
				//合并购物车
				listFromRedis=cartService.mergeCartList(listFromRedis, cartList_cookie);
				util.CookieUtil.deleteCookie(request, response, cartListString);
				cartService.saveCartListToRedis(username, listFromRedis);
				System.out.println("往redis里面存储数据");
				
			}
			System.out.println(listFromRedis);
			return listFromRedis;
		}
	
		
		
		
		
		
	}
	
	@RequestMapping("/addGoodsToCartList")
	
	public Result addGoodsToCartList(Long itemId,Integer num) {
		
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		//获取登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录用户："+username);
		
		try {
			List<Cart> cartList = findCartList();//获取购物车列表
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if(username.equals("anonymousUser")) {
				//存入到cookie
				util.CookieUtil.setCookie(request, response, "cart", JSON.toJSONString(cartList), 3600*24, "UTF-8");
				System.out.println("向cookie中存储数据");
			}else {
				cartService.saveCartListToRedis(username, cartList);
			}
			
			return new Result(true, "添加成功");
		} catch (Exception e) {
				
			e.printStackTrace();
			return new Result(false, "添加失败");
			
		}
		
		
	}
}
