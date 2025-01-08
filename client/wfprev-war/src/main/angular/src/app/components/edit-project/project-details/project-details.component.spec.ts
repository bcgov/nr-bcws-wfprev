import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectDetailsComponent } from './project-details.component';
import { ReactiveFormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('ProjectDetailsComponent', () => {
  let component: ProjectDetailsComponent;
  let fixture: ComponentFixture<ProjectDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectDetailsComponent, ReactiveFormsModule,BrowserAnimationsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Initialization', () => {
    it('should initialize the form with sample data', () => {
      const formValues = component.detailsForm.value;
      expect(formValues.projectLead).toEqual(component.projectDetail.projectLead);
      expect(formValues.projectLeadEmailAddress).toEqual(
        component.projectDetail.projectLeadEmailAddress
      );
      expect(formValues.projectTypeCode).toEqual(
        component.projectDetail.projectTypeCode.projectTypeCode
      );
      expect(formValues.coordinates).toEqual(component.projectDetail.coordinates);
    });

    it('should mark the form as invalid if required fields are missing', () => {
      component.detailsForm.controls['projectTypeCode'].setValue('');
      expect(component.detailsForm.invalid).toBeTrue();
    });

    it('should mark the form as valid if all required fields are provided', () => {
      expect(component.detailsForm.valid).toBeTrue();
    });
  });

  describe('Map Initialization', () => {
    let mapSpy: jasmine.SpyObj<L.Map>;

    beforeEach(() => {
      mapSpy = jasmine.createSpyObj('L.Map', ['setView', 'addLayer', 'remove', 'invalidateSize']);
      spyOn(L, 'map').and.returnValue(mapSpy);
    });

    it('should not reinitialize the map if it already exists', () => {
      component['map'] = mapSpy;
      component.ngAfterViewInit();
      expect(L.map).toHaveBeenCalledTimes(0); // Should not be called again
    });

    it('should add a marker to the map', () => {
      component.initMap();
      expect(mapSpy.addLayer).toHaveBeenCalled();
    });
  });

  describe('onCancel Method', () => {
    it('should reset the form', () => {
      spyOn(component.detailsForm, 'reset');
      component.onCancel();
      expect(component.detailsForm.reset).toHaveBeenCalled();
    });
  });

  describe('Integration Tests', () => {
    it('should display form inputs with correct initial values', () => {
      const projectLeadInput = fixture.nativeElement.querySelector('#projectLead');
      expect(projectLeadInput.value).toBe(component.projectDetail.projectLead);
    });

    it('should update form values when inputs are changed', () => {
      const projectLeadControl = component.detailsForm.controls['projectLead'];
      projectLeadControl.setValue('New Lead');
      expect(component.detailsForm.controls['projectLead'].value).toBe('New Lead');
    });
  });
});
