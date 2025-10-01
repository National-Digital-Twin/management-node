# Entity-DTO Converter Pattern

## Overview

This document describes the Entity-DTO converter pattern implemented in the project to handle conversions between entity objects and DTOs (Data Transfer Objects). This pattern replaces the previous approach of using ModelMapper for these conversions.

## Benefits

- **Separation of Concerns**: Each converter is responsible for a specific entity-DTO pair, making the code more modular and easier to maintain.
- **Type Safety**: Converters provide type-safe conversions, reducing the risk of runtime errors.
- **Explicit Mapping**: Mappings between entities and DTOs are explicitly defined, making the code more readable and easier to debug.
- **Testability**: Converters can be easily unit tested in isolation.
- **Performance**: Custom converters can be more performant than reflection-based mapping libraries like ModelMapper.

## Implementation

### EntityDtoConverter Interface

The `EntityDtoConverter` interface defines the contract for all entity-DTO converters:

```java
public interface EntityDtoConverter<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
    
    default List<D> toDtoList(List<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    default List<E> toEntityList(List<D> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
```

### Concrete Converter Implementations

Concrete converter implementations are located in the `uk.gov.dbt.ndtp.ia.node.management.converter.impl` package. Each converter:

1. Implements the `EntityDtoConverter` interface for a specific entity-DTO pair
2. Is annotated with `@Component` for Spring dependency injection
3. Handles null values safely
4. Resolves entity relationships as needed

Example:

```java
@Component
public class OrganisationProducerConverter implements EntityDtoConverter<OrganisationProducer, OrganisationProducerDTO> {
    private final OrganisationRepository organisationRepository;
    
    public OrganisationProducerConverter(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }
    
    @Override
    public OrganisationProducerDTO toDto(OrganisationProducer entity) {
        if (entity == null) {
            return null;
        }
        
        return OrganisationProducerDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .orgId(entity.getOrg() != null ? entity.getOrg().getId() : null)
                // ... other fields
                .build();
    }
    
    @Override
    public OrganisationProducer toEntity(OrganisationProducerDTO dto) {
        if (dto == null) {
            return null;
        }
        
        OrganisationProducer entity = new OrganisationProducer();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        
        // Resolve relationships
        if (dto.getOrgId() != null) {
            Organisation organisation = organisationRepository.findById(dto.getOrgId())
                    .orElse(null);
            entity.setOrg(organisation);
        }
        
        // ... other fields
        
        return entity;
    }
}
```

## Usage in Services

Service implementations use the converters for entity-DTO conversions:

```java
@Service
public class OrganisationProducerServiceImpl implements OrganisationProducerService {
    private final OrganisationProducerRepository organisationProducerRepository;
    private final OrganisationProducerConverter organisationProducerConverter;
    
    public OrganisationProducerServiceImpl(
            OrganisationProducerRepository organisationProducerRepository,
            OrganisationProducerConverter organisationProducerConverter) {
        this.organisationProducerRepository = organisationProducerRepository;
        this.organisationProducerConverter = organisationProducerConverter;
    }
    
    @Override
    public List<OrganisationProducerDTO> getProducers(List<Long> producerIds) {
        List<OrganisationProducer> producers = organisationProducerRepository.findByIds(producerIds);
        return organisationProducerConverter.toDtoList(producers);
    }
}
```

## Best Practices

1. **Null Safety**: Always check for null values in converter methods.
2. **Relationship Handling**: Use repository dependencies to resolve entity relationships in `toEntity` methods.
3. **Builder Pattern**: Use the builder pattern for DTO creation when available.
4. **List Conversions**: Use the default `toDtoList` and `toEntityList` methods for list conversions.
5. **Documentation**: Document any non-trivial mappings or special handling in the converter methods.
6. **Testing**: Write unit tests for converters to ensure correct mapping behavior.

## ModelMapper

ModelMapper is still available in the application for backward compatibility and other potential uses, but it's no longer used for entity-DTO conversions. The `ModelMapperConfig` class provides a basic ModelMapper bean with STRICT matching strategy.