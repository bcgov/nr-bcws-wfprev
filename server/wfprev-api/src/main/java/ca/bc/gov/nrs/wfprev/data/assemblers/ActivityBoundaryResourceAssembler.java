package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import ca.bc.gov.nrs.wfprev.controllers.ActivityController;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.controllers.ActivityBoundaryController;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;

import java.util.UUID;

@Slf4j
@Component
public class ActivityBoundaryResourceAssembler extends RepresentationModelAssemblerSupport<ActivityBoundaryEntity, ActivityBoundaryModel> {

    public ActivityBoundaryResourceAssembler() {
        super(ActivityBoundaryController.class, ActivityBoundaryModel.class);
    }

    public ActivityBoundaryEntity toEntity(ActivityBoundaryModel resource) {
        ActivityBoundaryEntity entity = new ActivityBoundaryEntity();

        entity.setActivityBoundaryGuid(UUID.fromString(resource.getActivityBoundaryGuid()));
        entity.setActivityGuid(UUID.fromString(resource.getActivityGuid()));
        entity.setSystemStartTimestamp(resource.getSystemStartTimestamp());
        entity.setSystemEndTimestamp(resource.getSystemEndTimestamp());
        entity.setMappingLabel(resource.getMappingLabel());
        entity.setCollectionDate(resource.getCollectionDate());
        entity.setCollectionMethod(resource.getCollectionMethod());
        entity.setCollectorName(resource.getCollectorName());
        entity.setBoundarySizeHa(resource.getBoundarySizeHa());
        entity.setBoundaryComment(resource.getBoundaryComment());
        entity.setGeometry(resource.getGeometry());
        entity.setRevisionCount(resource.getRevisionCount());
        entity.setCreateUser(resource.getCreateUser());
        entity.setCreateDate(resource.getCreateDate());
        entity.setUpdateUser(resource.getUpdateUser());
        entity.setUpdateDate(resource.getUpdateDate());

        return entity;
    }

    @Override
    public ActivityBoundaryModel toModel(ActivityBoundaryEntity entity) {
        ActivityBoundaryModel resource = instantiateModel(entity);

        resource.setActivityBoundaryGuid(String.valueOf(entity.getActivityBoundaryGuid()));
        resource.setActivityGuid(String.valueOf(entity.getActivityGuid()));
        resource.setSystemStartTimestamp(entity.getSystemStartTimestamp());
        resource.setSystemEndTimestamp(entity.getSystemEndTimestamp());
        resource.setMappingLabel(entity.getMappingLabel());
        resource.setCollectionDate(entity.getCollectionDate());
        resource.setCollectionMethod(entity.getCollectionMethod());
        resource.setCollectorName(entity.getCollectorName());
        resource.setBoundarySizeHa(entity.getBoundarySizeHa());
        resource.setBoundaryComment(entity.getBoundaryComment());
        resource.setGeometry(entity.getGeometry());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setCreateUser(entity.getCreateUser());
        resource.setCreateDate(entity.getCreateDate());
        resource.setUpdateUser(entity.getUpdateUser());
        resource.setUpdateDate(entity.getUpdateDate());

        return resource;
    }

    @Override
    public CollectionModel<ActivityBoundaryModel> toCollectionModel(Iterable<? extends ActivityBoundaryEntity> entities) {
        return super.toCollectionModel(entities);
    }

    public ActivityBoundaryEntity updateEntity(ActivityBoundaryModel model, ActivityBoundaryEntity existingEntity) {
        log.debug(">> updateEntity");
        ActivityBoundaryEntity entity = new ActivityBoundaryEntity();

        entity.setActivityBoundaryGuid(existingEntity.getActivityBoundaryGuid());
        entity.setActivityGuid(existingEntity.getActivityGuid());
        entity.setSystemStartTimestamp(nonNullOrDefault(model.getSystemStartTimestamp(), existingEntity.getSystemStartTimestamp()));
        entity.setSystemEndTimestamp(nonNullOrDefault(model.getSystemEndTimestamp(), existingEntity.getSystemEndTimestamp()));
        entity.setMappingLabel(nonNullOrDefault(model.getMappingLabel(), existingEntity.getMappingLabel()));
        entity.setCollectionDate(nonNullOrDefault(model.getCollectionDate(), existingEntity.getCollectionDate()));
        entity.setCollectionMethod(nonNullOrDefault(model.getCollectionMethod(), existingEntity.getCollectionMethod()));
        entity.setCollectorName(nonNullOrDefault(model.getCollectorName(), existingEntity.getCollectorName()));
        entity.setBoundarySizeHa(nonNullOrDefault(model.getBoundarySizeHa(), existingEntity.getBoundarySizeHa()));
        entity.setBoundaryComment(nonNullOrDefault(model.getBoundaryComment(), existingEntity.getBoundaryComment()));
        entity.setGeometry(nonNullOrDefault(model.getGeometry(), existingEntity.getGeometry()));
        entity.setRevisionCount(nonNullOrDefault(model.getRevisionCount(), existingEntity.getRevisionCount()));
        entity.setCreateUser(existingEntity.getCreateUser());
        entity.setCreateDate(existingEntity.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    private <T> T nonNullOrDefault(T newValue, T existingValue) {
        return newValue != null ? newValue : existingValue;
    }
}
