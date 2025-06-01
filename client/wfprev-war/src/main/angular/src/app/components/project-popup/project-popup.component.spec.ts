import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ProjectPopupComponent } from './project-popup.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { CodeTableKeys } from 'src/app/utils/constants';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ProjectPopupComponent', () => {
  let component: ProjectPopupComponent;
  let fixture: ComponentFixture<ProjectPopupComponent>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;

  beforeEach(async () => {
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);

    await TestBed.configureTestingModule({
      imports: [ProjectPopupComponent,HttpClientTestingModule],
      providers: [{ provide: CodeTableServices, useValue: mockCodeTableService }]
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectPopupComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call loadCodeTables on init', () => {
    mockCodeTableService.fetchCodeTable.and.returnValue(of({ _embedded: { projectTypeCode: [] } }));
    spyOn(component as any, 'loadCodeTables').and.callThrough();

    component.ngOnInit();

    expect((component as any).loadCodeTables).toHaveBeenCalled();
  });

  it('should assign projectTypeCode data correctly', () => {
    const data = { _embedded: { projectTypeCode: [{ projectTypeCode: 1, description: 'Type A' }] } };
    component.assignCodeTableData(CodeTableKeys.PROJECT_TYPE_CODE, data);

    expect(component.projectTypeCode.length).toBe(1);
    expect(component.projectTypeCode[0].description).toBe('Type A');
  });

  it('should handle error and assign empty array on fetch failure', () => {
    mockCodeTableService.fetchCodeTable.and.returnValue(throwError(() => 'error'));

    component.ngOnInit();

    expect(component.projectTypeCode).toEqual([]);
  });

  it('should return correct code description', () => {
    component.projectTypeCode = [{ projectTypeCode: '2', description: 'Bridge', displayOrder: 1, effectiveDate: '', expiryDate: '' }];
    const result = component.getCodeDescription(CodeTableKeys.PROJECT_TYPE_CODE, '2');

    expect(result).toBe('Bridge');
  });

  it('should return formatted currency', () => {
    expect(component.formatCurrency(50000)).toBe('$50,000');
    expect(component.formatCurrency(undefined)).toBe('N/A');
  });

  it('should open project edit page in new tab', () => {
    spyOn(window, 'open');
    component.project = { projectGuid: 'abc-123' };
    component.navigateToProject();

    expect(window.open).toHaveBeenCalledWith(
      `${window.location.origin}/edit-project?projectGuid=abc-123`,
      '_blank'
    );
  });

  it('should close the popup if map is set', () => {
    const closeSpy = jasmine.createSpy('closePopup');
    component.map = { closePopup: closeSpy } as any;

    component.closePopup();

    expect(closeSpy).toHaveBeenCalled();
  });

  it('should return correct activity category code description', () => {
    component.activityCategoryCodes = [
      { activityCategoryCode: 'AC1', description: 'Fuel Management', displayOrder: 1, effectiveDate: '', expiryDate: '' }
    ];

    const result = component.getCodeDescription(CodeTableKeys.ACTIVITY_CATEGORY_CODE, 'AC1');
    expect(result).toBe('Fuel Management');
  });

  it('should return null for unknown control key in getCodeDescription', () => {
    const result = component.getCodeDescription('UNKNOWN_KEY', 'someValue');
    expect(result).toBeNull();
  });


});
