/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search criteria for {@code POST /v1/product/discovery}. All fields are optional; an
 * empty/absent field means "no filter" on that attribute. Filters only narrow the set of
 * products the requester is authorised to discover - they cannot widen it.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscoveryRequestDTO {

    @Size(max = 50)
    private String name;

    @Size(max = 150)
    private String topic;

    @Size(max = 255)
    private String type;
}
