package com.pinyougou.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum,int pageSize);


	/**
	 * 用户下单
	 * @param seckillId
	 * @param username
	 */
	public void submitOrder(Long seckillId, String username);


	/**
	 * 根据用户名查询秒杀订单
	 * @param username
	 * @return
	 */
	public TbSeckillOrder searchSeckillOrderFromRedis(String username);


	/**
	 * 支付成功保存订单
	 * @param username
	 * @param valueOf
	 * @param transactionId
	 */
	public void saveOrderFromRedisToDb(String username, Long orderId, String transactionId);

	/**
	 * 支付成功就删除缓存中的数据
	 * @param username
	 * @param orderId
	 */
	public void  deleteOrderFromRedis(String username, Long orderId);
	
	
	
}
