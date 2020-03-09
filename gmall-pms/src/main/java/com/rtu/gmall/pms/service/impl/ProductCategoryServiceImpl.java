package com.rtu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.pms.entity.ProductCategory;
import com.rtu.gmall.pms.mapper.ProductCategoryMapper;
import com.rtu.gmall.pms.service.ProductCategoryService;
import com.rtu.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rtu.gmall.constant.SysCacheConstant.CATEGORY_MENU_CACHE_KEY;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Component
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    ProductCategoryMapper productCategoryMapper;

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Override
    public List<PmsProductCategoryWithChildrenItem> listCategoryWithChildren(Integer i) {
        Object cache = redisTemplate.opsForValue().get(CATEGORY_MENU_CACHE_KEY);
        List<PmsProductCategoryWithChildrenItem> items;
        if(cache != null)
            items = (List<PmsProductCategoryWithChildrenItem>) cache;
        else
            items = productCategoryMapper.listCategoryWithChildren(i);

        return items;
    }
}
