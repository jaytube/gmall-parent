package com.rtu.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.rtu.gmall.constant.SysCacheConstant;
import com.rtu.gmall.oms.service.OrderService;
import com.rtu.gmall.to.CommonResult;
import com.rtu.gmall.ums.entity.Member;
import com.rtu.gmall.vo.order.OrderConfirmVo;
import com.rtu.gmall.vo.order.OrderCreateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController("/order")
public class OrderController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Reference
    OrderService orderService;

    @GetMapping("/confirm")
    public CommonResult orderConfirm(@RequestParam("accessToken") String accessToken) {
        if(StringUtils.isEmpty(accessToken))
            return new CommonResult().failed();

        String json = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if(StringUtils.isEmpty(json))
            return new CommonResult().failed();

        Member member = JSON.parseObject(json, Member.class);
        if(member == null)
            return new CommonResult().failed();

        RpcContext.getContext().setAttachment("accessToken", accessToken);
        OrderConfirmVo orderConfirm = orderService.orderConfirm(member.getId());

        return new CommonResult().success(orderConfirm);
    }

    @PostMapping("/create")
    public CommonResult createOrder(@RequestParam("accessToken") String accessToken,
                                    @RequestParam("totalPrice") BigDecimal totalPrice,
                                    @RequestParam("addressId") Long addressId,
                                    @RequestParam(value = "note", required = false) String note,
                                    @RequestParam("orderToken") String orderToken) {
        //创建订单要生成订单和订单项
        RpcContext.getContext().setAttachment("accessToken", accessToken);
        RpcContext.getContext().setAttachment("orderToken", orderToken);

        //防重复
        OrderCreateVo createVo = orderService.createOrder(totalPrice, addressId, note);
        if(!createVo.getTokenSuccess())
            return new CommonResult().failed();

        return new CommonResult().success(createVo);
    }

    @GetMapping(value="/pay", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn,
                           @RequestParam("accessToken") String accessToken) {
        String resp = orderService.pay(orderSn, accessToken);
        return resp;
    }
}
