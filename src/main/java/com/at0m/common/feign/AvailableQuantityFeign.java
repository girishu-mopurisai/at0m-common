package com.at0m.common.feign;

import com.at0m.common.model.AvailableQuantity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name="AT0M-AVAILABLE-QUANTITY-API")
public interface AvailableQuantityFeign {

    @GetMapping("/api/quantity")
    List<AvailableQuantity> getAllQuantity();
}
