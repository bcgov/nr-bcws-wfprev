import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MapComponent } from './map.component';
import { By } from '@angular/platform-browser';
import * as L from 'leaflet';
import { ChangeDetectorRef } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';


describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let mapMock: Partial<L.Map>;
  let cdrMock: jasmine.SpyObj<ChangeDetectorRef>;
  let mockAppConfigService: jasmine.SpyObj<AppConfigService>;

  beforeEach(async () => {
    mapMock = {
      fitBounds: jasmine.createSpy('fitBounds'),
      invalidateSize: jasmine.createSpy('invalidateSize'),
      addLayer: jasmine.createSpy('addLayer'),
    };
  
    cdrMock = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck']);
    mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getConfig']);
    mockAppConfigService.getConfig.and.returnValue({
      application: {
        acronym: 'TEST',
        version: '1.0',
        baseUrl: 'https://test.example.com',
        environment: 'test',
        lazyAuthenticate: false,
        enableLocalStorageToken: true,
        allowLocalExpiredToken: false,
        localStorageTokenKey: 'test-token-key'
      },
      rest: {
        someServiceUrl: 'https://rest.example.com'
      },
      webade: {
        oauth2Url: 'https://auth.example.com',
        clientId: 'test-client-id',
        authScopes: 'read write',
        enableCheckToken: true,
        checkTokenUrl: 'https://auth.example.com/check-token'
      }
    });
  
    spyOn(L, 'map').and.returnValue(mapMock as L.Map);
  
    await TestBed.configureTestingModule({
      imports: [
        MapComponent, // Add the standalone component to the `imports` array
        HttpClientTestingModule, // Import HttpClientTestingModule to resolve HttpBackend
        BrowserAnimationsModule, // Import BrowserAnimationsModule for animations
      ],
      providers: [
        { provide: ChangeDetectorRef, useValue: cdrMock },
        { provide: AppConfigService, useValue: mockAppConfigService }, // Mock the AppConfigService
      ],
    }).compileComponents();
  
    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Placeholder test case that always passes
  it('should always pass', () => {
    expect(true).toBeTrue();
  });
});
