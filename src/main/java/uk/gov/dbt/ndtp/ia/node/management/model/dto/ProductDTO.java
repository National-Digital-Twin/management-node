/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.*;

/**
 * DTO for OrganisationDataProvider entity.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    @JsonIgnore
    private Long id;

    private String name;
    private String topic;

    @JsonIgnore
    private Long producerId;

    private List<ConsumerDTO> consumers;
}
