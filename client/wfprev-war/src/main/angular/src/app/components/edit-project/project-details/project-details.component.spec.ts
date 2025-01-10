import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ProjectDetailsComponent } from './project-details.component';
import { ReactiveFormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs'; // Import 'of' from RxJS
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { OAuthService } from 'angular-oauth2-oidc';
import { MatSnackBar } from '@angular/material/snack-bar';


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

class MockOAuthService {
  // Mock any OAuthService methods used in your component
  getAccessToken(): string {
    return 'mock-access-token';
  }
}

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
  let mockSnackbar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);

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
        { provide: OAuthService, useClass: MockOAuthService }, // Provide MockOAuthService

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
      component.updateMap(49.553209, -119.965887);
      expect(L.map).toHaveBeenCalled();
    });

    it('should update the map view with the new latitude and longitude', () => {
      component['map'] = mapSpy;
      component.updateMap(49.553209, -119.965887);
      expect(mapSpy.setView).toHaveBeenCalledWith([49.553209, -119.965887], 13);
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
        latitude: '49.553209',
        longitude: '-119.965887',
      });

      component.onCancel();

      expect(component.latLongForm.value.latitude).toBe(component.projectDetail.latitude.toString());
      expect(component.latLongForm.value.longitude).toBe(component.projectDetail.longitude.toString());
    });
  });

  describe('Integration Tests', () => {
    beforeEach(() => {
      // Set mock data for projectDetail and patch the form
      component.projectDetail = { projectLead: 'John Doe', projectLeadEmailAddress: 'john.doe@example.com' };
      component.detailsForm.patchValue({
        projectLead: component.projectDetail.projectLead,
        projectLeadEmailAddress: component.projectDetail.projectLeadEmailAddress,
      });
  
      // Trigger change detection to update the DOM
      fixture.detectChanges();
    });
  
    it('should display form inputs with correct initial values', () => {
      const projectLeadInput = fixture.nativeElement.querySelector('#projectLead');
      expect(projectLeadInput.value).toBe('John Doe'); // Expect the value to match the mock data
    });
  
    it('should update form values when inputs are changed', () => {
      const projectLeadControl = component.detailsForm.controls['projectLead'];
      projectLeadControl.setValue('New Lead');
  
      // Trigger change detection to reflect the updated value in the DOM
      fixture.detectChanges();
  
      expect(component.detailsForm.controls['projectLead'].value).toBe('New Lead');
    });
  });

  describe('onProjectDescriptionChange Method', () => {
    it('should mark the description as dirty if it changes', () => {
      component.projectDetail = { projectDescription: 'Old Description' };
      component.projectDescription = 'New Description';

      component.onProjectDescriptionChange('New Description');

      expect(component.isProjectDescriptionDirty).toBeTrue();
    });

    it('should not mark as dirty if the description remains unchanged', () => {
      component.projectDetail = { projectDescription: 'Same Description' };
      component.projectDescription = 'Same Description';

      component.onProjectDescriptionChange('Same Description');

      expect(component.isProjectDescriptionDirty).toBeFalse();
    });
  });

  describe('combineCoordinates Method', () => {
    it('should combine latitude and longitude into a string', () => {
      const result = component.combineCoordinates(49.553209, -119.965887);
      expect(result).toBe('49.553209, -119.965887');
    });
  });
  
});
