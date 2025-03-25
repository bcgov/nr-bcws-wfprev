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

    private parseKMLToCoordinates(kmlString: string): CoordinateTypes[] {
        const kmlDom = new DOMParser().parseFromString(kmlString, 'text/xml');
        const geoJson = toGeoJSON.kml(kmlDom);
      
        return geoJson.features
          .map(feature => this.getCoordinatesFromGeometry(feature.geometry))
          .filter((coord): coord is CoordinateTypes => coord !== null);
      }
      

    extractKMLCoordinates(kmlString: string): CoordinateTypes[] {
        const coordinates = this.parseKMLToCoordinates(kmlString);
        console.log(coordinates);
        return coordinates;
    }

    async extractKMZCoordinates(kmzFile: File): Promise<CoordinateTypes[]> {
        const zipReader = new ZipReader(new BlobReader(kmzFile));
        
        try {
            const entries = await zipReader.getEntries();
            const kmlEntry = entries.find(entry => entry.filename.endsWith('.kml'));
            
            if (!kmlEntry) throw new Error('No KML file found in KMZ');
            
            const kmlString = await kmlEntry.getData?.(new TextWriter());
            
            if (!kmlString) throw new Error('Failed to extract KML content');
            
            const coordinates = this.parseKMLToCoordinates(kmlString);
            console.log(coordinates);
            
            return coordinates;
        } catch (error) {
            console.error('Error extracting KMZ coordinates:', error);
            return [];
        } finally {
            // Ensure the zip reader is closed
            await zipReader.close();
        }
    }

    async extractSHPCoordinates(shpZipFile: File): Promise<CoordinateTypes[]> {
        try {
          // Read the zip file as an ArrayBuffer
          const arrayBuffer = await shpZipFile.arrayBuffer();
      
          // Process the shapefile, which can handle zip files directly
          const geoJson = await shp.default(arrayBuffer);
      
          // Normalize to always work with an array of feature collections
          const featureCollections = Array.isArray(geoJson) ? geoJson : [geoJson];
      
          // Extract coordinates from each feature
          const coordinates = featureCollections.flatMap(fc => 
            fc.features.map((feature: { geometry: Geometry | null | undefined; }) => this.getCoordinatesFromGeometry(feature.geometry))
          ).filter(Boolean) as CoordinateTypes[];
      
          console.log(`Extracted ${coordinates.length} coordinates`);
          console.log(coordinates)
          return coordinates;
        } catch (error) {
          console.error('Error extracting coordinates from shapefile:', error);
          throw error;
        }
    }


// Helper method to extract coordinates from feature geometry
public extractGeometryCoordinates(feature: any): number[][] {
    try {
        if (!feature.geometry) {
            console.warn('No geometry found in feature');
            return [];
        }

        switch (feature.geometry.type) {
            case 'Point':
                return [[feature.geometry.coordinates[0], feature.geometry.coordinates[1]]];

            case 'LineString':
                return feature.geometry.coordinates;

            case 'Polygon':
                return feature.geometry.coordinates[0];

            case 'MultiPoint':
                return feature.geometry.coordinates;

            case 'MultiLineString':
                return feature.geometry.coordinates.flat();

            case 'MultiPolygon':
                return feature.geometry.coordinates[0][0];

            default:
                console.warn(`Unsupported geometry type: ${feature.geometry.type}`);
                return [];
        }
    } catch (error) {
        console.error('Error extracting coordinates from feature:', error);
        return [];
    }
}
    // Extracts coordinates from a geometry object
    public getCoordinatesFromGeometry(geometry: Geometry | null | undefined): CoordinateTypes | null {
        if (!geometry) return null;

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
}
