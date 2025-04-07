import { Injectable } from "@angular/core";
import * as toGeoJSON from '@tmcw/togeojson';
import * as shp from 'shpjs';
import { DOMParser } from '@xmldom/xmldom';
import { Geometry, Position } from 'geojson';
import { ZipReader, BlobReader, TextWriter, BlobWriter } from '@zip.js/zip.js';
import { catchError, lastValueFrom, map, Observable, of, throwError } from "rxjs";
import { HttpClient } from "@angular/common/http";
import * as turf from '@turf/turf';
type CoordinateTypes = Position | Position[] | Position[][] | Position[][][];

@Injectable({
    providedIn: 'root',
})
export class SpatialService {

    constructor(private readonly httpClient: HttpClient) { }

    BC_BOUNDARY = turf.polygon([
        [
            [-139.05, 60.0],
            [-139.05, 48.3],
            [-114.05, 48.3],
            [-114.05, 60.0],
            [-139.05, 60.0]
        ]
    ]);

    private parseKMLToCoordinates(kmlString: string): Position[][][] {
        const kmlDom = new DOMParser().parseFromString(kmlString, 'text/xml');
        const geoJson = toGeoJSON.kml(kmlDom);

        // Process each feature individually to maintain correct nesting
        let allCoords: Position[][][] = [];

        geoJson.features.forEach(feature => {
            const coords = this.extractMultiPolygonCoordinates(feature.geometry);
            if (coords) {
                // Check if we need to flatten or add nesting based on the structure
                if (Array.isArray(coords[0]) && Array.isArray(coords[0][0]) &&
                    Array.isArray(coords[0][0][0]) && coords[0][0][0].length === 2) {
                    // This is already properly nested for MultiPolygon [[[x,y], [x,y]]]
                    allCoords = allCoords.concat(coords);
                } else {
                    // Add proper nesting if needed
                    console.error("Unexpected coordinate structure:", coords);
                    // Handle special cases if needed
                }
            }
        });

        this.validateMultiPolygon(allCoords)

        return allCoords;
    }

    extractKMLCoordinates(kmlString: string): Position[][][] {
        const coordinates = this.parseKMLToCoordinates(kmlString);
        return coordinates;
    }

    async extractSHPCoordinates(shpZipFile: File): Promise<Position[][][]> {
        try {
            // Read the zip file as an ArrayBuffer
            const arrayBuffer = await shpZipFile.arrayBuffer();

            // Process the shapefile, which can handle zip files directly
            const geoJson = await shp.default(arrayBuffer);

            // Normalize to always work with an array of feature collections
            const featureCollections = Array.isArray(geoJson) ? geoJson : [geoJson];

            // Process each feature individually to maintain correct nesting
            let allCoords: Position[][][] = [];

            for (const fc of featureCollections) {
                for (const feature of fc.features) {
                    const coords = this.extractMultiPolygonCoordinates(feature.geometry);
                    if (coords) {
                        // Check if we need to flatten or add nesting based on the structure
                        if (Array.isArray(coords[0]) && Array.isArray(coords[0][0]) &&
                            Array.isArray(coords[0][0][0]) && coords[0][0][0].length === 2) {
                            // This is already properly nested for MultiPolygon [[[x,y], [x,y]]]
                            allCoords = allCoords.concat(coords);
                        } else {
                            // Add proper nesting if needed
                            console.error("Unexpected coordinate structure in SHP:", coords);
                            // Handle special cases if needed
                        }
                    }
                }
            }

            this.validateMultiPolygon(allCoords);
            return allCoords;
        } catch (error) {
            console.error('Error extracting coordinates from shapefile:', error);
            throw error;
        }
    }

