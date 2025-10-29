package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoundaryTileServiceTest {

    @Mock
    ActivityBoundaryRepository activityBoundaryRepository;

    @Mock
    ProjectBoundaryRepository projectBoundaryRepository;

    BoundaryTileService service;

    @BeforeEach
    void setUp() {
        service = new BoundaryTileService(activityBoundaryRepository, projectBoundaryRepository);
    }

    @Test
    void getProjectBoundaryTile_delegates_andReturnsBytes() {
        int z = 8, x = 123, y = 456;
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        byte[] expected = new byte[]{0x01, 0x02};

        when(projectBoundaryRepository.getProjectBoundaryTiles(eq(z), eq(x), eq(y), any(UUID[].class)))
                .thenReturn(expected);

        byte[] actual = service.getProjectBoundaryTile(z, x, y, List.of(a, b));

        assertArrayEquals(expected, actual);

        ArgumentCaptor<UUID[]> captor = ArgumentCaptor.forClass(UUID[].class);
        verify(projectBoundaryRepository).getProjectBoundaryTiles(eq(z), eq(x), eq(y), captor.capture());

        UUID[] sent = captor.getValue();
        assertEquals(2, sent.length);
        assertEquals(a, sent[0]);
        assertEquals(b, sent[1]);

        verifyNoMoreInteractions(projectBoundaryRepository, activityBoundaryRepository);
    }

    @Test
    void getActivityBoundaryTile_delegates_andReturnsBytes() {
        int z = 12, x = 654, y = 321;
        UUID p = UUID.randomUUID();
        byte[] expected = new byte[]{0x0A, 0x0B, 0x0C};

        when(activityBoundaryRepository.getActivityBoundaryTiles(eq(z), eq(x), eq(y), any(UUID[].class)))
                .thenReturn(expected);

        byte[] actual = service.getActivityBoundaryTile(z, x, y, List.of(p));

        assertArrayEquals(expected, actual);

        ArgumentCaptor<UUID[]> captor = ArgumentCaptor.forClass(UUID[].class);
        verify(activityBoundaryRepository).getActivityBoundaryTiles(eq(z), eq(x), eq(y), captor.capture());

        UUID[] sent = captor.getValue();
        assertEquals(1, sent.length);
        assertEquals(p, sent[0]);

        verifyNoMoreInteractions(activityBoundaryRepository, projectBoundaryRepository);
    }

    @Test
    void getProjectBoundaryTile_withEmptyList_passesEmptyArray() {
        int z = 5, x = 10, y = 20;
        byte[] expected = new byte[0];

        when(projectBoundaryRepository.getProjectBoundaryTiles(eq(z), eq(x), eq(y), any(UUID[].class)))
                .thenReturn(expected);

        byte[] actual = service.getProjectBoundaryTile(z, x, y, List.of());

        assertArrayEquals(expected, actual);

        ArgumentCaptor<UUID[]> captor = ArgumentCaptor.forClass(UUID[].class);
        verify(projectBoundaryRepository).getProjectBoundaryTiles(eq(z), eq(x), eq(y), captor.capture());

        UUID[] sent = captor.getValue();
        assertNotNull(sent);
        assertEquals(0, sent.length);

        verifyNoMoreInteractions(projectBoundaryRepository, activityBoundaryRepository);
    }

    @Test
    void getActivityBoundaryTile_propagatesRepositoryException() {
        int z = 7, x = 11, y = 22;
        UUID p = UUID.randomUUID();

        when(activityBoundaryRepository.getActivityBoundaryTiles(eq(z), eq(x), eq(y), any(UUID[].class)))
                .thenThrow(new RuntimeException("repo boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getActivityBoundaryTile(z, x, y, List.of(p)));
        assertEquals("repo boom", ex.getMessage());

        verify(activityBoundaryRepository).getActivityBoundaryTiles(eq(z), eq(x), eq(y), any(UUID[].class));
        verifyNoMoreInteractions(activityBoundaryRepository, projectBoundaryRepository);
    }
}
