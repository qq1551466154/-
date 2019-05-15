package com.pinyougou.seckill.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private SeckillOrderService seckillOrderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		//获取当前登陆用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//从redis中查询数据
		//TbPayLog fromRedis = orderService.searchPayLogFromRedis(name);
		TbSeckillOrder tbSeckillOrder = seckillOrderService.searchSeckillOrderFromRedis(username);
		//判断查询出来的数据是否不为空
		if(tbSeckillOrder!=null) {
			return weixinPayService.createNative(tbSeckillOrder.getId()+"",new BigDecimal(tbSeckillOrder.getMoney().doubleValue()*100)+"");
		}else {
			return new HashMap();
		}
		
	}
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
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
				//保存到数据库
				seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),status.get("transaction_id").toString());
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
				Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
				if("SUCCESS".equals(payResult.get("result_code"))) {
					//如果返回结果是正常关闭
					if("ORDERPAID".equals(payResult.get("err_code"))) {
						result = new Result(true, "支付成功");
						seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),status.get("transaction_id").toString());
					}
				}
				if(result.isSuccess()==false) {
					System.out.println("超时,取消订单");
					seckillOrderService.deleteOrderFromRedis(username,Long.valueOf(out_trade_no));
				}
				break;
			}
		}
		return result;
	}

}
