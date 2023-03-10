package com.at0m.common.service;

import com.at0m.common.feign.AvailableQuantityFeign;
import com.at0m.common.model.AvailableQuantity;
import com.at0m.common.model.Product;
import com.at0m.common.model.ProductResponseResource;
import com.at0m.common.util.ProductUtil;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

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
        List<AvailableQuantity> availableQuantities = availableQuantityFeign.getAllAvailableQuantities();
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
        log.info("Feign call to "+availableQuantityFeign);
        AvailableQuantity availableQuantity = availableQuantityFeign.getByProductname(productName);
        if (availableQuantity != null) {
            List<Product> productByName = mongoTemplate.find(query(where("productName").is(productName)), Product.class);
            productByName.get(0).setQuantityAvailable(availableQuantity.getQuantityAvailable());
            return productUtil.productTransformerSuccessful(productByName).get(0);
        } else {
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
        productUtil.removeDuplicatesFromList(products);
        BulkOperations bulkOps = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,Product.class);
        List<AvailableQuantity> availableQuantityList = new ArrayList<>();
        Iterator iterator = products.iterator();
        while(iterator.hasNext()){
            Product product = (Product) iterator.next();
            Query query = productUtil.createQuery(product.getProductName());
            Update update = productUtil.createUpdateQuery(product);
            bulkOps.upsert(query,update);
            AvailableQuantity availableQuantity = new AvailableQuantity();
            availableQuantity.setProductName(product.getProductName());
            availableQuantity.setQuantityAvailable(product.getQuantityAvailable());
            availableQuantity.setModifiedDate(new Date());
            availableQuantityList.add(availableQuantity);
        }
        try {
            bulkOps.execute();
            availableQuantityFeign.saveListOfQuantities(availableQuantityList);
        } catch(DuplicateKeyException duplicateKeyException) {
            Throwable cause = duplicateKeyException.getCause();
            if(cause instanceof MongoBulkWriteException){
                MongoBulkWriteException mongoBulkWriteException = (MongoBulkWriteException) cause;
                List<BulkWriteError> bulkWriteErrors = mongoBulkWriteException.getWriteErrors();
                bulkWriteErrors.forEach(bulkWriteError -> {
                    log.error(bulkWriteError.getIndex()+" Is already present in Database");
                });
            }
        }
        return productUtil.productTransformerSuccessful(products);
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
        mongoTemplate.dropCollection("product");
        return productUtil.productTransformerSuccessful(product);
    }

    public void deleteProduct(String productName){
        List<Product> product = mongoTemplate.find(query(where("productName").is(productName)),Product.class);
        if(product.size()>0){
            mongoTemplate.remove(mongoTemplate.findById(product.get(0).getProductName(),Product.class,"products"));
        }
    }
}
