package ca.bc.gov.nrs.wfprev.data.resources;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import ca.bc.gov.nrs.wfprev.data.model.ExampleCodeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The Resource model for objects
 * It's annoying to have an Entity model and a resource model as it feels like
 * a double-implementation of the same thing, but this can be really important
 * if you want your resource model to behave or be structured differently than
 * the data model represented in the "model" objects. They also serve two different
 * purposes. This model is Java->json representation out to the consumer requesting
 * data. The model is Java->db representation and has definitions specific to
 * JPA and working with the data. You can keep your json and data rules
 * and implementation seperate this way
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "example")
@Relation(collectionRelation = "examples")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExampleModel extends CommonModel<ExampleModel> {
  private String exampleGuid;
  private String exampleCode;
  private String exampleText;
  private String exampleVar;
  private BigDecimal exampleNum;
  private Boolean exampleInd;
	private Long lastUpdatedTimestamp;
  private Integer revisionCount;
	private String createdBy;
	private Date createDate;
	private String updatedBy;
	private Date updateDate;
}
