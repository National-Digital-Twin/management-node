package uk.gov.dbt.ndtp.ia.node.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for consumerId entity.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerDTO {
    @JsonIgnore
    private Long id;
    private String name;
    @JsonIgnore
    private Long orgId;
    private String idpClientId;
}