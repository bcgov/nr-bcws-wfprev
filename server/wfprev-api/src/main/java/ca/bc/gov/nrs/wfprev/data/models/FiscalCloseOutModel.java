package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.springframework.hateoas.server.core.Relation;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "fiscalCloseOut")
@Relation(collectionRelation = "fiscalCloseOuts")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FiscalCloseOutModel extends CommonModel<FiscalCloseOutModel> {
    private String projectPlanFiscalCloseOutGuid;
    private String projectPlanFiscalGuid;
    
    @NotBlank
    private String outcomeComment;
}
