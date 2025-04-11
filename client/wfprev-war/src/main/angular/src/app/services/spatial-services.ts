import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import * as toGeoJSON from '@tmcw/togeojson';
import * as turf from '@turf/turf';
import { DOMParser } from '@xmldom/xmldom';
import { BlobReader, TextWriter, ZipReader } from '@zip.js/zip.js';
import { Geometry, Position } from 'geojson';
import { catchError, lastValueFrom, map, Observable, of, throwError } from "rxjs";
import * as shp from 'shpjs';
export type CoordinateTypes = Position | Position[] | Position[][] | Position[][][];

@Injectable({
    providedIn: 'root',
})
export class SpatialService {

    constructor(private readonly httpClient: HttpClient,
        private readonly snackbarService: MatSnackBar
    ) { }

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
                    console.error("Unexpected coordinate structure:", coords);
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

    public async extractCoordinates(file: File): Promise<Position[][][]> {
        const fileType = file.name.split('.').pop()?.toLowerCase();
        if (!fileType) return [];
    
        try {
            if (fileType === 'kml') {
                return this.extractKMLCoordinates(await file.text());
            } else if (fileType === 'kmz') {
                return await this.handleKMZFile(file);
            } else if (['zip', 'gdb'].includes(fileType)) {
                return await this.handleCompressedFile(file);
            } else {
                this.snackbarService.open('Unsupported file type: ' + fileType, 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-error',
                });
                return [];
            }
        } catch (error) {
            console.error('Error extracting coordinates:', error);
            this.snackbarService.open('Error extracting coordinates.', 'Close', {
                duration: 5000,
                panelClass: 'snackbar-error',
            });
            return [];
        }
    }

    public async handleCompressedFile(file: File): Promise<Position[][][]> {
        const zipReader = new ZipReader(new BlobReader(file));
        try {
            const entries = await zipReader.getEntries();

            if (this.hasSHPEntry(entries)) {
                return await this.extractSHPCoordinates(file);
            } else if (this.hasGDBEntries(entries)) {
                return await this.handleGDB(file);
            } else if (this.hasKMZEntry(entries)) {
                return await this.handleKMZ(entries);
            } else {
                this.snackbarService.open('Geometry is invalid.', 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-error',
                });
                return [];
            }
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

    private hasKMZEntry(entries: any[]): boolean {
        return entries.some(entry => entry.filename.toLowerCase().endsWith('.kml'));
    }
    
    private async handleGDB(file: File): Promise<Position[][][]> {
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
    
    public async handleKMZFile(file: File): Promise<Position[][][]> {
        try {
            const zipReader = new ZipReader(new BlobReader(file));
            const entries = await zipReader.getEntries();
            
            // Look for any KML file in the archive
            const kmlEntry = entries.find(entry => 
                entry.filename.toLowerCase().endsWith('.kml'));
                
            if (!kmlEntry) {
                await zipReader.close();
                throw new Error('No KML file found inside KMZ archive');
            }
            
            // Get the KML content and process it
            const kmlText = await kmlEntry?.getData?.(new TextWriter());
            await zipReader.close();

            if(kmlText) {
                return this.extractKMLCoordinates(kmlText);
            } else return[];
            
        } catch (error) {
            console.error("Error processing KMZ file:", error);
            return[];
        }
    }

    private async handleKMZ(entries: any[]): Promise<Position[][][]> {
        try {
            const kmlEntry = entries.find(entry => 
                entry.filename.toLowerCase().endsWith('.kml'));
                
            if (!kmlEntry) {
                console.error('No KML file found inside KMZ archive');
                return [];
            }
            
            try {
                const kmlText = await kmlEntry.getData(new TextWriter()) as string;
                return this.extractKMLCoordinates(kmlText);
            } catch (dataError) {
                console.error('Failed to extract KML data:', dataError);
                return [];
            }
        } catch (error) {
            console.error("Error processing KMZ file:", error);
            return [];
        }
    }

    validateMultiPolygon(coordinates: Position[][][] | GeoJSON.MultiPolygon): void {
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
                this.snackbarService.open(`Found ${selfIntersections.length} self-intersections in the uploaded geometry.`, 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-error',
                });
                throw new Error('Self-intersections found in the uploaded geometry.');
            }

            const britishColumbiaBoundary = this.getBritishColumbiaGeoJSON();
            const isInBC = turf.booleanIntersects(multiPolygon, britishColumbiaBoundary);

            if (!isInBC) {
                this.snackbarService.open('Geometry is outside of BC.', 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-error',
                });
                throw new Error('Geometry is outside of BC.');
            }

            if (!isValid) {
                this.snackbarService.open('Geometry is invalid.', 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-error',
                });
                throw new Error('Geometry is invalid.');
            }

        } catch (error) {
            console.error('Validation error:', error);
            throw error;
        }
    }

    getBritishColumbiaGeoJSON(): GeoJSON.Polygon {
        return {
            type: "Polygon",
            coordinates: [[
                [-139.05, 60.00],
                [-114.05, 60.00],
                [-114.05, 48.30],
                [-139.05, 48.30],
                [-139.05, 60.00]
            ]]
        };
    }

}