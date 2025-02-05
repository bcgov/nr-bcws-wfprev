package ca.bc.gov.nrs.wfprev.data.models;

import java.util.Date;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "fundingSourceCode")
@Relation(collectionRelation = "fundingSourceCode")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundingSourceCodeModel extends CommonModel<FundingSourceCodeModel> {
    private String fundingSourceGuid;
    private String fundingSourceAbbreviation;
    private String fundingSourceName;
    private Integer displayOrder;
    private Date effectiveDate;
    private Date expiryDate;
}
