package com.rtu.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.ums.entity.Admin;
import com.rtu.gmall.ums.mapper.AdminMapper;
import com.rtu.gmall.ums.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.DigestUtils;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Component
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin login(String username, String password) {
        String encodedPwd = DigestUtils.md5DigestAsHex(password.getBytes());
        QueryWrapper<Admin> wrapper = new QueryWrapper<Admin>().eq("username", username).eq("password", encodedPwd);
        Admin admin = adminMapper.selectOne(wrapper);
        return admin;
    }

    @Override
    public Admin getUserInfo(String username) {
        QueryWrapper<Admin> wrapper = new QueryWrapper<Admin>().eq("username", username);
        Admin admin = adminMapper.selectOne(wrapper);
        return admin;
    }
}
