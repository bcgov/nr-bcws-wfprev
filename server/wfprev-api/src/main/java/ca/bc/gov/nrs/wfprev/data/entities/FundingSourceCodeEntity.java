package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "funding_source", schema = "wfprev")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundingSourceCodeEntity implements Serializable {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "funding_source_guid", nullable = false, updatable = false)
    private UUID fundingSourceGuid;

    @Column(name = "funding_source_abbreviation", length = 20, nullable = false)
    private String fundingSourceAbbreviation;

    @Column(name = "funding_source_name", length = 50, nullable = false)
    private String fundingSourceName;

    @Version
    @Column(name = "revision_count", precision = 10, nullable = false)
    private Integer revisionCount;

    @CreatedBy
    @Column(name = "create_user", length = 64, nullable = false)
    private String createUser;

    @CreatedDate
    @Column(name = "create_date", nullable = false, columnDefinition = "DATE DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @LastModifiedBy
    @Column(name = "update_user", length = 64, nullable = false)
    private String updateUser;

    @LastModifiedDate
    @Column(name = "update_date", nullable = false, columnDefinition = "DATE DEFAULT CURRENT_TIMESTAMP")
    private Date updateDate;
}