package com.rtu.gmall.admin.ums.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.rtu.gmall.to.CommonResult;
import com.rtu.gmall.ums.entity.MemberLevel;
import com.rtu.gmall.ums.service.MemberLevelService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class UmsMemberLevelController {

    @Reference
    MemberLevelService memberLevelService;

    @GetMapping("/memberLevel/list")
    public CommonResult memberLevelList() {
        List<MemberLevel> memberLevels = memberLevelService.list();
        return new CommonResult().success(memberLevels);
    }
}
