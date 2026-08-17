package com.vitaledge.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application-specific configuration. Maps to the {@code vitaledge.*} prefix and
 * is populated from environment variables with relaxed binding.
 */
@ConfigurationProperties(prefix = "vitaledge")
public class ApplicationProperties {

    private final Security security = new Security();
    private final Cors cors = new Cors();
    private final RateLimit rateLimit = new RateLimit();
    private final Cache cache = new Cache();

    public static class Security {
        /** JWT signing key (min 32 characters). */
        private String secretKey = "";
        private String algorithm = "HS256";
        private int accessTokenExpireMinutes = 15;
        private int refreshTokenExpireDays = 7;
        private int emailVerificationExpireHours = 24;

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public int getAccessTokenExpireMinutes() {
            return accessTokenExpireMinutes;
        }

        public void setAccessTokenExpireMinutes(int accessTokenExpireMinutes) {
            this.accessTokenExpireMinutes = accessTokenExpireMinutes;
        }

        public int getRefreshTokenExpireDays() {
            return refreshTokenExpireDays;
        }

        public void setRefreshTokenExpireDays(int refreshTokenExpireDays) {
            this.refreshTokenExpireDays = refreshTokenExpireDays;
        }

        public int getEmailVerificationExpireHours() {
            return emailVerificationExpireHours;
        }

        public void setEmailVerificationExpireHours(int emailVerificationExpireHours) {
            this.emailVerificationExpireHours = emailVerificationExpireHours;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:3000", "http://localhost:8080"));
        private boolean allowCredentials = true;
        private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "Accept"));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }
    }

    public static class RateLimit {
        private int globalRequestsPerMinute = 60;
        private int registrationPerIpPerHour = 2;
        private int loginPerIpPerHour = 10;
        private int loginPerEmailPerHour = 5;
        private int assessmentCreatePerHour = 5;
        private int assessmentAnswerPerHour = 30;
        private int assessmentCompletePerHour = 5;
        private int trustedProxyCount = 0;

        public int getGlobalRequestsPerMinute() {
            return globalRequestsPerMinute;
        }

        public void setGlobalRequestsPerMinute(int globalRequestsPerMinute) {
            this.globalRequestsPerMinute = globalRequestsPerMinute;
        }

        public int getRegistrationPerIpPerHour() {
            return registrationPerIpPerHour;
        }

        public void setRegistrationPerIpPerHour(int registrationPerIpPerHour) {
            this.registrationPerIpPerHour = registrationPerIpPerHour;
        }

        public int getLoginPerIpPerHour() {
            return loginPerIpPerHour;
        }

        public void setLoginPerIpPerHour(int loginPerIpPerHour) {
            this.loginPerIpPerHour = loginPerIpPerHour;
        }

        public int getLoginPerEmailPerHour() {
            return loginPerEmailPerHour;
        }

        public void setLoginPerEmailPerHour(int loginPerEmailPerHour) {
            this.loginPerEmailPerHour = loginPerEmailPerHour;
        }

        public int getAssessmentCreatePerHour() {
            return assessmentCreatePerHour;
        }

        public void setAssessmentCreatePerHour(int assessmentCreatePerHour) {
            this.assessmentCreatePerHour = assessmentCreatePerHour;
        }

        public int getAssessmentAnswerPerHour() {
            return assessmentAnswerPerHour;
        }

        public void setAssessmentAnswerPerHour(int assessmentAnswerPerHour) {
            this.assessmentAnswerPerHour = assessmentAnswerPerHour;
        }

        public int getAssessmentCompletePerHour() {
            return assessmentCompletePerHour;
        }

        public void setAssessmentCompletePerHour(int assessmentCompletePerHour) {
            this.assessmentCompletePerHour = assessmentCompletePerHour;
        }

        public int getTrustedProxyCount() {
            return trustedProxyCount;
        }

        public void setTrustedProxyCount(int trustedProxyCount) {
            this.trustedProxyCount = trustedProxyCount;
        }
    }

    public static class Cache {
        private String namespace = "vitaledge";
        private int productsTtlSeconds = 300;
        private int productDetailTtlSeconds = 600;

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public int getProductsTtlSeconds() {
            return productsTtlSeconds;
        }

        public void setProductsTtlSeconds(int productsTtlSeconds) {
            this.productsTtlSeconds = productsTtlSeconds;
        }

        public int getProductDetailTtlSeconds() {
            return productDetailTtlSeconds;
        }

        public void setProductDetailTtlSeconds(int productDetailTtlSeconds) {
            this.productDetailTtlSeconds = productDetailTtlSeconds;
        }
    }

    public boolean isDebug() {
        return Boolean.parseBoolean(System.getenv().getOrDefault("DEBUG", "false"));
    }

    public Security getSecurity() {
        return security;
    }

    public Cors getCors() {
        return cors;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public Cache getCache() {
        return cache;
    }
}