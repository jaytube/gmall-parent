package com.rtu.gmall.pms.service;

import com.rtu.gmall.pms.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rtu.gmall.to.es.EsProduct;
import com.rtu.gmall.vo.PageInfoVo;
import com.rtu.gmall.vo.product.PmsProductQueryParam;

import java.util.List;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
public interface ProductService extends IService<Product> {

    Product productInfo(Long id);

    PageInfoVo productPageInfo(PmsProductQueryParam param);

    void updatePublishStatus(List<Long> ids, Integer publishStatus);

    EsProduct allProductInfo(Long id);

    EsProduct allSkuInfo(Long id);
}
