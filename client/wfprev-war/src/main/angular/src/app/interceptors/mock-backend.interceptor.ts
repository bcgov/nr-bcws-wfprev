import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable()
export class MockBackendInterceptor implements HttpInterceptor {

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {

    const simulateError = false;

    if (simulateError) {
      return throwError(() =>
        new HttpErrorResponse({
          status: 500,
          statusText: 'Internal Server Error',
          error: { message: 'Mocked backend error' }
        })
      ).pipe(delay(400));
    }

    if (req.method === 'GET' && req.url.includes('/performanceUpdates')) {

      return of(
        new HttpResponse
          ({
            status: 200,
            body: [{
              date: "2025-01-08",
              reportingPeriod: "End of Q2",
              timeStatus: "DELAYED",
              forecastStatus: "CHANGED_DECREASED",
              updateGeneralStatus: "IN_PROGRESS",

              generalUpdates: "Weather has delayed the progression",
              submitedBy: "Stachiw Lena",

              revisedForecastAmount: "$ 8,100",
              forecastAdjustmentAmount: "-$ 1,000",
              forecastAdjustmentRational: "Decrised material cost",

              highRisk: "$1,400",
              highRiskDescription: "A discription of why this amount is high risk",
              mediumRisk: "$5,000",
              mediumRiskDescription: "A discription of why this amount is medium risk",
              lowRisk: "$1,700",
              lowRiskDescription: "A discription of why this amount is low risk",
              complete: "$0",
              total: "$8,100",
            }, {
              date: "2025-01-07",
              reportingPeriod: "End of Q1",
              timeStatus: "ON_TRACK",
              forecastStatus: "CHANGED_INCREASED",
              updateGeneralStatus: "PREPARED",

              generalUpdates: "Weather has delayed the progression",
              submitedBy: "Stachiw Lena",

              revisedForecastAmount: "$ 8,100",
              forecastAdjustmentAmount: "-$ 1,000",
              forecastAdjustmentRational: "Decrised material cost",

              highRisk: "$1,400",
              highRiskDescription: "A discription of why this amount is high risk",
              mediumRisk: "$5,000",
              mediumRiskDescription: "A discription of why this amount is medium risk",
              lowRisk: "$1,700",
              lowRiskDescription: "A discription of why this amount is low risk",
              complete: "$0",
              total: "$8,100",
            },
            {
              date: "2025-01-06",
              reportingPeriod: "End of Q1",
              timeStatus: "ON_TRACK",
              forecastStatus: "NON_CHANGED",
              updateGeneralStatus: "PREPARED",

              generalUpdates: "Weather has delayed the progression",
              submitedBy: "Stachiw Lena",

              revisedForecastAmount: "$ 8,100",
              forecastAdjustmentAmount: "-$ 1,000",
              forecastAdjustmentRational: "Decrised material cost",

              highRisk: "$1,400",
              highRiskDescription: "A discription of why this amount is high risk",
              mediumRisk: "$5,000",
              mediumRiskDescription: "A discription of why this amount is medium risk",
              lowRisk: "$1,700",
              lowRiskDescription: "A discription of why this amount is low risk",
              complete: "$0",
              total: "$8,100",
            },
          {
              date: "2025-01-05",
              reportingPeriod: "End of Q1",
              timeStatus: "ON_TRACK",
              forecastStatus: "NON_CHANGED",
              updateGeneralStatus: "IN_PROGRESS",

              generalUpdates: "Weather has delayed the progression",
              submitedBy: "Stachiw Lena",

              revisedForecastAmount: "$ 8,100",
              forecastAdjustmentAmount: "-$ 1,000",
              forecastAdjustmentRational: "Decrised material cost",

              highRisk: "$1,400",
              highRiskDescription: "A discription of why this amount is high risk",
              mediumRisk: "$5,000",
              mediumRiskDescription: "A discription of why this amount is medium risk",
              lowRisk: "$1,700",
              lowRiskDescription: "A discription of why this amount is low risk",
              complete: "$0",
              total: "$8,100",
            }]
          })
      ).pipe(delay(300));
    } else if (req.method === 'GET' && req.url.includes('/getPerformanceUpdatesEstimates')) {
      return of(new HttpResponse
        ({
          status: 200,
          body: {
            currentForecast: "8950",
            originalCostEstimate: "8500"
          }
        })).pipe(delay(300));
    } else if (req.method === 'POST' && req.url.includes('/savePerformanceUpdates')) {
      return of(
        new HttpResponse({
          status: 200,
          body: {
            success: true,
            message: 'Performance updates saved successfully'
          }
        })
      );
    }

    return next.handle(req);
  }
}
