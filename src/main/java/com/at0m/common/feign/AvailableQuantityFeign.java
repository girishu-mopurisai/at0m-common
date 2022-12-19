package com.at0m.common.feign;

import com.at0m.common.model.AvailableQuantity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name="AT0M-AVAILABLE-QUANTITY-API")
public interface AvailableQuantityFeign {

    @GetMapping("/api/quantities")
    List<AvailableQuantity> getAllAvailableQuantities();

    @PostMapping("/api/quantities")
    List<AvailableQuantity> saveListOfQuantities(List<AvailableQuantity> availableQuantities);

    @GetMapping("/api/quantity/{productName}")
    AvailableQuantity getByProductname(@PathVariable String productName);
}
