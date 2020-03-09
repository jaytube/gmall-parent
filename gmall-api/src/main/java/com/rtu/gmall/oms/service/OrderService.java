package com.rtu.gmall.oms.service;

import com.rtu.gmall.oms.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rtu.gmall.vo.order.OrderConfirmVo;
import com.rtu.gmall.vo.order.OrderCreateVo;

import java.math.BigDecimal;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
public interface OrderService extends IService<Order> {

    OrderConfirmVo orderConfirm(Long id);

    OrderCreateVo createOrder(BigDecimal totalPrice, Long addressId, String note);

    String pay(String orderSn, String accessToken);
}
