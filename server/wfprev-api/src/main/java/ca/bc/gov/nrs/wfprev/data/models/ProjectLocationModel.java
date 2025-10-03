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
import org.hibernate.annotations.Immutable;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "project")
@Relation(collectionRelation = "project")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Immutable
public class ProjectLocationModel extends CommonModel<ProjectLocationModel> {
    private String projectGuid;
    private BigDecimal latitude;
    private BigDecimal longitude;
}