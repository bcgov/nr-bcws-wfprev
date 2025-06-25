package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;

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
@JsonRootName(value = "fuelManagementObjectiveCode")
@Relation(collectionRelation = "fuelManagementObjectiveCode")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuelManagementObjectiveCodeModel extends CommonModel<FuelManagementObjectiveCodeModel> {
    private String fuelManagementObjectiveGuid;
    private String fuelManagementObjectiveTypeCode;
    private String objectiveLabel;
    private BigDecimal weightedRank;
}
