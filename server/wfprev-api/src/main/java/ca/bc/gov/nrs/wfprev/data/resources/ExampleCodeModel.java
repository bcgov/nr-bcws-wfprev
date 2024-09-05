package ca.bc.gov.nrs.wfprev.data.resources;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;

import org.springframework.hateoas.server.core.Relation;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "exampleCode")
@Relation(collectionRelation = "exampleCodes")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExampleCodeModel extends CommonModel<ExampleCodeModel> {
  private String exampleCode;
  private String description;
  private Integer displayOrder;
  private Date effectiveDate;
  private Date expiryDate;
  private Integer revisionCount;
	private String createdBy;
	private Date createDate;
	private String updatedBy;
	private Date updateDate;
}
