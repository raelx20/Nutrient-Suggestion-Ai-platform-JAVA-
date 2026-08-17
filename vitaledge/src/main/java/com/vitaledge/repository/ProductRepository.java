package com.vitaledge.repository;

import com.vitaledge.domain.product.Product;
import com.vitaledge.domain.product.ProductStatusEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByStatus(ProductStatusEnum status);

    @Query("SELECT p FROM Product p WHERE p.status = :status "
            + "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR :query = '')")
    List<Product> search(@Param("status") ProductStatusEnum status, @Param("query") String query, Pageable pageable);

    Optional<Product> findByIdAndStatus(UUID id, ProductStatusEnum status);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findByIds(@Param("ids") List<UUID> ids);

    long countByStatus(ProductStatusEnum status);
}