package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ProducerConfigDTO {
    private String clientId;
    private List<ProducerDTO> producers;
}
