import { Injectable } from "@angular/core";
import * as toGeoJSON from '@tmcw/togeojson';
import { DOMParser } from '@xmldom/xmldom';
import JSZip from 'jszip';
import * as shp from 'shpjs';
import { Geometry, Position } from 'geojson';

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
        const zip = new JSZip();
        const content = await zip.loadAsync(kmzFile);

        const kmlFile = Object.keys(content.files).find(file => file.endsWith('.kml'));
        if (!kmlFile) throw new Error('No KML file found in KMZ');

        const kmlString = await zip.file(kmlFile)?.async('text');
        if (!kmlString) throw new Error('Failed to extract KML content');

        const coordinates = this.parseKMLToCoordinates(kmlString);
        console.log(coordinates);
        return coordinates;
    }

    async extractSHPCoordinates(shpFile: File): Promise<CoordinateTypes[]> {
        const arrayBuffer = await shpFile.arrayBuffer();
        const geoJson = await shp.default(arrayBuffer);

        const featureCollections = Array.isArray(geoJson) ? geoJson : [geoJson];

        const coordinates = featureCollections.flatMap(fc => 
            fc.features.map(feature => this.getCoordinatesFromGeometry(feature.geometry))
        ).filter(Boolean) as CoordinateTypes[];

        console.log(coordinates);
        return coordinates;
    }

    // Extracts coordinates from a geometry object
    private getCoordinatesFromGeometry(geometry: Geometry | null | undefined): CoordinateTypes | null {
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
