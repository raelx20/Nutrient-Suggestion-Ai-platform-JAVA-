package com.vitaledge.repository;

import com.vitaledge.domain.product.ProductRule;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRuleRepository extends JpaRepository<ProductRule, UUID> {

    List<ProductRule> findByProductIdInAndActiveTrue(Collection<UUID> productIds);

    List<ProductRule> findByProductId(UUID productId);
}