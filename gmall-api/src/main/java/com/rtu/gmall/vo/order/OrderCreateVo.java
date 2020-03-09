package com.rtu.gmall.vo.order;

import com.rtu.gmall.cart.vo.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateVo {

    private String orderSn;
    private BigDecimal totalPrice = new BigDecimal(0);
    private Long addressId;
    private String detailInfo;
    private Long memberId;
    private List<CartItem> items;

    //用于验价
    private Boolean limit;
    //令牌验证是否成功
    private Boolean tokenSuccess;
}
