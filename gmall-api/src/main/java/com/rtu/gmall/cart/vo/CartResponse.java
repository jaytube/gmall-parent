package com.rtu.gmall.cart.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartResponse implements Serializable {

    private Cart cart;
    private CartItem cartItem;

    private String cartKey;
}
