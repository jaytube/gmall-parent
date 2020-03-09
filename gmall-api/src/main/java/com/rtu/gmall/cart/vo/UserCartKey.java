package com.rtu.gmall.cart.vo;

import lombok.Data;

@Data
public class UserCartKey {

    private boolean login;
    private Long userId;
    private String tempCartKey;
    private String finalCartKey;

}
