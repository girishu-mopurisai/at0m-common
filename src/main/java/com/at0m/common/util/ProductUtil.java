package com.at0m.common.util;

import com.at0m.common.model.Product;
import com.at0m.common.model.ProductResponseResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ProductUtil {

    public List<ProductResponseResource> productTransformerSuccessful(List<Product> products){
        List<ProductResponseResource> productResponseResources = new ArrayList<>();
        for(int i=0;i<products.size();i++){
            ProductResponseResource productResponseResource = new ProductResponseResource();
            productResponseResource.setSuccessful(true);
            productResponseResource.setProductName(products.get(i).getProductName());
            productResponseResource.setPrice(products.get(i).getPrice());
            if(products.get(i).getQuantityAvailable() != 0) {
                productResponseResource.setQuantityAvailable(products.get(i).getQuantityAvailable());
            }
            productResponseResource.setModifiedDate(new Date());
            productResponseResource.setCreationDate(new Date());
            productResponseResources.add(productResponseResource);
        }
        return productResponseResources;
    }

    public List<ProductResponseResource> productTransformerFail(List<Product> products){
        List<ProductResponseResource> productResponseResources = new ArrayList<>();
        for(int i=0;i<products.size();i++){
            ProductResponseResource productResponseResource = new ProductResponseResource();
            productResponseResource.setSuccessful(false);
            productResponseResource.setProductName(products.get(i).getProductName());
            productResponseResources.add(productResponseResource);
        }
        return productResponseResources;
    }
}

