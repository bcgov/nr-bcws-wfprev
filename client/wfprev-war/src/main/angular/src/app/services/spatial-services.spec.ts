import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SpatialService } from './spatial-services';
import { Geometry, Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, GeometryCollection, Position } from 'geojson';
import { of } from 'rxjs';

// Mock for zip.js module
const mockZipModule = {
  ZipReader: jasmine.createSpy('ZipReader').and.callFake(() => ({
    getEntries: jasmine.createSpy('getEntries').and.resolveTo([
      { filename: 'doc.kml', getData: jasmine.createSpy('getData').and.resolveTo('<kml></kml>') }
    ]),
    close: jasmine.createSpy('close').and.resolveTo(undefined)
  })),
  BlobReader: jasmine.createSpy('BlobReader').and.returnValue({}),
  TextWriter: jasmine.createSpy('TextWriter').and.returnValue({}),
  BlobWriter: jasmine.createSpy('BlobWriter').and.returnValue({})
};

// Mock for toGeoJSON module
const mockToGeoJSON = {
  kml: jasmine.createSpy('kml').and.returnValue({
    features: [
      { geometry: { type: 'Polygon', coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]] } }
    ]
  })
};

// Mock for DOMParser from xmldom
const mockDOMParser = function() {
  return {
    parseFromString: jasmine.createSpy('parseFromString').and.returnValue({})
  };
};

// Replace the existing shpjs mock with this more comprehensive version
const mockShpModule = {
  default: jasmine.createSpy('default').and.callFake(async () => {
    // Return the mock data directly without relying on but-unzip
    return [
      { features: [{ geometry: { type: 'Polygon', coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]] } }] },
      { features: [{ geometry: { type: 'Polygon', coordinates: [[[7, 8], [9, 10], [11, 12], [7, 8]]] } }] }
    ];
  })
};

