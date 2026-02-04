package com.example.albanet.catalog.internal;

import com.example.albanet.catalog.api.dto.CatalogDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CatalogRepository repository;
    private final CatalogMapper mapper;

    public CatalogService(CatalogRepository repository, CatalogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CatalogDto> getActiveCatalogs() {
        return repository.findByActiveTrue()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
