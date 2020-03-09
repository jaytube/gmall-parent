package com.rtu.gmall.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.ums.entity.Member;
import com.rtu.gmall.ums.entity.MemberReceiveAddress;
import com.rtu.gmall.ums.mapper.MemberMapper;
import com.rtu.gmall.ums.mapper.MemberReceiveAddressMapper;
import com.rtu.gmall.ums.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    MemberReceiveAddressMapper addressMapper;

    @Override
    public List<MemberReceiveAddress> getMemberAddress(Long id) {
        return addressMapper.selectList(new QueryWrapper<MemberReceiveAddress>().eq("member_id", id));
    }

    @Override
    public MemberReceiveAddress getMemberAddressByAdressId(Long addressId) {
        return addressMapper.selectById(addressId);
    }
}
