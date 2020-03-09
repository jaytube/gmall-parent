package com.rtu.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.rtu.gmall.search.SearchProductService;
import com.rtu.gmall.vo.search.SearchParam;
import com.rtu.gmall.vo.search.SearchResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductSearchController {

    @Reference
    SearchProductService searchProductService;

    @GetMapping("/search")
    public SearchResponse productSearch(SearchParam param) {
        SearchResponse response = searchProductService.searchProduct(param);
        return response;
    }
}
