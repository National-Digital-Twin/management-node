package uk.gov.dbt.ndtp.ia.node.management.persistency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "product_consumer")
public class ProductConsumer {
    @EmbeddedId
    private ProductConsumerId id;

    @Column(name = "granted_ts", nullable = false)
    private Timestamp grantedTs;

    @Column(name = "validity", nullable = false)
    private BigDecimal validity;


}