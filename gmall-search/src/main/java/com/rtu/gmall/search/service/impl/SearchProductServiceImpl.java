package com.rtu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.rtu.gmall.constant.EsConstant;
import com.rtu.gmall.search.SearchProductService;
import com.rtu.gmall.to.es.EsProduct;
import com.rtu.gmall.vo.search.SearchParam;
import com.rtu.gmall.vo.search.SearchResponse;
import com.rtu.gmall.vo.search.SearchResponseAttrVo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.*;
import static org.elasticsearch.search.sort.SortBuilders.fieldSort;


@Slf4j
@Service
@Component
public class SearchProductServiceImpl implements SearchProductService {
    @Autowired
    JestClient jestClient;

    @Override
    public SearchResponse searchProduct(SearchParam param) {
        //构建检索条件
        String dsl = buildDsl(param);
        log.info(dsl);
        //搜索
        Search search = new Search.Builder(dsl).addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_ES_TYPE)
                .build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
        }

        //封装
        SearchResponse response = buildSearchReponse(execute);
        response.setPageSize(param.getPageSize());
        response.setPageNum(param.getPageNum());
        return response;
    }

    private SearchResponse buildSearchReponse(SearchResult execute) {
        SearchResponse response = new SearchResponse();

        MetricAggregation aggregations = execute.getAggregations();
        //set attributes
        TermsAggregation attrNameAgg = aggregations.getChildrenAggregation("attr_agg").getTermsAggregation("attr_name");
        List<SearchResponseAttrVo> attrList = new ArrayList<>();
        attrNameAgg.getBuckets().forEach((bucket) -> {
            SearchResponseAttrVo vo = new SearchResponseAttrVo();
            //属性名
            String attrName = bucket.getKeyAsString();
            vo.setName(attrName);

            //属性值
            TermsAggregation attrValueAgg = bucket.getTermsAggregation("attrValue_agg");
            List<String> valList = new ArrayList<>();
            attrValueAgg.getBuckets().forEach((vb) -> {
                String val = vb.getKeyAsString();
                valList.add(val);
            });
            vo.setValue(valList);

            //属性id
            TermsAggregation attrIdAgg = bucket.getTermsAggregation("attrId_agg");
            Long attrId = Long.parseLong(attrIdAgg.getBuckets().get(0).getKeyAsString());
            vo.setProductAttributeId(attrId);

            attrList.add(vo);
        });
        response.setAttrs(attrList);

        //set brand info
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brand_agg");
        List<String> brandNames = new ArrayList();
        brandAgg.getBuckets().forEach((bucket) -> {
            brandNames.add(bucket.getKeyAsString());
        });
        SearchResponseAttrVo brandVo = new SearchResponseAttrVo();
        brandVo.setName("brand");
        brandVo.setValue(brandNames);
        response.setBrand(brandVo);

        //set cate info
        TermsAggregation cateAgg = aggregations.getTermsAggregation("cate_agg");
        List<String> cateInfos = new ArrayList<>();
        cateAgg.getBuckets().forEach((cate) -> {
            String cateName = cate.getKeyAsString();
            String cateId = cate.getTermsAggregation("cateId_agg").getBuckets().get(0).getKeyAsString();
            Map<String, String> map = new HashMap<>();
            map.put("name", cateName);
            map.put("id", cateId);
            String cateInfo = JSON.toJSONString(map);
            cateInfos.add(cateInfo);
        });
        SearchResponseAttrVo cateVo = new SearchResponseAttrVo();
        cateVo.setName("category");
        cateVo.setValue(cateInfos);
        response.setCatelog(cateVo);

        //set products
        List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
        List<EsProduct> products = new ArrayList<>();
        hits.forEach((hit) -> {
            EsProduct ep = hit.source;
            String title = hit.highlight.get("skuProductInfos.skuTitle").get(0);
            ep.setName(title);
            products.add(ep);
        });
        response.setProducts(products);

        //set total
        response.setTotal(execute.getTotal());
        return response;
    }

    private String buildDsl(SearchParam param) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = boolQuery();
        //1. 查询
          //1.1 检索
        if(StringUtils.isNotBlank(param.getKeyword())) {
            MatchQueryBuilder matchQuery = matchQuery("skuProductInfos.skuTitle", param.getKeyword());
            NestedQueryBuilder nestedQuery = nestedQuery("skuProductInfos", matchQuery, ScoreMode.None);
            boolQuery.must(nestedQuery);
        }
          //1.2 过滤
        if(ArrayUtils.isNotEmpty(param.getCatelog3()))
            boolQuery.filter(termsQuery("productCategoryId", param.getCatelog3()));

        if(ArrayUtils.isNotEmpty(param.getBrand()))
            boolQuery.filter(termsQuery("brandName.keyWord", param.getBrand()));

        if(ArrayUtils.isNotEmpty(param.getProps())) {
            String[] props = param.getProps();
            for (String prop : props) {
                //2:4G-3G
                String[] split = prop.trim().split(":");
                boolQuery().must(matchQuery("attrValueList.productAttributeId", split[0]))
                        .must(termsQuery("attrValueList.value", split[1].trim().split("-")));
                NestedQueryBuilder propQuery = nestedQuery("attrValueList", null, ScoreMode.None);
                boolQuery.filter(propQuery);
            }
        }

        if(param.getPriceFrom() != null || param.getPriceTo() != null) {
            RangeQueryBuilder rangeQuery = rangeQuery("price");
            if(param.getPriceFrom() != null)
                rangeQuery.gte(param.getPriceFrom());
            if(param.getPriceTo() != null)
                rangeQuery.lte(param.getPriceTo());
            boolQuery.filter(rangeQuery);
        }
        builder.query(boolQuery);

        //2. 聚合
        TermsAggregationBuilder brandAgg = terms("brand_agg").field("brandName.keyword");
        brandAgg.subAggregation(terms("brandId").field("brandId"));
        builder.aggregation(brandAgg);

        TermsAggregationBuilder cateAgg = terms("cate_agg").field("productCategoryName.keyword");
        cateAgg.subAggregation(terms("cateId_agg").field("productCategoryId"));;
        builder.aggregation(cateAgg);

        NestedAggregationBuilder attrAgg = nested("attr_agg", "attrValueList");
        TermsAggregationBuilder attrNameAgg = terms("attrName_agg").field("attrValueList.name");
        attrNameAgg.subAggregation(terms("attrValue_agg").field("attrValueList.value"));
        attrNameAgg.subAggregation(terms("attrId_agg").field("attrValueList.productAttributeId"));
        attrAgg.subAggregation(attrNameAgg);
        builder.aggregation(cateAgg);

        //3. 高亮
        if(StringUtils.isNotBlank(param.getKeyword())) {
            HighlightBuilder hb = new HighlightBuilder();
            hb.field("skuProductInfos.skuTitle");
            hb.preTags("<b style='color:red>");
            hb.postTags("</b>");
            builder.highlighter(hb);
        }

        //4. 分页
        int pageNo = param.getPageNum() == null ? 0 : param.getPageNum();
        builder.from((pageNo - 1) * param.getPageSize());
        builder.size(param.getPageSize());

        //5. 排序
        if(StringUtils.isNotBlank(param.getOrder())) {
            String orderParam = param.getOrder();
            String[] sp = orderParam.trim().split(":");
            String order = sp[0].trim();
            String desc = sp[1].trim();
            if(order.equals("0")) {

            }
            if(order.equals("1")) {
                FieldSortBuilder sale = fieldSort("sale");
                if(desc.equalsIgnoreCase("desc"))
                    sale.order(SortOrder.DESC);
                else
                    sale.order(SortOrder.ASC);
                builder.sort(sale);
            }
            if (order.equals("2")) {
                FieldSortBuilder priceSort = fieldSort("price");
                if(desc.equalsIgnoreCase("desc"))
                    priceSort.order(SortOrder.DESC);
                else
                    priceSort.order(SortOrder.ASC);
                builder.sort(priceSort);
            }
        }
        return builder.toString();
    }
}
