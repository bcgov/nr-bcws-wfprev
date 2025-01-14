import { parseLatLong, validateLatLong, formatLatLong } from './tools';

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
});
