package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class BoundaryTileService {

    private final ActivityBoundaryRepository activityBoundaryRepository;
    private final ProjectBoundaryRepository projectBoundaryRepository;

    public BoundaryTileService(ActivityBoundaryRepository activityBoundaryRepository,
                               ProjectBoundaryRepository projectBoundaryRepository) {
        this.activityBoundaryRepository = activityBoundaryRepository;
        this.projectBoundaryRepository = projectBoundaryRepository;
    }

    public byte[] getProjectBoundaryTile(int z, int x, int y, List<UUID> projectGuids) {
        UUID[] guids = projectGuids.toArray(UUID[]::new);
        return projectBoundaryRepository.getProjectBoundaryTiles(z, x, y, guids);
    }

    public byte[] getActivityBoundaryTile(int z, int x, int y, List<UUID> projectGuids) {
        UUID[] guids = projectGuids.toArray(UUID[]::new);
        return activityBoundaryRepository.getActivityBoundaryTiles(z, x, y, guids);
    }
}
