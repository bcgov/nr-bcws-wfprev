package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectLocationResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectLocationEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ProjectLocationServiceTest {

    private ProjectLocationRepository repository;
    private ProjectLocationResourceAssembler assembler;
    private ProjectLocationService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProjectLocationRepository.class);
        assembler = mock(ProjectLocationResourceAssembler.class);
        service = new ProjectLocationService(repository, assembler);
    }

    @Test
    void getAllProjectLocations_returnsModels() throws ServiceException {
        UUID guid1 = UUID.randomUUID();
        UUID guid2 = UUID.randomUUID();

        ProjectLocationEntity e1 = ProjectLocationEntity.builder()
                .projectGuid(guid1)
                .latitude(new BigDecimal("49.282700"))
                .longitude(new BigDecimal("-123.120700"))
                .build();

        ProjectLocationEntity e2 = ProjectLocationEntity.builder()
                .projectGuid(guid2)
                .latitude(new BigDecimal("48.428400"))
                .longitude(new BigDecimal("-123.365600"))
                .build();

        List<ProjectLocationEntity> entities = List.of(e1, e2);

        ProjectLocationModel m1 = ProjectLocationModel.builder()
                .projectGuid(guid1.toString())
                .latitude(BigDecimal.valueOf(49.282700))
                .longitude(BigDecimal.valueOf(-123.120700))
                .build();

        ProjectLocationModel m2 = ProjectLocationModel.builder()
                .projectGuid(guid2.toString())
                .latitude(BigDecimal.valueOf(48.428400))
                .longitude(BigDecimal.valueOf(-123.365600))
                .build();

        when(repository.findByLatitudeIsNotNullAndLongitudeIsNotNull()).thenReturn(entities);
        when(assembler.toCollectionModel(entities)).thenReturn(CollectionModel.of(List.of(m1, m2)));

        CollectionModel<ProjectLocationModel> result = service.getAllProjectLocations();

        assertNotNull(result);
        List<ProjectLocationModel> list = new ArrayList<>(result.getContent());
        assertEquals(2, list.size());
        assertEquals(guid1.toString(), list.get(0).getProjectGuid());
        assertEquals(guid2.toString(), list.get(1).getProjectGuid());

        verify(repository).findByLatitudeIsNotNullAndLongitudeIsNotNull();
        verify(assembler).toCollectionModel(entities);
        verifyNoMoreInteractions(repository, assembler);
    }

    @Test
    void getAllProjectLocations_emptyList_returnsEmptyCollection() throws ServiceException {
        when(repository.findByLatitudeIsNotNullAndLongitudeIsNotNull()).thenReturn(Collections.emptyList());
        when(assembler.toCollectionModel(Collections.emptyList()))
                .thenReturn(CollectionModel.of(Collections.emptyList()));

        CollectionModel<ProjectLocationModel> result = service.getAllProjectLocations();

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());

        verify(repository).findByLatitudeIsNotNullAndLongitudeIsNotNull();
        verify(assembler).toCollectionModel(Collections.emptyList());
        verifyNoMoreInteractions(repository, assembler);
    }

    @Test
    void getAllProjectLocations_repositoryThrows_wrappedAsServiceException() {
        when(repository.findByLatitudeIsNotNullAndLongitudeIsNotNull())
                .thenThrow(new RuntimeException("DB down"));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getAllProjectLocations());
        assertTrue(ex.getMessage().contains("DB down"));

        verify(repository).findByLatitudeIsNotNullAndLongitudeIsNotNull();
        verify(assembler, never()).toCollectionModel(anyList());
        verifyNoMoreInteractions(repository, assembler);
    }

    @Test
    void getAllProjectLocations_assemblerThrows_wrappedAsServiceException() {
        List<ProjectLocationEntity> entities = Collections.singletonList(
                ProjectLocationEntity.builder()
                        .projectGuid(UUID.randomUUID())
                        .latitude(new BigDecimal("49.0"))
                        .longitude(new BigDecimal("-123.0"))
                        .build()
        );

        when(repository.findByLatitudeIsNotNullAndLongitudeIsNotNull()).thenReturn(entities);
        when(assembler.toCollectionModel(entities))
                .thenThrow(new IllegalStateException("Assembler error"));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getAllProjectLocations());
        assertTrue(ex.getMessage().contains("Assembler error"));

        verify(repository).findByLatitudeIsNotNullAndLongitudeIsNotNull();
        verify(assembler).toCollectionModel(entities);
        verifyNoMoreInteractions(repository, assembler);
    }
}
