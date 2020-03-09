package com.rtu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.pms.entity.ProductAttribute;
import com.rtu.gmall.pms.mapper.ProductAttributeMapper;
import com.rtu.gmall.pms.service.ProductAttributeService;
import com.rtu.gmall.vo.PageInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 商品属性参数表 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Component
@Service
public class ProductAttributeServiceImpl extends ServiceImpl<ProductAttributeMapper, ProductAttribute> implements ProductAttributeService {

    @Autowired
    ProductAttributeMapper productAttributeMapper;

    @Override
    public PageInfoVo getCategoryAttributes(Long cid, Integer type, Integer pageSize, Integer pageNum) {
        QueryWrapper<ProductAttribute> eq = new QueryWrapper<ProductAttribute>().eq("product_attribute_category_id", cid)
                .eq("type", type);
        IPage<ProductAttribute> productAttributeIPage = productAttributeMapper.selectPage(new Page<ProductAttribute>(pageNum, pageSize), eq);
        return PageInfoVo.getVo(productAttributeIPage, pageSize.longValue());
    }
}
