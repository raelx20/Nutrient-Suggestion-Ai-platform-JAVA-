package com.vitaledge.service;

import com.vitaledge.common.exception.ApiException;
import com.vitaledge.config.ApplicationProperties;
import com.vitaledge.domain.product.AgeGroupEnum;
import com.vitaledge.domain.product.Product;
import com.vitaledge.domain.product.ProductCategoryEnum;
import com.vitaledge.domain.product.ProductStatusEnum;
import com.vitaledge.repository.ProductRepository;
import com.vitaledge.repository.ProductRuleRepository;
import com.vitaledge.web.dto.product.ProductRequest;
import com.vitaledge.web.dto.product.ProductResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product catalog service with Redis caching. Ports the source project's
 * {@code api/products.py} plus the {@code utils/cache.py} product cache keys.
 */
@Service
public class ProductService {

    public static final String CACHE_KEY_PRODUCTS = "catalog:products:v1";
    public static final String CACHE_KEY_PRODUCT_DETAIL = "catalog:product:";
    public static final int MAX_SEARCH_LENGTH = 50;
    public static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final ProductRuleRepository productRuleRepository;
    private final CacheService cacheService;
    private final ApplicationProperties properties;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, ProductRuleRepository productRuleRepository,
                          CacheService cacheService, ApplicationProperties properties,
                          tools.jackson.databind.ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.productRuleRepository = productRuleRepository;
        this.cacheService = cacheService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public List<ProductResponse> listActive(String category, String ageGroup, String query, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String safeQuery = query == null ? "" : query.trim().substring(0, Math.min(query.trim().length(), MAX_SEARCH_LENGTH));

        String cacheKey = CACHE_KEY_PRODUCTS + ":" + safePage + ":" + safeSize + ":"
                + safeQuery.toLowerCase() + ":" + (category == null ? "" : category) + ":"
                + (ageGroup == null ? "" : ageGroup);

        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return jsonListToProducts(cached.get());
        }

        List<Product> products = productRepository.search(ProductStatusEnum.ACTIVE, safeQuery,
                PageRequest.of(safePage, safeSize));
        List<ProductResponse> response = products.stream()
                .filter(p -> matchesCategory(p, category))
                .filter(p -> matchesAgeGroup(p, ageGroup))
                .map(ProductResponse::from)
                .toList();

        cacheService.set(cacheKey, productsToJson(response), properties.getCache().getProductsTtlSeconds());
        return response;
    }

    public ProductResponse getActiveDetail(UUID id) {
        String cacheKey = CACHE_KEY_PRODUCT_DETAIL + id + ":v1";
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return jsonToProductResponse(cached.get());
        }
        Product product = productRepository.findByIdAndStatus(id, ProductStatusEnum.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND",
                        "Active product not found."));
        ProductResponse response = ProductResponse.from(product);
        cacheService.set(cacheKey, productResponseToJson(response), properties.getCache().getProductDetailTtlSeconds());
        return response;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(
                request.name(), request.sku(), request.categoryCode(),
                parseCategory(request.category()), parseAgeGroup(request.ageGroup()));
        product.setDescription(request.description());
        product.setAllergens(request.allergens() == null ? List.of() : request.allergens());
        product.setDietaryTags(request.dietaryTags() == null ? List.of() : request.dietaryTags());
        product.setNutritionalInfo(request.nutritionalInfo());
        product.setStatus(parseStatus(request.status()));
        productRepository.save(product);
        cacheService.deleteByPattern(CACHE_KEY_PRODUCTS + "*");
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found."));
        product.setName(request.name());
        product.setSku(request.sku());
        product.setCategoryCode(request.categoryCode());
        product.setCategory(parseCategory(request.category()));
        product.setAgeGroup(parseAgeGroup(request.ageGroup()));
        if (request.status() != null) {
            product.setStatus(parseStatus(request.status()));
        }
        product.setDescription(request.description());
        product.setAllergens(request.allergens() == null ? List.of() : request.allergens());
        product.setDietaryTags(request.dietaryTags() == null ? List.of() : request.dietaryTags());
        product.setNutritionalInfo(request.nutritionalInfo());
        productRepository.save(product);
        cacheService.deleteByPattern(CACHE_KEY_PRODUCTS + "*");
        cacheService.deleteByPattern(CACHE_KEY_PRODUCT_DETAIL + id + "*");
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found."));
        productRuleRepository.findByProductId(id)
                .forEach(rule -> productRuleRepository.delete(rule));
        productRepository.delete(product);
        cacheService.deleteByPattern(CACHE_KEY_PRODUCTS + "*");
        cacheService.deleteByPattern(CACHE_KEY_PRODUCT_DETAIL + id + "*");
    }

    private boolean matchesCategory(Product product, String category) {
        return category == null || category.isBlank() || product.getCategory().name().equalsIgnoreCase(category);
    }

    private boolean matchesAgeGroup(Product product, String ageGroup) {
        return ageGroup == null || ageGroup.isBlank()
                || product.getAgeGroup() == AgeGroupEnum.all
                || product.getAgeGroup().name().equalsIgnoreCase(ageGroup);
    }

    private ProductCategoryEnum parseCategory(String value) {
        try {
            return ProductCategoryEnum.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY",
                    "Invalid product category: " + value);
        }
    }

    private AgeGroupEnum parseAgeGroup(String value) {
        try {
            return AgeGroupEnum.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AGE_GROUP",
                    "Invalid age group: " + value);
        }
    }

    private ProductStatusEnum parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return ProductStatusEnum.DRAFT;
        }
        try {
            return ProductStatusEnum.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS",
                    "Invalid product status: " + value);
        }
    }

    private String productsToJson(List<ProductResponse> products) {
        try {
            return objectMapper.writeValueAsString(products);
        } catch (Exception e) {
            throw new IllegalStateException("Serialization failure", e);
        }
    }

    private List<ProductResponse> jsonListToProducts(String json) {
        try {
            return objectMapper.readValue(json,
                    new tools.jackson.core.type.TypeReference<List<ProductResponse>>() {
                    });
        } catch (Exception e) {
            return List.of();
        }
    }

    private String productResponseToJson(ProductResponse product) {
        try {
            return objectMapper.writeValueAsString(product);
        } catch (Exception e) {
            throw new IllegalStateException("Serialization failure", e);
        }
    }

    private ProductResponse jsonToProductResponse(String json) {
        try {
            return objectMapper.readValue(json, ProductResponse.class);
        } catch (Exception e) {
            return null;
        }
    }
}