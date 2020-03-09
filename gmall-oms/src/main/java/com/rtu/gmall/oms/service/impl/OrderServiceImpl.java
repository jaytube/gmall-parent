package com.rtu.gmall.oms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.cart.service.CartService;
import com.rtu.gmall.cart.vo.CartItem;
import com.rtu.gmall.constant.OrderStatusEnume;
import com.rtu.gmall.constant.SysCacheConstant;
import com.rtu.gmall.oms.component.MemberComponent;
import com.rtu.gmall.oms.config.AlipayConfig;
import com.rtu.gmall.oms.entity.Order;
import com.rtu.gmall.oms.entity.OrderItem;
import com.rtu.gmall.oms.mapper.OrderItemMapper;
import com.rtu.gmall.oms.mapper.OrderMapper;
import com.rtu.gmall.oms.service.OrderService;
import com.rtu.gmall.pms.service.ProductService;
import com.rtu.gmall.ums.entity.Member;
import com.rtu.gmall.ums.entity.MemberReceiveAddress;
import com.rtu.gmall.ums.service.MemberService;
import com.rtu.gmall.vo.order.OrderConfirmVo;
import com.rtu.gmall.vo.order.OrderCreateVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Service
@Component
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Reference
    MemberService memberService;

    @Reference
    CartService cartService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    OrderMapper orderMapper;

    @Reference
    ProductService productService;

    @Autowired
    OrderItemMapper orderItemMapper;

    ThreadLocal<List<CartItem>> threadLocal = new ThreadLocal<>();

    @Override
    public OrderConfirmVo orderConfirm(Long id) {
        String accessToken = RpcContext.getContext().getAttachment("accessToken");
        OrderConfirmVo orderConfirm = new OrderConfirmVo();
        orderConfirm.setAddresses(memberService.getMemberAddress(id));

        List<CartItem> items = cartService.getCartItemsOfOrder(accessToken);
        orderConfirm.setItems(items);

        //设置订单的防重令牌
        String uuId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForSet().add(SysCacheConstant.ORDER_TOKEN, uuId);
        orderConfirm.setOrderToken(uuId);

        items.forEach((item) -> {
            orderConfirm.setCount(orderConfirm.getCount() + item.getCount());
            orderConfirm.setProductTotalPrice(orderConfirm.getProductTotalPrice().add(item.getTotalPrice()));
        });

        orderConfirm.setTotalPrice(orderConfirm.getProductTotalPrice().add(orderConfirm.getFreight()));

        return orderConfirm;
    }

    @Transactional
    @Override
    public OrderCreateVo createOrder(BigDecimal totalPrice, Long addressId, String note) {
        OrderCreateVo orderCreateVo = new OrderCreateVo();
        //防重
        String orderToken = RpcContext.getContext().getAttachment("orderToken");
        if(StringUtils.isBlank(orderToken)) {

        }

        Long remove = redisTemplate.opsForSet().remove(SysCacheConstant.ORDER_TOKEN);
        if(remove != 1) {
            orderCreateVo.setTokenSuccess(false);
            return orderCreateVo;
        }

        //隐式传参禁止传以下参数： token retries ... dubbo规定的
        String accessToken = RpcContext.getContext().getAttachment("accessToken");
        //验价
        if(!validatePrice(totalPrice, accessToken)) {
            OrderCreateVo vo = new OrderCreateVo();
            vo.setLimit(false);
            return vo;
        }

        String orderSn = IdWorker.getTimeId();
        Member member = memberComponent.getMemberByAccessToken(accessToken);

        orderCreateVo = initOrderCreateVo(orderCreateVo, totalPrice, orderSn, addressId, accessToken, member);

        //加工数据处理
        Order order = initOrder(addressId, note, member, orderSn);

        //订单保存
        orderMapper.insert(order);

        //构造保存订单项
        saveOrderItem(order);

        return orderCreateVo;
    }

    @Override
    public String pay(String orderSn, String accessToken) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        return payOrder(orderSn, order.getTotalAmount().toPlainString(), "aaa", "ddd");
    }

    private void saveOrderItem(Order order) {
        List<CartItem> cartItems = threadLocal.get();

        List<OrderItem> orderItems = new ArrayList<>();
        cartItems.forEach((i) -> {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderSn(order.getOrderSn());
            Long skuId = i.getSkuId();
            //用skuid查找product信息
            item.setProductPrice(i.getPrice());
            item.setProductQuantity(i.getCount());
            item.setProductSkuId(skuId);
            orderItems.add(item);
            orderItemMapper.insert(item);
        });
    }

    private OrderCreateVo initOrderCreateVo(OrderCreateVo orderCreateVo, BigDecimal totalPrice, String orderSn, Long addressId, String accessToken, Member member) {
        orderCreateVo.setOrderSn(orderSn);
        orderCreateVo.setAddressId(addressId);
        orderCreateVo.setItems(cartService.getCartItemsOfOrder(accessToken));
        orderCreateVo.setMemberId(member.getId());
        orderCreateVo.setTotalPrice(totalPrice);
        orderCreateVo.setDetailInfo("aaallalala");
        return orderCreateVo;
    }

    private Order initOrder(Long addressId, String note, Member member, String orderSn) {
        Order order = new Order();
        order.setOrderSn(orderSn);
        order.setMemberId(member.getId());
        order.setCreateTime(new Date());
        order.setAutoConfirmDay(7);
        order.setNote(note);
        order.setMemberUsername(member.getUsername());
        order.setFreightAmount(new BigDecimal(10));
        order.setStatus(OrderStatusEnume.UNPAY.getCode());
        MemberReceiveAddress address = memberService.getMemberAddressByAdressId(addressId);
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        return order;
    }

    private boolean validatePrice(BigDecimal totalPrice, String accessToken) {
        List<CartItem> items = cartService.getCartItemsOfOrder(accessToken);
        threadLocal.set(items);
        BigDecimal total = new BigDecimal(0);
        for (CartItem item : items) {
            total = total.add(item.getTotalPrice());
        }

        total = total.add(new BigDecimal(10));
        return totalPrice.compareTo(total) == 0;
    }

    // 支付
    private String payOrder(String out_trade_no, String total_amount, String subject, String body) {
        // 1、创建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id,
                AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key,
                AlipayConfig.sign_type);

        // 2、创建一次支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        // 付款金额，必填
        // 订单名称，必填
        // 商品描述，可空

        // 3、构造支付请求数据
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\"," + "\"total_amount\":\"" + total_amount
                + "\"," + "\"subject\":\"" + subject + "\"," + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = "";
        try {
            // 4、请求
            result = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;// 支付跳转页的代码

    }

}
