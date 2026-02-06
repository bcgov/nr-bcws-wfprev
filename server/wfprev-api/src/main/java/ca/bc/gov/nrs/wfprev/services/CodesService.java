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
    private final ForestAreaCodeRepository forestAreaCodeRepository;
    private final ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler;
    private final GeneralScopeCodeRepository generalScopeCodeRepository;
    private final GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler;
    private final ProjectTypeCodeRepository projectTypeCodeRepository;
    private final ProjectTypeCodeResourceAssembler projectTypeCodeResourceAssembler;
    private final ProgramAreaRepository programAreaRepository;
    private final ProgramAreaResourceAssembler programAreaResourceAssembler;
    private final ForestOrgUnitCodeRepository forestOrgUnitCodeRepository;
    private final ForestRegionUnitCodeResourceAssembler forestRegionCodeResourceAssembler;
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
    private final FundingSourceCodeResourceAssembler fundingSourceCodeResourceAssembler;
    private final FundingSourceCodeRepository fundingSourceCodeRepository;
    private final SourceObjectNameCodeResourceAssembler sourceObjectNameCodeResourceAssembler;
    private final SourceObjectNameCodeRepository sourceObjectNameCodeRepository;
    private final AttachmentContentTypeCodeResourceAssembler attachmentContentTypeCodeResourceAssembler;
    private final AttachmentContentTypeCodeRepository attachmentContentTypeCodeRepository;
    private final SilvicultureBaseCodeResourceAssembler silvicultureBaseCodeResourceAssembler;
    private final SilvicultureBaseCodeRepository silvicultureBaseCodeRepository;
    private final SilvicultureMethodCodeResourceAssembler silvicultureMethodCodeResourceAssembler;
    private final SilvicultureMethodCodeRepository silvicultureMethodCodeRepository;
    private final SilvicultureTechniqueCodeResourceAssembler silvicultureTechniqueCodeResourceAssembler;
    private final SilvicultureTechniqueCodeRepository silvicultureTechniqueCodeRepository;
    private final ProposalTypeCodeRepository proposalTypeCodeRepository;
    private final ProposalTypeCodeResourceAssembler proposalTypeCodeResourceAssembler;
    private final WUIRiskClassCodeRepository wuiRiskClassCodeRepository;
    private final WUIRiskClassCodeResourceAssembler wuiRiskClassCodeResourceAssembler;
    private final EvaluationCriteriaCodeRepository evaluationCriteriaCodeRepository;
    private final EvaluationCriteriaCodeResourceAssembler evaluationCriteriaCodeResourceAssembler;
    private final ProjectStatusCodeRepository projectStatusCodeRepository;
    private final ProjectStatusCodeResourceAssembler projectStatusCodeResourceAssembler;
    private final WildfireOrgUnitRepository wildfireOrgUnitRepository;
    private final WildfireOrgUnitResourceAssembler wildfireOrgUnitResourceAssembler;
    private final ReportingPeriodCodeRepository reportingPeriodCodeRepository;
    private final ReportingPeriodCodeResourceAssembler reportingPeriodCodeResourceAssembler;


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
                        AncillaryFundingSourceCodeResourceAssembler ancillaryFundingSourceCodeResourceAssembler, AncillaryFundingSourceCodeRepository ancillaryFundingSourceCodeRepository,
                        FundingSourceCodeResourceAssembler fundingSourceCodeResourceAssembler, FundingSourceCodeRepository fundingSourceCodeRepository, SourceObjectNameCodeResourceAssembler sourceObjectNameCodeResourceAssembler, SourceObjectNameCodeRepository sourceObjectNameCodeRepository,
                        AttachmentContentTypeCodeResourceAssembler attachmentContentTypeCodeResourceAssembler, AttachmentContentTypeCodeRepository attachmentContentTypeCodeRepository, SilvicultureBaseCodeResourceAssembler silvicultureBaseCodeResourceAssembler, SilvicultureBaseCodeRepository silvicultureBaseCodeRepository,
                        SilvicultureMethodCodeResourceAssembler silvicultureMethodCodeResourceAssembler, SilvicultureMethodCodeRepository silvicultureMethodCodeRepository, SilvicultureTechniqueCodeResourceAssembler silvicultureTechniqueCodeResourceAssembler, SilvicultureTechniqueCodeRepository silvicultureTechniqueCodeRepository, 
                        ProposalTypeCodeRepository proposalTypeCodeRepository, ProposalTypeCodeResourceAssembler proposalTypeCodeResourceAssembler, WUIRiskClassCodeRepository wuiRiskClassCodeRepository, WUIRiskClassCodeResourceAssembler wuiRiskClassCodeResourceAssembler,
                        EvaluationCriteriaCodeRepository evaluationCriteriaCodeRepository, EvaluationCriteriaCodeResourceAssembler evaluationCriteriaCodeResourceAssembler, ProjectStatusCodeRepository projectStatusCodeRepository, ProjectStatusCodeResourceAssembler projectStatusCodeResourceAssembler,
                        WildfireOrgUnitRepository wildfireOrgUnitRepository, WildfireOrgUnitResourceAssembler wildfireOrgUnitResourceAssembler,
                        ReportingPeriodCodeRepository reportingPeriodCodeRepository, ReportingPeriodCodeResourceAssembler reportingPeriodCodeResourceAssembler) {
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
        this.fundingSourceCodeResourceAssembler = fundingSourceCodeResourceAssembler;
        this.fundingSourceCodeRepository = fundingSourceCodeRepository;
        this.sourceObjectNameCodeRepository = sourceObjectNameCodeRepository;
        this.sourceObjectNameCodeResourceAssembler = sourceObjectNameCodeResourceAssembler;
        this.attachmentContentTypeCodeResourceAssembler = attachmentContentTypeCodeResourceAssembler;
        this.attachmentContentTypeCodeRepository = attachmentContentTypeCodeRepository;
        this.silvicultureBaseCodeResourceAssembler = silvicultureBaseCodeResourceAssembler;
        this.silvicultureBaseCodeRepository = silvicultureBaseCodeRepository;
        this.silvicultureMethodCodeResourceAssembler = silvicultureMethodCodeResourceAssembler;
        this.silvicultureMethodCodeRepository = silvicultureMethodCodeRepository;
        this.silvicultureTechniqueCodeResourceAssembler = silvicultureTechniqueCodeResourceAssembler;
        this.silvicultureTechniqueCodeRepository = silvicultureTechniqueCodeRepository;
        this.proposalTypeCodeResourceAssembler = proposalTypeCodeResourceAssembler;
        this.proposalTypeCodeRepository = proposalTypeCodeRepository;
        this.wuiRiskClassCodeResourceAssembler = wuiRiskClassCodeResourceAssembler;
        this.wuiRiskClassCodeRepository = wuiRiskClassCodeRepository;
        this.evaluationCriteriaCodeResourceAssembler = evaluationCriteriaCodeResourceAssembler;
        this.evaluationCriteriaCodeRepository = evaluationCriteriaCodeRepository;
        this.projectStatusCodeRepository = projectStatusCodeRepository;
        this.projectStatusCodeResourceAssembler = projectStatusCodeResourceAssembler;
        this.wildfireOrgUnitRepository = wildfireOrgUnitRepository;
        this.wildfireOrgUnitResourceAssembler = wildfireOrgUnitResourceAssembler;
        this.reportingPeriodCodeRepository = reportingPeriodCodeRepository;
        this.reportingPeriodCodeResourceAssembler = reportingPeriodCodeResourceAssembler;
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

    public CollectionModel<FundingSourceCodeModel> getAllFundingSourceCodes() throws ServiceException {
        try {
            List<FundingSourceCodeEntity> entities = fundingSourceCodeRepository.findAll();
            return fundingSourceCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public FundingSourceCodeModel getFundingSourceCodeById(String id) throws ServiceException {
        try {
            return fundingSourceCodeRepository.findById(id)
                    .map(fundingSourceCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<SourceObjectNameCodeModel> getAllSourceObjectNameCodes() throws ServiceException {
        try {
            List<SourceObjectNameCodeEntity> entities = sourceObjectNameCodeRepository.findAll();
            return sourceObjectNameCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public SourceObjectNameCodeModel getSourceObjectNameCodeById(String id) throws ServiceException {
        try {
            return sourceObjectNameCodeRepository.findById(id)
                    .map(sourceObjectNameCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<AttachmentContentTypeCodeModel> getAllAttachmentContentTypeCodes() throws ServiceException {
        try {
            List<AttachmentContentTypeCodeEntity> entities = attachmentContentTypeCodeRepository.findAll();
            return attachmentContentTypeCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public AttachmentContentTypeCodeModel getAttachmentContentTypeCodeById(String id) throws ServiceException {
        try {
            return attachmentContentTypeCodeRepository.findById(id)
                    .map(attachmentContentTypeCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<SilvicultureBaseCodeModel> getAllSilvicultureBaseCodes() throws ServiceException {
        try {
            List<SilvicultureBaseCodeEntity> entities = silvicultureBaseCodeRepository.findAll();
            return silvicultureBaseCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public SilvicultureBaseCodeModel getSilvicultureBaseCodeById(String id) throws ServiceException {
        try {
            UUID uuid = UUID.fromString(id);
            return silvicultureBaseCodeRepository.findById(uuid)
                    .map(silvicultureBaseCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid UUID format for base: " + id, e);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<SilvicultureMethodCodeModel> getAllSilvicultureMethodCodes() throws ServiceException {
        try {
            List<SilvicultureMethodCodeEntity> entities = silvicultureMethodCodeRepository.findAll();
            return silvicultureMethodCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public SilvicultureMethodCodeModel getSilvicultureMethodCodeById(String id) throws ServiceException {
        try {
            UUID uuid = UUID.fromString(id);
            return silvicultureMethodCodeRepository.findById(uuid)
                    .map(silvicultureMethodCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid UUID format for method: " + id, e);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<SilvicultureTechniqueCodeModel> getAllSilvicultureTechniqueCodes() throws ServiceException {
        try {
            List<SilvicultureTechniqueCodeEntity> entities = silvicultureTechniqueCodeRepository.findAll();
            return silvicultureTechniqueCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public SilvicultureTechniqueCodeModel getSilvicultureTechniqueCodeById(String id) throws ServiceException {
        try {
            UUID uuid = UUID.fromString(id);
            return silvicultureTechniqueCodeRepository.findById(uuid)
                    .map(silvicultureTechniqueCodeResourceAssembler::toModel)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid UUID format for technique: " + id, e);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
    
    public CollectionModel<ProposalTypeCodeModel> getAllProposalTypeCodes() throws ServiceException {
        try {
            List<ProposalTypeCodeEntity> entities = proposalTypeCodeRepository.findAll();
            return proposalTypeCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ProposalTypeCodeModel getProposalTypeCodeById(String id) throws ServiceException {
        try {
            return proposalTypeCodeRepository.findById(id).map(proposalTypeCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<WUIRiskClassRankModel> getAllWuiRiskClassCodes() throws ServiceException {
        try {
            List<WUIRiskClassRankEntity> entities = wuiRiskClassCodeRepository.findAll();
            return wuiRiskClassCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public WUIRiskClassRankModel getWuiRiskClassCodeById(String id) throws ServiceException {
        try {
            return wuiRiskClassCodeRepository.findById(id).map(wuiRiskClassCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<EvaluationCriteriaCodeModel> getAllEvaluationCriteriaCodes() throws ServiceException {
        try {
            List<EvaluationCriteriaCodeEntity> entities = evaluationCriteriaCodeRepository.findAll();
            return evaluationCriteriaCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public EvaluationCriteriaCodeModel getEvaluationCriteriaCodeById(String id) throws ServiceException {
        try {
            return evaluationCriteriaCodeRepository.findById(UUID.fromString(id))
                .map(evaluationCriteriaCodeResourceAssembler::toModel)
                .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ProjectStatusCodeModel> getAllProjectStatusCodes() throws ServiceException {
        try {
            List<ProjectStatusCodeEntity> entities = projectStatusCodeRepository.findAll();
            return projectStatusCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ProjectStatusCodeModel getProjectStatusCodeById(String id) throws ServiceException {
        try {
            return projectStatusCodeRepository.findById(id).map(projectStatusCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<WildfireOrgUnitModel> getAllWildfireOrgUnits() throws ServiceException {
        try {
            List<WildfireOrgUnitEntity> entities = wildfireOrgUnitRepository.findAll();
            return wildfireOrgUnitResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public WildfireOrgUnitModel getWildfireOrgUnitById(String id) throws ServiceException {
        try {
            return wildfireOrgUnitRepository.findById(id)
                    .map(wildfireOrgUnitResourceAssembler::toModel)
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public CollectionModel<ReportingPeriodCodeModel> getAllReportingPeriodCodes() throws ServiceException {
        try {
            List<ReportingPeriodCodeEntity> entities = reportingPeriodCodeRepository.findAll();
            return reportingPeriodCodeResourceAssembler.toCollectionModel(entities);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ReportingPeriodCodeModel getReportingPeriodCodeById(String id) throws ServiceException {
        try {
            return reportingPeriodCodeRepository.findById(id).map(reportingPeriodCodeResourceAssembler::toModel).orElse(null);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }


}
