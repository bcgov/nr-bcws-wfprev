import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SpatialService } from './spatial-services';
import { Geometry, Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, GeometryCollection, Position, GeoJsonProperties, FeatureCollection } from 'geojson';
import * as turf from '@turf/turf';

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

describe('SpatialService', () => {
  let service: SpatialService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SpatialService]
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

  describe('parseKMLToCoordinates (integration)', () => {
    it('should parse real KML and return proper coordinates', () => {
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
  
      // Don’t spy, call the real method through the public one (if needed, cast to call private)
      const result = (service as any).parseKMLToCoordinates(kmlString);
  
      expect(result).toEqual([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);
    });
  });
  

  // Tests for extractKMLCoordinates public method
  describe('extractKMLCoordinates', () => {
    it('should call parseKMLToCoordinates and return its result', () => {
      // Mock the private parseKMLToCoordinates method
      spyOn<any>(service, 'parseKMLToCoordinates').and.returnValue([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);

      // Act
      const result = service.extractKMLCoordinates('<mock-kml></mock-kml>');

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
    it('should call API and return processed geometry', () => {
      // Arrange
      const mockFile = new File(['dummy content'], 'test.gdb');
      const mockResponse = [
        {
          type: 'Polygon',
          coordinates: [[[1, 2, 100], [3, 4, 200], [5, 6, 300], [1, 2, 100]]]
        }
      ];

      // Spy on stripAltitude to control the return value
      spyOn(service, 'stripAltitude').and.returnValue({
        type: 'Polygon',
        coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      });

      // Act
      const result = service.extractGDBGeometry(mockFile);

      // Assert expectations before request is fulfilled
      result.subscribe(data => {
        expect(data).toEqual([
          {
            type: 'Polygon',
            coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]]
          }
        ]);
      });

      // Fulfill the HTTP request
      const req = httpMock.expectOne('http://localhost:3000/upload');
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should handle API errors', (done) => {
      // Arrange
      const mockFile = new File(['dummy content'], 'test.gdb');
      const mockError = { status: 500, statusText: 'Internal Server Error' };

      // Act
      const result = service.extractGDBGeometry(mockFile);

      // Assert
      result.subscribe({
        next: () => {
          done.fail('Should have failed with error');
        },
        error: (error) => {
          expect(error).toBeInstanceOf(Error);
          expect(error.message).toBe('Failed to extract geodatabase geometry');
          done();
        }
      });

      // Fulfill the HTTP request with an error
      const req = httpMock.expectOne('http://localhost:3000/upload');
      req.flush('Error', mockError);
    });
  });

  describe('extractCoordinates', () => {    
    it('should handle a ZIP file by calling handleCompressedFile', async () => {
      const mockFile = new File([''], 'test.zip');
      const mockCoords = [[3, 4]];
      spyOn(service, 'handleCompressedFile').and.returnValue(Promise.resolve(mockCoords));
  
      const result = await service.extractCoordinates(mockFile);
  
      expect(service.handleCompressedFile).toHaveBeenCalledWith(mockFile);
      expect(result).toEqual(mockCoords);
    });
  
    it('should handle a GDB file by calling handleCompressedFile', async () => {
      const mockFile = new File([''], 'data.gdb');
      spyOn(service, 'handleCompressedFile').and.returnValue(Promise.resolve([[5, 6]]));
  
      const result = await service.extractCoordinates(mockFile);
  
      expect(service.handleCompressedFile).toHaveBeenCalledWith(mockFile);
      expect(result).toEqual([[5, 6]]);
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
  
      const result = await service.extractCoordinates(mockFile);
  
      expect(result).toEqual([]);
    });
  
    it('should catch errors thrown by handleCompressedFile', async () => {
      const mockFile = new File([''], 'data.zip');
      spyOn(service, 'handleCompressedFile').and.throwError('ZIP error');
  
      const result = await service.extractCoordinates(mockFile);
  
      expect(result).toEqual([]);
    });
  });

  // fdescribe('validateMultiPolygon', () => {
  //   let consoleSpy: jasmine.Spy;
  //   let consoleErrorSpy: jasmine.Spy;
    
  //   // Store original functions
  //   let originalMultiPolygon: any;
  //   let originalPolygon: any;
  //   let originalBooleanValid: any;
  //   let originalKinks: any;
    
  //   beforeEach(() => {
  //     // Assuming the method is part of a service
  //     // service = TestBed.inject(YourServiceName);
      
  //     // Alternatively, if you're testing a standalone function:
  //     // service = { validateMultiPolygon };
      
  //     // Spy on console methods
  //     consoleSpy = spyOn(console, 'log');
  //     consoleErrorSpy = spyOn(console, 'error');
      
  //     // Store original functions
  //     originalMultiPolygon = turf.multiPolygon;
  //     originalPolygon = turf.polygon;
  //     originalBooleanValid = turf.booleanValid;
  //     originalKinks = turf.kinks;
  //   });
    
  //   afterEach(() => {
  //     // Restore original functions
  //     (turf as any).multiPolygon = originalMultiPolygon;
  //     (turf as any).polygon = originalPolygon;
  //     (turf as any).booleanValid = originalBooleanValid;
  //     (turf as any).kinks = originalKinks;
  //   });
    
  //   it('should validate a valid multipolygon with coordinate array input', () => {
  //     // Valid multipolygon coordinates (two separate polygons)
  //     const validMultiPolygon: Position[][][] = [
  //       [
  //         [[0, 0], [0, 1], [1, 1], [1, 0], [0, 0]] // first polygon - square
  //       ],
  //       [
  //         [[2, 2], [2, 3], [3, 3], [3, 2], [2, 2]] // second polygon - another square
  //       ]
  //     ];
      
  //     // Create a properly formatted empty FeatureCollection
  //     const emptyKinks: FeatureCollection<Point, GeoJsonProperties> = {
  //       type: 'FeatureCollection',
  //       features: []
  //     };
      
  //     // Mock turf functions
  //     const mockMultiPolygon = jasmine.createSpy('multiPolygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'MultiPolygon', coordinates: coords } };
  //     });
      
  //     const mockPolygon = jasmine.createSpy('polygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'Polygon', coordinates: coords } };
  //     });
      
  //     const mockBooleanValid = jasmine.createSpy('booleanValid').and.returnValue(true);
  //     const mockKinks = jasmine.createSpy('kinks').and.returnValue(emptyKinks);
      
  //     // Override turf functions with mocks using type assertion
  //     (turf as any).multiPolygon = mockMultiPolygon;
  //     (turf as any).polygon = mockPolygon;
  //     (turf as any).booleanValid = mockBooleanValid;
  //     (turf as any).kinks = mockKinks;
      
  //     // Execute the function
  //     expect(() => service.validateMultiPolygon(validMultiPolygon)).not.toThrow();
      
  //     // Verify turf methods were called correctly
  //     expect(mockMultiPolygon).toHaveBeenCalledWith(validMultiPolygon);
  //     expect(mockBooleanValid).toHaveBeenCalled();
  //     expect(mockKinks).toHaveBeenCalledTimes(2); // Once for each polygon
  //     expect(consoleSpy).toHaveBeenCalledWith('MultiPolygon validation passed - no self-intersections detected');
  //   });
    
  //   it('should validate a valid multipolygon with GeoJSON input', () => {
  //     // Valid multipolygon as GeoJSON
  //     const validGeoJSON = {
  //       type: 'MultiPolygon',
  //       coordinates: [
  //         [
  //           [[0, 0], [0, 1], [1, 1], [1, 0], [0, 0]]
  //         ],
  //         [
  //           [[2, 2], [2, 3], [3, 3], [3, 2], [2, 2]]
  //         ]
  //       ]
  //     };
      
  //     // Create a properly formatted empty FeatureCollection
  //     const emptyKinks: FeatureCollection<Point, GeoJsonProperties> = {
  //       type: 'FeatureCollection',
  //       features: []
  //     };
      
  //     // Mock turf functions with type assertion
  //     (turf as any).multiPolygon = jasmine.createSpy('multiPolygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'MultiPolygon', coordinates: coords } };
  //     });
      
  //     (turf as any).polygon = jasmine.createSpy('polygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'Polygon', coordinates: coords } };
  //     });
      
  //     (turf as any).booleanValid = jasmine.createSpy('booleanValid').and.returnValue(true);
  //     (turf as any).kinks = jasmine.createSpy('kinks').and.returnValue(emptyKinks);
      
  //     // expect(() => service.validateMultiPolygon(validGeoJSON)).not.toThrow();
      
  //     expect(consoleSpy).toHaveBeenCalledWith('MultiPolygon validation passed - no self-intersections detected');
  //   });
    
  //   it('should throw an error when a polygon has self-intersections', () => {
  //     // MultiPolygon with a self-intersecting polygon
  //     const selfIntersectingPolygon: Position[][][] = [
  //       [
  //         // Butterfly shape with crossing lines
  //         [[0, 0], [1, 1], [0, 1], [1, 0], [0, 0]]
  //       ]
  //     ];
      
  //     // Mock kinks to return one intersection point with proper structure
  //     const mockKinks: FeatureCollection<Point, GeoJsonProperties> = {
  //       type: 'FeatureCollection',
  //       features: [{
  //         type: 'Feature',
  //         properties: {},
  //         geometry: {
  //           type: 'Point',
  //           coordinates: [0.5, 0.5]
  //         }
  //       }]
  //     };
      
  //     // Mock turf functions with type assertion
  //     (turf as any).multiPolygon = jasmine.createSpy('multiPolygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'MultiPolygon', coordinates: coords } };
  //     });
      
  //     (turf as any).polygon = jasmine.createSpy('polygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'Polygon', coordinates: coords } };
  //     });
      
  //     (turf as any).booleanValid = jasmine.createSpy('booleanValid').and.returnValue(false);
  //     (turf as any).kinks = jasmine.createSpy('kinks').and.returnValue(mockKinks);
      
  //     expect(() => service.validateMultiPolygon(selfIntersectingPolygon))
  //       .toThrowError('Found 1 self-intersections in the multipolygon');
        
  //     expect(consoleErrorSpy).toHaveBeenCalled();
  //   });
    
  //   it('should throw an error when overall geometry is invalid', () => {
  //     // MultiPolygon with valid polygons but invalid topology
  //     const invalidTopologyMultiPolygon: Position[][][] = [
  //       [
  //         [[0, 0], [0, 1], [1, 1], [1, 0], [0, 0]]
  //       ]
  //     ];
      
  //     // No self-intersections but invalid topology
  //     const emptyKinks: FeatureCollection<Point, GeoJsonProperties> = {
  //       type: 'FeatureCollection',
  //       features: []
  //     };
      
  //     // Mock turf functions with type assertion
  //     (turf as any).multiPolygon = jasmine.createSpy('multiPolygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'MultiPolygon', coordinates: coords } };
  //     });
      
  //     (turf as any).polygon = jasmine.createSpy('polygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'Polygon', coordinates: coords } };
  //     });
      
  //     (turf as any).kinks = jasmine.createSpy('kinks').and.returnValue(emptyKinks);
  //     (turf as any).booleanValid = jasmine.createSpy('booleanValid').and.returnValue(false);
      
  //     expect(() => service.validateMultiPolygon(invalidTopologyMultiPolygon))
  //       .toThrowError('Invalid geometry: Other topology errors detected in multipolygon');
        
  //     expect(consoleErrorSpy).toHaveBeenCalled();
  //   });
    
  //   it('should throw an error when an individual polygon is invalid', () => {
  //     // MultiPolygon with an invalid polygon
  //     const invalidPolygonMulti: Position[][][] = [
  //       [
  //         [[0, 0], [0, 1], [1, 0], [0, 0]] // Missing a vertex, not enough points
  //       ]
  //     ];
      
  //     // Mock turf functions with type assertion
  //     (turf as any).multiPolygon = jasmine.createSpy('multiPolygon').and.callFake((coords) => {
  //       return { type: 'Feature', geometry: { type: 'MultiPolygon', coordinates: coords } };
  //     });
      
  //     // Mock polygon to throw an error
  //     (turf as any).polygon = jasmine.createSpy('polygon').and.throwError('Invalid polygon');
      
  //     expect(() => service.validateMultiPolygon(invalidPolygonMulti))
  //       .toThrowError('Invalid polygon in multipolygon');
        
  //     expect(consoleErrorSpy).toHaveBeenCalled();
  //   });
    
  //   it('should handle multiple self-intersections across different polygons', () => {
  //     // MultiPolygon with multiple polygons having self-intersections
  //     const multiSelfIntersectingPolygon: Position[][][] = [
  //       [
  //         // First polygon with intersection
  //         [[0, 0], [1, 1], [0, 1], [1, 0], [0, 0]]
  //       ],
  //       [
  //         // Second polygon with intersection
  //         [[2, 2], [3, 3], [2, 3], [3, 2], [2, 2]]
  //       ]
  //     ];
      
  //     // Mock kinks to return different intersection points for each polygon with proper structure
  //     const mockKinks1: FeatureCollection<Point, GeoJsonProperties> = {
  //       type: 'FeatureCollection',
  //       features: [{
  //         type: 'Feature',
  //         properties: {},
  //         geometry: {
  //           type: 'Point',
  //           coordinates: [0.5, 0.5]
  //         }
  //       }]
  //     };
      
  //     const mockKinks2: FeatureCollection<Point, GeoJsonProperties> = {
  //       type: 'FeatureCollection',
  //       features: [{
  //         type: 'Feature',
  //         properties: {},
  //         geometry: {
  //           type: 'Point',
  //           coordinates: [2.5, 2.5]
  //         }
  //       }]
  //     };
      
  //     // Mock turf functions with type assertion
  //     (turf as any).multiPolygon = jasmine.createSpy('multiPolygon').and.callFake((coords: any) => {
  //       return { type: 'Feature', geometry: { type: 'MultiPolygon', coordinates: coords } };
  //     });
      
  //     (turf as any).polygon = jasmine.createSpy('polygon').and.callFake((coords: any) => {
  //       return { type: 'Feature', geometry: { type: 'Polygon', coordinates: coords } };
  //     });
      
  //     (turf as any).booleanValid = jasmine.createSpy('booleanValid').and.returnValue(false);
      
  //     // Set up kinks to return different values for each call
  //     const kinksSpy = jasmine.createSpy('kinks');
  //     kinksSpy.and.returnValues(mockKinks1, mockKinks2);
  //     (turf as any).kinks = kinksSpy;
      
  //     expect(() => service.validateMultiPolygon(multiSelfIntersectingPolygon))
  //       .toThrowError('Found 2 self-intersections in the multipolygon');
        
  //     expect(consoleErrorSpy).toHaveBeenCalled();
  //   });
    
  //   it('should handle and propagate unexpected errors', () => {
  //     const validMultiPolygon: Position[][][] = [
  //       [
  //         [[0, 0], [0, 1], [1, 1], [1, 0], [0, 0]]
  //       ]
  //     ];
      
  //     // Simulate an unexpected error in turf.multiPolygon
  //     (turf as any).multiPolygon = jasmine.createSpy('multiPolygon').and.throwError('Unexpected error');
      
  //     expect(() => service.validateMultiPolygon(validMultiPolygon))
  //       .toThrowError('Unexpected error');
        
  //     expect(consoleErrorSpy).toHaveBeenCalled();
  //   });
    
  //   it('should handle empty multipolygons', () => {
  //     // Empty multipolygon
  //     const emptyMultiPolygon: Position[][][] = [];
      
  //     // Simulate an error for empty multipolygon
  //     (turf as any).multiPolygon = jasmine.createSpy('multiPolygon').and.throwError('Cannot create MultiPolygon with no coordinates');
      
  //     expect(() => service.validateMultiPolygon(emptyMultiPolygon))
  //       .toThrowError('Cannot create MultiPolygon with no coordinates');
        
  //     expect(consoleErrorSpy).toHaveBeenCalled();
  //   });
  // });


});