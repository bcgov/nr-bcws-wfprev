package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestAreaCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestDistrictUnitCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestRegionUnitCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.GeneralScopeCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProgramAreaResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectTypeCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ForestOrgUnitCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProgramAreaEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestDistrictUnitCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestRegionUnitCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProgramAreaModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestOrgUnitCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CodesService implements CommonService {
    private ForestAreaCodeRepository forestAreaCodeRepository;
    private ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler;
    private GeneralScopeCodeRepository generalScopeCodeRepository;
    private GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler;
    private ProjectTypeCodeRepository projectTypeCodeRepository;
    private ProjectTypeCodeResourceAssembler projectTypeCodeResourceAssembler;
    private final ProgramAreaRepository programAreaRepository;
    private ProgramAreaResourceAssembler programAreaResourceAssembler;
    private ForestOrgUnitCodeRepository forestOrgUnitCodeRepository;
    private ForestRegionUnitCodeResourceAssembler forestRegionCodeResourceAssembler;
    private final ForestDistrictUnitCodeResourceAssembler forestDistrictCodeResourceAssembler;

    public CodesService(ForestAreaCodeRepository forestAreaCodeRepository, ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler,
                        GeneralScopeCodeRepository generalScopeCodeRepository, GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler,
                        ProjectTypeCodeRepository projectTypeCodeRepository, ProjectTypeCodeResourceAssembler projectTypeCodeResourceAssembler, ProgramAreaRepository programAreaRepository, ProgramAreaResourceAssembler programAreaResourceAssembler,
                        ForestOrgUnitCodeRepository forestRegionCodeRepository, ForestRegionUnitCodeResourceAssembler forestRegionCodeResourceAssembler, ForestDistrictUnitCodeResourceAssembler forestDistrictUnitCodeResourceAssembler) {
        this.forestAreaCodeRepository = forestAreaCodeRepository;
        this.forestAreaCodeResourceAssembler = forestAreaCodeResourceAssembler;
        this.generalScopeCodeRepository = generalScopeCodeRepository;
        this.generalScopeCodeResourceAssembler = generalScopeCodeResourceAssembler;
        this.projectTypeCodeRepository = projectTypeCodeRepository;
        this.projectTypeCodeResourceAssembler = projectTypeCodeResourceAssembler;
        this.programAreaRepository = programAreaRepository;
        this.programAreaResourceAssembler = programAreaResourceAssembler;
        this.forestOrgUnitCodeRepository = forestRegionCodeRepository;
        this.forestRegionCodeResourceAssembler = forestRegionCodeResourceAssembler;
        this.forestDistrictCodeResourceAssembler = forestDistrictUnitCodeResourceAssembler;
    }

    /**
     * FOREST AREA CODES
     **/
    public CollectionModel<ForestAreaCodeModel> getAllForestAreaCodes() throws ServiceException {
        try {
            List<ForestAreaCodeEntity> entities = forestAreaCodeRepository.findAll();
            return forestAreaCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ForestAreaCodeModel getForestAreaCodeById(String id) throws ServiceException {
        try {
            return forestAreaCodeRepository.findById(id).map(forestAreaCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * GENERAL SCOPE CODES
     **/
    public CollectionModel<GeneralScopeCodeModel> getAllGeneralScopeCodes() throws ServiceException {
        try {
            List<GeneralScopeCodeEntity> entities = generalScopeCodeRepository.findAll();
            return generalScopeCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public GeneralScopeCodeModel getGeneralScopeCodeById(String id) throws ServiceException {
        try {
            return generalScopeCodeRepository.findById(id).map(generalScopeCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * PROJECT TYPE CODES
     **/
    public CollectionModel<ProjectTypeCodeModel> getAllProjectTypeCodes() throws ServiceException {
        try {
            List<ProjectTypeCodeEntity> entities = projectTypeCodeRepository.findAll();
            return projectTypeCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ProjectTypeCodeModel getProjectTypeCodeById(String id) throws ServiceException {
        try {
            return projectTypeCodeRepository.findById(id).map(projectTypeCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ProgramAreaModel> getAllProgramAreaCodes() {
        try {
            List<ProgramAreaEntity> entities = programAreaRepository.findAll();
            return programAreaResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ProgramAreaModel getProgramAreaCodeById(String id) {
        try {
            UUID guid = UUID.fromString(id);
            return programAreaRepository.findById(guid).map(programAreaResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ForestRegionUnitCodeModel> getAllForestRegionCodes() {
        try {
            List<ForestOrgUnitCodeEntity> entities = forestOrgUnitCodeRepository.findByForestOrgUnitTypeCode("REGION");
            return forestRegionCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }


    public CollectionModel<ForestDistrictUnitCodeModel> getAllForestDistrictCodes() {
        try {
            List<ForestOrgUnitCodeEntity> entities = forestOrgUnitCodeRepository.findByForestOrgUnitTypeCode("DISTRICT");
            return forestDistrictCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ForestRegionUnitCodeModel getForestRegionCodeById(Integer forestRegionId) {
        try {
            return forestOrgUnitCodeRepository.findById(forestRegionId).map(forestRegionCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ForestDistrictUnitCodeModel getForestDistrictCodeById(Integer forestDistrictId) {
        try {
            return forestOrgUnitCodeRepository.findById(forestDistrictId).map(forestDistrictCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
}
