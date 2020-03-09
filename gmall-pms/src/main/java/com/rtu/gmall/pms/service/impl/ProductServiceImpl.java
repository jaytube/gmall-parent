package com.rtu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rtu.gmall.constant.EsConstant;
import com.rtu.gmall.pms.entity.Product;
import com.rtu.gmall.pms.entity.ProductAttribute;
import com.rtu.gmall.pms.entity.SkuStock;
import com.rtu.gmall.pms.mapper.ProductAttributeMapper;
import com.rtu.gmall.pms.mapper.ProductMapper;
import com.rtu.gmall.pms.mapper.SkuStockMapper;
import com.rtu.gmall.pms.service.ProductService;
import com.rtu.gmall.to.es.EsProduct;
import com.rtu.gmall.to.es.EsProductAttributeValue;
import com.rtu.gmall.to.es.EsSkuProductInfo;
import com.rtu.gmall.vo.PageInfoVo;
import com.rtu.gmall.vo.product.PmsProductQueryParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author tuxiaoyue
 * @since 2020-02-22
 */
@Slf4j
@Service
@Component
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    SkuStockMapper skuStockMapper;

    @Autowired
    ProductAttributeMapper productAttributeMapper;

    @Autowired
    JestClient jestClient;

    @Override
    public Product productInfo(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public PageInfoVo productPageInfo(PmsProductQueryParam param) {
        QueryWrapper wrapper = new QueryWrapper();
        if(param.getBrandId() != null)
            wrapper.eq("brand_id", param.getBrandId());

        if(StringUtils.isNotBlank(param.getKeyword()))
            wrapper.like("name", param.getKeyword());

        if(param.getProductCategoryId() != null)
            wrapper.eq("product_category_id", param.getProductCategoryId());

        if(StringUtils.isNotBlank(param.getProductSn()))
            wrapper.like("product_sn", param.getProductSn());

        if(param.getPublishStatus() != null)
            wrapper.eq("publish_status", param.getPublishStatus());

        if(param.getVerifyStatus() != null)
            wrapper.eq("verify_status", param.getVerifyStatus());

        IPage<Product> page = productMapper.selectPage(new Page<Product>(param.getPageNum(), param.getPageSize()),
                wrapper);
        PageInfoVo pageInfoVo = new PageInfoVo(page.getTotal(), page.getPages(), param.getPageSize(),
                page.getRecords(), page.getCurrent());
        return pageInfoVo;
    }

    @Override
    public void updatePublishStatus(List<Long> ids, Integer publishStatus) {
        if(publishStatus == 0) {
            //下架, 改数据库状态 删es
            ids.forEach((id) -> {
                updateProductPushblishStatus(publishStatus, id);
                deleteProductsToEs(id);
            });
        } else {
            //上架, 改数据库状态 添加到es
            ids.forEach((id) -> {
                updateProductPushblishStatus(publishStatus, id);
                saveProductsToEs(publishStatus, id);
            });
        }

        //对于数据库，修改商品的状态位

    }

    @Override
    public EsProduct allProductInfo(Long id) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("id", id));
        Search search = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX).addType(EsConstant.PRODUCT_ES_TYPE).build();
        try {
            SearchResult execute = jestClient.execute(search);
            if(execute != null && execute.getTotal() != 0)
                return execute.getHits(EsProduct.class).get(0).source;
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public EsProduct allSkuInfo(Long id) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.nestedQuery("skuProductInfos", QueryBuilders.termQuery("skuProductInfos.id", id), ScoreMode.None));
        Search search = new Search.Builder(builder.toString()).addIndex(EsConstant.PRODUCT_ES_INDEX).addType(EsConstant.PRODUCT_ES_TYPE).build();
        try {
            SearchResult execute = jestClient.execute(search);
            if(execute != null && execute.getTotal() != 0)
                return execute.getHits(EsProduct.class).get(0).source;
        } catch (IOException e) {
        }
        return null;
    }

    private void deleteProductsToEs(Long id) {
        Delete delete = new Delete.Builder(id.toString())
                .index(EsConstant.PRODUCT_ES_INDEX).type(EsConstant.PRODUCT_ES_TYPE).build();
        try {
            DocumentResult execute = jestClient.execute(delete);
            if(execute.isSucceeded())
                log.info("es删除产品{}成功", id);
            else
                log.error("ES产品{}删除失败", id);
        } catch (Exception e) {
            log.error("ES产品{}删除失败, error", id, e.getMessage());
        }
    }

    private void saveProductsToEs(Integer publishStatus, Long id) {
        //1. 查出商品的基本信息
        Product productInfo = productInfo(id);
        productInfo.setPublishStatus(publishStatus);

        EsProduct esProduct = new EsProduct();
        //复制基本信息
        BeanUtils.copyProperties(productInfo, esProduct);
        List<SkuStock> stocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));

        //查出当前商品的sku属性
        List<ProductAttribute> skuAttrNames = productAttributeMapper.selectProductSaleAttrName(id);

        List<EsSkuProductInfo> esSkuProductInfos = new ArrayList<>(stocks.size());
        stocks.forEach((stock) -> {
            EsSkuProductInfo esSkuProductInfo = new EsSkuProductInfo();
            BeanUtils.copyProperties(stock, esSkuProductInfo);
            String subTitle = esProduct.getName();
            if(StringUtils.isNotBlank(stock.getSp2()))
                subTitle += " " + stock.getSp2();
            if(StringUtils.isNotBlank(stock.getSp3()))
                subTitle += " " + stock.getSp1();
            if(StringUtils.isNotBlank(stock.getSp3()))
                subTitle += " " + stock.getSp1();
            esSkuProductInfo.setSkuTitle(subTitle);

            List<EsProductAttributeValue> skuValues = new ArrayList<>();
            for (int i = 0; i < skuAttrNames.size(); i++) {
                EsProductAttributeValue value = new EsProductAttributeValue();
                value.setName(skuAttrNames.get(i).getName());
                value.setType(skuAttrNames.get(i).getType());
                value.setProductAttributeId(skuAttrNames.get(i).getId());
                value.setProductId(id);
                if(i == 0)
                    value.setValue(stock.getSp1());
                if(i == 1)
                    value.setValue(stock.getSp2());
                if(i == 2)
                    value.setValue(stock.getSp3());
                skuValues.add(value);
            }
            esSkuProductInfo.setAttributeValues(skuValues);
            esSkuProductInfos.add(esSkuProductInfo);

        });

        esProduct.setSkuProductInfos(esSkuProductInfos);

        //3.查出商品的公共属性
        List<EsProductAttributeValue> baseAttrList = productAttributeMapper.selectProductBaseAttrAndName(id);
        esProduct.setAttrValueList(baseAttrList);

        //添加至es
        try {
            Index index = new Index.Builder(esProduct).index(EsConstant.PRODUCT_ES_INDEX).type(EsConstant.PRODUCT_ES_TYPE)
                    .id(id.toString()).build();
            DocumentResult execute = jestClient.execute(index);
            if(execute.isSucceeded())
                log.info("产品{}上架成功", id);
            else
                log.error("产品{}上架失败", id);

        } catch(Exception e) {
            log.error("产品{}上架失败, {}", id, e.getMessage());
        }
    }

    private void updateProductPushblishStatus(Integer publishStatus, Long id) {
        Product product = new Product();
        product.setId(id);
        product.setPublishStatus(publishStatus);
        //mybatis-plus 不更新null字段
        productMapper.updateById(product);
    }

}
