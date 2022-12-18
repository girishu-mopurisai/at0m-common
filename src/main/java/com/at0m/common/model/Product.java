package com.at0m.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "product")
public class Product {
    private String productName;
    private String price;
    private int quantityAvailable;
    private Date creationDate;
    private Date modifiedDate;
}
