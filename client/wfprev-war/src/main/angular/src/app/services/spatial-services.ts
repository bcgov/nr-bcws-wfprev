import { Injectable } from "@angular/core";
import * as toGeoJSON from '@tmcw/togeojson';
import * as shp from 'shpjs';
import { DOMParser } from '@xmldom/xmldom';
import { Geometry, Position } from 'geojson';
import { ZipReader, BlobReader, TextWriter, BlobWriter } from '@zip.js/zip.js';
import { catchError, lastValueFrom, map, Observable, of, throwError } from "rxjs";
import { HttpClient } from "@angular/common/http";
type CoordinateTypes = Position | Position[] | Position[][] | Position[][][];

@Injectable({
    providedIn: 'root',
})
export class SpatialService {

     constructor(private readonly httpClient: HttpClient){}

    private parseKMLToCoordinates(kmlString: string): Position[][][] {
        const kmlDom = new DOMParser().parseFromString(kmlString, 'text/xml');
        const geoJson = toGeoJSON.kml(kmlDom);
      
        return geoJson.features
            .map(feature => this.extractMultiPolygonCoordinates(feature.geometry))
            .filter(Boolean) as unknown as Position[][][];
    }
      
    extractKMLCoordinates(kmlString: string): Position[][][] {
        const coordinates = this.parseKMLToCoordinates(kmlString);
        return coordinates;
    }

    async extractKMZCoordinates(kmzFile: File): Promise<Position[][][]> {
        const zipReader = new ZipReader(new BlobReader(kmzFile));
        
        try {
            const entries = await zipReader.getEntries();
            const kmlEntry = entries.find(entry => entry.filename.endsWith('.kml'));
            
            if (!kmlEntry) throw new Error('No KML file found in KMZ');
            
            const kmlString = await kmlEntry.getData?.(new TextWriter());
            
            if (!kmlString) throw new Error('Failed to extract KML content');
            
            const coordinates = this.parseKMLToCoordinates(kmlString);
            
            return coordinates;
        } catch (error) {
            console.error('Error extracting KMZ coordinates:', error);
            return [];
        } finally {
            // Ensure the zip reader is closed
            await zipReader.close();
        }
    }

    async extractSHPCoordinates(shpZipFile: File): Promise<Position[][][]> {
        try {
            // Read the zip file as an ArrayBuffer
            const arrayBuffer = await shpZipFile.arrayBuffer();
        
            // Process the shapefile, which can handle zip files directly
            const geoJson = await shp.default(arrayBuffer);
        
            // Normalize to always work with an array of feature collections
            const featureCollections = Array.isArray(geoJson) ? geoJson : [geoJson];
        
            // Extract MultiPolygon coordinates from each feature
            const coordinates = featureCollections.flatMap(fc => fc.features
                .map(feature => this.extractMultiPolygonCoordinates(feature.geometry))
                .filter(Boolean)
            ) as unknown as Position[][][];
        
            return coordinates;
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
                const closedLine = line[0][0] === line[line.length-1][0] && 
                                line[0][1] === line[line.length-1][1] 
                                ? line : [...line, line[0]];
                return [[closedLine]];
            }
                
            case 'Polygon':
                // Convert to MultiPolygon format
                return [geometry.coordinates];
                
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
                    const closedLine = line[0][0] === line[line.length-1][0] && 
                                    line[0][1] === line[line.length-1][1] 
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
        if (geometry.type === "Polygon" || geometry.type === "MultiPolygon") {
            geometry.coordinates = geometry.coordinates.map((ring: any) =>
                ring.map((coords: any) => 
                    coords.map(([lng, lat]: number[]) => [lng, lat]) // Remove altitude
                )
            );
        }
        return geometry;
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
    
            if (['zip', 'gdb', 'kmz'].includes(fileType)) {
                return await this.handleCompressedFile(file);
            }
    
            console.error('Unsupported file type:', fileType);
            return [];
        } catch (error) {
            console.error('Error extracting coordinates:', error);
            return [];
        }
    }
    
    private async handleCompressedFile(file: File): Promise<number[][]> {
        const zipReader = new ZipReader(new BlobReader(file));
        try {
            const entries = await zipReader.getEntries();
    
            if (this.hasKMLEntry(entries)) {
                return await this.handleKMZ(entries);
            }
    
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
    
    private hasKMLEntry(entries: any[]): boolean {
        return entries.some(entry => entry.filename.endsWith('.kml'));
    }
    
    private hasSHPEntry(entries: any[]): boolean {
        return entries.some(entry => entry.filename.endsWith('.shp')) &&
               entries.some(entry => entry.filename.endsWith('.dbf'));
    }
    
    private hasGDBEntries(entries: any[]): boolean {
        return entries.some(entry => entry.filename.includes('.gdbtable'));
    }
    
    private async handleKMZ(entries: any[]): Promise<number[][]> {
        const kmlEntry = entries.find(entry => entry.filename.endsWith('.kml'));
        const kmlBlob = await kmlEntry?.getData?.(new BlobWriter());
        if (kmlBlob) {
            const kmlFile = new File([kmlBlob], 'extracted.kml');
            return await this.extractKMZCoordinates(kmlFile) as unknown as number[][];
        }
        return [];
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
            return geojsonData?.[0]?.coordinates || [];
        } catch (error) {
            console.error("Error processing Geodatabase file:", error);
            return [];
        }
    }
}