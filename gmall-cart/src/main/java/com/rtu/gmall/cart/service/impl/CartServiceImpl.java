package com.rtu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.rtu.gmall.cart.component.MemberComponent;
import com.rtu.gmall.cart.vo.Cart;
import com.rtu.gmall.cart.vo.CartItem;
import com.rtu.gmall.cart.service.CartService;
import com.rtu.gmall.cart.vo.CartResponse;
import com.rtu.gmall.cart.vo.UserCartKey;
import com.rtu.gmall.constant.CartConstant;
import com.rtu.gmall.pms.entity.Product;
import com.rtu.gmall.pms.entity.SkuStock;
import com.rtu.gmall.pms.service.ProductService;
import com.rtu.gmall.pms.service.SkuStockService;
import com.rtu.gmall.ums.entity.Member;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Component
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    RedissonClient redisson;

    @Reference
    SkuStockService skuStockService;

    @Reference
    ProductService productService;

    @Override
    public CartResponse addToCart(Long skuId, Integer num, String cartKey, String accessToken) throws ExecutionException, InterruptedException {
        //根据accesstoken获取用户id
        Member member = memberComponent.getMemberByAccessToken(accessToken);
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        if(member != null && !StringUtils.isEmpty(cartKey))
            mergeCart(cartKey, member.getId());


        //获取用户能挣够拿到的购物车
        String finalCartKey = userCartKey.getFinalCartKey();
        CartItem cartItem = addItemToCart(skuId, num, finalCartKey);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        cartResponse.setCart(listCart(cartKey, accessToken).getCart());
        cartResponse.setCartKey(userCartKey.getTempCartKey());

        return cartResponse;
    }

    @Override
    public CartResponse updateCartItem(Long skuId, Integer num, String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redisson.getMap(finalCartKey);
        String json = map.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        map.put(skuId.toString(), s);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        return cartResponse;
    }

    @Override
    public CartResponse listCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        if(userCartKey.isLogin())
            mergeCart(cartKey, userCartKey.getUserId());

        String finalKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redisson.getMap(finalKey);
        Cart cart = new Cart();
        List<CartItem> cartList = new ArrayList<>();
        CartResponse response = new CartResponse();
        if(CollectionUtils.isEmpty(map)) {
            map.entrySet().forEach((e) -> {
                if(e.getKey().equalsIgnoreCase(CartConstant.CART_CHECK_KEY)) {
                    String value = e.getValue();
                    CartItem ci = JSON.parseObject(value, CartItem.class);
                    cartList.add(ci);
                }
            });
            cart.setCartItems(cartList);
        } else {
            response.setCartKey(userCartKey.getTempCartKey());
        }
        response.setCart(cart);
        return response;
    }

    @Override
    public CartResponse delCartItem(Long skuId, String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);

        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redisson.getMap(finalCartKey);
        checkItems(Arrays.asList(skuId), false, finalCartKey);
        map.remove(skuId.toString());

        CartResponse response = listCart(cartKey, accessToken);
        return response;
    }

    @Override
    public CartResponse clearCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redisson.getMap(finalKey);
        map.clear();
        return new CartResponse();
    }

    @Override
    public CartResponse checkItems(String skuIds, Integer ops, String cartKey, String accessToken) {
        if(StringUtils.isEmpty(skuIds))
            return new CartResponse();

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redisson.getMap(finalKey);
        List<Long> skuList = new ArrayList<>();
        boolean checked = ops == 1 ? true : false;

        String[] splits = skuIds.split(",");
        for (String split : splits) {
            Long id = Long.parseLong(split);
            skuList.add(id);
            if(!CollectionUtils.isEmpty(map)) {
                String jsonVal = map.get(split);
                CartItem item = JSON.parseObject(jsonVal, CartItem.class);
                item.setCheck(checked);

                map.put(split, JSON.toJSONString(item));
            }
        }
        //为了快速找到勾选状态的item，维护了check数组在redis中
        checkItems(skuList, checked, finalKey);

        return listCart(cartKey, accessToken);
    }

    @Override
    public List<CartItem> getCartItemsOfOrder(String accessToken) {
        return null;
    }

    private void mergeCart(String cartKey, Long id) {
        String oldKey = CartConstant.TEMP_CART_KEY_PREFIX + cartKey;
        String newKey = CartConstant.USER_CART_KEY_PREFIX + id.toString();

        //获取老购物车数据
        RMap<String, String> map = redisson.getMap(oldKey);
        if(CollectionUtils.isEmpty(map))
            return;

        map.entrySet().forEach((item) -> {
            if(item.getKey().equalsIgnoreCase(CartConstant.CART_CHECK_KEY)) {
                String key = item.getKey();
                String value = item.getValue();
                CartItem ci = JSON.parseObject(value, CartItem.class);
                try {
                    addItemToCart(Long.parseLong(key), ci.getCount(), newKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        map.clear();
    }

    private CartItem addItemToCart(Long skuId, Integer num, String cartKey) throws ExecutionException, InterruptedException {
        CartItem newItem = new CartItem();
        CompletableFuture<Void> skuFuture = CompletableFuture.supplyAsync(() -> {
            SkuStock skuStock = skuStockService.getById(skuId);
            return skuStock;
        }).thenAcceptAsync((r) -> {
            Product product = productService.getById(r.getProductId());
            BeanUtils.copyProperties(r, newItem);
            newItem.setSkuId(skuId);
            newItem.setName(product.getName());
            newItem.setCount(num);
        });

        //查出购物车对应的购物项
        RMap<String, String> map = redisson.getMap(cartKey);
        skuFuture.get();
        String itemJson = map.get(skuId.toString());
        if(!StringUtils.isEmpty(itemJson)) {
            //数量叠加
            CartItem oldItem = JSON.parseObject(itemJson, CartItem.class);
            Integer count = oldItem.getCount();
            newItem.setCount(count + newItem.getCount());
            String json = JSON.toJSONString(newItem);
            map.put(skuId.toString(), json);
        } else {
            //新增购物项
            String json = JSON.toJSONString(newItem);
            map.put(skuId.toString(), json);
        }
        checkItems(Arrays.asList(skuId), true, cartKey);
        return newItem;
    }

    private void checkItems(List<Long> skuIdList, boolean checked, String finalCartKey) {
        RMap<String, String> map = redisson.getMap(finalCartKey);
        String checkedJson = map.get(CartConstant.CART_CHECK_KEY);
        Set<Long> checkedSet = JSON.parseObject(checkedJson, new TypeReference<Set<Long>>() { });
        if(CollectionUtils.isEmpty(checkedSet))
            checkedSet = new LinkedHashSet<>();

        if(checked) {
            checkedSet.addAll(skuIdList);
        } else {
            checkedSet.removeAll(skuIdList);
        }
        map.put(CartConstant.CART_CHECK_KEY, JSON.toJSONString(checkedSet));
    }
}
