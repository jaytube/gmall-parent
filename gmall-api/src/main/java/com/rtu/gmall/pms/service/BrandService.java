package com.rtu.gmall.pms.service;

import com.rtu.gmall.pms.entity.Brand;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rtu.gmall.vo.PageInfoVo;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
public interface BrandService extends IService<Brand> {

    PageInfoVo brandPageInfo(String keyword, Integer pageNum, Integer pageSize);
}
