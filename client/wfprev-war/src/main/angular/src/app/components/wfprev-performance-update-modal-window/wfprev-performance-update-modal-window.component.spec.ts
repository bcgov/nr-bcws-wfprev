import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectService } from 'src/app/services/project-services';
import { PerformanceUpdateModalWindowComponent } from './wfprev-performance-update-modal-window.component';

describe('PerformanceUpdateModalWindowComponent', () => {
  let component: PerformanceUpdateModalWindowComponent;
  let fixture: ComponentFixture<PerformanceUpdateModalWindowComponent>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<PerformanceUpdateModalWindowComponent>>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockSnackbar: jasmine.SpyObj<MatSnackBar>;
  let mockDialog: jasmine.SpyObj<MatDialog>;

  beforeEach(async () => {
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockProjectService = jasmine.createSpyObj('ProjectService', ['savePerformanceUpdates']);
    mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);

    await TestBed.configureTestingModule({
      imports: [PerformanceUpdateModalWindowComponent, BrowserAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: MatSnackBar, useValue: mockSnackbar },
        { provide: MatDialog, useValue: mockDialog },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            currentForecast: 100,
            reportingPeriod: [],
            progressStatus: [],
            projectGuid: 'test-project',
            fiscalGuid: 'test-fiscal'
          }
        }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PerformanceUpdateModalWindowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
