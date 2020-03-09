package com.rtu.gmall.oms.component;

import com.alibaba.fastjson.JSON;
import com.rtu.gmall.constant.SysCacheConstant;
import com.rtu.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MemberComponent {

    @Autowired
    StringRedisTemplate redisTemplate;

    public Member getMemberByAccessToken(String token) {
        String memberJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + token);
        if(StringUtils.isEmpty(memberJson))
            return null;

        return JSON.parseObject(memberJson, Member.class);
    }
}
