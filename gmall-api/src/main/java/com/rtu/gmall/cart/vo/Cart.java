package com.rtu.gmall.cart.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Setter
public class Cart {

    @Getter
    List<CartItem> cartItems;
    private Integer count;
    private BigDecimal totalPrice;

    public Integer getCount() {
        if(CollectionUtils.isEmpty(cartItems))
            return 0;

        AtomicInteger all = new AtomicInteger(0);
        cartItems.forEach((item) -> {
            all.getAndAdd(item.getCount());
        });
        return all.get();
    }

    public BigDecimal getTotalPrice() {
        if(CollectionUtils.isEmpty(cartItems))
            return new BigDecimal(0.0);

        AtomicReference<BigDecimal> t = new AtomicReference<>(new BigDecimal(0));
        cartItems.forEach((c) -> {
            BigDecimal add = t.get().add(c.getTotalPrice());
            t.set(add);
        });
        return t.get();
    }
}
