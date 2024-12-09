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
@JsonRootName(value = "project_status_code")
@Relation(collectionRelation = "project_status_code")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStatusCodeModel extends CommonModel<ProjectStatusCodeModel> {
    private String projectStatusCode;
    private String description;
    private Integer displayOrder;
    private Date effectiveDate;
    private Date expiryDate;
}
