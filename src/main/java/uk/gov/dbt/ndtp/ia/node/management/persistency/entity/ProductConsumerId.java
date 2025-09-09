/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.persistency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@Embeddable
public class ProductConsumerId implements Serializable {
    @Serial
    private static final long serialVersionUID = -1247742635043749804L;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "consumer_id", nullable = false)
    private Long consumerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductConsumerId entity = (ProductConsumerId) o;
        return Objects.equals(this.consumerId, entity.consumerId) && Objects.equals(this.productId, entity.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerId, productId);
    }
}
