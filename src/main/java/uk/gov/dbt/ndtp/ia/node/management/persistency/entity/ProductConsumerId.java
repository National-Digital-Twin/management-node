package uk.gov.dbt.ndtp.ia.node.management.persistency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

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
        return Objects.equals(this.consumerId, entity.consumerId) &&
                Objects.equals(this.productId, entity.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerId, productId);
    }

}