package com.vitaledge;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;

/**
 * Test-only H2 dialect that maps JSON columns to plain text. H2's native {@code json}
 * type round-trips JSON as a double-encoded string through {@link java.sql.ResultSet},
 * which Hibernate's JSON format mapper cannot parse. Storing JSON as text keeps the
 * in-memory test database (and the JSON read/write path) compatible with production.
 */
public class TestH2Dialect extends H2Dialect {

    @Override
    protected void registerColumnTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.registerColumnTypes(typeContributions, serviceRegistry);
        DdlTypeRegistry registry = typeContributions.getTypeConfiguration().getDdlTypeRegistry();
        registry.addDescriptor(new DdlTypeImpl(SqlTypes.JSON, "text", this));
    }

    @Override
    protected String columnType(int sqlTypeCode) {
        if (SqlTypes.isJsonType(sqlTypeCode)) {
            return "text";
        }
        return super.columnType(sqlTypeCode);
    }
}