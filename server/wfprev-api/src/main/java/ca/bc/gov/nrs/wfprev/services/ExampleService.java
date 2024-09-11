package ca.bc.gov.nrs.wfprev.services;

import java.util.Date;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ExampleCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ExampleResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.model.ExampleCodeEntity;
import ca.bc.gov.nrs.wfprev.data.model.ExampleEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ExampleCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ExampleRepository;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleCodeModel;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleModel;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation. Services should map nearly 1:1 with your controllers, and act
 * as the "business logic" layer to your controller. Ideally we can keep this split up
 * by resource/endpoint so a single service doesn't wind up getting insanely large.
 * Other services can communicate with each other, so autowire sercvices together
 * where needed.
 * 
 * Note: If you make a service and don't implement CommonService, be sure to add
 * the @Component annotation if you intend to autowire this anywhere else.
 */
@Slf4j
@Component
public class ExampleService implements CommonService {
  /* The Repository Objects used in this Service */
  private ExampleRepository exampleRepository;
  private ExampleCodeRepository exampleCodeRepository;
  /* The resource assemblers. Use this to convert Entity to Resource and vice-versa */
  private ExampleResourceAssembler exampleResourceAssembler;
  private ExampleCodeResourceAssembler exampleCodeResourceAssembler;

  public ExampleService(ExampleRepository exampleRepository, ExampleCodeRepository exampleCodeRepository, ExampleResourceAssembler exampleResourceAssembler, ExampleCodeResourceAssembler exampleCodeResourceAssembler) {
    this.exampleRepository = exampleRepository;
    this.exampleCodeRepository = exampleCodeRepository;
    this.exampleResourceAssembler = exampleResourceAssembler;
    this.exampleCodeResourceAssembler = exampleCodeResourceAssembler;
  }

  public CollectionModel<ExampleModel> getAllExamples() throws ServiceException {
    try {
      List<ExampleEntity> entities = exampleRepository.findAll();
      return exampleResourceAssembler.toCollectionModel(entities);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
   
  public ExampleModel getExampleById(String id) throws ServiceException {
    try {
      return exampleRepository.findById(id).map(exampleResourceAssembler::toModel).orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
  
  @Transactional
  public ExampleModel createOrUpdateExample(ExampleModel resource) throws ServiceException {
    try {
      resource.setLastUpdatedTimestamp(new Date().getTime());
      resource.setUpdateDate(new Date());

      ExampleEntity oldEntity = exampleResourceAssembler.toEntity(resource);
      ExampleEntity newEntity = exampleRepository.saveAndFlush(oldEntity);

      return exampleResourceAssembler.toModel(newEntity);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  public CollectionModel<ExampleCodeModel> getAllExampleCodes() throws ServiceException {
    try {
      List<ExampleCodeEntity> entities = exampleCodeRepository.findAll();
      return exampleCodeResourceAssembler.toCollectionModel(entities);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
  
  public ExampleCodeModel getExampleCodeById(String id) throws ServiceException {
    try {
      return exampleCodeRepository.findById(id).map(exampleCodeResourceAssembler::toModel).orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
}
