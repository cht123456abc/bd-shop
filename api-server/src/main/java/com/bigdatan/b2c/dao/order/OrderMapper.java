package com.bigdatan.b2c.dao.order;


import java.util.List;

import com.bigdatan.b2c.dao.base.IBaseDao;
import com.bigdatan.b2c.entity.order.Order;
import com.bigdatan.b2c.entity.order.OrderAdminSearchVO;

public interface OrderMapper extends IBaseDao<Order> {
	public Order getOne(String orderNumber);

	/**
	 * @param orderNumber
	 * @return
	 */
	public Order getOrderByNumber(String orderNumber);

	/**
	 * 获取订单支付，未支付金额
	 * 
	 * @return
	 */
	public Integer getAmountByPaystate(Integer payState);
	
	
	/**
	 * 获取订单数
	 */
	public Integer getCountByPaystate(Integer payState);
	
	/**
	 * 依据商品订单查询类，查询商品订单列表
	 * @param entity
	 * @return
	 */
	public List<Order> getPageByOrderAdminSearchVO(OrderAdminSearchVO entity);
	
	/**
	 * 依据商品订单查询类，查询商品订单导出列表
	 * @param entity
	 * @return
	 */
	public List<Order> getPageExportByOrderAdminSearchVO(OrderAdminSearchVO entity);
}