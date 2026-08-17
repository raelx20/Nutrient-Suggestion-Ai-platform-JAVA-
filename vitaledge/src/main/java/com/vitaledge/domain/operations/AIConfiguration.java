package com.vitaledge.domain.operations;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ai_configurations")
@Getter
@Setter
public class AIConfiguration extends BaseEntity {

    @Column(name = "model_name", nullable = false, length = 120)
    private String modelName;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "parameters")
    private Map<String, Object> parameters;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public AIConfiguration() {
    }

    public AIConfiguration(String modelName, Map<String, Object> parameters) {
        this.modelName = modelName;
        this.parameters = parameters;
    }
}
