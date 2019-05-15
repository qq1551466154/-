package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Autowired
		private RedisTemplate redisTemplate;
		
		@Autowired
		private TbSeckillGoodsMapper seckillGoodsMapper;
		
		@Autowired
		private IdWorker idWorker;
		
		@Override
		public void submitOrder(Long seckillId, String username) {
			//从缓存中查询秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			if(seckillGoods==null) {
				throw new RuntimeException("商品不存在");
			}
			if(seckillGoods.getStockCount()<=0) {
				throw new RuntimeException("已经抢空了");
			}
			seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);//更新缓存
			
			if(seckillGoods.getStockCount()==0) {
				//已经被秒光了
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//更新到数据库
				//删除缓存中的数据
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
			}
			//保存redis订单
			long orderId = idWorker.nextId();//订单id
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			seckillOrder.setId(orderId);//订单id
			seckillOrder.setSeckillId(seckillId);//秒杀商品id
			seckillOrder.setMoney(seckillGoods.getCostPrice());//金额
			seckillOrder.setUserId(username);//用户名
			seckillOrder.setCreateTime(new Date());//订单创建时间
			seckillOrder.setStatus("0");//未支付状态
			redisTemplate.boundHashOps("seckillOrder").put(username, seckillOrder);//保存订单到缓存中
			
			
		}

		@Override
		public TbSeckillOrder searchSeckillOrderFromRedis(String username) {
			TbSeckillOrder tbSeckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
			return tbSeckillOrder;
		}

		@Override
		public void saveOrderFromRedisToDb(String username, Long orderId, String transactionId) {
			System.out.println("保存订单从缓存到数据库");
			//根据用户查询日志
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
			if(seckillOrder==null) {
				throw new RuntimeException("订单不存在");	
			}
			//如果传递过来的订单号不符
			if(seckillOrder.getId().longValue()!=orderId.longValue()) {
				throw new RuntimeException("订单不相符");
			}
			seckillOrder.setTransactionId(transactionId);
			seckillOrder.setPayTime(new Date());
			seckillOrder.setStatus("1");
			seckillOrderMapper.insert(seckillOrder);//保存到数据库
			redisTemplate.boundHashOps("seckillOrder").delete(username);//删除缓存
		}

		@Override
		public void deleteOrderFromRedis(String username, Long orderId) {
			//根据用户id查询日志
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
			if(seckillOrder!=null&&seckillOrder.getId().longValue()==orderId.longValue()) {
				redisTemplate.boundHashOps("seckillOrder").delete(username);
				
				//恢复库存
				TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(orderId);
				if(seckillGoods!=null) {
					seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);//存入缓存
					
				}
			}
			
		}
	
}
