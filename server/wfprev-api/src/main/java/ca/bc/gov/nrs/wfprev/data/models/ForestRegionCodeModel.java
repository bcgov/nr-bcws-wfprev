package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "forestAreaCode")
@Relation(collectionRelation = "forestAreaCode")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForestRegionCodeModel extends CommonModel<ForestRegionCodeModel> {
    private String orgunitId;
    private Date effectiveDate;
    private Date expiryDate;
    private String forestOrgUnitTypeCode;
    private String parentOrgUnitId;
    private String orgUnitName;
    private Integer integerAlias;
    private String characterAlias;
}
