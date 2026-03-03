package ca.bc.gov.nrs.wfprev.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.models.ForecastAmountsModel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class ForecastAmountCalculatorService {

    public ForecastAmountsModel calculate(
            BigDecimal previousForecast,
            BigDecimal adjustmentAmount
    ) {
        BigDecimal prev = defaultZero(previousForecast);
        BigDecimal adj = defaultZero(adjustmentAmount);

        BigDecimal finalForecast =
                adj.compareTo(BigDecimal.ZERO) == 0 ? prev : adj;

        BigDecimal adjustment = finalForecast.subtract(prev);

        return new ForecastAmountsModel(finalForecast, adjustment);
    }

    public BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
