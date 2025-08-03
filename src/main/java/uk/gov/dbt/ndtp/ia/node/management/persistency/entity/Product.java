package uk.gov.dbt.ndtp.ia.node.management.persistency.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "topic", nullable = false, length = 150)
    private String topic;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producer_id", nullable = false)
    private Producer producer;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "product_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false)
  private List<ProductConsumer> productConsumer;
}
