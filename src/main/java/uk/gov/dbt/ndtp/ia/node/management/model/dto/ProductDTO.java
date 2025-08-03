package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO for OrganisationDataProvider entity.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    @JsonIgnore
    private Long id;
    private String name;
    private String topic;
    @JsonIgnore
    private Long producerId;
    private List<ConsumerDTO> consumers;
}