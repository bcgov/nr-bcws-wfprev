// Parse latitude/longitude string from various formats
export function parseLatLong(latLong: string): { latitude: number; longitude: number } | null {
  const regexWithDirection = /^(-?\d{1,3}(?:\.\d{1,8})?)°?\s*[N]?,?\s+(-?\d{1,3}(?:\.\d{1,8})?)°?\s*[W]?$/;
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