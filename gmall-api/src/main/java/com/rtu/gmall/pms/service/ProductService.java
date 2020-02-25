package com.rtu.gmall.pms.service;

import com.rtu.gmall.pms.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rtu.gmall.vo.PageInfoVo;
import com.rtu.gmall.vo.product.PmsProductQueryParam;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
public interface ProductService extends IService<Product> {

    PageInfoVo productPageInfo(PmsProductQueryParam param);
}
