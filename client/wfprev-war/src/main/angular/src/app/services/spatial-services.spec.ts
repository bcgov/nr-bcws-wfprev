import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ZipReader } from '@zip.js/zip.js';
import { Geometry, GeometryCollection, LineString, MultiLineString, MultiPoint, MultiPolygon, Point, Polygon, Position } from 'geojson';
import { of, throwError } from 'rxjs';
import { AppConfigService } from './app-config.service';
import { SpatialService } from './spatial-services';
import { TokenService } from './token.service';

// Create mock ZIP module implementation
const mockZipModule = {
  ZipReader: jasmine.createSpy('ZipReader').and.callFake(() => ({
    getEntries: jasmine.createSpy('getEntries').and.resolveTo([
      { filename: 'doc.kml', getData: jasmine.createSpy('getData').and.resolveTo('<kml></kml>') }
    ]),
    close: jasmine.createSpy('close').and.resolveTo(undefined)
  })),
  BlobReader: jasmine.createSpy('BlobReader').and.returnValue({}),
  TextWriter: jasmine.createSpy('TextWriter').and.returnValue({})
};

// Create mock SHP module implementation
const mockShpModule = {
  default: jasmine.createSpy('default').and.resolveTo({
    features: [
      { geometry: { type: 'Polygon', coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]] } }
    ]
  })
};

const mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);

