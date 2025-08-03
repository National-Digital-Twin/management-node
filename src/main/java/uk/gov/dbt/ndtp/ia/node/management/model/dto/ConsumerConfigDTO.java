package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConsumerConfigDTO {

  private final String clientId;
  private final List<ProducerDTO> producers;
}