    // Convert any geometry to MultiPolygon coordinates
    private extractMultiPolygonCoordinates(geometry: Geometry | null | undefined): Position[][][] | null {
        if (!geometry) return null;

        // Strip altitude if present
        geometry = this.stripAltitude(geometry);

        switch (geometry.type) {
            case 'Point': {
                // Convert Point to small polygon coordinates
                const point = geometry.coordinates;
                const buffer = 0.0001; // Small buffer around point
                return [[
                    [
                        [point[0] - buffer, point[1] - buffer],
                        [point[0] + buffer, point[1] - buffer],
                        [point[0] + buffer, point[1] + buffer],
                        [point[0] - buffer, point[1] + buffer],
                        [point[0] - buffer, point[1] - buffer]
                    ]
                ]];
            }

            case 'LineString': {
                // Create a simple polygon from the line
                const line = geometry.coordinates;
                // Close the line if not already closed
                const closedLine = line[0][0] === line[line.length - 1][0] &&
                    line[0][1] === line[line.length - 1][1]
                    ? line : [...line, line[0]];
                return [[closedLine]];
            }
            case 'Polygon':
                // Check if the Polygon already has excessive nesting
                const polygonCoords = geometry.coordinates;

                // Check if we're dealing with an over-nested structure
                if (polygonCoords.length > 0 &&
                    Array.isArray(polygonCoords[0]) &&
                    Array.isArray(polygonCoords[0][0]) &&
                    Array.isArray(polygonCoords[0][0][0])) {
                    return polygonCoords as unknown as Position[][][];
                }

                // Normal case
                return [polygonCoords];

            case 'MultiPoint': {
                // Convert each point to a small polygon
                return geometry.coordinates.map(point => {
                    const buffer = 0.0001;
                    return [[
                        [point[0] - buffer, point[1] - buffer],
                        [point[0] + buffer, point[1] - buffer],
                        [point[0] + buffer, point[1] + buffer],
                        [point[0] - buffer, point[1] + buffer],
                        [point[0] - buffer, point[1] - buffer]
                    ]];
                });
            }

            case 'MultiLineString': {
                // Convert each line to a polygon
                return geometry.coordinates.map(line => {
                    // Close the line if not already closed
                    const closedLine = line[0][0] === line[line.length - 1][0] &&
                        line[0][1] === line[line.length - 1][1]
                        ? line : [...line, line[0]];
                    return [closedLine];
                });
            }

            case 'MultiPolygon':
                // Already in the right format
                return geometry.coordinates;

            case 'GeometryCollection': {
                // Process each geometry and combine
                const coordinates = geometry.geometries
                    .map(geom => this.extractMultiPolygonCoordinates(geom))
                    .filter(Boolean) as Position[][][][];

                return coordinates.flat();
            }

            default:
                return null;
        }
    }

    // Helper method for backward compatibility
    public getCoordinatesFromGeometry(geometry: Geometry | null | undefined): CoordinateTypes | null {
        if (!geometry) {
            return null;
        } else geometry = this.stripAltitude(geometry);

        switch (geometry.type) {
            case 'Point':
            case 'LineString':
            case 'Polygon':
            case 'MultiPoint':
            case 'MultiLineString':
            case 'MultiPolygon':
                return geometry.coordinates;
            case 'GeometryCollection':
                return geometry.geometries
                    .map(geom => this.getCoordinatesFromGeometry(geom))
                    .find(coords => coords !== null) || null;
            default:
                return null;
        }
    }

    stripAltitude(geometry: Geometry): Geometry {
        const strip = (coord: any): any => {
            if (Array.isArray(coord)) {
                if (typeof coord[0] === 'number') {
                    // [lng, lat, alt?] → [lng, lat]
                    return coord.slice(0, 2);
                } else {
                    return coord.map(strip);
                }
            }
            return coord;
        };

        switch (geometry.type) {
            case 'Point':
            case 'MultiPoint':
            case 'LineString':
            case 'MultiLineString':
            case 'Polygon':
            case 'MultiPolygon':
                return {
                    type: geometry.type,
                    coordinates: strip(geometry.coordinates),
                    ...(geometry.bbox ? { bbox: geometry.bbox } : {})
                };

            case 'GeometryCollection':
                return {
                    type: 'GeometryCollection',
                    geometries: geometry.geometries.map(g => this.stripAltitude(g)),
                    ...(geometry.bbox ? { bbox: geometry.bbox } : {})
                };

            default:
                return geometry;
        }
    }

    extractGDBGeometry(file: File): Observable<any> {
        // this will be replaced with node js server in AWS - WFPREV-402
        const url = 'http://localhost:3000/upload';

        const formData = new FormData();
        formData.append('file', file);

        return this.httpClient.post<any>(url, formData).pipe(
            map((response) => {
                return response.map((geom: any) => {
                    return this.stripAltitude(geom);
                });
            }),
            catchError((error) => {
                console.error("Error extracting geodatabase geometry", error);
                return throwError(() => new Error("Failed to extract geodatabase geometry"));
            })
        );
    }

