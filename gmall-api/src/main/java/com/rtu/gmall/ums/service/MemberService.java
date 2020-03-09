package com.rtu.gmall.ums.service;

import com.rtu.gmall.ums.entity.Member;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rtu.gmall.ums.entity.MemberReceiveAddress;

import java.util.List;

/**
 * <p>
 * 会员表 服务类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
public interface MemberService extends IService<Member> {

    List<MemberReceiveAddress> getMemberAddress(Long id);

    MemberReceiveAddress getMemberAddressByAdressId(Long addressId);
}
