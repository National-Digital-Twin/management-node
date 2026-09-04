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
@Table(name = "attribute_definition_scope")
public class AttributeDefinitionScope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private AttributeDefinition attributeDefinition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_scope_id", nullable = false)
    private AttributeScope attributeScope;

    @NotNull
    @Column(name = "required", nullable = false)
    private Boolean required = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_value")
    private String defaultValue;

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
