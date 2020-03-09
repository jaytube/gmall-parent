package com.rtu.gmall.pms.mapper;

import com.rtu.gmall.pms.entity.ProductAttribute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rtu.gmall.to.es.EsProductAttributeValue;

import java.util.List;

/**
 * <p>
 * 商品属性参数表 Mapper 接口
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
public interface ProductAttributeMapper extends BaseMapper<ProductAttribute> {

    List<EsProductAttributeValue> selectProductBaseAttrAndName(Long id);

    List<ProductAttribute> selectProductSaleAttrName(Long id);
}
