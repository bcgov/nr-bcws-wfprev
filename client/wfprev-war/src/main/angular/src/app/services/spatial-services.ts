import { Injectable } from "@angular/core";
import * as toGeoJSON from '@tmcw/togeojson';
import * as shp from 'shpjs';
import { DOMParser } from '@xmldom/xmldom';
import { Geometry, Position } from 'geojson';
import { ZipReader, BlobReader, TextWriter } from '@zip.js/zip.js';
type CoordinateTypes = Position | Position[] | Position[][] | Position[][][];

@Injectable({
    providedIn: 'root',
})
export class SpatialService {

    private parseKMLToCoordinates(kmlString: string): Position[][][] {
        const kmlDom = new DOMParser().parseFromString(kmlString, 'text/xml');
        const geoJson = toGeoJSON.kml(kmlDom);
      
        return geoJson.features
            .map(feature => this.extractMultiPolygonCoordinates(feature.geometry))
            .filter(Boolean) as unknown as Position[][][];
    }
      
    extractKMLCoordinates(kmlString: string): Position[][][] {
        const coordinates = this.parseKMLToCoordinates(kmlString);
        console.log('Extracted coordinates from KML:', coordinates);
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
            console.log('Extracted coordinates from KMZ:', coordinates);
            
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
        
            console.log(`Extracted ${coordinates.length} coordinate sets from shapefile`);
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

    stripAltitude(geometry: Geometry) {
        if (geometry.type === "Polygon" || geometry.type === "MultiPolygon") {
            geometry.coordinates = geometry.coordinates.map((ring: any) =>
                ring.map((coords: any) => coords.map(([lng, lat]: number[]) => [lng, lat]))
            );
        }
        return geometry;
    }
}