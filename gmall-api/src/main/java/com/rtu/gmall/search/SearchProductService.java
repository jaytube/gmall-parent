package com.rtu.gmall.search;

import com.rtu.gmall.vo.search.SearchParam;
import com.rtu.gmall.vo.search.SearchResponse;

public interface SearchProductService {

    SearchResponse searchProduct(SearchParam param);
}
