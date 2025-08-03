package uk.gov.dbt.ndtp.ia.node.management.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ModelMapper.
 * Note: Entity-DTO conversions are now handled by dedicated converter classes in the converter package.
 * ModelMapper is kept for backward compatibility and other potential uses.
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Creates a ModelMapper bean with basic configuration.
     *
     * @return the configured ModelMapper
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}