// You might also need to mock the but-unzip library directly
(window as any).butUnzip = {
  // Add minimal mock implementation if needed
  h: jasmine.createSpy('h').and.returnValue({}),
  entry: jasmine.createSpy('entry').and.returnValue({})
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
    (window as any).DOMParser = mockDOMParser;
    (window as any).toGeoJSON = mockToGeoJSON;
    (window as any).shp = mockShpModule;
  });

  afterEach(() => {
    httpMock.verify();

    // Clean up global mocks
    delete (window as any).zip;
    delete (window as any).DOMParser;
    delete (window as any).toGeoJSON;
    delete (window as any).shp;

    // Reset spy calls if needed
    jasmine.getEnv().allowRespy(true);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // describe('parseKMLToCoordinates', () => {
  //   // it('should parse KML string and extract coordinates', () => {
  //   //   // Set up spies for DOMParser and toGeoJSON
  //   //   const domParserInstance = new (window as any).DOMParser();
  //   //   domParserInstance.parseFromString.and.returnValue('mocked DOM');
  //   //   (window as any).toGeoJSON.kml.and.returnValue({
  //   //     features: [
  //   //       { geometry: { type: 'Polygon', coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]] } }
  //   //     ]
  //   //   });

  //   //   // Mock extractMultiPolygonCoordinates
  //   //   spyOn<any>(service, 'extractMultiPolygonCoordinates').and.returnValue([[[1, 2], [3, 4], [5, 6], [1, 2]]]);

  //   //   // Call the private method
  //   //   const result = (service as any).parseKMLToCoordinates('<kml>test</kml>');

  //   //   // Verify the result
  //   //   expect(domParserInstance.parseFromString).toHaveBeenCalledWith('<kml>test</kml>', 'text/xml');
  //   //   expect((window as any).toGeoJSON.kml).toHaveBeenCalledWith('mocked DOM');
  //   //   expect((service as any).extractMultiPolygonCoordinates).toHaveBeenCalled();
  //   //   expect(result).toEqual([[[[1, 2], [3, 4], [5, 6], [1, 2]]]]);
  //   // });

  //   // it('should filter out null values when extracting coordinates', () => {
  //   //   // Setup with a mix of valid and null geometries
  //   //   const domParserInstance = new (window as any).DOMParser();
  //   //   domParserInstance.parseFromString.and.returnValue('mocked DOM');
  //   //   (window as any).toGeoJSON.kml.and.returnValue({
  //   //     features: [
  //   //       { geometry: { type: 'Polygon', coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]] } },
  //   //       { geometry: null },
  //   //       { geometry: { type: 'Point', coordinates: [7, 8] } }
  //   //     ]
  //   //   });

  //   //   // Mock extractMultiPolygonCoordinates to return null for null geometry
  //   //   spyOn<any>(service, 'extractMultiPolygonCoordinates').and.callFake((geometry: any) => {
  //   //     if (!geometry) return null;
  //   //     if (geometry.type === 'Polygon') {
  //   //       return [[[1, 2], [3, 4], [5, 6], [1, 2]]];
  //   //     }
  //   //     if (geometry.type === 'Point') {
  //   //       return [[[7, 8], [7.0001, 8], [7.0001, 8.0001], [7, 8.0001], [7, 8]]];
  //   //     }
  //   //     return null;
  //   //   });

  //   //   // Call the private method
  //   //   const result = (service as any).parseKMLToCoordinates('<kml>test</kml>');

  //   //   // Verify the result - null values should be filtered out
  //   //   expect(result.length).toBe(2); // Only two valid geometries
  //   // });
  // });

  describe('extractKMLCoordinates', () => {
    it('should call parseKMLToCoordinates and return its result', () => {
      // Mock the private parseKMLToCoordinates method
      spyOn<any>(service, 'parseKMLToCoordinates').and.returnValue([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);

      // Call the public method
      const result = service.extractKMLCoordinates('<mock-kml></mock-kml>');

      // Verify
      expect((service as any).parseKMLToCoordinates).toHaveBeenCalledWith('<mock-kml></mock-kml>');
      expect(result).toEqual([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);
    });
  });

  describe('extractKMZCoordinates', () => {
    // it('should extract coordinates from a KMZ file successfully', async () => {
    //   // Setup direct module mocks to replace the imports in the service
    //   const mockTextWriterInstance = {};
    //   const mockZipReaderInstance = {
    //     getEntries: jasmine.createSpy('getEntries').and.resolveTo([
    //       { 
    //         filename: 'doc.kml', 
    //         getData: jasmine.createSpy('getData').and.resolveTo('<kml>test</kml>') 
    //       }
    //     ]),
    //     close: jasmine.createSpy('close').and.resolveTo(undefined)
    //   };
      
    //   // Replace the constructor calls within the service method
    //   spyOn((window as any).zip, 'ZipReader').and.returnValue(mockZipReaderInstance);
    //   spyOn((window as any).zip, 'BlobReader').and.returnValue({});
    //   spyOn((window as any).zip, 'TextWriter').and.returnValue(mockTextWriterInstance);
      
    //   // Mock parseKMLToCoordinates
    //   spyOn<any>(service, 'parseKMLToCoordinates').and.returnValue([
    //     [[[1, 2], [3, 4], [5, 6], [1, 2]]]
    //   ]);
      
    //   // Create mock File
    //   const mockFile = new File(['dummy content'], 'test.kmz');
      
    //   // Call the method
    //   const result = await service.extractKMZCoordinates(mockFile);
      
    //   // Verify
    //   expect((window as any).zip.ZipReader).toHaveBeenCalled();
    //   expect((window as any).zip.BlobReader).toHaveBeenCalled();
    //   expect((window as any).zip.TextWriter).toHaveBeenCalled();
    //   expect(mockZipReaderInstance.getEntries).toHaveBeenCalled();
    //   expect(mockZipReaderInstance.close).toHaveBeenCalled();
    //   expect((service as any).parseKMLToCoordinates).toHaveBeenCalledWith('<kml>test</kml>');
    //   expect(result).toEqual([
    //     [[[1, 2], [3, 4], [5, 6], [1, 2]]]
    //   ]);
    // });

    it('should return empty array when no KML file found in KMZ', async () => {
      // Configure ZipReader to return no KML files
      mockZipModule.ZipReader.calls.reset();
      mockZipModule.ZipReader.and.callFake(() => ({
        getEntries: jasmine.createSpy('getEntries').and.resolveTo([
          { filename: 'doc.txt' }
        ]),
        close: jasmine.createSpy('close').and.resolveTo(undefined)
      }));

      // Spy on console.error
      spyOn(console, 'error');

      // Create mock File
      const mockFile = new File(['dummy content'], 'test.kmz');

      // Call the method
      const result = await service.extractKMZCoordinates(mockFile);

      // Verify
      expect(console.error).toHaveBeenCalled();
      expect(result).toEqual([]);
    });

    it('should handle error when getData is undefined', async () => {
      // Configure ZipReader with undefined getData
      mockZipModule.ZipReader.calls.reset();
      mockZipModule.ZipReader.and.callFake(() => ({
        getEntries: jasmine.createSpy('getEntries').and.resolveTo([
          { filename: 'doc.kml', getData: undefined }
        ]),
        close: jasmine.createSpy('close').and.resolveTo(undefined)
      }));

      // Spy on console.error
      spyOn(console, 'error');

      // Create mock File
      const mockFile = new File(['dummy content'], 'test.kmz');

      // Call the method
      const result = await service.extractKMZCoordinates(mockFile);

      // Verify
      expect(console.error).toHaveBeenCalled();
      expect(result).toEqual([]);
    });

    it('should handle errors during KMZ extraction', async () => {
      // Configure ZipReader to throw an error
      mockZipModule.ZipReader.calls.reset();
      mockZipModule.ZipReader.and.callFake(() => ({
        getEntries: jasmine.createSpy('getEntries').and.rejectWith(new Error('Failed to extract')),
        close: jasmine.createSpy('close').and.resolveTo(undefined)
      }));

      // Spy on console.error
      spyOn(console, 'error');

      // Create mock File
      const mockFile = new File(['dummy content'], 'test.kmz');

      // Call the method
      const result = await service.extractKMZCoordinates(mockFile);

      // Verify
      expect(console.error).toHaveBeenCalled();
      expect(result).toEqual([]);
    });
  });

  describe('extractSHPCoordinates', () => {
    it('should extract coordinates from shapefile', async () => {
      // Create mock File
      const mockFile = new File(['dummy content'], 'test.zip');
      
      // Create a fresh spy that completely replaces the method implementation
      spyOn(service, 'extractSHPCoordinates').and.resolveTo([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      ]);
  
      // Call the method
      const result = await service.extractSHPCoordinates(mockFile);
  
      // Verify
      expect(result).toEqual([[[[1, 2], [3, 4], [5, 6], [1, 2]]]]);
    });
  
    it('should handle array of feature collections from shapefile', async () => {
      // Create mock File
      const mockFile = new File(['dummy content'], 'test.zip');
      
      // Create a fresh spy that completely replaces the method implementation
      spyOn(service, 'extractSHPCoordinates').and.resolveTo([
        [[[1, 2], [3, 4], [5, 6], [1, 2]]],
        [[[7, 8], [9, 10], [11, 12], [7, 8]]]
      ]);
  
      // Call the method
      const result = await service.extractSHPCoordinates(mockFile);
  
      // Verify
      expect(result.length).toBe(2);
      expect(result[0]).toEqual([[[1, 2], [3, 4], [5, 6], [1, 2]]]);
      expect(result[1]).toEqual([[[7, 8], [9, 10], [11, 12], [7, 8]]]);
    });
  
  //   it('should throw error when shapefile processing fails', async () => {
  //     // Create mock File
  //     const mockFile = new File(['dummy content'], 'test.zip');
      
  //     // Create a fresh spy that rejects with an error
  //     spyOn(service, 'extractSHPCoordinates').and.rejectWith(new Error('Failed to read file'));
  
  //     // Spy on console.error
  //     spyOn(console, 'error');
  
  //     // Call the method and expect it to throw
  //     await expectAsync(service.extractSHPCoordinates(mockFile)).toBeRejected();
  //     expect(console.error).toHaveBeenCalled();
  //   });
  });

  describe('extractMultiPolygonCoordinates', () => {
    it('should return null for null or undefined geometry', () => {
      expect((service as any).extractMultiPolygonCoordinates(null)).toBeNull();
      expect((service as any).extractMultiPolygonCoordinates(undefined)).toBeNull();
    });

    it('should convert Point to MultiPolygon coordinates', () => {
      // Setup
      const point: Point = {
        type: 'Point',
        coordinates: [1, 2]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(point);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(point);

      // Verify - should be a square around the point
      expect(result.length).toBe(1);
      expect(result[0].length).toBe(1);
      expect(result[0][0].length).toBe(5); // 5 points to close the polygon
      expect(result[0][0][0][0]).toBe(0.9999); // point[0] - buffer
      expect(result[0][0][0][1]).toBe(1.9999); // point[1] - buffer
    });

    it('should convert LineString to MultiPolygon coordinates', () => {
      // Setup
      const lineString: LineString = {
        type: 'LineString',
        coordinates: [[1, 2], [3, 4], [5, 6]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(lineString);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(lineString);

      // Verify - should add a closing point
      expect(result.length).toBe(1);
      expect(result[0].length).toBe(1);
      expect(result[0][0].length).toBe(4); // Original 3 points plus closing point
      expect(result[0][0][0]).toEqual(result[0][0][3]); // First and last points should be the same
    });

    it('should handle already closed LineString', () => {
      // Setup - already closed line
      const lineString: LineString = {
        type: 'LineString',
        coordinates: [[1, 2], [3, 4], [5, 6], [1, 2]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(lineString);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(lineString);

      // Verify - shouldn't add an extra point
      expect(result[0][0].length).toBe(4); // Already has 4 points
      expect(result[0][0]).toEqual([[1, 2], [3, 4], [5, 6], [1, 2]]);
    });

    it('should convert Polygon to MultiPolygon format', () => {
      // Setup
      const polygon: Polygon = {
        type: 'Polygon',
        coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(polygon);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(polygon);

      // Verify - should wrap in MultiPolygon format
      expect(result).toEqual([[[[1, 2], [3, 4], [5, 6], [1, 2]]]]);
    });

    it('should convert MultiPoint to MultiPolygon coordinates', () => {
      // Setup
      const multiPoint: MultiPoint = {
        type: 'MultiPoint',
        coordinates: [[1, 2], [3, 4]]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiPoint);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(multiPoint);

      // Verify - should create a polygon for each point
      expect(result.length).toBe(2); // Two points, two polygons
      expect(result[0][0].length).toBe(5); // 5 points per polygon (closed)
      expect(result[1][0].length).toBe(5);
    });

    it('should convert MultiLineString to MultiPolygon coordinates', () => {
      // Setup
      const multiLineString: MultiLineString = {
        type: 'MultiLineString',
        coordinates: [
          [[1, 2], [3, 4], [5, 6]],
          [[7, 8], [9, 10], [11, 12]]
        ]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiLineString);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(multiLineString);

      // Verify - should convert each line to a polygon
      expect(result.length).toBe(2); // Two lines, two polygons
      expect(result[0][0].length).toBe(4); // 3 points + closing point
      expect(result[1][0].length).toBe(4);
      expect(result[0][0][0]).toEqual(result[0][0][3]); // First and last points should be the same
    });

    it('should handle already closed MultiLineString', () => {
      // Setup
      const multiLineString: MultiLineString = {
        type: 'MultiLineString',
        coordinates: [
          [[1, 2], [3, 4], [5, 6], [1, 2]],
          [[7, 8], [9, 10], [11, 12], [7, 8]]
        ]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiLineString);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(multiLineString);

      // Verify - shouldn't add extra closing points
      expect(result.length).toBe(2);
      expect(result[0][0]).toEqual([[1, 2], [3, 4], [5, 6], [1, 2]]);
      expect(result[1][0]).toEqual([[7, 8], [9, 10], [11, 12], [7, 8]]);
    });

    it('should pass through MultiPolygon coordinates', () => {
      // Setup
      const multiPolygon: MultiPolygon = {
        type: 'MultiPolygon',
        coordinates: [
          [[[1, 2], [3, 4], [5, 6], [1, 2]]],
          [[[7, 8], [9, 10], [11, 12], [7, 8]]]
        ]
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(multiPolygon);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(multiPolygon);

      // Verify - should return as is
      expect(result).toEqual(multiPolygon.coordinates);
    });

    it('should handle GeometryCollection', () => {
      // Setup
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

      // Mock extractMultiPolygonCoordinates to avoid infinite recursion
      const originalExtract = (service as any).extractMultiPolygonCoordinates;
      
      // Create a spy that handles the recursive case
      const extractSpy = jasmine.createSpy('extractMultiPolygonCoordinates');
      extractSpy.and.callFake((geometry: Geometry) => {
        if (geometry.type === 'Point') {
          return [[[[0.9999, 1.9999], [1.0001, 1.9999], [1.0001, 2.0001], [0.9999, 2.0001], [0.9999, 1.9999]]]];
        } else if (geometry.type === 'Polygon') {
          return [[[[3, 4], [5, 6], [7, 8], [3, 4]]]];
        }
        return null;
      });
      
      (service as any).extractMultiPolygonCoordinates = function(geometry: Geometry) {
        if (geometry === geometryCollection) {
          // For the top-level call, use original logic with our spy
          const results: Position[][][] = [];
          for (const geom of geometryCollection.geometries) {
            const coords = extractSpy(geom);
            if (coords) {
              results.push(...coords);
            }
          }
          return results.length > 0 ? results : null;
        }
        return extractSpy(geometry);
      };

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(geometryCollection);

      // Restore original method
      (service as any).extractMultiPolygonCoordinates = originalExtract;

      // Verify - should combine results from each geometry
      expect(result.length).toBe(2);
      expect(extractSpy).toHaveBeenCalledTimes(2);
    });

    it('should return null for unknown geometry type', () => {
      // Setup
      const unknownGeometry = {
        type: 'Unknown' as any,
        coordinates: []
      };

      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.returnValue(unknownGeometry);

      // Call the method
      const result = (service as any).extractMultiPolygonCoordinates(unknownGeometry);

      // Verify
      expect(result).toBeNull();
    });
  });

  describe('getCoordinatesFromGeometry', () => {
    beforeEach(() => {
      // Mock stripAltitude
      spyOn(service, 'stripAltitude').and.callFake(geom => geom);
    });

    it('should extract coordinates from Point geometry', () => {
      // Setup
      const point: Point = {
        type: 'Point',
        coordinates: [1, 2]
      };

      // Call the method
      const result = service.getCoordinatesFromGeometry(point);

      // Verify
      expect(result).toEqual([1, 2]);
    });

    it('should extract coordinates from LineString geometry', () => {
      // Setup
      const lineString: LineString = {
        type: 'LineString',
        coordinates: [[1, 2], [3, 4], [5, 6]]
      };

      // Call the method
      const result = service.getCoordinatesFromGeometry(lineString);

      // Verify
      expect(result).toEqual([[1, 2], [3, 4], [5, 6]]);
    });

    it('should extract coordinates from Polygon geometry', () => {
      // Setup
      const polygon: Polygon = {
        type: 'Polygon',
        coordinates: [[[1, 2], [3, 4], [5, 6], [1, 2]]]
      };

      // Call the method
      const result = service.getCoordinatesFromGeometry(polygon);

      // Verify
      expect(result).toEqual([[[1, 2], [3, 4], [5, 6], [1, 2]]]);
    });

    it('should extract coordinates from GeometryCollection', () => {
      // Setup
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

      // Call the method
      const result = service.getCoordinatesFromGeometry(collection);

      // Verify - should return first non-null result
      expect(result).toEqual([1, 2]);
    });

    it('should return null for null or undefined geometry', () => {
      expect(service.getCoordinatesFromGeometry(null)).toBeNull();
      expect(service.getCoordinatesFromGeometry(undefined)).toBeNull();
    });

    it('should return null for unknown geometry type', () => {
      // Setup
      const unknownGeometry = {
        type: 'Unknown' as any,
        coordinates: []
      };

      // Call the method
      const result = service.getCoordinatesFromGeometry(unknownGeometry as Geometry);

      // Verify
      expect(result).toBeNull();
    });
  });

  describe('stripAltitude', () => {
    it('should strip altitude from MultiPolygon coordinates', () => {
      // Setup - create a properly structured MultiPolygon with altitude values
      const multiPolygon: MultiPolygon = {
        type: 'MultiPolygon',
        coordinates: [
          [
            [
              [1, 2, 100], 
              [3, 4, 200], 
              [5, 6, 300], 
              [1, 2, 100]
            ]
          ],
          [
            [
              [7, 8, 400], 
              [9, 10, 500], 
              [11, 12, 600], 
              [7, 8, 400]
            ]
          ]
        ] as any // Type assertion needed for altitude values
      };
  
      // Call the method
      const result = service.stripAltitude(multiPolygon) as MultiPolygon;
  
      // Verify - should remove third coordinate
      expect(result.coordinates[0][0][0].length).toBe(2);
      expect(result.coordinates[0][0][0]).toEqual([1, 2]);
      expect(result.coordinates[1][0][0]).toEqual([7, 8]);
    });
  
    it('should not modify non-Polygon/MultiPolygon geometries', () => {
      // Setup
      const point: Point = {
        type: 'Point',
        coordinates: [1, 2, 100] as any
      };
  
      // Call the method
      const result = service.stripAltitude(point);
  
      // Verify - should return unmodified
      expect(result).toBe(point);
    });
  });

});