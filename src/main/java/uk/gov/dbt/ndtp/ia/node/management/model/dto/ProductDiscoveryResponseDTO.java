/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response for {@code POST /v1/product/discovery}: the products the requester is authorised
 * to discover, after policy filtering and search criteria are both applied. Empty (never
 * null) when no products are authorised or none match the search criteria.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscoveryResponseDTO {

    @Builder.Default
    private List<ProductDTO> products = new ArrayList<>();
}
