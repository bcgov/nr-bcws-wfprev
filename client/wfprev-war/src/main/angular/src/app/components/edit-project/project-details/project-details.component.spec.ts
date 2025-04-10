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
import { ProjectService } from 'src/app/services/project-services';
import { ActivatedRoute } from '@angular/router';
import { formatLatLong } from 'src/app/utils/tools';
import * as Tools from 'src/app/utils/tools'; // Import the tools module

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

const mockBounds = jasmine.createSpyObj('LatLngBounds', ['isValid']);
mockBounds.isValid.and.returnValue(true);

const mockGeoJsonLayer = jasmine.createSpyObj('Layer', ['addTo', 'getBounds']);
mockGeoJsonLayer.addTo.and.returnValue(mockGeoJsonLayer);
mockGeoJsonLayer.getBounds.and.returnValue(mockBounds);

class MockOAuthService {
  // Mock any OAuthService methods used in your component
  getAccessToken(): string {
    return 'mock-access-token';
  }
  configure(config: any): void {
    // no-op
  }
  initImplicitFlow(): void {
    // no-op
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
  let mockProjectService: jasmine.SpyObj<ProjectService>;

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', ['updateProject', 'getProjectByProjectGuid']);
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
        { provide: ProjectService, useValue: mockProjectService },
        { provide: MatSnackBar, useValue: mockSnackbar },
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
    let markerSpy: jasmine.SpyObj<L.Marker>;
    let geoJsonLayerSpy: jasmine.SpyObj<L.GeoJSON>;
    let projectServiceSpy: jasmine.SpyObj<ProjectService>;
  
    beforeEach(() => {
      mapSpy = jasmine.createSpyObj('L.Map', ['setView', 'addLayer', 'remove', 'invalidateSize', 'fitBounds', 'removeLayer']);
      markerSpy = jasmine.createSpyObj('L.Marker', ['addTo']);
      geoJsonLayerSpy = jasmine.createSpyObj('L.GeoJSON', ['addTo', 'getBounds']);
      projectServiceSpy = jasmine.createSpyObj('ProjectService', ['getProjectBoundaries']);
    
      // Provide the service to the component
      component['projectService'] = projectServiceSpy;
      spyOn(L, 'map').and.returnValue(mapSpy);
      spyOn(L, 'marker').and.returnValue(markerSpy);
      spyOn(L, 'geoJSON').and.returnValue(geoJsonLayerSpy);
    
      // Return a valid bounds object from getBounds
      geoJsonLayerSpy.getBounds.and.returnValue({
        isValid: () => true,
      } as any);
    });

    it('should fetch project boundaries and display the latest boundary on the map', fakeAsync(() => {
      const mockBoundary = {
        boundaryGeometry: {
          type: 'Polygon',
          coordinates: [[[0, 0], [0, 1], [1, 1], [1, 0], [0, 0]]]
        },
        systemStartTimestamp: '2024-01-01T00:00:00Z'
      };
    
      component['map'] = mapSpy;
      component['projectGuid'] = 'some-guid';
    
      projectServiceSpy.getProjectBoundaries.and.returnValue(of({
        _embedded: {
          projectBoundary: [mockBoundary]
        }
      }));
    
      component.updateMap(49.553209, -119.965887);
      tick(); // advance the observable

      const mockBound = {
        boundaryGeometry: {
          type: 'Polygon',
          coordinates: [[[0, 0], [0, 1], [1, 1], [1, 0], [0, 0]]]
        } as GeoJSON.Polygon,
        systemStartTimestamp: '2024-01-01T00:00:00Z'
      };
    
      expect(projectServiceSpy.getProjectBoundaries).toHaveBeenCalledWith('some-guid');
      expect(L.geoJSON).toHaveBeenCalledWith(mockBound.boundaryGeometry, jasmine.any(Object));
      expect(geoJsonLayerSpy.addTo).toHaveBeenCalledWith(mapSpy);
    }));

    it('should log error when fetching project boundaries fails', fakeAsync(() => {
      const consoleSpy = spyOn(console, 'error');
      component['map'] = mapSpy;
      component['projectGuid'] = 'some-guid';
    
      projectServiceSpy.getProjectBoundaries.and.returnValue(throwError(() => new Error('Fetch failed')));
    
      component.updateMap(49.553209, -119.965887);
      tick();
    
      expect(consoleSpy).toHaveBeenCalledWith('Error fetching project boundaries', jasmine.any(Error));
    }));

    it('should initialize the map when updateMap is called without initializing the map', () => {
      component['map'] = undefined;
      component.updateMap(49.553209, -119.965887);
    
      expect(L.map).toHaveBeenCalled();
      expect(L.marker).toHaveBeenCalledWith([49.553209, -119.965887]);
      expect(markerSpy.addTo).toHaveBeenCalledWith(mapSpy);
    });

    it('should remove old boundary layer if it exists before adding a new one', fakeAsync(() => {
      const mockOldLayer = jasmine.createSpyObj('L.Layer', ['remove']);
      const mockBoundary = {
        boundaryGeometry: {
          type: 'Polygon',
          coordinates: [[[0, 0], [0, 1], [1, 1], [1, 0], [0, 0]]]
        },
        systemStartTimestamp: '2024-01-01T00:00:00Z'
      };
    
      // Setup existing map, old boundaryLayer, and projectGuid
      component['map'] = mapSpy;
      component['boundaryLayer'] = mockOldLayer as any; // Simulate existing boundary
      component['projectGuid'] = 'some-guid';
    
      // Mock response from the service
      projectServiceSpy.getProjectBoundaries.and.returnValue(of({
        _embedded: {
          projectBoundary: [mockBoundary]
        }
      }));
    
      component.updateMap(49.553209, -119.965887);
      tick(); // flush observable
    
      expect(mapSpy.removeLayer).toHaveBeenCalledWith(mockOldLayer);
      expect(component['boundaryLayer']).not.toBe(mockOldLayer);
    }));

    it('should not reinitialize the map if initMap is called and map already exists', () => {
      component['map'] = mapSpy;
      component.initMap();
    
      expect(L.map).not.toHaveBeenCalled();
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
  
    it('should initialize map with default BC bounds if map is not defined', () => {
      component.initMap();
      expect(L.map).toHaveBeenCalled();
      expect(mapSpy.fitBounds).toHaveBeenCalledWith([
        [48.3, -139.1], // Southwest corner of BC
        [60.0, -114.0], // Northeast corner of BC
      ]);
    });
  
    it('should initialize the map when initMap is called and map does not exist', () => {
      component['map'] = undefined; // Ensure map is not already initialized
      component.initMap();
  
      expect(L.map).toHaveBeenCalled(); // Verify that the map was created
      expect(mapSpy.fitBounds).toHaveBeenCalledWith([
        [48.3, -139.1], // Southwest corner of BC
        [60.0, -114.0], // Northeast corner of BC
      ]); // Verify that fitBounds was called with default bounds
    });
  
    it('should update the map view with the new latitude and longitude', () => {
      component['map'] = mapSpy;
      component.updateMap(49.553209, -119.965887);
      expect(mapSpy.setView).toHaveBeenCalledWith([49.553209, -119.965887], 13);
    });
  
    it('should add a marker when updating the map view', () => {
      component['map'] = mapSpy;
      component.updateMap(49.553209, -119.965887);
      expect(L.marker).toHaveBeenCalledWith([49.553209, -119.965887]);
      expect(markerSpy.addTo).toHaveBeenCalledWith(mapSpy);
    });
  
    it('should remove the existing marker when updating the map', () => {
      component['map'] = mapSpy;
      component['marker'] = markerSpy;
    
      component.updateMap(49.553209, -119.965887);
    
      expect(mapSpy.removeLayer).toHaveBeenCalledWith(markerSpy); // Ensure the old marker is removed
      expect(L.marker).toHaveBeenCalledWith([49.553209, -119.965887]); // New marker added
      expect(markerSpy.addTo).toHaveBeenCalledWith(mapSpy); // New marker added to the map
    });
    
  
    it('should initialize the map and add a marker when coordinates are provided', () => {
      component['map'] = undefined; // Ensure the map is not already initialized
  
      component.updateMap(49.553209, -119.965887);
  
      expect(L.map).toHaveBeenCalled(); // Verify that the map is created
      expect(L.marker).toHaveBeenCalledWith([49.553209, -119.965887]); // Marker created
      expect(markerSpy.addTo).toHaveBeenCalledWith(mapSpy); // Marker added to the map
    });
  
    it('should clean up the map on component destroy', () => {
      component['map'] = mapSpy; // Assign the mock map to the component
      component.ngOnDestroy(); // Trigger the lifecycle hook
  
      expect(mapSpy.remove).toHaveBeenCalled(); // Ensure the map was removed
    });
  
    it('should do nothing when ngOnDestroy is called if map is not initialized', () => {
      component['map'] = undefined; // Ensure the map is not initialized
      component.ngOnDestroy(); // Trigger the lifecycle hook
  
      // No errors should occur, and no calls should be made
      expect(mapSpy.remove).not.toHaveBeenCalled();
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
    it('should return an empty string if latitude or longitude is missing', () => {
      // Test case: Missing latitude
      let result = component.combineCoordinates('', -119.965887);
      expect(result).toBe('');
  
      // Test case: Missing longitude
      result = component.combineCoordinates(49.553209, '');
      expect(result).toBe('');
  
      // Test case: Both latitude and longitude are missing
      result = component.combineCoordinates('', '');
      expect(result).toBe('');
    });

    it('should combine latitude and longitude into a string', () => {
      const result = component.combineCoordinates(49.553209, -119.965887);
      expect(result).toBe('49.553209, -119.965887');
    });
  });

  describe('loadProjectDetails Method', () => {
    let projectServiceSpy: jasmine.SpyObj<ProjectService>;
    let routeSnapshotSpy: jasmine.SpyObj<ActivatedRoute>;
    
    beforeEach(() => {
      projectServiceSpy = jasmine.createSpyObj('ProjectService', ['getProjectByProjectGuid']);
      routeSnapshotSpy = jasmine.createSpyObj('ActivatedRoute', ['snapshot']);
      component['projectService'] = projectServiceSpy;
      component['route'] = routeSnapshotSpy;
    });
  
    it('should exit early if projectGuid is missing', () => {
      routeSnapshotSpy.snapshot = { queryParamMap: new Map() } as any;
      component.loadProjectDetails();
      expect(projectServiceSpy.getProjectByProjectGuid).not.toHaveBeenCalled();
    });
  
    it('should call projectService.getProjectByProjectGuid if projectGuid is present', () => {
      routeSnapshotSpy.snapshot = { queryParamMap: { get: () => 'test-guid' } } as any;
      projectServiceSpy.getProjectByProjectGuid.and.returnValue(of({}));
      component.loadProjectDetails();
      expect(projectServiceSpy.getProjectByProjectGuid).toHaveBeenCalledWith('test-guid');
    });
  
    it('should not call getProjectByProjectGuid if projectGuid is missing', () => {
      component.projectGuid = '';
      component.loadProjectDetails();
    
      expect(mockProjectService.getProjectByProjectGuid).not.toHaveBeenCalled();
    });

    
    it('should handle successful response and update component state', () => {
      const mockResponse = {
        projectName: 'Test Project',
        latitude: 49.2827,
        longitude: -123.1207,
        projectDescription: 'Test Description',
      };
      routeSnapshotSpy.snapshot = { queryParamMap: { get: () => 'test-guid' } } as any;
      projectServiceSpy.getProjectByProjectGuid.and.returnValue(of(mockResponse));
      spyOn(component, 'updateMap');
      spyOn(component, 'populateFormWithProjectDetails');
      spyOn(component.projectNameChange, 'emit');
    
      component.loadProjectDetails();
    
      const expectedLatLong = formatLatLong(mockResponse.latitude, mockResponse.longitude);
    
      expect(component.projectDetail).toEqual(mockResponse);
      expect(component.projectNameChange.emit).toHaveBeenCalledWith('Test Project');
      expect(component.latLong).toBe(expectedLatLong); // Use the utility's output
      expect(component.updateMap).toHaveBeenCalledWith(49.2827, -123.1207);
      expect(component.populateFormWithProjectDetails).toHaveBeenCalledWith(mockResponse);
      expect(component.originalFormValues).toEqual(component.detailsForm.getRawValue());
      expect(component.projectDescription).toBe('Test Description');
      expect(component.isLatLongDirty).toBeFalse();
      expect(component.isProjectDescriptionDirty).toBeFalse();
    });
  
    it('should handle error response and set projectDetail to null', () => {
      routeSnapshotSpy.snapshot = { queryParamMap: { get: () => 'test-guid' } } as any;
      projectServiceSpy.getProjectByProjectGuid.and.returnValue(throwError(() => new Error('Error fetching data')));
    
      // Spy on console.error
      spyOn(console, 'error');
    
      component.loadProjectDetails();
    
      expect(component.projectDetail).toBeNull();
      expect(console.error).toHaveBeenCalledWith('Error fetching project details:', jasmine.any(Error));
    });

    describe('onLatLongChange Method', () => {
      beforeEach(() => {
        spyOn<any>(component, 'callValidateLatLong');
      });
    
      it('should set isLatLongDirty to true when newLatLong is valid', () => {
        (component['callValidateLatLong'] as jasmine.Spy).and.returnValue({ latitude: 49.2827, longitude: -123.1207 });
    
        component.onLatLongChange('49.2827, -123.1207');
        expect(component.isLatLongDirty).toBeTrue();
      });
    
      it('should set isLatLongDirty to true when newLatLong is invalid', () => {
        (component['callValidateLatLong'] as jasmine.Spy).and.returnValue(null);
    
        component.onLatLongChange('invalid-lat-long');
            expect(component.isLatLongDirty).toBeTrue();
      });
    });

    describe('populateFormWithProjectDetails Method', () => {
      it('should call patchFormValues with the correct data', () => {
        const mockData = {
          projectTypeCode: { projectTypeCode: 'Code1' },
          fundingStream: 'Stream1',
          programAreaGuid: 'Guid1',
          projectLead: 'Lead1',
          projectLeadEmailAddress: 'email@example.com',
          siteUnitName: 'Site1',
          closestCommunityName: 'Community1',
          forestRegionOrgUnitId: 1,
          forestDistrictOrgUnitId: 2,
          primaryObjective: 'Objective1',
          secondaryObjective: 'Objective2',
          secondaryObjectiveRationale: 'Rationale1',
          bcParksRegionOrgUnitId: 3,
          bcParksSectionOrgUnitId: 4,
          latitude: 49.2827,
          longitude: -123.1207,
        };
    
        spyOn(component, 'patchFormValues');
        component.populateFormWithProjectDetails(mockData);
    
        expect(component.patchFormValues).toHaveBeenCalledWith(mockData);
      });
    });

    describe('assignCodeTableData Method', () => {
      it('should assign projectTypeCode when key is "projectTypeCode"', () => {
        const mockData = {
          _embedded: { projectTypeCode: ['Code1', 'Code2'] },
        };
    
        component.assignCodeTableData('projectTypeCode', mockData);
    
        expect(component.projectTypeCode).toEqual(['Code1', 'Code2']);
      });
    
      it('should assign programAreaCode when key is "programAreaCode"', () => {
        const mockData = {
          _embedded: { programArea: ['Area1', 'Area2'] },
        };
    
        component.assignCodeTableData('programAreaCode', mockData);
    
        expect(component.programAreaCode).toEqual(['Area1', 'Area2']);
      });
    
      it('should assign forestRegionCode when key is "forestRegionCode"', () => {
        const mockData = {
          _embedded: { forestRegionCode: ['Region1', 'Region2'] },
        };
    
        component.assignCodeTableData('forestRegionCode', mockData);
    
        expect(component.forestRegionCode).toEqual(['Region1', 'Region2']);
      });
    
      it('should assign forestDistrictCode when key is "forestDistrictCode"', () => {
        const mockData = {
          _embedded: { forestDistrictCode: ['District1', 'District2'] },
        };
    
        component.assignCodeTableData('forestDistrictCode', mockData);
    
        expect(component.forestDistrictCode).toEqual(['District1', 'District2']);
      });
    
      it('should assign bcParksRegionCode when key is "bcParksRegionCode"', () => {
        const mockData = {
          _embedded: { bcParksRegionCode: ['Region1', 'Region2'] },
        };
    
        component.assignCodeTableData('bcParksRegionCode', mockData);
    
        expect(component.bcParksRegionCode).toEqual(['Region1', 'Region2']);
      });
    
      it('should assign bcParksSectionCode when key is "bcParksSectionCode"', () => {
        const mockData = {
          _embedded: { bcParksSectionCode: ['Section1', 'Section2'] },
        };
    
        component.assignCodeTableData('bcParksSectionCode', mockData);
    
        expect(component.bcParksSectionCode).toEqual(['Section1', 'Section2']);
      });
    });
    
    describe('onSaveProjectDescription Method', () => {
      beforeEach(() => {
        // Mock the ProjectService methods
        mockProjectService.updateProject.and.returnValue(of({}));
        mockProjectService.getProjectByProjectGuid.and.returnValue(
          of({
            projectDescription: 'Updated description',
          })
        );
        // Initialize component state for the test
        component.isProjectDescriptionDirty = true;
        component.projectDescription = 'New Description';
        component.projectDetail = { projectDescription: 'Old Description' };
        component.projectGuid = 'test-guid';
      });

      it('should not call updateProject if isProjectDescriptionDirty is false', () => {
        // Arrange
        component.isProjectDescriptionDirty = false;
    
        // Act
        component.onSaveProjectDescription();
    
        // Assert
        expect(mockProjectService.updateProject).not.toHaveBeenCalled();
        expect(mockProjectService.getProjectByProjectGuid).not.toHaveBeenCalled();
      });
    
    });

    describe('onSaveLatLong Method', () => {
      beforeEach(() => {
        // Set up mock data or necessary initialization
        component.projectDetail = { latitude: 48.4284, longitude: -123.3656 };
        component.projectGuid = 'test-guid';
        component.latLong = 'xawe, -123.3656';
        component.isLatLongValid = false;
      });
    
      it('should not update latitude and longitude if latLong is not valid', () => {
        component.isLatLongValid = false;
        component.onSaveLatLong();
    
        expect(component.isLatLongDirty).toBeFalse();
        expect(component.projectDetail.latitude).toBeGreaterThan(0);
      });
    });

    describe('onCancelProjectDescription Method', () => {
      it('should reset projectDescription and set isProjectDescriptionDirty to false if projectDetail exists', () => {
        // Arrange: Set up the projectDetail with a mock description
        component.projectDetail = { projectDescription: 'Original Description' };
        component.projectDescription = 'Modified Description'; // Simulate a changed description
        component.isProjectDescriptionDirty = true; // Simulate the dirty state
  
        // Act: Call the method
        component.onCancelProjectDescription();
  
        // Assert: Check that projectDescription and isProjectDescriptionDirty are reset
        expect(component.projectDescription).toBe('Original Description');
        expect(component.isProjectDescriptionDirty).toBeFalse();
      });
  
      it('should not change projectDescription or isProjectDescriptionDirty if projectDetail is null', () => {
        // Arrange: Set projectDetail to null
        component.projectDetail = null;
        component.projectDescription = 'Some Description'; // Simulate a description
        component.isProjectDescriptionDirty = true; // Simulate the dirty state
  
        // Act: Call the method
        component.onCancelProjectDescription();
  
        // Assert: Ensure no changes are made
        expect(component.projectDescription).toBe('Some Description');
        expect(component.isProjectDescriptionDirty).toBeFalse();
      });

    });

    describe('callValidateLatLong Method', () => {
      it('should call validateLatLong and return the expected result', () => {
        const mockValue = '49.2827, -123.1207';
        const mockReturn = { latitude: 49.2827, longitude: -123.1207 };
    
        spyOn<any>(component, 'callValidateLatLong').and.returnValue(mockReturn);
    
        const result = component['callValidateLatLong'](mockValue);
    
        expect(result).toEqual(mockReturn);
      });
    });
    
    it('should not call updateProject if detailsForm is invalid', () => {
      component.detailsForm.controls['projectTypeCode'].setValue('');
      component.onSave();
    
      expect(mockProjectService.updateProject).not.toHaveBeenCalled();
    });

  });
});
