import L from "leaflet";
import { FiscalYearColors, PlanFiscalStatus } from "src/app/utils/constants";

// Parse latitude/longitude string from various formats
export function parseLatLong(latLong: string): { latitude: number; longitude: number } | null {
  const regexWithDirection = /^(-?\d+(\.\d+)?)°?\s*[N]?,?\s*(-?\d+(\.\d+)?)°?\s*[W]?$/;
  const regexWithoutDirection = /^(-?\d+(\.\d+)?),\s*(-?\d+(\.\d+)?)$/;

  let match = latLong.match(regexWithDirection);
  if (!match) {
    match = latLong.match(regexWithoutDirection);
  }

  if (match) {
    return {
      latitude: parseFloat(match[1]),
      longitude: parseFloat(match[3]),
    };
  }
  return null;
}

// Validate latitude/longitude range and format
export function validateLatLong(latLong: string): { latitude: number; longitude: number } | false {
  const parsed = parseLatLong(latLong);
  if (parsed) {
    const { latitude, longitude } = parsed;

    // Validate latitude and longitude range for BC
    if (latitude >= 48.3 && latitude <= 60 && longitude >= -139 && longitude <= -114) {
      return { latitude, longitude };
    }
  }
  return false;
}

// Format latitude/longitude for display
export function formatLatLong(latitude: number, longitude: number): string {
  return `${latitude.toFixed(4)}° N, ${longitude.toFixed(4)}° W`;
}

export function convertFiscalYear(fiscalYear: number): string {
  return `${fiscalYear}/${(fiscalYear + 1).toString().slice(-2)}`;
}

export class LeafletLegendService {
  addLegend(map: L.Map, fiscalColorMap: Record<'past' | 'present' | 'future', string>): L.Control {
    const legend = new L.Control({ position: 'bottomleft' }) as L.Control;
    legend.onAdd = () => {
      const div = L.DomUtil.create('div', 'legend');
      div.innerHTML = `
        <div class="legend-title">Polygon Colour Legend</div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: #3f3f3f;"></span>
          Gross Project Boundary
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: ${fiscalColorMap.past};"></span>
          Past Fiscal Activities
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: ${fiscalColorMap.present};"></span>
          Present Fiscal Activities
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: ${fiscalColorMap.future};"></span>
          Future Fiscal Activities
        </div>
      `;
      return div;
    };

    legend.addTo(map);
    return legend;
  }

}

export function createFullPageControl(callback: () => void, iconPath: string = 'assets/full-image.svg'): L.Control {
  const fullScreenControl = new L.Control({ position: 'bottomright' });

  fullScreenControl.onAdd = () => {
    const container = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-custom');

    container.innerHTML = `
      <div style="display: flex; align-items: center; gap: 10px;">
        <span style="font-size: 15px; font-family: 'BCSans', 'Noto Sans', Verdana, Arial, sans-serif;">Full Page</span>
        <img src="${iconPath}" alt="Full screen" style="width: 19px; height: 19px;" />
      </div>
    `;

    container.style.backgroundColor = 'white';
    container.style.padding = '2px 6px';
    container.style.cursor = 'pointer';
    container.style.fontSize = '15px';
    container.style.boxShadow = '0px 4px 4px rgba(0, 0, 0, 0.25)';
    container.style.borderRadius = '6px';
    container.style.border = 'none';
    container.style.color = '#000';

    container.onclick = () => {
      callback();
    };

    return container;
  };

  return fullScreenControl;
}

export function trimLatLong(value: number): number {
  if (value == null) return value;

  // Limit to 6 decimals
  const trimmed = Number(value.toFixed(6));

  if (Math.abs(trimmed) >= 1000) {
    throw new Error('Latitude/Longitude value too large');
  }

  return trimmed;
}

export function getFiscalYearDisplay(fiscalYear: number | null | undefined): string | null {
  if (typeof fiscalYear !== 'number') return null;
  const nextYear = (fiscalYear + 1) % 100;
  return `${fiscalYear}/${nextYear.toString().padStart(2, '0')}`;
}

export function getBluePinIcon(): L.Icon {
  return L.icon({
    iconUrl: 'assets/blue-pin-drop.svg',
    iconSize: [30, 50],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
  });
}

export function getActivePinIcon(): L.Icon {
  return L.icon({
    iconUrl: 'assets/active-pin-drop.svg',
    iconSize: [50, 70],
    iconAnchor: [20, 51],
    popupAnchor: [1, -34],
  });
}

export const PlanFiscalStatusIcons: Record<string, { src: string; alt: string; title: string }> = {
  [PlanFiscalStatus.DRAFT]: {
    src: 'assets/draft-icon.svg',
    alt: 'draft',
    title: 'Draft'
  },
  [PlanFiscalStatus.PROPOSED]: {
    src: 'assets/proposed-icon.svg',
    alt: 'proposed',
    title: 'Proposed'
  },
  [PlanFiscalStatus.IN_PROG]: {
    src: 'assets/in-progress-icon-only.svg',
    alt: 'in progress',
    title: 'In Progress'
  },
  [PlanFiscalStatus.COMPLETE]: {
    src: 'assets/complete-icon.svg',
    alt: 'complete',
    title: 'Complete'
  },
  [PlanFiscalStatus.CANCELLED]: {
    src: 'assets/cancelled-icon.svg',
    alt: 'abandoned',
    title: 'Abandoned'
  },
  [PlanFiscalStatus.PREPARED]: {
    src: 'assets/prepared-icon.svg',
    alt: 'prepared',
    title: 'Prepared'
  }
};

export function getFiscalYearColor(fiscalYear: number, currentFiscalYear: number): string {
  if (fiscalYear < currentFiscalYear) return FiscalYearColors.past;
  if (fiscalYear === currentFiscalYear) return FiscalYearColors.present;
  return FiscalYearColors.future;
}


export const LOCAL_ISO_FORMAT = Intl.DateTimeFormat('en-CA', {
  timeZone: undefined,
  hour12: false,
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit'
});

export function getLocalIsoTimestamp(date?: Date): string {
  // Example: 2025-08-08T11:20:30
  const targetDate = date ?? new Date();
  return LOCAL_ISO_FORMAT.format(targetDate)
    .replace(',', '')
    .replace(' ', 'T');
}

export function getUtcIsoTimestamp(date?: Date): string {
  // Example: 2025-08-08T18:20:30.000Z (always UTC)
  return (date ?? new Date()).toISOString();
}