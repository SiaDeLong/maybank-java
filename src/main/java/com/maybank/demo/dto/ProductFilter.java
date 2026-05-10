package com.maybank.demo.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductFilter {
    private String name;
    private String description;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minQuantity;
    private Integer maxQuantity;
}
