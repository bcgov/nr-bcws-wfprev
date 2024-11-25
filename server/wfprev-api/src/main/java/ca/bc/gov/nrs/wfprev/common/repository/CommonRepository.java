package ca.bc.gov.nrs.wfprev.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/*
 * Default base class for JPA Repositories. You can include any default queries that should be common
 * across all repositories. This would be things like:
 * findByLastUpdateTimestampGreaterThan or whatever makes sense... for example, all repos might
 * have a search by last update time, create time, etc
 */
@NoRepositoryBean
public interface CommonRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T>  {
  Page<T> findByupdateDateGreaterThan(@Param("updateDate") Date updateDate, Pageable pageable);
}
