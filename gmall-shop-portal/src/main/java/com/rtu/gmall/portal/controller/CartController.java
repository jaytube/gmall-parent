package com.rtu.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.rtu.gmall.cart.vo.CartItem;
import com.rtu.gmall.cart.service.CartService;
import com.rtu.gmall.cart.vo.CartResponse;
import com.rtu.gmall.to.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    CartService cartService;

    @PostMapping("/add")
    public CommonResult addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam(value="num", defaultValue = "1") Integer num,
                            @RequestParam(value="cartKey", required = false) String cartKey,
                            @RequestParam(value="accessToken", required = false) String accessToken) throws ExecutionException, InterruptedException {

        CartResponse resp = cartService.addToCart(skuId, num, cartKey, accessToken);
        return new CommonResult().success(resp);
    }

    @PostMapping("/update")
    public CommonResult updateCartNum(@RequestParam("skuId") Long skuId,
                                  @RequestParam(value="num", defaultValue = "1") Integer num,
                                  @RequestParam(value="cartKey", required = false) String cartKey,
                                  @RequestParam(value="accessToken", required = false) String accessToken) {

        CartResponse resp = cartService.updateCartItem(skuId, num, cartKey, accessToken);
        return new CommonResult().success(resp);
    }

    @GetMapping("/list")
    public CommonResult cartList(@RequestParam(value="cartKey", required = false) String cartKey,
                                 @RequestParam(value="accessToken", required = false) String accessToken) {
        CartResponse response = cartService.listCart(cartKey, accessToken);
        return new CommonResult().success(response);
    }

    @GetMapping("/del")
    public CommonResult delCart(@RequestParam("skuId") Long skuId,
                                @RequestParam(value="cartKey", required = false) String cartKey,
                                @RequestParam(value="accessToken", required = false) String accessToken) {
        CartResponse response = cartService.delCartItem(skuId, cartKey, accessToken);
        return new CommonResult().success(response);
    }

    @GetMapping("/clear")
    public CommonResult clearCart(@RequestParam(value="cartKey", required = false) String cartKey,
                                @RequestParam(value="accessToken", required = false) String accessToken) {
        CartResponse response = cartService.clearCart(cartKey, accessToken);
        return new CommonResult().success(response);
    }

    @PostMapping("/check")
    public CommonResult cartCheck(@RequestParam("skuIds") String skuIds,
                                  @RequestParam(value = "ops", defaultValue = "1") Integer ops,
                                  @RequestParam(value="cartKey", required = false) String cartKey,
                                  @RequestParam(value="accessToken", required = false) String accessToken) {
        CartResponse response = cartService.checkItems(skuIds, ops, cartKey, accessToken);
        return new CommonResult().success(response);
    }

}
