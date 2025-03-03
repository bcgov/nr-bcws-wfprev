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
@JsonRootName(value = "silvicultureTechniqueCode")
@Relation(collectionRelation = "silvicultureTechniqueCode")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SilvicultureTechniqueCodeModel extends CommonModel<SilvicultureTechniqueCodeModel> {
    private String silvicultureTechniqueGuid;
    private String silvicultureBaseGuid;
    private String silvicultureTechniqueCode;
    private String description;
    private Date systemStartTimestamp;
    private Date systemEndTimestamp;
    private Integer revisionCount;
}
