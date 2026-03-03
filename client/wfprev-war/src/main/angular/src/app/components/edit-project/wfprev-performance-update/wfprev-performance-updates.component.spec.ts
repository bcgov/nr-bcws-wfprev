import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectFiscalsSignalService } from 'src/app/services/project-fiscals-signal.service';
import { ProjectService } from 'src/app/services/project-services';
import { PerformanceUpdatesComponent } from './wfprev-performance-updates.component';

describe('PerformanceUpdatesComponent', () => {
  let component: PerformanceUpdatesComponent;
  let fixture: ComponentFixture<PerformanceUpdatesComponent>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockProjectFiscalsSignalService: jasmine.SpyObj<ProjectFiscalsSignalService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', ['getPerformanceUpdates', 'getProjectFiscalByProjectPlanFiscalGuid']);
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockProjectFiscalsSignalService = jasmine.createSpyObj('ProjectFiscalsSignalService', ['trigger']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [PerformanceUpdatesComponent, BrowserAnimationsModule],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: ProjectFiscalsSignalService, useValue: mockProjectFiscalsSignalService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatSnackBar, useValue: mockSnackBar },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: () => 'test-project-guid'
              }
            }
          }
        }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PerformanceUpdatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
