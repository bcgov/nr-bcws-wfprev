package ca.bc.gov.nrs.wfprev.data.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FiscalCloseoutSubmitRequest {

    @NotNull
    @Valid
    private ProjectFiscalModel projectFiscal;

    @NotNull
    @Valid
    private FiscalCloseoutResponse closeout;

    @NotNull
    private List<@Valid ActivityModel> activities;
}