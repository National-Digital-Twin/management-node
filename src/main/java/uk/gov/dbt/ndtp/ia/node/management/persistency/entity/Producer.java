package uk.gov.dbt.ndtp.ia.node.management.persistency.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "producer")
public class Producer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation org;

    @Column(name = "active", nullable = false)
    private Boolean active = false;

    @Column(name = "host", nullable = false, length = 500)
    private String host;

    @Column(name = "port", nullable = false)
    private BigDecimal port;

    @Column(name = "tls", nullable = false)
    private Boolean tls = false;

    @Column(name = "idp_client_id", nullable = false, length = 50)
    private String idpClientId;

  @OneToMany(mappedBy = "producer", fetch = FetchType.LAZY)
  private List<Product> products;
}
