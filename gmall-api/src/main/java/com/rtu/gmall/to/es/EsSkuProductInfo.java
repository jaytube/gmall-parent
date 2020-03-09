package com.rtu.gmall.to.es;

import com.rtu.gmall.pms.entity.SkuStock;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EsSkuProductInfo extends SkuStock implements Serializable {

    private String skuTitle;
    private List<EsProductAttributeValue> attributeValues;
}
