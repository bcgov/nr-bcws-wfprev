package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectLocationResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectLocationEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectLocationRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectLocationService {

    private final ProjectLocationRepository projectLocationRepository;
    private final ProjectLocationResourceAssembler projectLocationResourceAssembler;

    public ProjectLocationService(
            ProjectLocationRepository projectLocationRepository,
            ProjectLocationResourceAssembler projectLocationResourceAssembler) {
        this.projectLocationRepository = projectLocationRepository;
        this.projectLocationResourceAssembler = projectLocationResourceAssembler;
    }

    public CollectionModel<ProjectLocationModel> getAllProjectLocations() throws ServiceException {
        try {
            List<ProjectLocationEntity> locations = projectLocationRepository.findByLatitudeIsNotNullAndLongitudeIsNotNull();
            return projectLocationResourceAssembler.toCollectionModel(locations);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
}
