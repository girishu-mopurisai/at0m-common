package com.at0m.common.service;

import com.at0m.common.feign.AvailableQuantityFeign;
import com.at0m.common.model.AvailableQuantity;
import com.at0m.common.model.Product;
import com.at0m.common.model.ProductResponseResource;
import com.at0m.common.util.ProductUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Slf4j
public class ProductService {

    private final MongoTemplate mongoTemplate;

    private final ProductUtil productUtil;

    @Autowired
    private AvailableQuantityFeign availableQuantityFeign;

    public ProductService(MongoTemplate mongoTemplate, ProductUtil productUtil) {
        this.mongoTemplate = mongoTemplate;
        this.productUtil = productUtil;
    }

    public List<ProductResponseResource> getAllProductsList() {
        log.info("Executing GET http://AT0M-AVAILABLE-QUANTITY-API/api/quantity");
        List<AvailableQuantity> availableQuantities = availableQuantityFeign.getAllQuantity();
        log.info("Connecting to "+mongoTemplate.getDb());
        List<Product> products = mongoTemplate.findAll(Product.class, "product");

        for(int i=0;i<products.size();i++){
            String productName = products.get(i).getProductName();
            for(int j=0;j<availableQuantities.size();j++){
                if(productName.equals(availableQuantities.get(j).getProductName())) {
                    products.get(i).setQuantityAvailable(availableQuantities.get(j).getQuantityAvailable());
                }
            }
        }
        return productUtil.productTransformerSuccessful(products);
    }

    public ProductResponseResource getProductByproductName(String productName) {
        List<Product> productByName = mongoTemplate.find(query(where("productName").is(productName)),Product.class);
        if(productByName.size() > 0){
            return productUtil.productTransformerSuccessful(productByName).get(0);
        }
        else{
            ProductResponseResource productResponseResource = new ProductResponseResource();
            productResponseResource.setSuccessful(false);
            return productResponseResource;
        }
    }

    public List<ProductResponseResource> saveProduct(Product product) {
        List<Product> products = new ArrayList<>();
        if(mongoTemplate.find(query(where("productName").is(product.getProductName())), Product.class).size()==0){
            product.setCreationDate(new Date());
            product.setModifiedDate(new Date());
            products.add(mongoTemplate.save(product));
            return productUtil.productTransformerSuccessful(products);
        }
        else{
            products.add(product);
            return productUtil.productTransformerFail(products);
        }

    }

    public List<ProductResponseResource> saveListOfProducts(List<Product> products) {
        List<Product> productsSaved = new ArrayList<>();
        List<Product> productsNotSaved = new ArrayList<>();
        for(int i=0;i<products.size();i++){
            if(mongoTemplate.find(query(where("productName").is(products.get(i).getProductName())),Product.class).size()==0) {
                products.get(i).setCreationDate(new Date());
                products.get(i).setModifiedDate(new Date());
                productsSaved.add(mongoTemplate.save(products.get(i)));
            }
            else {
                productsNotSaved.add(products.get(i));
            }
        }
        List<ProductResponseResource> productsSavedTransformed = productUtil.productTransformerSuccessful(productsSaved);
        List<ProductResponseResource> productsNotSavedTransformed = productUtil.productTransformerFail(productsNotSaved);
        List<ProductResponseResource> response = new ArrayList<>();
        response.addAll(productsSavedTransformed);
        response.addAll(productsNotSavedTransformed);
        return response;
    }

    public ProductResponseResource updateProduct(Product product) {
        if (mongoTemplate.find(query(where("productName").is(product.getProductName())),Product.class).size()==1) {
            mongoTemplate.save(product);
            return (ProductResponseResource) productUtil.productTransformerSuccessful(Collections.singletonList(mongoTemplate.save(product)));
        } else {
            return productUtil.productTransformerFail((List<Product>) product).get(0);
        }
    }

    public List<ProductResponseResource> deleteAll() {
        List<Product> product = mongoTemplate.findAll(Product.class, "product");
        mongoTemplate.dropCollection("products");
        return productUtil.productTransformerSuccessful(product);
    }

    public void deleteProduct(String productName){
        List<Product> product = mongoTemplate.find(query(where("productName").is(productName)),Product.class);
        if(product.size()>0){
            mongoTemplate.remove(mongoTemplate.findById(product.get(0).getProductName(),Product.class,"products"));
        }
    }
}
