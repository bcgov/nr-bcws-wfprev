package ca.bc.gov.nrs.wfprev.data.resources;

import java.util.Date;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "forestAreaCode")
@Relation(collectionRelation = "forestAreaCode")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForestAreaCodeModel extends CommonModel<ForestAreaCodeModel> {
  private String forestAreaCode;
	private String description;
	private Integer displayOrder;
	private Date effectiveDate;
	private Date expiryDate;
}
