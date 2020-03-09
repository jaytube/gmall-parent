package com.rtu.gmall.cart.component;


import com.alibaba.fastjson.JSON;
import com.rtu.gmall.cart.vo.UserCartKey;
import com.rtu.gmall.constant.CartConstant;
import com.rtu.gmall.constant.SysCacheConstant;
import com.rtu.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class MemberComponent {

    @Autowired
    StringRedisTemplate redisTemplate;

    public Member getMemberByAccessToken(String accessToken) {
        String userJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        return JSON.parseObject(userJson, Member.class);
    }

    public UserCartKey getCartKey(String accessToken, String cartKey) {
        UserCartKey userCartKey = new UserCartKey();
        Member member = null;
        if(!StringUtils.isEmpty(accessToken))
            member = getMemberByAccessToken(accessToken);

        if(member != null) {
            userCartKey.setLogin(true);
            userCartKey.setUserId(member.getId());
            userCartKey.setFinalCartKey(CartConstant.USER_CART_KEY_PREFIX + member.getId());
            return userCartKey;
        }
        else if (!StringUtils.isEmpty(cartKey)) {
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX + cartKey);
            return userCartKey;
        }
        else {
            userCartKey.setLogin(false);
            String tk = UUID.randomUUID().toString().replace("-", "");
            userCartKey.setTempCartKey(tk);
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX + tk);
            return userCartKey;
        }
    }
}
