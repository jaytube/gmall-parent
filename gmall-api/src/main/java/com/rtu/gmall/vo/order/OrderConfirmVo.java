package com.rtu.gmall.vo.order;

import com.rtu.gmall.cart.vo.CartItem;
import com.rtu.gmall.sms.entity.Coupon;
import com.rtu.gmall.ums.entity.MemberReceiveAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVo {

    private List<CartItem> items;
    private List<MemberReceiveAddress> addresses;
    private List<Coupon> coupons;

    private String orderToken;

    private BigDecimal totalPrice = new BigDecimal(0);
    private BigDecimal productTotalPrice = new BigDecimal(0);
    private Integer count = 0;
    private BigDecimal discount = new BigDecimal(0);
    private BigDecimal freight = new BigDecimal(10);
}
