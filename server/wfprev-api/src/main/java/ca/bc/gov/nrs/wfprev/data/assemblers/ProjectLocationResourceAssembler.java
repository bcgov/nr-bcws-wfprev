package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectLocationEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectLocationModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ProjectLocationResourceAssembler extends RepresentationModelAssemblerSupport<ProjectLocationEntity, ProjectLocationModel> {

    public ProjectLocationResourceAssembler() {
        super(ProjectController.class, ProjectLocationModel.class);
    }

    public ProjectLocationEntity toEntity(ProjectLocationModel resource) {
        ProjectLocationEntity entity = new ProjectLocationEntity();

        if (resource.getProjectGuid() != null) entity.setProjectGuid(UUID.fromString(resource.getProjectGuid()));
        entity.setLatitude(resource.getLatitude());
        entity.setLongitude(resource.getLongitude());

        return entity;
    }

    @Override
    public ProjectLocationModel toModel(ProjectLocationEntity entity) {
        ProjectLocationModel resource = instantiateModel(entity);

        if (entity.getProjectGuid() != null) resource.setProjectGuid(entity.getProjectGuid().toString());
        resource.setLatitude(entity.getLatitude());
        resource.setLongitude(entity.getLongitude());

        return resource;
    }


}
