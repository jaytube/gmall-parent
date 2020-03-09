package com.rtu.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.rtu.gmall.pms.service.ProductService;
import com.rtu.gmall.to.CommonResult;
import com.rtu.gmall.to.es.EsProduct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductItemController {

    @Reference
    ProductService productService;

    @GetMapping("/item/{id}.html")
    public CommonResult productAllInfo(@PathVariable("id") Long id) {
        EsProduct product = productService.allProductInfo(id);
        return new CommonResult().success(product);
    }

    @GetMapping("/item/sku/{id}.html")
    public CommonResult skuAllInfo(@PathVariable("id") Long id) {
        EsProduct sku = productService.allSkuInfo(id);
        return new CommonResult().success(sku);
    }
}
