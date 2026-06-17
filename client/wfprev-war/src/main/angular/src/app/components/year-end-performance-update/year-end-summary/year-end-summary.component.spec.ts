import { ComponentFixture, TestBed } from '@angular/core/testing';
import { YearEndSummaryComponent } from './year-end-summary.component';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { ProjectService } from 'src/app/services/project-services';

describe('YearEndSummaryComponent', () => {
  let component: YearEndSummaryComponent;
  let fixture: ComponentFixture<YearEndSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [YearEndSummaryComponent],
      providers: [
        { provide: AppConfigService, useValue: { getConfig: () => ({}) } },
        { provide: TokenService, useValue: {} },
        { provide: ProjectService, useValue: {
          getAllFiscalCloseouts: () => ({ subscribe: () => {} }),
          getProjectFiscalByProjectPlanFiscalGuid: () => ({ subscribe: () => {} }),
          getFiscalActivities: () => ({ subscribe: () => {} }),
        }},
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(YearEndSummaryComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});