package com.rtu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.pms.entity.ProductFullReduction;
import com.rtu.gmall.pms.mapper.ProductFullReductionMapper;
import com.rtu.gmall.pms.service.ProductFullReductionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 产品满减表(只针对同商品) 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Service
public class ProductFullReductionServiceImpl extends ServiceImpl<ProductFullReductionMapper, ProductFullReduction> implements ProductFullReductionService {

}