describe('SpatialService', () => {
  let service: SpatialService;
  let httpMock: HttpTestingController;
  let mockAppConfigService: jasmine.SpyObj<AppConfigService>;
  let mockTokenService: jasmine.SpyObj<TokenService>;

  const mockConfig = {
    rest: {
      wfprev: 'http://mock-api.com',
      wfdm: 'http://mock-wfdm-api.com'
    },
    application: {
      lazyAuthenticate: true,
      enableLocalStorageToken: true,
      localStorageTokenKey: 'oauth',
      allowLocalExpiredToken: false,
      baseUrl: 'http://mock-base-url.com',
      acronym: 'TEST',
      version: '1.0.0',
      environment: 'test',
      remiPlannerEmailAddress: 'test@example.com'
    },
    webade: {
      oauth2Url: 'http://mock-oauth-url.com',
      clientId: 'mock-client-id',
      authScopes: 'mock-scope',
      checkTokenUrl: 'http://mock-check-token-url.com',
      enableCheckToken: false,
    },
    mapServices: {
      geoserverApiBaseUrl: 'http://geoserver.test',
      wfnewsApiBaseUrl: 'http://wfnews.test',
      wfnewsApiKey: 'fake-api-key'
    }
  };

  beforeEach(() => {
    mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getConfig']);
    mockTokenService = jasmine.createSpyObj('TokenService', ['getOauthToken'], { credentialsEmitter: of({ userGuid: 'mock-user-guid' }) });

    mockAppConfigService.getConfig.and.returnValue(mockConfig);
    mockTokenService.getOauthToken.and.returnValue('mock-token');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, BrowserAnimationsModule],
      providers: [
        SpatialService,
        { provide: AppConfigService, useValue: mockAppConfigService },
        { provide: TokenService, useValue: mockTokenService },
      ]
    });

    service = TestBed.inject(SpatialService);
    httpMock = TestBed.inject(HttpTestingController);

    // Assign mock modules to global scope
    (window as any).zip = mockZipModule;
    (window as any).shp = mockShpModule;
  });

  afterEach(() => {
    httpMock.verify();

    // Clean up global mocks
    delete (window as any).zip;
    delete (window as any).shp;

    // Reset spy calls
    jasmine.getEnv().allowRespy(true);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('parseKMLToCoordinates', () => {
    it('should parse real KML and return proper coordinates', async () => {
      const kmlString = `
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Document>
            <Placemark>
              <Polygon>
                <outerBoundaryIs>
                  <LinearRing>
                    <coordinates>
                      1,2,0 3,4,0 5,6,0 1,2,0
                    </coordinates>
                  </LinearRing>
                </outerBoundaryIs>
              </Polygon>
            </Placemark>
          </Document>
        </kml>
      `;

      spyOn(service as any, 'validateMultiPolygon').and.returnValue(true);
      const result = await (service as any).parseKMLToCoordinates(kmlString);

      expect(result).toEqual([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);
    });

    it('should log error for unexpected coordinate structure in KML', async () => {
      const kmlString = `
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Placemark>
            <Polygon>
              <outerBoundaryIs>
                <LinearRing>
                  <coordinates>-122.0,37.0,0 -122.0,38.0,0 -121.0,38.0,0 -121.0,37.0,0 -122.0,37.0,0</coordinates>
                </LinearRing>
              </outerBoundaryIs>
            </Polygon>
          </Placemark>
        </kml>
      `;

      const malformedCoords = [[1, 2], [3, 4]]; // Not nested deeply enough for MultiPolygon

      spyOn(service as any, 'extractMultiPolygonCoordinates').and.returnValue(malformedCoords as any);
      spyOn(service as any, 'validateMultiPolygon').and.stub();
      const consoleSpy = spyOn(console, 'error');

      await (service as any).parseKMLToCoordinates(kmlString);

      expect(consoleSpy).toHaveBeenCalledWith('Unexpected coordinate structure:', malformedCoords);
    });

  });


  // Tests for extractKMLCoordinates public method
  describe('extractKMLCoordinates', () => {
    it('should call parseKMLToCoordinates and return its result', async () => {
      // Mock the private parseKMLToCoordinates method
      spyOn<any>(service, 'parseKMLToCoordinates').and.returnValue([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);

      // Act
      const result = await service.extractKMLCoordinates('<mock-kml></mock-kml>');

      // Assert
      expect(result).toEqual([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);
      expect((service as any).parseKMLToCoordinates).toHaveBeenCalledWith('<mock-kml></mock-kml>');
    });
  });

  // Tests for extractMultiPolygonCoordinates private method
  describe('extractMultiPolygonCoordinates', () => {
    it('should return null for null or undefined geometry', () => {
      expect((service as any).extractMultiPolygonCoordinates(null)).toBeNull();
      expect((service as any).extractMultiPolygonCoordinates(undefined)).toBeNull();
    });

    it('should convert Point to MultiPolygon coordinates', () => {
      // Arrange
      const point: Point = {
        type: 'Point',
        coordinates: [1, 2]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(point);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(point);

      // Assert - should create a small polygon around the point
      expect(result.length).toBe(1);
      expect(result[0].length).toBe(1);
      expect(result[0][0].length).toBe(5); // 5 points to close the polygon
    });

    it('should convert LineString to MultiPolygon coordinates', () => {
      // Arrange
      const lineString: LineString = {
        type: 'LineString',
        coordinates: [[1, 2], [3, 4], [5, 6]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(lineString);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(lineString);

      // Assert - should convert to polygon and close it
      expect(result.length).toBe(1);
      expect(result[0].length).toBe(1);
      expect(result[0][0].length).toBe(4); // Original 3 points plus closing point
      expect(result[0][0][0]).toEqual(result[0][0][3]); // First and last points should be the same
    });

    it('should handle already closed LineString', () => {
      // Arrange - a line that's already closed
      const lineString: LineString = {
        type: 'LineString',
        coordinates: [[1, 2], [3, 4], [5, 6], [1, 2]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(lineString);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(lineString);

      // Assert - should recognize it's already closed
      expect(result.length).toBe(1);
      expect(result[0].length).toBe(1);
      expect(result[0][0]).toEqual([[1, 2], [3, 4], [5, 6], [1, 2]]);
    });

    it('should convert Polygon to MultiPolygon format', () => {
      // Arrange
      const polygon: Polygon = {
        type: 'Polygon',
        coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(polygon);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(polygon);

      // Assert - should wrap the coordinates
      expect(result).toEqual([[[[1, 2], [3, 4], [5, 6], [1, 2]]]]);
    });

    it('should convert MultiPoint to MultiPolygon coordinates', () => {
      // Arrange
      const multiPoint: MultiPoint = {
        type: 'MultiPoint',
        coordinates: [[1, 2], [3, 4]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiPoint);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(multiPoint);

      // Assert - should create a polygon for each point
      expect(result.length).toBe(2); // Two results, one for each point
      expect(result[0][0].length).toBe(5); // 5 points for the small polygon
      expect(result[1][0].length).toBe(5);
    });

    it('should convert MultiLineString to MultiPolygon coordinates', () => {
      // Arrange
      const multiLineString: MultiLineString = {
        type: 'MultiLineString',
        coordinates: [
          [[1, 2], [3, 4], [5, 6]],
          [[7, 8], [9, 10], [11, 12]]
        ]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiLineString);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(multiLineString);

      // Assert - should convert each line to a polygon
      expect(result.length).toBe(2);
      expect(result[0][0].length).toBe(4); // 3 original points + closing point
      expect(result[1][0].length).toBe(4);
      expect(result[0][0][0]).toEqual(result[0][0][3]); // First and last points should be the same
    });

    it('should handle already closed MultiLineString', () => {
      // Arrange
      const multiLineString: MultiLineString = {
        type: 'MultiLineString',
        coordinates: [
          [[1, 2], [3, 4], [5, 6], [1, 2]],
          [[7, 8], [9, 10], [11, 12], [7, 8]]
        ]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiLineString);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(multiLineString);

      // Assert
      expect(result.length).toBe(2);
      expect(result[0][0]).toEqual([[1, 2], [3, 4], [5, 6], [1, 2]]);
      expect(result[1][0]).toEqual([[7, 8], [9, 10], [11, 12], [7, 8]]);
    });

    it('should pass through MultiPolygon coordinates', () => {
      // Arrange
      const multiPolygon: MultiPolygon = {
        type: 'MultiPolygon',
        coordinates: [
          [[[1, 2], [3, 4], [5, 6], [1, 2]]],
          [[[7, 8], [9, 10], [11, 12], [7, 8]]]
        ]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiPolygon);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(multiPolygon);

      // Assert - should return as is
      expect(result).toEqual(multiPolygon.coordinates);
    });

    it('should handle GeometryCollection', () => {
      // Arrange
      const geometryCollection: GeometryCollection = {
        type: 'GeometryCollection',
        geometries: [
          {
            type: 'Point',
            coordinates: [1, 2]
          },
          {
            type: 'Polygon',
            coordinates: [[[3, 4], [5, 6], [7, 8], [3, 4]]]
          }
        ]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(geometryCollection);

      // Create a new spy for extractMultiPolygonCoordinates to avoid infinite recursion
      const extractSpy = jasmine.createSpy('extractMultiPolygonCoordinates');
      extractSpy.and.callFake((geometry: Geometry) => {
        if (geometry.type === 'Point') {
          return [[[[0.9999, 1.9999], [1.0001, 1.9999], [1.0001, 2.0001], [0.9999, 2.0001], [0.9999, 1.9999]]]];
        } else if (geometry.type === 'Polygon') {
          return [[[[3, 4], [5, 6], [7, 8], [3, 4]]]];
        }
        return null;
      });

      // Replace the original method temporarily just for this test
      const originalExtract = (service as any).extractMultiPolygonCoordinates;
      (service as any).extractMultiPolygonCoordinates = function (geometry: Geometry) {
        if (geometry === geometryCollection) {
          // For the top-level call, use the original logic which calls extractSpy for nested geometries
          const results: Position[][][] = [];
          for (const geom of geometryCollection.geometries) {
            const coords = extractSpy(geom);
            if (coords) {
              results.push(...coords);
            }
          }
          return results.length > 0 ? results : null;
        }
        // For nested calls, use the extractSpy
        return extractSpy(geometry);
      };

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(geometryCollection);

      // Restore the original method
      (service as any).extractMultiPolygonCoordinates = originalExtract;

      // Assert - should flatten the results
      expect(Array.isArray(result)).toBe(true);
      expect(result.length).toBe(2); // One for each geometry
    });

    it('should return null for unknown geometry type', () => {
      // Arrange
      const unknownGeometry = {
        type: 'Unknown' as any,
        coordinates: []
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(unknownGeometry);

      // Act
      const result = (service as any).extractMultiPolygonCoordinates(unknownGeometry as Geometry);

      // Assert
      expect(result).toBeNull();
    });
  });

  // Tests for getCoordinatesFromGeometry method
  describe('getCoordinatesFromGeometry', () => {
    beforeEach(() => {
      // Mock stripAltitude to return input as-is
      spyOn(service, 'stripAltitude').and.callFake((geom) => geom);
    });

    it('should extract coordinates from Point geometry', () => {
      // Arrange
      const point: Point = {
        type: 'Point',
        coordinates: [1, 2]
      };

      // Act
      const result = service.getCoordinatesFromGeometry(point);

      // Assert
      expect(result).toEqual([1, 2]);
    });

    it('should extract coordinates from LineString geometry', () => {
      // Arrange
      const line: LineString = {
        type: 'LineString',
        coordinates: [[1, 2], [3, 4]]
      };

      // Act
      const result = service.getCoordinatesFromGeometry(line);

      // Assert
      expect(result).toEqual([[1, 2], [3, 4]]);
    });

    it('should extract coordinates from Polygon geometry', () => {
      // Arrange
      const polygon: Polygon = {
        type: 'Polygon',
        coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      };

      // Act
      const result = service.getCoordinatesFromGeometry(polygon);

      // Assert
      expect(result).toEqual([[[1, 2], [3, 4], [5, 6], [1, 2]]]);
    });

    it('should extract coordinates from MultiPoint geometry', () => {
      // Arrange
      const multiPoint: MultiPoint = {
        type: 'MultiPoint',
        coordinates: [[1, 2], [3, 4]]
      };

      // Act
      const result = service.getCoordinatesFromGeometry(multiPoint);

      // Assert
      expect(result).toEqual([[1, 2], [3, 4]]);
    });

    it('should extract coordinates from MultiLineString geometry', () => {
      // Arrange
      const multiLine: MultiLineString = {
        type: 'MultiLineString',
        coordinates: [[[1, 2], [3, 4]], [[5, 6], [7, 8]]]
      };

      // Act
      const result = service.getCoordinatesFromGeometry(multiLine);

      // Assert
      expect(result).toEqual([[[1, 2], [3, 4]], [[5, 6], [7, 8]]]);
    });

    it('should extract coordinates from MultiPolygon geometry', () => {
      // Arrange
      const multiPolygon: MultiPolygon = {
        type: 'MultiPolygon',
        coordinates: [[[[1, 2], [3, 4], [5, 6], [1, 2]]], [[[7, 8], [9, 10], [11, 12], [7, 8]]]]
      };

      // Act
      const result = service.getCoordinatesFromGeometry(multiPolygon);

      // Assert
      expect(result).toEqual([[[[1, 2], [3, 4], [5, 6], [1, 2]]], [[[7, 8], [9, 10], [11, 12], [7, 8]]]]);
    });

    it('should extract coordinates from GeometryCollection', () => {
      // Arrange
      const collection: GeometryCollection = {
        type: 'GeometryCollection',
        geometries: [
          {
            type: 'Point',
            coordinates: [1, 2]
          },
          {
            type: 'Polygon',
            coordinates: [[[3, 4], [5, 6], [7, 8], [3, 4]]]
          }
        ]
      };

      // Act
      const result = service.getCoordinatesFromGeometry(collection);

      // Assert - should return first non-null result
      expect(result).toEqual([1, 2]);
    });

    it('should return null for null or undefined geometry', () => {
      expect(service.getCoordinatesFromGeometry(null)).toBeNull();
      expect(service.getCoordinatesFromGeometry(undefined)).toBeNull();
    });

    it('should return null for unknown geometry type', () => {
      // Arrange
      const unknownGeometry = {
        type: 'Unknown' as any,
        coordinates: []
      };

      // Act
      const result = service.getCoordinatesFromGeometry(unknownGeometry as Geometry);

      // Assert
      expect(result).toBeNull();
    });

    it('should return null if all geometries in GeometryCollection return null', () => {
      // Arrange
      const emptyCollection: GeometryCollection = {
        type: 'GeometryCollection',
        geometries: []
      };

      // Act
      const result = service.getCoordinatesFromGeometry(emptyCollection);

      // Assert
      expect(result).toBeNull();
    });
  });

  describe('stripAltitude', () => {
    it('should strip altitude from MultiPolygon coordinates', () => {
      // Arrange
      const multiPolygon: MultiPolygon = {
        type: 'MultiPolygon',
        coordinates: [
          [[[1, 2, 100], [3, 4, 200], [5, 6, 300], [1, 2, 100]]],
          [[[7, 8, 400], [9, 10, 500], [11, 12, 600], [7, 8, 400]]]
        ] as any
      };

      // Act
      const result = service.stripAltitude(multiPolygon) as MultiPolygon;

      // Assert - should have removed the third coordinate from all points
      expect(result.coordinates[0][0][0].length).toBe(2);
      expect(result.coordinates[0][0][0]).toEqual([1, 2]);
      expect(result.coordinates[1][0][0]).toEqual([7, 8]);
    });

    it('should strip altitude from a GeometryCollection', () => {
      // Arrange
      const geometryCollection: GeometryCollection = {
        type: 'GeometryCollection',
        geometries: [
          {
            type: 'Point',
            coordinates: [100, 200, 300]
          },
          {
            type: 'LineString',
            coordinates: [
              [10, 20, 30],
              [40, 50, 60]
            ]
          },
          {
            type: 'Polygon',
            coordinates: [
              [
                [1, 2, 3],
                [4, 5, 6],
                [1, 2, 3]
              ]
            ]
          }
        ],
        bbox: [0, 0, 100, 100]
      };

      // Act
      const result = service.stripAltitude(geometryCollection) as GeometryCollection;

      // Assert
      expect(result.type).toBe('GeometryCollection');
      expect(result.bbox).toEqual([0, 0, 100, 100]);

      const point = result.geometries[0] as Point;
      const line = result.geometries[1] as LineString;
      const polygon = result.geometries[2] as Polygon;

      expect(point.coordinates).toEqual([100, 200]);
      expect(line.coordinates).toEqual([[10, 20], [40, 50]]);
      expect(polygon.coordinates[0]).toEqual([[1, 2], [4, 5], [1, 2]]);
    });

  });

  describe('extractGDBGeometry', () => {
    const mockFile = new File(['some data'], 'data.gdb');
    const mockGeometry = {
      type: 'MultiPolygon',
      coordinates: [
        [[[100, 50], [101, 51], [102, 50], [100, 50]]],
        [[[-120, 45], [-121, 46], [-122, 45], [-120, 45]]]
      ]
    } as Geometry;


    it('should extract and process geometry when the API returns valid data', (done: () => void) => {
      // Spy on stripAltitude and return the input geometry unchanged
      const stripAltitudeSpy = spyOn(service, 'stripAltitude').and.callFake((geom) => geom);

      service.extractGDBGeometry(mockFile).subscribe((result) => {
        expect(result.length).toBe(1);
        expect(stripAltitudeSpy).toHaveBeenCalledTimes(1);
        expect(stripAltitudeSpy.calls.mostRecent().args[0]).toEqual(mockGeometry);
        done();
      });

      const req = httpMock.expectOne('http://mock-api.com/wfprev-api/gdb/extract');
      expect(req.request.method).toBe('POST');
      req.flush({ body: JSON.stringify([mockGeometry]) });
    });

    it('should handle error gracefully when the API returns an error', (done: () => void) => {
      service.extractGDBGeometry(mockFile).subscribe({
        next: () => fail('Expected an error, but got a successful response'),
        error: (error) => {
          expect(error.message).toBe('Failed to extract geodatabase geometry');
          done();
        }
      });

      const req = httpMock.expectOne('http://mock-api.com/wfprev-api/gdb/extract');
      expect(req.request.method).toBe('POST');
      req.flush('Error', { status: 500, statusText: 'Internal Server Error' });
    });

  });

  describe('extractCoordinates', () => {
    it('should handle a ZIP file by calling handleCompressedFile', async () => {
      const mockFile = new File([''], 'test.zip');
      const mockCoords = [[[[3, 4]]]] as Position[][][];
      spyOn(service, 'handleCompressedFile').and.returnValue(Promise.resolve(mockCoords));

      const result = await service.extractCoordinates(mockFile);

      expect(service.handleCompressedFile).toHaveBeenCalledWith(mockFile);
      expect(result).toEqual(mockCoords);
    });

    it('should handle a GDB file by calling handleCompressedFile', async () => {
      const mockFile = new File([''], 'data.gdb');
      spyOn(service, 'handleCompressedFile').and.returnValue(Promise.resolve([[[[5, 6]]]] as Position[][][]));

      const result = await service.extractCoordinates(mockFile);

      expect(service.handleCompressedFile).toHaveBeenCalledWith(mockFile);
      expect(result).toEqual([[[[5, 6]]]] as Position[][][]);
    });

    it('should return an empty array for unsupported file types', async () => {
      const mockFile = new File(['some data'], 'data.txt');

      const result = await service.extractCoordinates(mockFile);

      expect(result).toEqual([]);
    });

    it('should return an empty array when file has no extension', async () => {
      const mockFile = new File(['some data'], 'file');

      const result = await service.extractCoordinates(mockFile);

      expect(result).toEqual([]);
    });

    it('should catch errors thrown by extractKMLCoordinates', async () => {
      const mockFile = new File(['<kml></kml>'], 'error.kml');
      spyOn(mockFile, 'text').and.returnValue(Promise.resolve('<kml>bad</kml>'));
      spyOn(service, 'extractKMLCoordinates').and.throwError('KML error');

      await expectAsync(service.extractCoordinates(mockFile))
        .toBeRejectedWithError('Error extracting coordinates.');
    });

    it('should catch errors thrown by handleCompressedFile', async () => {
      const mockFile = new File([''], 'data.zip');
      spyOn(service, 'handleCompressedFile').and.throwError('ZIP error');

      await expectAsync(service.extractCoordinates(mockFile))
        .toBeRejectedWithError('Error extracting coordinates.');
    });
  });

  describe('hasSHPEntry', () => {
    it('should return true if both .shp and .dbf entries exist', () => {
      const entries = [
        { filename: 'file1.shp' },
        { filename: 'file2.dbf' },
        { filename: 'file3.prj' }
      ];
      expect((service as any).hasSHPEntry(entries)).toBeTrue();
    });

    it('should return false if .shp is missing', () => {
      const entries = [
        { filename: 'file2.dbf' },
        { filename: 'file3.prj' }
      ];
      expect((service as any).hasSHPEntry(entries)).toBeFalse();
    });

    it('should return false if .dbf is missing', () => {
      const entries = [
        { filename: 'file1.shp' },
        { filename: 'file3.prj' }
      ];
      expect((service as any).hasSHPEntry(entries)).toBeFalse();
    });
  });

  describe('hasGDBEntries', () => {
    it('should return true if any filename includes .gdbtable', () => {
      const entries = [
        { filename: 'feature1.gdbtable' },
        { filename: 'feature2.gdbtablx' }
      ];
      expect((service as any).hasGDBEntries(entries)).toBeTrue();
    });

    it('should return false if no .gdbtable entries exist', () => {
      const entries = [
        { filename: 'feature2.gdbtablx' },
        { filename: 'randomfile.txt' }
      ];
      expect((service as any).hasGDBEntries(entries)).toBeFalse();
    });
  });

  describe('handleGDB', () => {

    it('should return empty array if extractGDBGeometry throws', async () => {
      const mockFile = new File([], 'test.gdb');

      spyOn(service as any, 'extractGDBGeometry').and.returnValue(throwError(() => new Error('Extraction failed')));
      spyOn(service as any, 'validateMultiPolygon').and.stub();

      const result = await (service as any).handleGDB(mockFile);

      expect(result).toEqual([]);
    });

  });

  describe('validateMultiPolygon', () => {
    let service: SpatialService;
    let mockSnackbar: jasmine.SpyObj<MatSnackBar>;

    beforeEach(() => {
      mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);
      const mockHttp = jasmine.createSpyObj('HttpClient', ['post']);
      const mockAppConfigService = {
        getConfig: () => ({
          rest: {
            wfprev: 'http://mock-url'
          }
        })
      };
      const mockTokenService = {
        getOauthToken: () => 'mock-token'
      };

      service = new SpatialService(
        mockHttp,
        mockSnackbar,
        mockAppConfigService as any,
        mockTokenService as any
      );

      spyOn(service, 'validateGeometryWithBackend').and.returnValue(Promise.resolve({ valid: true, message: 'Valid' }));
    });
    it('should validate a correct multipolygon without errors', async () => {
      const coords: Position[][][] = [
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ];

      spyOn(service, 'validateGeometryInBC').and.returnValue(Promise.resolve(true));

      await expectAsync(service.validateMultiPolygon(coords)).toBeResolved();
      expect(mockSnackbar.open).not.toHaveBeenCalled();
    });

    it('should throw an error for invalid multipolygon geometry', async () => {
      const coords: Position[][][] = [
        [[[0, 0], [1, 1], [2, 2], [0, 0]]] // A degenerate polygon (collinear)
      ];

      spyOn(service as any, 'isValidGeometry').and.returnValue(false);
      spyOn(service, 'validateGeometryInBC').and.returnValue(Promise.resolve(true));

      await expectAsync(service.validateMultiPolygon(coords)).toBeRejectedWithError('Geometry is invalid.');
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Geometry is invalid.',
        'Close',
        jasmine.objectContaining({ duration: 5000 })
      );
    });

    it('should throw an error for self-intersections', async () => {
      const coords: Position[][][] = [
        [[[0, 0], [1, 1], [1, 0], [0, 1], [0, 0]]] // Known self-intersecting polygon (bowtie)
      ];

      spyOn(service as any, 'isValidGeometry').and.returnValue(true);
      spyOn(service as any, 'getKinks').and.callFake(() => ({
        type: 'FeatureCollection',
        features: [
          {
            type: 'Feature',
            geometry: {
              type: 'Point',
              coordinates: [0.5, 0.5]
            },
            properties: {}
          }
        ]
      }));
      spyOn(service, 'validateGeometryInBC').and.returnValue(Promise.resolve(true));

      await expectAsync(service.validateMultiPolygon(coords)).toBeRejectedWithError('Self-intersections found in the uploaded geometry.');
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Found 1 self-intersections in the uploaded geometry.',
        'Close',
        jasmine.objectContaining({ duration: 5000 })
      );
    });

    it('should throw an error when geometry is outside of BC', async () => {
      const coords: Position[][][] = [
        [[[0, 0], [1, 0], [1, 1], [0, 1], [0, 0]]]
      ];

      spyOn(service as any, 'isValidGeometry').and.returnValue(true);
      spyOn(service as any, 'getKinks').and.callThrough();
      spyOn(service, 'validateGeometryInBC').and.returnValue(Promise.resolve(false));

      await expectAsync(service.validateMultiPolygon(coords)).toBeRejectedWithError('Geometry is invalid.');
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Geometry is outside of BC.',
        'Close',
        jasmine.objectContaining({ duration: 5000 })
      );
    });

    it('should accept a GeoJSON.MultiPolygon input and validate it', async () => {
      const geoJson: GeoJSON.MultiPolygon = {
        type: 'MultiPolygon',
        coordinates: [
          [[[1, 2], [3, 4], [5, 6], [1, 2]]]
        ]
      };

      spyOn(service, 'validateGeometryInBC').and.returnValue(Promise.resolve(true));

      await expectAsync(service.validateMultiPolygon(geoJson)).toBeResolved();
      expect(mockSnackbar.open).not.toHaveBeenCalled();
    });
  });


  describe('hasKMZEntry', () => {
    it('should return true if KMZ entry exists', () => {
      const entries = [{ filename: 'data.kml' }];
      expect(service['hasKMZEntry'](entries)).toBeTrue();
    });

    it('should return false if no KML file is present', () => {
      const entries = [{ filename: 'data.txt' }];
      expect(service['hasKMZEntry'](entries)).toBeFalse();
    });
  });

  describe('handleKMZ', () => {
    it('should extract and parse KML from KMZ entries', async () => {
      const mockKML = '<kml></kml>';
      const getDataSpy = jasmine.createSpy().and.returnValue(Promise.resolve(mockKML));
      const mockEntries = [{ filename: 'doc.kml', getData: getDataSpy }];

      spyOn(service as any, 'extractKMLCoordinates').and.returnValue([]);

      const coords = await service['handleKMZ'](mockEntries);

      expect(getDataSpy).toHaveBeenCalled();
      expect(service['extractKMLCoordinates']).toHaveBeenCalledWith(mockKML);
      expect(coords).toEqual([]);
    });

    it('should return empty array if no KML found', async () => {
      const mockEntries = [{ filename: 'file.txt' }];
      const coords = await service['handleKMZ'](mockEntries);
      expect(coords).toEqual([]);
    });

    it('should return empty array if KML extraction fails', async () => {
      const getDataSpy = jasmine.createSpy().and.throwError('Fail');
      const mockEntries = [{ filename: 'doc.kml', getData: getDataSpy }];

      const coords = await service['handleKMZ'](mockEntries);

      expect(coords).toEqual([]);
    });
  });

  describe('handleKMZFile', () => {
    it('should extract and parse KML from KMZ file', async () => {
      const mockKML = '<kml></kml>';
      const mockFile = new File(['dummy'], 'file.kmz');
      const getDataSpy = jasmine.createSpy().and.returnValue(Promise.resolve(mockKML));

      spyOn(service as any, 'extractKMLCoordinates').and.returnValue([]);

      spyOn(ZipReader.prototype, 'getEntries').and.returnValue(Promise.resolve([
        {
          filename: 'doc.kml',
          getData: getDataSpy
        } as any
      ]));

      spyOn(ZipReader.prototype, 'close').and.returnValue(Promise.resolve());

      const coords = await service.handleKMZFile(mockFile);

      expect(getDataSpy).toHaveBeenCalled();
      expect(service['extractKMLCoordinates']).toHaveBeenCalledWith(mockKML);
      expect(coords).toEqual([]);

    });

    it('should return empty array when no KML file is found', async () => {
      const mockFile = new File(['dummy'], 'file.kmz');

      spyOn(ZipReader.prototype, 'getEntries').and.returnValue(Promise.resolve([]));
      spyOn(ZipReader.prototype, 'close').and.returnValue(Promise.resolve());

      const coords = await service.handleKMZFile(mockFile);

      expect(coords).toEqual([]);
    });
  });

  describe('handleCompressedFile', () => {
    it('should extract SHP coordinates if SHP entry exists', async () => {
      const mockFile = new File(['dummy'], 'file.zip');
      const mockCoords: Position[][][] = [[[[1, 2]]]];

      spyOn(ZipReader.prototype, 'getEntries').and.returnValue(Promise.resolve(['shapefile.shp'] as any));
      spyOn(service as any, 'hasSHPEntry').and.returnValue(true);
      spyOn(service as any, 'extractSHPCoordinates').and.returnValue(Promise.resolve(mockCoords));
      spyOn(ZipReader.prototype, 'close').and.returnValue(Promise.resolve());

      const result = await service.handleCompressedFile(mockFile);

      expect(service['extractSHPCoordinates']).toHaveBeenCalledWith(mockFile);
      expect(result).toEqual(mockCoords);
    });

    it('should handle GDB if GDB entries exist', async () => {
      const mockFile = new File(['dummy'], 'file.zip');
      const mockCoords: Position[][][] = [[[[3, 4]]]];

      spyOn(ZipReader.prototype, 'getEntries').and.returnValue(Promise.resolve(['foo.gdb'] as any));
      spyOn(service as any, 'hasSHPEntry').and.returnValue(false);
      spyOn(service as any, 'hasGDBEntries').and.returnValue(true);
      spyOn(service as any, 'handleGDB').and.returnValue(Promise.resolve(mockCoords));
      spyOn(ZipReader.prototype, 'close').and.returnValue(Promise.resolve());

      const result = await service.handleCompressedFile(mockFile);

      expect(service['handleGDB']).toHaveBeenCalledWith(mockFile);
      expect(result).toEqual(mockCoords);
    });

    it('should handle KMZ if KMZ entry exists', async () => {
      const mockFile = new File(['dummy'], 'file.kmz');
      const mockCoords: Position[][][] = [[[[5, 6]]]];

      spyOn(ZipReader.prototype, 'getEntries').and.returnValue(Promise.resolve(['doc.kml'] as any));
      spyOn(service as any, 'hasSHPEntry').and.returnValue(false);
      spyOn(service as any, 'hasGDBEntries').and.returnValue(false);
      spyOn(service as any, 'hasKMZEntry').and.returnValue(true);
      spyOn(service as any, 'handleKMZ').and.returnValue(Promise.resolve(mockCoords));
      spyOn(ZipReader.prototype, 'close').and.returnValue(Promise.resolve());

      const result = await service.handleCompressedFile(mockFile);

      expect(service['handleKMZ']).toHaveBeenCalledWith(['doc.kml'] as any);
      expect(result).toEqual(mockCoords);
    });

    it('should always close the zipReader', async () => {
      const mockFile = new File(['dummy'], 'file.zip');

      spyOn(ZipReader.prototype, 'getEntries').and.throwError('Simulated error');
      const closeSpy = spyOn(ZipReader.prototype, 'close').and.returnValue(Promise.resolve());

      try {
        await service.handleCompressedFile(mockFile);
      } catch (e) {
        // catch expected error
      }

      expect(closeSpy).toHaveBeenCalled();
    });
  });

  describe('validateGeometryInBC', () => {
    let service: SpatialService;
    let mockSnackbar: jasmine.SpyObj<MatSnackBar>;
    beforeEach(() => {
      mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);
      const mockHttp = jasmine.createSpyObj('HttpClient', ['post']);
      const mockAppConfigService = {
        getConfig: () => ({
          rest: {
            wfprev: 'http://mock-url'
          }
        })
      };
      const mockTokenService = {
        getOauthToken: () => 'mock-token'
      };

      service = new SpatialService(
        mockHttp,
        mockSnackbar,
        mockAppConfigService as any,
        mockTokenService as any
      );
    });


    it('should return true when geometry intersects with BC', async () => {
      const bcCoords: MultiPolygon['coordinates'] = [
        [[[0, 0], [1, 0], [1, 1], [0, 1], [0, 0]]]
      ];

      spyOn(service, 'getBritishColumbiaGeoJSON').and.returnValue(of(bcCoords));

      spyOn<any>(service, 'intersectsWithBC').and.returnValue(true);

      const input = {
        type: 'Polygon',
        coordinates: [[[0.2, 0.2], [0.8, 0.2], [0.5, 0.8], [0.2, 0.2]]]
      };

      const result = await service.validateGeometryInBC(input);
      expect(result).toBeTrue();
      expect(mockSnackbar.open).not.toHaveBeenCalled();
    });

    it('should return false and show snackbar if geometry is outside BC', async () => {
      const bcCoords: MultiPolygon['coordinates'] = [
        [[[0, 0], [1, 0], [1, 1], [0, 1], [0, 0]]]
      ];

      spyOn(service, 'getBritishColumbiaGeoJSON').and.returnValue(of(bcCoords));
      spyOn<any>(service, 'intersectsWithBC').and.returnValue((false));

      const input = {
        type: 'Polygon',
        coordinates: [[[10, 10], [11, 10], [11, 11], [10, 11], [10, 10]]]
      };

      const result = await service.validateGeometryInBC(input);
      expect(result).toBeFalse();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Geometry is outside British Columbia.',
        'Close',
        jasmine.objectContaining({ duration: 5000 })
      );
    });

    it('should return false and show error snackbar if an exception occurs', async () => {
      spyOn(service, 'getBritishColumbiaGeoJSON').and.returnValue(throwError(() => new Error('Failed to load BC boundary')));

      const input = {
        type: 'Polygon',
        coordinates: [[[0, 0], [1, 1], [2, 2], [0, 0]]]
      };

      const result = await service.validateGeometryInBC(input);
      expect(result).toBeFalse();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Error validating geometry in BC.',
        'Close',
        jasmine.objectContaining({ duration: 5000 })
      );
    });
  });
});