package com.vitaledge.domain.product;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "product_rules",
        indexes = @Index(name = "idx_product_rule_product", columnList = "product_id"))
@Getter
@Setter
public class ProductRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "rule_definition", nullable = false)
    private Map<String, Object> ruleDefinition;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public ProductRule() {
    }

    public ProductRule(Product product, Map<String, Object> ruleDefinition) {
        this.product = product;
        this.ruleDefinition = ruleDefinition;
    }
}
