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
@JsonRootName(value = "fiscalCloseout")
@Relation(collectionRelation = "fiscalCloseouts")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FiscalCloseoutResponse extends CommonModel<FiscalCloseoutResponse> {
    private String projectPlanFiscalCloseoutGuid;
    private String projectPlanFiscalGuid;
    
    @NotBlank
    private String outcomeComment;
}
