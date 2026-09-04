/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.persistency.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "attribute_definition")
public class AttributeDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 150)
    @NotNull
    @Column(name = "namespace", nullable = false, length = 150)
    private String namespace;

    @Size(max = 150)
    @NotNull
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 255)
    @Column(name = "display_name", length = 255)
    private String displayName;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @Size(max = 50)
    @NotNull
    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @NotNull
    @Column(name = "multi_valued", nullable = false)
    private Boolean multiValued = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_values")
    private String allowedValues;

    @Size(max = 500)
    @Column(name = "validation_pattern", length = 500)
    private String validationPattern;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "classification")
    private String classification;

    @NotNull
    @Column(name = "sensitive", nullable = false)
    private Boolean sensitive = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Size(max = 255)
    @NotNull
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Size(max = 255)
    @Column(name = "updated_by", length = 255)
    private String updatedBy;
}
