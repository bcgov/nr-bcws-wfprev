import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { QueryList } from '@angular/core';
import { YearEndActivitiesComponent } from './year-end-activities.component';
import { YearEndActivityItemComponent } from '../year-end-activity-item/year-end-activity-item.component';
import { ProjectService } from '../../../services/project-services';
import { TokenService } from '../../../services/token.service';
import { of } from 'rxjs';

class MockTokenService {
  credentialsEmitter = of(null);
  authTokenEmitter = of('');
  doesUserHaveApplicationPermissions() { return true; }
}

describe('YearEndActivitiesComponent', () => {
  let component: YearEndActivitiesComponent;
  let fixture: ComponentFixture<YearEndActivitiesComponent>;

  beforeEach(async () => {
    const mockProjectService = jasmine.createSpyObj('ProjectService', ['getProjectBoundaries']);

    await TestBed.configureTestingModule({
      imports: [
        YearEndActivitiesComponent,
        CommonModule,
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: TokenService, useClass: MockTokenService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(YearEndActivitiesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should return empty activity data when activityItems query list is empty', () => {
    fixture.detectChanges();
    expect(component.getActivityData()).toEqual([]);
  });

  it('should return true for isAllValid when activityItems query list is empty', () => {
    fixture.detectChanges();
    expect(component.isAllValid()).toBeTrue();
  });
});
