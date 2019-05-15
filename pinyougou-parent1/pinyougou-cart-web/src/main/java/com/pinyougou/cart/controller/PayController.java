package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private OrderService orderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		//获取当前登陆用户
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		//从redis中查询数据
		TbPayLog fromRedis = orderService.searchPayLogFromRedis(name);
		//判断查询出来的数据是否不为空
		if(fromRedis!=null) {
			return weixinPayService.createNative(fromRedis.getOutTradeNo(), fromRedis.getTotalFee()+"");
		}else {
			return new HashMap();
		}
		
	}
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result = null;
		int x = 0;
		while(true) {
			Map status = weixinPayService.queryPayStatus(out_trade_no);
			if(status==null) {
				result = new Result(false, "支付出错");
				break;
			}
			if(status.get("trade_state").equals("SUCCESS")) {
				result = new Result(true, "支付成功");
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, (String) status.get("transaction_id"));
				break;
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			x++;
			if(x>=100) {
				result = new Result(false, "二维码超时");
				break;
			}
		}
		return result;
	}

}
