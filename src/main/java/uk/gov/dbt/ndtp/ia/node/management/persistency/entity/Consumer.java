package uk.gov.dbt.ndtp.ia.node.management.persistency.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "consumer")
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation org;

    @Column(name = "idp_client_id", nullable = false, length = 50)
    private String idpClientId;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "consumer_id",referencedColumnName = "id",insertable = false,updatable = false)
  private List<ProductConsumer> productConsumers;
}
