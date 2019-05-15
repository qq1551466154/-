package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.web.servlet.view.document.AbstractPdfStamperView;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;
import util.IdWorker;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
	
	@Value("${appid}")
	private String appid;
	
	@Value("${partner}")
	private String partner;
	
	@Value("${partnerkey}")
	private String partnerkey;

	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		//请求参数
		Map param = new HashMap();
		param.put("appid", appid);//公众账号id
		param.put("mch_id", partner);//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		param.put("body", "品优购");
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee", total_fee);//金额
		param.put("spbill_create_ip", "127.0.0.1");//终端ip
		param.put("notify_url", "http://test.itcast.cn");//通知地址
		param.put("trade_type", "NATIVE");//支付类型
		
		try {
			//生成要发送的XML
			String xmlParam= WXPayUtil.generateSignedXml(param,partnerkey);
			System.out.println(xmlParam);
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			//获得结果
			String result = client.getContent();
			System.out.println(result);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlParam);
			Map<String,String> map = new HashMap();
			map.put("total_fee", total_fee);//总金额
			map.put("out_trade_no", out_trade_no);//订单号
			map.put("code_url", resultMap.get("code_url"));//支付地址
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap();
		}
		
	}

	@Override
	public Map queryPayStatus(String out_trade_no) {
		Map param = new HashMap();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String content = client.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
			System.out.println(resultMap);
			return resultMap;
		} catch (Exception e) {
			
			e.printStackTrace();
			return null;
		}
		
	}

	/**
	 * 关闭订单
	 */
	@Override
	public Map<String, String> closePay(String out_trade_no) {
		Map param = new HashMap();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
			
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String content = client.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
			System.out.println(resultMap);
			return  resultMap;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	

}
