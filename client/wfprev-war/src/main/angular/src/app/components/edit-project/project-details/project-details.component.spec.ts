import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectDetailsComponent } from './project-details.component';
import { ReactiveFormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs'; // Import 'of' from RxJS
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppConfigService } from 'src/app/services/app-config.service';

const mockApplicationConfig = {
  application: {
    baseUrl: 'http://test.com',
    lazyAuthenticate: false, // Ensure this property is defined
    enableLocalStorageToken: true,
    acronym: 'TEST',
    environment: 'DEV',
    version: '1.0.0',
  },
  webade: {
    oauth2Url: 'http://oauth.test',
    clientId: 'test-client',
    authScopes: 'TEST.*',
  },
  rest: {},
};

class MockAppConfigService {
  private appConfig = mockApplicationConfig;

  loadAppConfig(): Promise<void> {
    return Promise.resolve(); // Simulate successful configuration loading
  }

  getConfig(): any {
    return this.appConfig; // Return mock configuration
  }
}

describe('ProjectDetailsComponent', () => {
  let component: ProjectDetailsComponent;
  let fixture: ComponentFixture<ProjectDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ProjectDetailsComponent,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: AppConfigService, useClass: MockAppConfigService },
      ],
    }).compileComponents();
  
    const appConfigService = TestBed.inject(AppConfigService);
    await appConfigService.loadAppConfig(); // Ensure the config is loaded before running tests

    fixture = TestBed.createComponent(ProjectDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Initialization', () => {

    it('should initialize latLongForm with empty values', () => {
      const formValues = component.latLongForm.value;
      expect(formValues.latitude).toBe('');
      expect(formValues.longitude).toBe('');
    });

    it('should mark detailsForm as invalid if required fields are missing', () => {
      component.detailsForm.controls['projectTypeCode'].setValue('');
      expect(component.detailsForm.invalid).toBeTrue();
    });


    it('should mark latLongForm as invalid if latitude or longitude is empty', () => {
      component.latLongForm.controls['latitude'].setValue('');
      component.latLongForm.controls['longitude'].setValue('');
      expect(component.latLongForm.invalid).toBeTrue();
    });

    it('should mark latLongForm as valid if latitude and longitude are provided', () => {
      component.latLongForm.controls['latitude'].setValue('49.553209');
      component.latLongForm.controls['longitude'].setValue('-119.965887');
      expect(component.latLongForm.valid).toBeTrue();
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
      expect(L.map).toHaveBeenCalledTimes(0);
    });

    it('should initialize the map if it does not already exist', () => {
      spyOn(L, 'map').and.callThrough();
      component.updateMap(49.553209, -119.965887);
      expect(L.map).toHaveBeenCalled();
    });

    it('should update the map view with the new latitude and longitude', () => {
      component['map'] = mapSpy;
      component.updateMap(49.553209, -119.965887);
      expect(mapSpy.setView).toHaveBeenCalledWith([49.553209, -119.965887], 13);
    });

    it('should add a marker to the map', () => {
      component.initMap();
      expect(mapSpy.addLayer).toHaveBeenCalled();
    });
  });

  describe('loadProjectDetails Method', () => {
    it('should populate detailsForm and latLongForm with project data', () => {
      const mockData = {
        latitude: 49.553209,
        longitude: -119.965887,
        projectTypeCode: { projectTypeCode: 'FUEL_MGMT' },
        projectLead: 'John Doe',
        projectLeadEmailAddress: 'john.doe@example.com',
      };
    
      spyOn(component['projectService'], 'getProjectByProjectGuid').and.returnValue(of(mockData)); // Use 'of' to mock the Observable
    
      component.loadProjectDetails();
    
      expect(component.detailsForm.value.projectLead).toBe(mockData.projectLead);
      expect(component.latLongForm.value.latitude).toBe(mockData.latitude.toString());
      expect(component.latLongForm.value.longitude).toBe(mockData.longitude.toString());
    });
  });

  describe('onSaveLatLong Method', () => {
    it('should call updateMap with correct latitude and longitude', () => {
      const latitude = '49.553209';
      const longitude = '-119.965887';
      spyOn(component, 'updateMap');

      component.latLongForm.controls['latitude'].setValue(latitude);
      component.latLongForm.controls['longitude'].setValue(longitude);
      component.onSaveLatLong();

      expect(component.updateMap).toHaveBeenCalledWith(parseFloat(latitude), parseFloat(longitude));
    });

    it('should log an error if latLongForm is invalid', () => {
      spyOn(console, 'error');
      component.latLongForm.controls['latitude'].setValue('');
      component.onSaveLatLong();
      expect(console.error).toHaveBeenCalledWith('Latitude/Longitude form is invalid!');
    });
  });

  describe('onCancel Method', () => {
    it('should reset the form', () => {
      spyOn(component.detailsForm, 'reset');
      component.onCancel();
      expect(component.detailsForm.reset).toHaveBeenCalled();
    });

    it('should reset latLongForm to original values', () => {
      component.projectDetail = { latitude: 49.553209, longitude: -119.965887 };
      component.latLongForm.patchValue({
        latitude: '0',
        longitude: '0',
      });

      component.onCancel();

      expect(component.latLongForm.value.latitude).toBe(component.projectDetail.latitude.toString());
      expect(component.latLongForm.value.longitude).toBe(component.projectDetail.longitude.toString());
    });
  });

  describe('Integration Tests', () => {
    it('should display form inputs with correct initial values', () => {
      const projectLeadInput = fixture.nativeElement.querySelector('#projectLead');
      expect(projectLeadInput.value).toBe(component.projectDetail?.projectLead || '');
    });

    it('should update form values when inputs are changed', () => {
      const projectLeadControl = component.detailsForm.controls['projectLead'];
      projectLeadControl.setValue('New Lead');
      expect(component.detailsForm.controls['projectLead'].value).toBe('New Lead');
    });
  });
});
