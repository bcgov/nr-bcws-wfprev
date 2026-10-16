import { FiscalYearColors } from 'src/app/utils/constants';
import { parseLatLong, validateLatLong, formatLatLong, trimLatLong, getFiscalYearDisplay, getFiscalYearColor, LOCAL_ISO_FORMAT, getLocalIsoTimestamp, getUtcIsoTimestamp, isEmpty, formatLocalDate } from './tools';

describe('Latitude/Longitude Utilities', () => {
  describe('parseLatLong', () => {
    it('should parse valid latitude/longitude string with direction', () => {
      const result = parseLatLong('49.2827° N, -123.1207° W');
      expect(result).toEqual({ latitude: 49.2827, longitude: -123.1207 });
    });

    it('should parse valid latitude/longitude string without direction', () => {
      const result = parseLatLong('49.2827, -123.1207');
      expect(result).toEqual({ latitude: 49.2827, longitude: -123.1207 });
    });

    it('should return null for invalid latitude/longitude string', () => {
      const result = parseLatLong('invalid lat/long');
      expect(result).toBeNull();
    });

    it('should handle missing longitude and return null', () => {
      const result = parseLatLong('49.2827° N');
      expect(result).toBeNull();
    });
  });

  describe('validateLatLong', () => {
    it('should validate latitude/longitude within BC range', () => {
      const result = validateLatLong('49.2827, -123.1207');
      expect(result).toEqual({ latitude: 49.2827, longitude: -123.1207 });
    });

    it('should return false for latitude out of range', () => {
      const result = validateLatLong('61.0, -123.1207');
      expect(result).toBeFalse();
    });

    it('should return false for longitude out of range', () => {
      const result = validateLatLong('49.2827, -140.0');
      expect(result).toBeFalse();
    });

    it('should return false for invalid latitude/longitude string', () => {
      const result = validateLatLong('invalid lat/long');
      expect(result).toBeFalse();
    });
  });

  describe('formatLatLong', () => {
    it('should format latitude/longitude to 4 decimal places', () => {
      const result = formatLatLong(49.2827, -123.1207);
      expect(result).toBe('49.2827° N, -123.1207° W');
    });

    it('should handle negative latitude and longitude correctly', () => {
      const result = formatLatLong(-49.2827, -123.1207);
      expect(result).toBe('-49.2827° N, -123.1207° W');
    });

    it('should handle zero values correctly', () => {
      const result = formatLatLong(0, 0);
      expect(result).toBe('0.0000° N, 0.0000° W');
    });

    it('should format small decimal values correctly', () => {
      const result = formatLatLong(49.0001, -123.0001);
      expect(result).toBe('49.0001° N, -123.0001° W');
    });
  });

  describe('trimLatLong', () => {
    it('should trim number to 6 decimal places', () => {
      expect(trimLatLong(49.123456789)).toBe(49.123457);
    });
  
    it('should return null if input is null', () => {
      expect(trimLatLong(null as any)).toBeNull();
    });
  
    it('should throw error if value >= 1000', () => {
      expect(() => trimLatLong(1234.567891)).toThrow();
    });
  
    it('should handle negative numbers correctly', () => {
      expect(trimLatLong(-123.456789)).toBe(-123.456789);
    });
  });

  it ('should display fiscal activity in correct format',() =>{
    expect(getFiscalYearDisplay(2024)).toBe('2024/25');
  })

  
  describe('getFiscalYearColor', () => {
    const currentYear = 2024;

    it('should return past color for fiscal activity less than current year', () => {
      expect(getFiscalYearColor(2022, currentYear)).toBe(FiscalYearColors.past);
    });

    it('should return present color for fiscal activity equal to current year', () => {
      expect(getFiscalYearColor(2024, currentYear)).toBe(FiscalYearColors.present);
    });

    it('should return future color for fiscal activity greater than current year', () => {
      expect(getFiscalYearColor(2025, currentYear)).toBe(FiscalYearColors.future);
    });
  });

    
  describe('LOCAL_ISO_FORMAT and getLocalIsoTimestamp', () => {

    it('should format a given date in the expected en-CA local ISO format', () => {
      const date = new Date('2025-08-08T11:20:30Z');
      const formatted = LOCAL_ISO_FORMAT.format(date);
      expect(formatted).toMatch(/^\d{4}-\d{2}-\d{2}, \d{2}:\d{2}:\d{2}$/);
    });

    it('should return a timestamp string with "T" separator for given date', () => {
      const date = new Date('2025-08-08T11:20:30Z');
      const ts = getLocalIsoTimestamp(date);
      expect(ts).toContain('T');
      expect(ts).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/);
    });

    it('should default to current date/time when no date is provided', () => {
      const ts = getLocalIsoTimestamp();
      expect(typeof ts).toBe('string');
      expect(ts).toMatch(/T\d{2}:\d{2}:\d{2}$/);
    });
  });

  describe('getUtcIsoTimestamp', () => {
    it('should return an ISO UTC string ending with Z', () => {
      const date = new Date('2025-08-08T18:20:30Z');
      const ts = getUtcIsoTimestamp(date);
      expect(ts).toBe('2025-08-08T18:20:30.000Z');
    });
  });

  describe('isEmpty', () => {
    it('should return true for null or undefined', () => {
      expect(isEmpty(null)).toBeTrue();
      expect(isEmpty(undefined)).toBeTrue();
    });

    it('should return true for an empty string', () => {
      expect(isEmpty('')).toBeTrue();
    });

    it('should return false for 0 or valid numbers', () => {
      expect(isEmpty(0)).toBeFalse();
      expect(isEmpty(123)).toBeFalse();
    });

    it('should return false for valid strings or objects', () => {
      expect(isEmpty('hello')).toBeFalse();
      expect(isEmpty({})).toBeFalse();
      expect(isEmpty([])).toBeFalse();
    });
  });

  describe('formatLocalDate', () => {
    it('should confirm the helper builds the date from local date parts, so a time after 17:00 local gives the same day', () => {
      // 18:30:00 (after 17:00) on October 16, 2026 local time
      const dateAfter17 = new Date(2026, 9, 16, 18, 30, 0);
      expect(formatLocalDate(dateAfter17)).toBe('2026-10-16');

      // 23:59:59 local time
      const lateEvening = new Date(2026, 9, 16, 23, 59, 59);
      expect(formatLocalDate(lateEvening)).toBe('2026-10-16');

      // In timezones behind UTC (such as Pacific Time UTC-7 / UTC-8), toISOString() yields next day (2026-10-17),
      // whereas formatLocalDate preserves the device's local calendar day
      if (dateAfter17.getTimezoneOffset() > 0) {
        expect(dateAfter17.toISOString().split('T')[0]).toBe('2026-10-17');
      }
    });

    it('should format a yyyy-MM-dd string and not be off by one day', () => {
      expect(formatLocalDate('2025-05-01')).toBe('2025-05-01');
    });

    it('should format a full ISO timestamp and not be off by one day', () => {
      expect(formatLocalDate('2025-05-01T00:00:00.000+00:00')).toBe('2025-05-01');
      expect(formatLocalDate('2025-05-01T00:00:00.000Z')).toBe('2025-05-01');
    });

    it('should default to current local date when no date is provided', () => {
      const today = new Date();
      const expected = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
      expect(formatLocalDate()).toBe(expected);
    });

    it('should return empty string for empty string input', () => {
      expect(formatLocalDate('')).toBe('');
    });
  });
});

