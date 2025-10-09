import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ProjectDetailsComponent } from './project-details.component';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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
import { CodeTableKeys } from 'src/app/utils/constants';
import * as toolUtils from 'src/app/utils/tools'
import { EvaluationCriteriaSummaryModel, ProjectFiscal } from 'src/app/components/models';
const mockApplicationConfig = {
  application: {
    baseUrl: 'http://test.com',
    lazyAuthenticate: false,
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
  let mapSpy: jasmine.SpyObj<L.Map>;
  let fixture: ComponentFixture<ProjectDetailsComponent>;
  let mockSnackbar: jasmine.SpyObj<MatSnackBar>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', [
      'updateProject',
      'getProjectByProjectGuid',
      'getProjectFiscalsByProjectGuid',
      'getFiscalActivities',
      'getActivityBoundaries',
      'deleteEvaluationCriteriaSummary'
    ]);
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

    spyOn(toolUtils.LeafletLegendService.prototype, 'addLegend')
      .and.callFake(() => ({
        addTo: () => { }
      } as unknown as L.Control));
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

    it('should include wildfireOrgUnitId as required in the form', () => {
      const control = component.detailsForm.get('wildfireOrgUnitId');
      expect(control).toBeTruthy();
      control?.setValue('');
      control?.markAsTouched();
      expect(control?.hasError('required')).toBeTrue();
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
      spyOn(L, 'map').and.returnValue(mapSpy as unknown as L.Map);
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
      expect(L.marker).toHaveBeenCalledWith(
        [49.553209, -119.965887],
        jasmine.objectContaining({
          icon: jasmine.any(Object),
        })
      );
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
      expect(L.marker).toHaveBeenCalledWith(
        [49.553209, -119.965887],
        jasmine.objectContaining({
          icon: jasmine.any(Object),
        })
      );
      expect(markerSpy.addTo).toHaveBeenCalledWith(mapSpy);
    });

    it('should remove the existing marker when updating the map', () => {
      component['map'] = mapSpy;
      component['marker'] = markerSpy;

      component.updateMap(49.553209, -119.965887);

      expect(mapSpy.removeLayer).toHaveBeenCalledWith(markerSpy); // Ensure the old marker is removed
      expect(L.marker).toHaveBeenCalledWith(
        [49.553209, -119.965887],
        jasmine.objectContaining({
          icon: jasmine.any(Object),
        })
      );
      expect(markerSpy.addTo).toHaveBeenCalledWith(mapSpy); // New marker added to the map
    });


    it('should initialize the map and add a marker when coordinates are provided', () => {
      component['map'] = undefined; // Ensure the map is not already initialized

      component.updateMap(49.553209, -119.965887);

      expect(L.map).toHaveBeenCalled(); // Verify that the map is created
      expect(L.marker).toHaveBeenCalledWith(
        [49.553209, -119.965887],
        jasmine.objectContaining({
          icon: jasmine.any(Object)
        })
      );
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
    let routeSnapshotSpy: jasmine.SpyObj<ActivatedRoute>;

    beforeEach(() => {
      routeSnapshotSpy = jasmine.createSpyObj('ActivatedRoute', ['snapshot']);
      component['route'] = routeSnapshotSpy;
    });

    it('should exit early if projectGuid is missing', () => {
      routeSnapshotSpy.snapshot = { queryParamMap: new Map() } as any;
      component.loadProjectDetails();
      expect(mockProjectService.getProjectByProjectGuid).not.toHaveBeenCalled();
    });

    it('should call projectService.getProjectByProjectGuid if projectGuid is present', () => {
      routeSnapshotSpy.snapshot = { queryParamMap: { get: () => 'test-guid' } } as any;
      mockProjectService.getProjectByProjectGuid.and.returnValue(of({}));
      component.loadProjectDetails();
      expect(mockProjectService.getProjectByProjectGuid).toHaveBeenCalledWith('test-guid');
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
      mockProjectService.getProjectByProjectGuid.and.returnValue(of(mockResponse));
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
      mockProjectService.getProjectByProjectGuid.and.returnValue(throwError(() => new Error('Error fetching data')));

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
        spyOn(component, 'patchFormValues');

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

        component.populateFormWithProjectDetails(mockData);

        expect(component.patchFormValues).toHaveBeenCalledWith(mockData);
      });

      it('should patch wildfireOrgUnitId from fireCentreOrgUnitId in projectDetail', () => {
        const mockData = { fireCentreOrgUnitId: 456 };
        component.patchFormValues(mockData);
        expect(component.detailsForm.get('wildfireOrgUnitId')?.value).toBe(456);
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

      it('should filter and assign only FRC wildfireOrgUnits to fireCentres', () => {
        const mockData = {
          _embedded: {
            wildfireOrgUnit: [
              {
                orgUnitName: 'Kamloops Fire Centre',
                orgUnitIdentifier: 1,
                wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: 'FRC' }
              },
              {
                orgUnitName: 'Non-Fire Centre',
                orgUnitIdentifier: 2,
                wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: 'OTHER' }
              },
              {
                orgUnitName: 'No Type Code',
                orgUnitIdentifier: 3,
                wildfireOrgUnitTypeCode: null
              }
            ]
          }
        };

        component.assignCodeTableData('wildfireOrgUnit', mockData);

        expect(component.fireCentres.length).toBe(1);
        expect(component.fireCentres[0].orgUnitName).toBe('Kamloops Fire Centre');
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

      it('should reset isSaving after updateProject success', () => {
        component.onSaveProjectDescription();
        expect(component.isSaving).toBeFalse();
      });

      it('should reset isSaving after updateProject error', () => {
        mockProjectService.updateProject.and.returnValue(throwError(() => new Error('fail')));
        component.onSaveProjectDescription();
        expect(component.isSaving).toBeFalse();
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
        expect(component.isSaving).toBeFalse();
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

    it('should emit projectNameChange after successful save', () => {
      component.projectGuid = 'test-guid';
      component.projectDetail = {
        projectName: 'Old Name',
        projectTypeCode: { projectTypeCode: 'TEST' },
        primaryObjectiveTypeCode: {}
      };

      component.detailsForm = new FormGroup({
        projectName: new FormControl('New Name'),
        projectTypeCode: new FormControl('FUEL_MGMT'),
        programAreaGuid: new FormControl('area-guid'),
        closestCommunityName: new FormControl('Test City'),
        primaryObjectiveTypeCode: new FormControl('WRR'),
        wildfireOrgUnitId: new FormControl(123)
      });
      spyOnProperty(component.detailsForm, 'valid', 'get').and.returnValue(true);

      mockProjectService.updateProject.and.returnValue(of({}));
      mockProjectService.getProjectByProjectGuid.and.returnValue(
        of({ projectName: 'New Name' })
      );

      spyOn(component.projectNameChange, 'emit');
      component.onSave();
      expect(component.projectNameChange.emit).toHaveBeenCalledWith('New Name');
    });


    it('should include fireCentreOrgUnitId in the updateProject payload', () => {
      component.projectGuid = 'test-guid';
      component.projectDetail = {
        projectTypeCode: { projectTypeCode: 'TEST' },
        primaryObjectiveTypeCode: {}
      };

      component.detailsForm = new FormGroup({
        projectTypeCode: new FormControl('FUEL_MGMT'),
        programAreaGuid: new FormControl('area-guid'),
        closestCommunityName: new FormControl('Test City'),
        primaryObjectiveTypeCode: new FormControl('WRR'),
        wildfireOrgUnitId: new FormControl(123)
      });

      spyOnProperty(component.detailsForm, 'valid', 'get').and.returnValue(true);

      mockProjectService.updateProject.and.returnValue(of({}));
      mockProjectService.getProjectByProjectGuid.and.returnValue(of({}));

      component.onSave();

      expect(mockProjectService.updateProject).toHaveBeenCalledWith(
        'test-guid',
        jasmine.objectContaining({
          fireCentreOrgUnitId: 123,
        })
      );
      expect(component.isSaving).toBeFalse();
    });

    describe('refreshFiscalData Method', () => {
      it('should call loadProjectFiscals on FiscalYearProjectsComponent', () => {
        // Arrange
        component.fiscalYearProjectsComponent = {
          loadProjectFiscals: jasmine.createSpy('loadProjectFiscals')
        } as any;

        // Act
        component.refreshFiscalData();

        // Assert
        expect(component.fiscalYearProjectsComponent.loadProjectFiscals).toHaveBeenCalled();
      });

      it('should not throw if fiscalYearProjectsComponent is undefined', () => {
        // Arrange
        component.fiscalYearProjectsComponent = undefined!;

        // Act
        expect(() => component.refreshFiscalData()).not.toThrow();
      });
    });

    describe('isFormDirty and canDeactivate', () => {
      it('should return true from isFormDirty if any of the flags or form is dirty', () => {
        component.detailsForm.markAsDirty();
        component.isProjectDescriptionDirty = false;
        component.isLatLongDirty = false;
        expect(component.isFormDirty()).toBeTrue();

        component.detailsForm.markAsPristine();
        component.isProjectDescriptionDirty = true;
        expect(component.isFormDirty()).toBeTrue();

        component.isProjectDescriptionDirty = false;
        component.isLatLongDirty = true;
        expect(component.isFormDirty()).toBeTrue();
      });

      it('should return false from isFormDirty if all are clean', () => {
        component.detailsForm.markAsPristine();
        component.isProjectDescriptionDirty = false;
        component.isLatLongDirty = false;
        expect(component.isFormDirty()).toBeFalse();
      });

      it('should return true from canDeactivate if form is clean', () => {
        component.detailsForm.markAsPristine();
        component.isProjectDescriptionDirty = false;
        component.isLatLongDirty = false;
        expect(component.canDeactivate()).toBeTrue();
      });

      it('should open confirmation dialog if form is dirty in canDeactivate', () => {
        const dialogRefSpyObj = jasmine.createSpyObj({ afterClosed: of(true) });
        const dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
        dialogSpy.open.and.returnValue(dialogRefSpyObj);
        component['dialog'] = dialogSpy;

        component.detailsForm.markAsDirty();
        const result = component.canDeactivate();
        expect(dialogSpy.open).toHaveBeenCalled();
        expect(result).toBe(dialogRefSpyObj.afterClosed());
      });
    });

  });

  describe('Dropdown tooltips', () => {
    beforeEach(() => {
      component.projectTypeCode = [
        { projectTypeCode: 'REST', description: 'Restoration' },
        { projectTypeCode: 'MIT', description: 'Mitigation' }
      ];

      component.programAreaCode = [
        { programAreaGuid: 'guid123', programAreaName: 'Wildfire Services' }
      ];

      component.forestRegionCode = [
        { orgUnitId: 10, orgUnitName: 'Coastal' }
      ];

      component.forestDistrictCode = [
        { orgUnitId: 20, orgUnitName: 'Chilliwack' }
      ];

      component.bcParksRegionCode = [
        { orgUnitId: 30, orgUnitName: 'Thompson-Okanagan' }
      ];

      component.bcParksSectionCode = [
        { orgUnitId: 40, orgUnitName: 'Shuswap Section' }
      ];

      component.fireCentres = [
        { orgUnitIdentifier: 50, orgUnitName: 'Kamloops Fire Centre' }
      ];

      component.objectiveTypeCode = [
        { objectiveTypeCode: 'OBJ1', description: 'Reduce Fire Risk' },
        { objectiveTypeCode: 'OBJ2', description: 'Ecosystem Restoration' }
      ];
    });

    it('should return the selected project type description', () => {
      component.detailsForm.patchValue({ projectTypeCode: 'REST' });
      expect(component.getCodeDescription(CodeTableKeys.PROJECT_TYPE_CODE)).toBe('Restoration');
    });

    it('should return the selected program area name', () => {
      component.detailsForm.patchValue({ programAreaGuid: 'guid123' });
      expect(component.getCodeDescription(CodeTableKeys.PROGRAM_AREA_GUID)).toBe('Wildfire Services');
    });

    it('should return the selected forest region name', () => {
      component.detailsForm.patchValue({ forestRegionOrgUnitId: 10 });
      expect(component.getCodeDescription(CodeTableKeys.FOREST_REGION_ORG_UNIT_ID)).toBe('Coastal');
    });

    it('should return the selected forest district name', () => {
      component.detailsForm.patchValue({ forestDistrictOrgUnitId: 20 });
      expect(component.getCodeDescription(CodeTableKeys.FOREST_DISTRICT_ORG_UNIT_ID)).toBe('Chilliwack');
    });

    it('should return the selected BC Parks region name', () => {
      component.detailsForm.patchValue({ bcParksRegionOrgUnitId: 30 });
      expect(component.getCodeDescription(CodeTableKeys.BC_PARKS_REGION_ORG_UNIT_ID)).toBe('Thompson-Okanagan');
    });

    it('should return the selected BC Parks section name', () => {
      component.detailsForm.patchValue({ bcParksSectionOrgUnitId: 40 });
      expect(component.getCodeDescription(CodeTableKeys.BC_PARKS_SECTION_ORG_UNIT_ID)).toBe('Shuswap Section');
    });

    it('should return the selected fire centre name', () => {
      component.detailsForm.patchValue({ wildfireOrgUnitId: 50 });
      expect(component.getCodeDescription(CodeTableKeys.WILDFIRE_ORG_UNIT_ID)).toBe('Kamloops Fire Centre');
    });

    it('should return the selected primary objective name', () => {
      component.detailsForm.patchValue({ primaryObjectiveTypeCode: 'OBJ1' });
      expect(component.getCodeDescription(CodeTableKeys.PRIMARY_OBJECTIVE_TYPE_CODE)).toBe('Reduce Fire Risk');
    });

    it('should return the selected secondary objective name', () => {
      component.detailsForm.patchValue({ secondaryObjectiveTypeCode: 'OBJ2' });
      expect(component.getCodeDescription(CodeTableKeys.SECONDARY_OBJECTIVE_TYPE_CODE)).toBe('Ecosystem Restoration');
    });
  });


  describe('Error messages', () => {
    beforeEach(() => {
      component.detailsForm = new FormGroup({
        name: new FormControl('', [Validators.maxLength(5)]),
        email: new FormControl('', [Validators.email]),
      });
    });
    it('should return maxLengthExceeded message for maxlength error', () => {
      fixture.detectChanges();
      const nameControl = component.detailsForm.get('name');
      nameControl?.setValue('TooLongName');
      nameControl?.markAsTouched();
      nameControl?.updateValueAndValidity();

      expect(component.getErrorMessage('name')).toBe('Maximum character limit has been reached.');
    });

    it('should return invalidEmail message for email error', () => {
      const emailControl = component.detailsForm.get('email');
      emailControl?.setValue('invalid-email');
      emailControl?.markAsTouched();
      emailControl?.updateValueAndValidity();

      expect(component.getErrorMessage('email')).toBe('Please enter a valid email address.');
    });

    it('should return null if no errors', () => {
      component.detailsForm.get('email')?.setValue('a@b.co');
      component.detailsForm.get('email')?.markAsTouched();

      expect(component.getErrorMessage('email')).toBeNull();
    });
  });

  it('should deduplicate and store latest activity boundaries', () => {
    const result = [
      {
        activityGuid: 'a1',
        fiscalYear: 2022,
        boundary: [
          { systemStartTimestamp: '2022-01-01T00:00:00Z', geometry: { type: 'Polygon', coordinates: [] } },
          { systemStartTimestamp: '2023-01-01T00:00:00Z', geometry: { type: 'Polygon', coordinates: [] } },
        ],
      },
      {
        activityGuid: 'a1',
        fiscalYear: 2022,
        boundary: [
          { systemStartTimestamp: '2021-01-01T00:00:00Z', geometry: { type: 'Polygon', coordinates: [] } },
        ],
      },
    ];

    component['isMapReady'] = true;
    spyOn<any>(component, 'renderActivityBoundaries');
    component['handleBoundariesResponse'](result);

    expect(component['allActivityBoundaries'].length).toBe(1);
    expect(component['renderActivityBoundaries']).toHaveBeenCalled();
  });

  describe('Dropdown dependencies', () => {
    beforeEach(() => {
      component['allForestDistricts'] = [
        { orgUnitId: 1, parentOrgUnitId: '100', orgUnitName: 'District A' },
        { orgUnitId: 2, parentOrgUnitId: '200', orgUnitName: 'District B' }
      ];

      component['allBcParksSections'] = [
        { orgUnitId: 10, parentOrgUnitId: '500', orgUnitName: 'Section X' },
        { orgUnitId: 11, parentOrgUnitId: '600', orgUnitName: 'Section Y' }
      ];

      component.detailsForm.patchValue({
        forestRegionOrgUnitId: 100,
        forestDistrictOrgUnitId: 1,
        bcParksRegionOrgUnitId: '',
        bcParksSectionOrgUnitId: ''
      });

      component.ngOnInit();
    });

    it('should filter forestDistrictCode based on selected forestRegionOrgUnitId', () => {
      component.detailsForm.get('forestRegionOrgUnitId')?.setValue(200);

      expect(component.forestDistrictCode).toEqual([
        { orgUnitId: 2, parentOrgUnitId: '200', orgUnitName: 'District B' }
      ]);
    });

    it('should clear forestDistrictOrgUnitId if it is not valid after filtering', () => {
      component.detailsForm.patchValue({
        forestRegionOrgUnitId: 200,
        forestDistrictOrgUnitId: 1
      });

      component.detailsForm.get('forestRegionOrgUnitId')?.setValue(200);

      expect(component.detailsForm.get('forestDistrictOrgUnitId')?.value).toBe('');
    });

    it('should filter bcParksSectionCode based on selected bcParksRegionOrgUnitId and enable the section control', () => {
      const bcParksSectionControl = component.detailsForm.get('bcParksSectionOrgUnitId');
      expect(bcParksSectionControl?.disabled).toBeFalse();

      component.detailsForm.get('bcParksRegionOrgUnitId')?.setValue(600);

      expect(component.bcParksSectionCode).toEqual([
        { orgUnitId: 11, parentOrgUnitId: '600', orgUnitName: 'Section Y' }
      ]);
      expect(bcParksSectionControl?.enabled).toBeTrue();
    });

    it('should reset and disable bcParksSectionOrgUnitId when region is unset', () => {
      component.detailsForm.get('bcParksRegionOrgUnitId')?.setValue('');

      const control = component.detailsForm.get('bcParksSectionOrgUnitId');
      expect(control?.value).toBeNull();
      expect(control?.disabled).toBeTrue();
    });
  });

  it('should return the correct FormControl from detailsForm', () => {
    component.detailsForm = new FormGroup({
      projectLead: new FormControl('Alice')
    });

    const spy = spyOn(component.detailsForm, 'get').and.callThrough();

    const control = component.getControl('projectLead');
    expect(control.value).toBe('Alice');
    expect(spy).toHaveBeenCalledWith('projectLead');
  });

  it('should show duplicate name error when updateProject returns 409', () => {
    component.projectGuid = 'test-guid';
    component.projectDetail = {
      projectTypeCode: { projectTypeCode: 'TEST' },
      primaryObjectiveTypeCode: {}
    };

    component.detailsForm = new FormGroup({
      projectTypeCode: new FormControl('FUEL_MGMT'),
      programAreaGuid: new FormControl('area-guid'),
      closestCommunityName: new FormControl('Test City'),
      primaryObjectiveTypeCode: new FormControl('WRR'),
      wildfireOrgUnitId: new FormControl(123)
    });
    spyOnProperty(component.detailsForm, 'valid', 'get').and.returnValue(true);

    mockProjectService.updateProject.and.returnValue(
      throwError(() => ({
        status: 409,
        error: { error: 'Duplicate project name' }
      }))
    );

    component.onSave();

    expect(mockSnackbar.open).toHaveBeenCalledWith(
      'Duplicate project name',
      'OK',
      jasmine.objectContaining({ panelClass: 'snackbar-error' })
    );
  });

  it('should show default failure message when updateProject returns other error', () => {
    component.projectGuid = 'test-guid';
    component.projectDetail = {
      projectTypeCode: { projectTypeCode: 'TEST' },
      primaryObjectiveTypeCode: {}
    };

    component.detailsForm = new FormGroup({
      projectTypeCode: new FormControl('FUEL_MGMT'),
      programAreaGuid: new FormControl('area-guid'),
      closestCommunityName: new FormControl('Test City'),
      primaryObjectiveTypeCode: new FormControl('WRR'),
      wildfireOrgUnitId: new FormControl(123)
    });
    spyOnProperty(component.detailsForm, 'valid', 'get').and.returnValue(true);

    mockProjectService.updateProject.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.onSave();

    expect(mockSnackbar.open).toHaveBeenCalledWith(
      component.messages.projectCreatedFailure,
      'OK',
      jasmine.objectContaining({ panelClass: 'snackbar-error' })
    );
    expect(component.isSaving).toBeFalse();
  });

  describe('project description input/paste', () => {
    beforeEach(() => {
      (component as any).PROJECT_DESC_MAX = 10;
      component.projectDescription = '';
    });

    it('onProjectInput should trim value over max and update model', () => {
      const textarea = document.createElement('textarea');
      textarea.value = 'abcdefghijk';

      component.onProjectInput({ target: textarea } as any);

      expect(textarea.value.length).toBe(10);
      expect(component.projectDescription.length).toBe(10);
    });

    it('onProjectPaste should clamp pasted text and update model', () => {
      const textarea = document.createElement('textarea');
      component.projectDescription = 'abc';
      const evt = {
        preventDefault: jasmine.createSpy(),
        clipboardData: { getData: () => '1234567890' },
        target: textarea
      } as any;

      component.onProjectPaste(evt);

      expect(component.projectDescription.length).toBe(10); // max
      expect(textarea.value.length).toBe(10);
      expect(evt.preventDefault).toHaveBeenCalled();
    });
  });

  describe('onSave projectType change confirmation', () => {
    let dialogSpy: jasmine.SpyObj<any>;

    beforeEach(() => {
      dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
      component['dialog'] = dialogSpy;

      component.originalFormValues = { projectTypeCode: 'OLD_TYPE' };
      component.detailsForm.patchValue({ projectTypeCode: 'NEW_TYPE' });

      component.evaluationCriteriaComponent = {
        evaluationCriteriaSummary: {
          evaluationCriteriaSummaryGuid: 'guid-123'
        }
      } as any;

      mockProjectService.deleteEvaluationCriteriaSummary.and.returnValue(of({}));
    });

    it('should open confirmation dialog when projectType changes and evaluationCriteriaSummary exists', fakeAsync(() => {
      const afterClosedSpy = jasmine.createSpy().and.returnValue(of(true));
      dialogSpy.open.and.returnValue({ afterClosed: afterClosedSpy });

      spyOn(component, 'onSave').and.callThrough();

      component.onSave();
      tick();

      expect(dialogSpy.open).toHaveBeenCalledWith(jasmine.any(Function), jasmine.objectContaining({
        data: jasmine.objectContaining({ indicator: 'change-project-type' })
      }));
      expect(afterClosedSpy).toHaveBeenCalled();
      expect(mockProjectService.deleteEvaluationCriteriaSummary)
        .toHaveBeenCalledWith(component.projectGuid, 'guid-123');
      expect(component.onSave).toHaveBeenCalledTimes(2); // recursive call after delete
    }));

    it('should log warning and skip delete if summaryGuid is missing', fakeAsync(() => {
      component.evaluationCriteriaComponent = {
        evaluationCriteriaSummary: {} as EvaluationCriteriaSummaryModel
      } as any;
      const afterClosedSpy = jasmine.createSpy().and.returnValue(of(true));
      dialogSpy.open.and.returnValue({ afterClosed: afterClosedSpy });

      const consoleSpy = spyOn(console, 'warn');

      component.onSave();
      tick();

      expect(consoleSpy).toHaveBeenCalledWith('No evaluationCriteriaSummaryGuid found, skipping delete.');
      expect(mockProjectService.deleteEvaluationCriteriaSummary).not.toHaveBeenCalled();
    }));

    it('should log when user cancels the dialog', fakeAsync(() => {
      const afterClosedSpy = jasmine.createSpy().and.returnValue(of(false));
      dialogSpy.open.and.returnValue({ afterClosed: afterClosedSpy });

      const consoleSpy = spyOn(console, 'log');

      component.onSave();
      tick();

      expect(consoleSpy).toHaveBeenCalledWith('User canceled project type change, save aborted.');
      expect(mockProjectService.deleteEvaluationCriteriaSummary).not.toHaveBeenCalled();
    }));

    it('should log error if deleteEvaluationCriteriaSummary fails', fakeAsync(() => {
      const afterClosedSpy = jasmine.createSpy().and.returnValue(of(true));
      dialogSpy.open.and.returnValue({ afterClosed: afterClosedSpy });
      mockProjectService.deleteEvaluationCriteriaSummary.and.returnValue(
        throwError(() => new Error('delete failed'))
      );

      const consoleErrorSpy = spyOn(console, 'error');

      component.onSave();
      tick();

      expect(consoleErrorSpy).toHaveBeenCalledWith(
        'Failed to delete evaluation criteria summary',
        jasmine.any(Error)
      );
    }));
  });

  describe('hasApprovedFiscals Method', () => {
    it('should return true if any fiscal has a locked status', () => {
      const fiscals = [
        { planFiscalStatusCode: { planFiscalStatusCode: 'PREPARED' } },
        { planFiscalStatusCode: { planFiscalStatusCode: 'DRAFT' } }
      ] as ProjectFiscal[];

      expect(component.hasApprovedFiscals(fiscals)).toBeTrue();
    });

    it('should return false if no fiscals have a locked status', () => {
      const fiscals = [
        { planFiscalStatusCode: { planFiscalStatusCode: 'DRAFT' } },
        { planFiscalStatusCode: { planFiscalStatusCode: '' } }
      ] as ProjectFiscal[];

      expect(component.hasApprovedFiscals(fiscals)).toBeFalse();
    });

  });

  describe('reloadFiscals Method', () => {
    it('should call getProjectFiscalsByProjectGuid and pass response to handleFiscalsResponse', () => {
      const mockData = { _embedded: { projectFiscals: [] } };
      spyOn(component as any, 'handleFiscalsResponse');
      mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(of(mockData));

      component.projectGuid = 'test-guid';
      component.reloadFiscals();

      expect(mockProjectService.getProjectFiscalsByProjectGuid).toHaveBeenCalledWith('test-guid');
      expect((component as any).handleFiscalsResponse).toHaveBeenCalledWith(mockData);
    });

    it('should not call service if projectGuid is empty', () => {
      component.projectGuid = '';
      component.reloadFiscals();

      expect(mockProjectService.getProjectFiscalsByProjectGuid).not.toHaveBeenCalled();
    });

    it('should log error when service call fails', () => {
      spyOn(console, 'error');
      mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(
        throwError(() => new Error('Service error'))
      );

      component.projectGuid = 'test-guid';
      component.reloadFiscals();

      expect(console.error).toHaveBeenCalledWith('Error reloading fiscals:', jasmine.any(Error));
    });
  });
  
  it('should set duplicate error and show snackbar when updateProject returns 409', () => {
    component.projectGuid = 'test-guid';
    component.projectDetail = {
      projectTypeCode: { projectTypeCode: 'TEST' },
      primaryObjectiveTypeCode: {}
    };

    component.detailsForm = new FormGroup({
      projectName: new FormControl('My Project'),
      projectTypeCode: new FormControl('FUEL_MGMT'),
      programAreaGuid: new FormControl('area-guid'),
      closestCommunityName: new FormControl('Test City'),
      primaryObjectiveTypeCode: new FormControl('WRR'),
      wildfireOrgUnitId: new FormControl(123)
    });
    spyOnProperty(component.detailsForm, 'valid', 'get').and.returnValue(true);

    mockProjectService.updateProject.and.returnValue(
      throwError(() => ({
        status: 409,
        error: { error: 'Duplicate project name' }
      }))
    );

    const projectNameControl = component.detailsForm.get('projectName')!;
    spyOn(projectNameControl, 'setErrors').and.callThrough();
    spyOn(projectNameControl, 'markAsTouched').and.callThrough();

    component.onSave();

    expect(projectNameControl.setErrors).toHaveBeenCalledWith({ duplicate: true });
    expect(projectNameControl.markAsTouched).toHaveBeenCalled();
    expect(projectNameControl.hasError('duplicate')).toBeTrue();

    expect(mockSnackbar.open).toHaveBeenCalledWith(
      'Duplicate project name',
      'OK',
      jasmine.objectContaining({ panelClass: 'snackbar-error' })
    );
  });

  describe('isSaving flag behavior', () => {
    beforeEach(() => {
      component.projectGuid = 'test-guid';
      component.projectDetail = {
        projectTypeCode: { projectTypeCode: 'TEST' },
        primaryObjectiveTypeCode: {}
      };

      component.detailsForm = new FormGroup({
        projectTypeCode: new FormControl('FUEL_MGMT'),
        programAreaGuid: new FormControl('area-guid'),
        closestCommunityName: new FormControl('Test City'),
        primaryObjectiveTypeCode: new FormControl('WRR'),
        wildfireOrgUnitId: new FormControl(123)
      });
      spyOnProperty(component.detailsForm, 'valid', 'get').and.returnValue(true);
    });

    it('should set isSaving to true while saving and reset on success', () => {
      mockProjectService.updateProject.and.returnValue(of({}));
      mockProjectService.getProjectByProjectGuid.and.returnValue(of({}));

      component.onSave();

      expect(component.isSaving).toBeFalse();
    });

    it('should set isSaving to false when updateProject errors', () => {
      mockProjectService.updateProject.and.returnValue(throwError(() => new Error('fail')));

      component.onSave();

      expect(component.isSaving).toBeFalse();
    });

    it('should not proceed with save if isSaving is already true', () => {
      component.isSaving = true;

      component.onSave();

      expect(mockProjectService.updateProject).not.toHaveBeenCalled();
    });

    it('should block onSaveLatLong if isSaving is already true', () => {
      component.isSaving = true;
      component.onSaveLatLong();
      expect(mockProjectService.updateProject).not.toHaveBeenCalled();
    });
  });

});
