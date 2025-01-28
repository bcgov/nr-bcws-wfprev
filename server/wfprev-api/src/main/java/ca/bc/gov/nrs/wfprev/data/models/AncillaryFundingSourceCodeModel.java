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
@JsonRootName(value = "ancillaryFundingSourceCode")
@Relation(collectionRelation = "ancillaryFundingSourceCode")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AncillaryFundingSourceCodeModel extends CommonModel<AncillaryFundingSourceCodeModel> {
    private String ancillaryFundingSourceGuid;
    private String fundingSourceAbbreviation;
    private String fundingSourceName;
    private Integer displayOrder;
    private Date effectiveDate;
    private Date expiryDate;
}
