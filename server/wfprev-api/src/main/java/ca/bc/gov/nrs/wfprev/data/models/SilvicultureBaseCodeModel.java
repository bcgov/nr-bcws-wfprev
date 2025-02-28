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
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "silvicultureBaseCode")
@Relation(collectionRelation = "silvicultureBaseCode")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SilvicultureBaseCodeModel extends CommonModel<SilvicultureBaseCodeModel> {
    private String silvicultureBaseGuid;
    private String silvicultureBaseCode;
    private String projectTypeCode;
    private String description;
    private Date systemStartTimestamp;
    private Date systemEndTimestamp;
    private Integer revisionCount;
}
