package com.maybank.demo.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.maybank.demo.dto.ProductFilter;
import com.maybank.demo.model.Product;

import java.math.BigDecimal;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> from(ProductFilter filter) {
        if (filter == null) {
            return Specification.allOf();
        }

        return Specification.allOf(
                nameLike(filter.getName()),
                descriptionLike(filter.getDescription()),
                priceGreaterThanOrEqual(filter.getMinPrice()),
                priceLessThanOrEqual(filter.getMaxPrice()),
                quantityGreaterThanOrEqual(filter.getMinQuantity()),
                quantityLessThanOrEqual(filter.getMaxQuantity())
        );
    }

    private static Specification<Product> nameLike(String name) {
        return (root, query, cb) -> StringUtils.hasText(name)
                ? cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
                : null;
    }

    private static Specification<Product> descriptionLike(String description) {
        return (root, query, cb) -> StringUtils.hasText(description)
                ? cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%")
                : null;
    }

    private static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice != null
                ? cb.greaterThanOrEqualTo(root.get("price"), minPrice)
                : null;
    }

    private static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice != null
                ? cb.lessThanOrEqualTo(root.get("price"), maxPrice)
                : null;
    }

    private static Specification<Product> quantityGreaterThanOrEqual(Integer minQuantity) {
        return (root, query, cb) -> minQuantity != null
                ? cb.greaterThanOrEqualTo(root.get("quantity"), minQuantity)
                : null;
    }

    private static Specification<Product> quantityLessThanOrEqual(Integer maxQuantity) {
        return (root, query, cb) -> maxQuantity != null
                ? cb.lessThanOrEqualTo(root.get("quantity"), maxQuantity)
                : null;
    }
}
