package ca.bc.gov.nrs.wfprev.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProgramAreaResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.model.ProgramAreaEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.resources.ProgramAreaModel;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProgramAreaService implements CommonService {
  private ProgramAreaRepository programAreaRepository;
  private ProgramAreaResourceAssembler programAreaResourceAssembler;

  public ProgramAreaService(ProgramAreaRepository programAreaRepository, ProgramAreaResourceAssembler programAreaResourceAssembler) {
    this.programAreaRepository = programAreaRepository;
    this.programAreaResourceAssembler = programAreaResourceAssembler;
  }
  
  public CollectionModel<ProgramAreaModel> getAllProgramAreas() throws ServiceException {
    try {
      List<ProgramAreaEntity> entities = new ArrayList<>();
      return programAreaResourceAssembler.toCollectionModel(entities);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  public ProgramAreaModel getProgramAreaById(String id) throws ServiceException {
    try {
      return programAreaRepository.findById(id).map(programAreaResourceAssembler::toModel).orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProgramAreaModel createOrUpdateProgramArea(ProgramAreaModel resource) throws ServiceException {
    try {
      resource.setUpdateDate(new Date());

      ProgramAreaEntity oldEntity = programAreaResourceAssembler.toEntity(resource);
      ProgramAreaEntity newEntity = programAreaRepository.saveAndFlush(oldEntity);

      return programAreaResourceAssembler.toModel(newEntity);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProgramAreaModel deleteProgramArea(String id) throws ServiceException {
    try {
      ProgramAreaModel model = getProgramAreaById(id);

      ProgramAreaEntity entity = programAreaResourceAssembler.toEntity(model);
      programAreaRepository.delete(entity);

      return programAreaResourceAssembler.toModel(entity);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
}
