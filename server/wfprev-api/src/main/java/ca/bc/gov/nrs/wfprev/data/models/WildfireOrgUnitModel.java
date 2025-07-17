package ca.bc.gov.nrs.wfprev.data.models;

import java.util.Date;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "wildfireOrgUnit")
@Relation(collectionRelation = "wildfireOrgUnit")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WildfireOrgUnitModel extends CommonModel<WildfireOrgUnitModel> {
    private Integer orgUnitIdentifier;
    private Date effectiveDate;
    private Date expiryDate;
    private WildfireOrgUnitTypeCodeModel wildfireOrgUnitTypeCode;
    private Integer parentOrgUnitIdentifier;
    private String orgUnitName;
    private Integer integerAlias;
    private String characterAlias;
}
