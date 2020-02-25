package com.rtu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.pms.entity.Brand;
import com.rtu.gmall.pms.mapper.BrandMapper;
import com.rtu.gmall.pms.service.BrandService;
import com.rtu.gmall.vo.PageInfoVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Service
@Component
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Autowired
    BrandMapper brandMapper;

    @Override
    public PageInfoVo brandPageInfo(String keyword, Integer pageNum, Integer pageSize) {
        QueryWrapper wrapper = null;
        if(StringUtils.isNotBlank(keyword)) {
            wrapper = new QueryWrapper();
            wrapper.like("name", keyword);
        }
        IPage<Brand> page = brandMapper.selectPage(new Page<>(pageNum.longValue(), pageSize.longValue()), wrapper);
        PageInfoVo vo = new PageInfoVo(page.getTotal(), page.getPages(), pageSize.longValue(), page.getRecords(), page.getCurrent());
        return vo;
    }
}
