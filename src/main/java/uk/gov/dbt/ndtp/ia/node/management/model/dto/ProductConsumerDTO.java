/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.*;

/**
 * DTO for ConsumerAllowedDataProvider entity.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductConsumerDTO {
    private Long productId;
    private Long consumerId;
    private Timestamp grantedTs;
    private BigDecimal validity;
}
