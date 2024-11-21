package ca.bc.gov.nrs.wfprev.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestAreaCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.GeneralScopeCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.model.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.model.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.resources.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.resources.GeneralScopeCodeModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CodesService implements CommonService {
  private ForestAreaCodeRepository forestAreaCodeRepository;
  private ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler;
  private GeneralScopeCodeRepository generalScopeCodeRepository;
  private GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler;

  public CodesService(ForestAreaCodeRepository forestAreaCodeRepository, ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler,
                      GeneralScopeCodeRepository generalScopeCodeRepository, GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler) {
    this.forestAreaCodeRepository = forestAreaCodeRepository;
    this.forestAreaCodeResourceAssembler = forestAreaCodeResourceAssembler;
    this.generalScopeCodeRepository = generalScopeCodeRepository;
    this.generalScopeCodeResourceAssembler = generalScopeCodeResourceAssembler;
  }

  /** FOREST AREA CODES **/
  public CollectionModel<ForestAreaCodeModel> getAllForestAreaCodes() throws ServiceException {
    try {
      List<ForestAreaCodeEntity> entities = new ArrayList<>();
      return forestAreaCodeResourceAssembler.toCollectionModel(entities);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
  
  public ForestAreaCodeModel getForestAreaCodeById(String id) throws ServiceException {
    try {
        return forestAreaCodeRepository.findById(id).map(forestAreaCodeResourceAssembler::toModel).orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  /** GENERAL SCOPE CODES **/
  public CollectionModel<GeneralScopeCodeModel> getAllGeneralScopeCodes() throws ServiceException {
    try {
      List<GeneralScopeCodeEntity> entities = new ArrayList<>();
      return generalScopeCodeResourceAssembler.toCollectionModel(entities);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
  
  public GeneralScopeCodeModel getGeneralScopeCodeById(String id) throws ServiceException {
    try {
        return generalScopeCodeRepository.findById(id).map(generalScopeCodeResourceAssembler::toModel).orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
}
