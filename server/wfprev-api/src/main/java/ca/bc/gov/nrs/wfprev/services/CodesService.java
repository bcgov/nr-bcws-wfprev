package ca.bc.gov.nrs.wfprev.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.bc.gov.nrs.wfprev.data.assemblers.*;
import ca.bc.gov.nrs.wfprev.data.entities.*;
import ca.bc.gov.nrs.wfprev.data.models.*;
import ca.bc.gov.nrs.wfprev.data.repositories.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CodesService implements CommonService {
    public static final String BC_PARKS_REGION_ORG_UNIT_TYPE_CODE = "REGION";
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
    private final BCParksRegionCodeResourceAssembler bcParksRegionCodeResourceAssembler;
    private final BCParksSectionCodeResourceAssembler bcParksSectionCodeResourceAssembler;
    private final BCParksOrgUnitCodeRepository bcParksOrgUnitCodeRepository;
    private final ObjectiveTypeCodeResourceAssembler objectiveTypeCodeResourceAssembler;
    private final ObjectiveTypeCodeRepository objectiveTypeCodeRepository;
    private final ProjectPlanStatusCodeResourceAssembler projectPlanStatusCodeResourceAssembler;
    private final ProjectPlanStatusCodeRepository projectPlanStatusCodeRepository;
    private final ActivityStatusCodeResourceAssembler activityStatusCodeResourceAssembler;
    private final ActivityStatusCodeRepository activityStatusCodeRepository;
    private final RiskRatingCodeResourceAssembler riskRatingCodeResourceAssembler;
    private final RiskRatingCodeRepository riskRatingCodeRepository;
    private final ContractPhaseCodeResourceAssembler contractPhaseCodeResourceAssembler;
    private final ContractPhaseCodeRepository contractPhaseCodeRepository;
    private final ActivityCategoryCodeResourceAssembler activityCategoryCodeResourceAssembler;
    private final ActivityCategoryCodeRepository activityCategoryCodeRepository;
    private final PlanFiscalStatusCodeResourceAssembler planFiscalStatusCodeResourceAssembler;
    private final PlanFiscalStatusCodeRepository planFiscalStatusCodeRepository;
    private final AncillaryFundingSourceCodeResourceAssembler ancillaryFundingSourceCodeResourceAssembler;
    private final AncillaryFundingSourceCodeRepository ancillaryFundingSourceCodeRepository;


    public CodesService(ForestAreaCodeRepository forestAreaCodeRepository, ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler,
                        GeneralScopeCodeRepository generalScopeCodeRepository, GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler,
                        ProjectTypeCodeRepository projectTypeCodeRepository, ProjectTypeCodeResourceAssembler projectTypeCodeResourceAssembler, ProgramAreaRepository programAreaRepository, ProgramAreaResourceAssembler programAreaResourceAssembler,
                        ForestOrgUnitCodeRepository forestRegionCodeRepository, ForestRegionUnitCodeResourceAssembler forestRegionCodeResourceAssembler, ForestDistrictUnitCodeResourceAssembler forestDistrictUnitCodeResourceAssembler,
                        BCParksOrgUnitCodeRepository bcParksOrgUnitCodeRepository, BCParksRegionCodeResourceAssembler bcParksRegionCodeResourceAssembler, BCParksSectionCodeResourceAssembler bcParksSectionCodeResourceAssembler,
                        ObjectiveTypeCodeResourceAssembler objectiveTypeCodeResourceAssembler, ObjectiveTypeCodeRepository objectiveTypeCodeRepository, ProjectPlanStatusCodeResourceAssembler projectPlanStatusCodeResourceAssembler,
                        ProjectPlanStatusCodeRepository projectPlanStatusCodeRepository, ActivityStatusCodeResourceAssembler activityStatusCodeResourceAssembler, ActivityStatusCodeRepository activityStatusCodeRepository,
                        RiskRatingCodeResourceAssembler riskRatingCodeResourceAssembler, RiskRatingCodeRepository riskRatingCodeRepository, ContractPhaseCodeResourceAssembler contractPhaseCodeResourceAssembler, ContractPhaseCodeRepository contractPhaseCodeRepository,
                        ActivityCategoryCodeResourceAssembler activityCategoryCodeResourceAssembler, ActivityCategoryCodeRepository activityCategoryCodeRepository,
                        PlanFiscalStatusCodeResourceAssembler planFiscalStatusCodeResourceAssembler, PlanFiscalStatusCodeRepository planFiscalStatusCodeRepository,
                        AncillaryFundingSourceCodeResourceAssembler ancillaryFundingSourceCodeResourceAssembler, AncillaryFundingSourceCodeRepository ancillaryFundingSourceCodeRepository) {
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
        this.bcParksOrgUnitCodeRepository = bcParksOrgUnitCodeRepository;
        this.bcParksRegionCodeResourceAssembler = bcParksRegionCodeResourceAssembler;
        this.bcParksSectionCodeResourceAssembler = bcParksSectionCodeResourceAssembler;
        this.objectiveTypeCodeResourceAssembler = objectiveTypeCodeResourceAssembler;
        this.objectiveTypeCodeRepository = objectiveTypeCodeRepository;
        this.projectPlanStatusCodeResourceAssembler = projectPlanStatusCodeResourceAssembler;
        this.projectPlanStatusCodeRepository = projectPlanStatusCodeRepository;
        this.activityStatusCodeResourceAssembler = activityStatusCodeResourceAssembler;
        this.activityStatusCodeRepository = activityStatusCodeRepository;
        this.riskRatingCodeResourceAssembler = riskRatingCodeResourceAssembler;
        this.riskRatingCodeRepository = riskRatingCodeRepository;
        this.contractPhaseCodeResourceAssembler = contractPhaseCodeResourceAssembler;
        this.contractPhaseCodeRepository = contractPhaseCodeRepository;
        this.activityCategoryCodeResourceAssembler = activityCategoryCodeResourceAssembler;
        this.activityCategoryCodeRepository = activityCategoryCodeRepository;
        this.planFiscalStatusCodeResourceAssembler = planFiscalStatusCodeResourceAssembler;
        this.planFiscalStatusCodeRepository = planFiscalStatusCodeRepository;
        this.ancillaryFundingSourceCodeResourceAssembler = ancillaryFundingSourceCodeResourceAssembler;
        this.ancillaryFundingSourceCodeRepository = ancillaryFundingSourceCodeRepository;
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
            List<ForestOrgUnitCodeEntity> entities = forestOrgUnitCodeRepository.findByForestOrgUnitTypeCode(BC_PARKS_REGION_ORG_UNIT_TYPE_CODE);
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

    public CollectionModel<BCParksRegionCodeModel> getAllBCParksRegionCodes() {
        try {
            List<BCParksOrgUnitEntity> entities = bcParksOrgUnitCodeRepository.findByBcParksOrgUnitTypeCode(BC_PARKS_REGION_ORG_UNIT_TYPE_CODE);
            return bcParksRegionCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public BCParksRegionCodeModel getBCParksRegionCodeById(Integer number) {
        try {
            Optional<BCParksOrgUnitEntity> byId = bcParksOrgUnitCodeRepository.findById(number);
            return byId.map(bcParksRegionCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<BCParksSectionCodeModel> getAllBCParksSectionCodes() {
        try {
            List<BCParksOrgUnitEntity> entities = bcParksOrgUnitCodeRepository.findByBcParksOrgUnitTypeCode("SECTION");
            return bcParksSectionCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public BCParksSectionCodeModel getBCParksSectionCodeById(Integer bcParksSectionId) {
        try {
            return bcParksOrgUnitCodeRepository.findById(bcParksSectionId).map(bcParksSectionCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ObjectiveTypeCodeModel> getAllObjectiveTypeCodes() throws ServiceException {
        try {
            List<ObjectiveTypeCodeEntity> entities = objectiveTypeCodeRepository.findAll();
            return objectiveTypeCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ObjectiveTypeCodeModel getObjectiveTypeCodeById(String id) throws ServiceException {
        try {
            return objectiveTypeCodeRepository.findById(id).map(objectiveTypeCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ProjectPlanStatusCodeModel> getAllProjectPlanStatusCodes() throws ServiceException {
        try {
            List<ProjectPlanStatusCodeEntity> entities = projectPlanStatusCodeRepository.findAll();
            return projectPlanStatusCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ProjectPlanStatusCodeModel getProjectPlanStatusCodeById(String id) throws ServiceException {
        try {
            return projectPlanStatusCodeRepository.findById(id).map(projectPlanStatusCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ActivityStatusCodeModel> getAllActivityStatusCodes() throws ServiceException {
        try {
            List<ActivityStatusCodeEntity> entities = activityStatusCodeRepository.findAll();
            return activityStatusCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ActivityStatusCodeModel getActivityStatusCodeById(String id) throws ServiceException {
        try {
            return activityStatusCodeRepository.findById(id).map(activityStatusCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<RiskRatingCodeModel> getAllRiskRatingCodes() throws ServiceException {
        try {
            List<RiskRatingCodeEntity> entities = riskRatingCodeRepository.findAll();
            return riskRatingCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ActivityCategoryCodeModel> getAllActivityCategoryCodes() throws ServiceException {
        try {
            List<ActivityCategoryCodeEntity> entities = activityCategoryCodeRepository.findAll();
            return activityCategoryCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public RiskRatingCodeModel getRiskRatingCodeById(String id) throws ServiceException {
        try {
            return riskRatingCodeRepository.findById(id).map(riskRatingCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
    public ActivityCategoryCodeModel getActivityCategoryCodeById(String id) throws ServiceException {
        try {
            return activityCategoryCodeRepository.findById(id)
                    .map(activityCategoryCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ContractPhaseCodeModel> getAllContractPhaseCodes() throws ServiceException {
        try {
            List<ContractPhaseCodeEntity> entities = contractPhaseCodeRepository.findAll();
            return contractPhaseCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<PlanFiscalStatusCodeModel> getAllPlanFiscalStatusCodes() throws ServiceException {
        try {
            List<PlanFiscalStatusCodeEntity> entities = planFiscalStatusCodeRepository.findAll();
            return planFiscalStatusCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
    
    public PlanFiscalStatusCodeModel getPlanFiscalStatusCodeById(String id) throws ServiceException {
        try {
            return planFiscalStatusCodeRepository.findById(id)
                    .map(planFiscalStatusCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ContractPhaseCodeModel getContractPhaseCodeById(String id) throws ServiceException {
        try {
            return contractPhaseCodeRepository.findById(id).map(contractPhaseCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<AncillaryFundingSourceCodeModel> getAllAncillaryFundingSourceCodes() throws ServiceException {
        try {
            List<AncillaryFundingSourceCodeEntity> entities = ancillaryFundingSourceCodeRepository.findAll();
            return ancillaryFundingSourceCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public AncillaryFundingSourceCodeModel getAncillaryFundingSourceCodeById(String id) throws ServiceException {
        try {
            return ancillaryFundingSourceCodeRepository.findById(id)
                    .map(ancillaryFundingSourceCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
    
}
