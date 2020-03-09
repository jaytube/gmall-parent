package com.rtu.gmall.cart.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Setter
public class CartItem implements Serializable {

    @Getter
    private Long skuId;
    @Getter
    private String name;
    @Getter
    private String skuCode;
    @Getter
    private Integer stock;
    @Getter
    private String sp1;
    @Getter
    private String sp2;
    @Getter
    private String sp3;
    @Getter
    private String pic;
    @Getter
    private BigDecimal price;
    @Getter
    private BigDecimal promotionPrice;

    @Getter
    private boolean check=true;

    @Getter
    private Integer count;

    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        totalPrice = price.multiply(new BigDecimal(count));
        return totalPrice;
    }
}
