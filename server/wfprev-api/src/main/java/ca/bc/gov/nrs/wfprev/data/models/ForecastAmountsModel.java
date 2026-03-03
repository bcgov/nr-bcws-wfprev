package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;

public record ForecastAmountsModel(
    BigDecimal forecastAmount,
    BigDecimal forecastAdjustmentAmount
) {}
