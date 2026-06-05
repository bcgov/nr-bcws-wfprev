package ca.bc.gov.nrs.wfprev.data.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FiscalCloseoutSubmitResponse {

    private FiscalCloseoutResponse closeout;
    private ProjectFiscalModel projectFiscal;
    private List<ActivityModel> activities;
}