    public async extractCoordinates(file: File): Promise<number[][]> {
        const fileType = file.name.split('.').pop()?.toLowerCase();
        if (!fileType) return [];

        try {
            if (fileType === 'kml') {
                return this.extractKMLCoordinates(await file.text()) as unknown as number[][];
            }

            if (['zip', 'gdb'].includes(fileType)) {
                return await this.handleCompressedFile(file);
            }

            console.error('Unsupported file type:', fileType);
            return [];
        } catch (error) {
            console.error('Error extracting coordinates:', error);
            return [];
        }
    }

    public async handleCompressedFile(file: File): Promise<number[][]> {
        const zipReader = new ZipReader(new BlobReader(file));
        try {
            const entries = await zipReader.getEntries();

            if (this.hasSHPEntry(entries)) {
                return await this.extractSHPCoordinates(file) as unknown as number[][];
            }

            if (this.hasGDBEntries(entries)) {
                return await this.handleGDB(file);
            }

            console.error('No supported spatial files found in ZIP or GDB.');
            return [];
        } finally {
            await zipReader.close();
        }
    }

    private hasSHPEntry(entries: any[]): boolean {
        return entries.some(entry => entry.filename.endsWith('.shp')) &&
            entries.some(entry => entry.filename.endsWith('.dbf'));
    }

    private hasGDBEntries(entries: any[]): boolean {
        return entries.some(entry => entry.filename.includes('.gdbtable'));
    }

    private async handleGDB(file: File): Promise<number[][]> {
        try {
            const geojsonData = await lastValueFrom(
                this.extractGDBGeometry(file).pipe(
                    catchError((error: any) => {
                        console.error("Error extracting GeoJSON:", error);
                        return of(null);
                    })
                )
            );
            this.validateMultiPolygon(geojsonData?.[0])
            return geojsonData?.[0]?.coordinates || [];
        } catch (error) {
            console.error("Error processing Geodatabase file:", error);
            return [];
        }
    }

    validateMultiPolygon(coordinates: Position[][][] | GeoJSON.MultiPolygon): void {
        console.log('Input received: ', JSON.stringify(coordinates));

        try {
            let multiPolygonCoords: Position[][][];

            // Check if input is already a GeoJSON object or just coordinates
            if (typeof coordinates === 'object' && 'type' in coordinates && coordinates.type === 'MultiPolygon') {
                // Extract just the coordinates array from the GeoJSON object
                multiPolygonCoords = coordinates.coordinates;
            } else {
                // Use coordinates as-is if they're already a coordinate array
                multiPolygonCoords = coordinates as Position[][][];
            }

            // Now create a proper MultiPolygon feature
            const multiPolygon = turf.multiPolygon(multiPolygonCoords);

            // Check overall validity
            const isValid = turf.booleanValid(multiPolygon);
            

            // Process each polygon individually for kinks
            let selfIntersections = [];

            // Loop through each polygon in the multiPolygon
            for (const polygonCoords of multiPolygonCoords) {
                try {
                    // Create a single polygon feature
                    const singlePolygon = turf.polygon(polygonCoords);

                    // Now use kinks() on the single polygon
                    const kinks = turf.kinks(singlePolygon);

                    // If we found intersections, add them to our results
                    if (kinks.features.length > 0) {
                        selfIntersections.push(...kinks.features);
                    }
                } catch (error) {
                    console.error('Error processing individual polygon:', error);
                    throw new Error('Invalid polygon in multipolygon');
                }
            }

            // Handle any found intersections
            if (selfIntersections.length > 0) {
                console.error('Self-intersections found at points:',
                    selfIntersections.map(f => f.geometry.coordinates)
                );

                throw new Error(`Found ${selfIntersections.length} self-intersections in the multipolygon`);
            }

            if (!isValid) {
                throw new Error('Invalid geometry: Other topology errors detected in multipolygon');
            }

            console.log('MultiPolygon validation passed - no self-intersections detected');

        } catch (error) {
            console.error('Validation error:', error);
            throw error;
        }
    }
}