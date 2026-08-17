package com.vitaledge.domain.product;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"),
        indexes = @Index(name = "idx_product_status", columnList = "status"))
@Getter
@Setter
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(name = "category_code", nullable = false, length = 30)
    private String categoryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ProductCategoryEnum category;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 20)
    private AgeGroupEnum ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatusEnum status = ProductStatusEnum.DRAFT;

    @Column(name = "description", length = 1000)
    private String description;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "allergens")
    private List<String> allergens = new ArrayList<>();

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "dietary_tags")
    private List<String> dietaryTags = new ArrayList<>();

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "nutritional_info")
    private Map<String, Object> nutritionalInfo;

    @OneToMany(mappedBy = "product")
    private List<ProductRule> rules = new ArrayList<>();

    public Product() {
    }

    public Product(String name, String sku, String categoryCode, ProductCategoryEnum category,
                   AgeGroupEnum ageGroup) {
        this.name = name;
        this.sku = sku;
        this.categoryCode = categoryCode;
        this.category = category;
        this.ageGroup = ageGroup;
    }
}
