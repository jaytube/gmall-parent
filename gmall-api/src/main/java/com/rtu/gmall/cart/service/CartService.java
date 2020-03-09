package com.rtu.gmall.cart.service;

import com.rtu.gmall.cart.vo.CartItem;
import com.rtu.gmall.cart.vo.CartResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartResponse addToCart(Long skuId, Integer num, String cartKey, String accessToken) throws ExecutionException, InterruptedException;

    CartResponse updateCartItem(Long skuId, Integer num, String cartKey, String accessToken);

    CartResponse listCart(String cartKey, String accessToken);

    CartResponse delCartItem(Long skuId, String cartKey, String accessToken);

    CartResponse clearCart(String cartKey, String accessToken);

    CartResponse checkItems(String skuIds, Integer ops, String cartKey, String accessToken);

    List<CartItem> getCartItemsOfOrder(String accessToken);
}
