package com.rtu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.pms.entity.ProductAttributeCategory;
import com.rtu.gmall.pms.mapper.ProductAttributeCategoryMapper;
import com.rtu.gmall.pms.service.ProductAttributeCategoryService;
import com.rtu.gmall.vo.PageInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 产品属性分类表 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */

@Service
@Component
public class ProductAttributeCategoryServiceImpl extends ServiceImpl<ProductAttributeCategoryMapper, ProductAttributeCategory> implements ProductAttributeCategoryService {

    @Autowired
    ProductAttributeCategoryMapper productAttributeCategoryMapper;

    @Override
    public PageInfoVo productAttributeCategoryPageInfo(Integer pageSize, Integer pageNum) {
        IPage<ProductAttributeCategory> productAttributeCategoryIPage = productAttributeCategoryMapper
                .selectPage(new Page<ProductAttributeCategory>(pageNum, pageSize), null);

        PageInfoVo pageInfoVo = PageInfoVo.getVo(productAttributeCategoryIPage, pageSize.longValue());
        return pageInfoVo;
    }
}
