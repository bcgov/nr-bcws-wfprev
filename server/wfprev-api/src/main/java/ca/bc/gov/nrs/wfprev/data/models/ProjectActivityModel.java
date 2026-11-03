package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.serializers.IsoInstantDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.MultiPolygon;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "projectActivity")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectActivityModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID projectGuid;
    private Integer projectNumber;
    private String projectTypeCode;
    private String projectTypeDesc;
    private String primaryObjectiveTypeCode;
    private String primaryObjectiveTypeDesc;
    private String secondaryObjectiveTypeCode;
    private String secondaryObjectiveTypeDesc;
    private String resultsProjectCode;
    private List<Activity> activities;
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = IsoInstantDeserializer.class)
    private Instant lastUpdated;

    @Data
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Activity implements Serializable {
        private static final long serialVersionUID = 1L;

        private UUID projectPlanFiscalGuid;
        private BigDecimal fiscalYear;
        private String projectFiscalName;
        private UUID activityGuid;
        private String activityName;
        private String activityStatusCode;
        private String activityStatusDesc;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Date activityStartDate;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Date activityEndDate;
        private BigDecimal completedAreaHa;
        private Boolean outstandingObligationsInd;
        private MultiPolygon geometry;
    }
}

