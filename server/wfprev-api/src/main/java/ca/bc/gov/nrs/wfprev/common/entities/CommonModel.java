package ca.bc.gov.nrs.wfprev.common.entities;

import java.io.Serializable;
import java.util.Date;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Common Representation Model used for any resources.
 * Mainly for ensuring we include the eTag methods and any other common functionality
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class CommonModel<T extends RepresentationModel<? extends T>> extends RepresentationModel<T> implements Serializable {
  public String eTag() {
    return ETag.generate(this);
  }

  public String quotedETag() {
    return "\"" + ETag.generate(this) + "\"";
  }
}
