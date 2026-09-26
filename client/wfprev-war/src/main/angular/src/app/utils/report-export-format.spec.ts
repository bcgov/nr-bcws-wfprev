import { WildfireOrgUnitTypeCodes } from 'src/app/utils/constants';
import { buildFilterDescription, filterDescriptionLines, filterSummary, formatRequestTime } from './report-export-format';

describe('report export formatting', () => {
  const codeTables = {
    wildfireOrgUnit: [
      { orgUnitIdentifier: 3, orgUnitName: 'Coastal Fire Centre', wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: WildfireOrgUnitTypeCodes.FIRE_CENTRE } },
      { orgUnitIdentifier: 7, orgUnitName: 'Northwest Fire Centre', wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: WildfireOrgUnitTypeCodes.FIRE_CENTRE } },
      { orgUnitIdentifier: 99, orgUnitName: 'Some Zone', wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: 'ZONE' } }
    ],
    forestRegions: [{ orgUnitId: 101, orgUnitName: 'Coast Area' }],
    forestDistricts: [{ orgUnitId: 201, orgUnitName: 'Chilliwack' }],
    businessAreas: [{ programAreaGuid: 'pa-1', programAreaName: 'Wildfire Prevention' }],
    planFiscalStatusCode: [{ planFiscalStatusCode: 'DRAFT', description: 'Draft' }, { planFiscalStatusCode: 'PROPOSED', description: 'Proposed' }],
    activityCategoryCode: [{ activityCategoryCode: 'FUEL_MGMT', description: 'Fuel Management' }],
    projectTypeCode: [{ projectTypeCode: 'FUEL_MGMT', description: 'Fuel Management' }]
  };

  describe('buildFilterDescription', () => {
    it('describes each filter as a labelled line, search and location first', () => {
      const description = buildFilterDescription({
        projectTypeCodes: ['FUEL_MGMT'],
        fiscalYears: ['2026', '2025'],
        fireCentreOrgUnitIds: ['3', '7'],
        searchText: '  fuel break  ',
        planFiscalStatusCodes: ['DRAFT', 'PROPOSED']
      }, codeTables);

      expect(description.split('\n')).toEqual([
        'Search: fuel break',
        'Fire centres: Coastal Fire Centre, Northwest Fire Centre',
        'Fiscal years: FY 2026/27, FY 2025/26',
        'Fiscal statuses: Draft, Proposed',
        'Project types: Fuel Management'
      ]);
    });

    it('uses names from the code tables for regions, districts, business areas and categories', () => {
      const description = buildFilterDescription({
        forestRegionOrgUnitIds: ['101'],
        forestDistrictOrgUnitIds: ['201'],
        programAreaGuids: ['pa-1'],
        activityCategoryCodes: ['FUEL_MGMT']
      }, codeTables);

      expect(description.split('\n')).toEqual([
        'Forest regions: Coast Area',
        'Forest districts: Chilliwack',
        'Business areas: Wildfire Prevention',
        'Activity categories: Fuel Management'
      ]);
    });

    it('says "All" when everything in a filter is selected', () => {
      expect(buildFilterDescription({ fireCentreOrgUnitIds: ['__ALL__', '3', '7'] }, codeTables)).toBe('Fire centres: All');
    });

    it('describes the "no year" option and keeps values it cannot name', () => {
      expect(buildFilterDescription({ fiscalYears: ['null'], fireCentreOrgUnitIds: ['42'] }, codeTables).split('\n'))
        .toEqual(['Fire centres: 42', 'Fiscal years: No year assigned']);
    });

    it('only names fire centres, not other org units', () => {
      expect(buildFilterDescription({ fireCentreOrgUnitIds: ['99'] }, codeTables)).toBe('Fire centres: 99');
    });

    it('skips empty filters and copes with missing code tables', () => {
      expect(buildFilterDescription({ fiscalYears: [], searchText: '' }, undefined)).toBe('');
      expect(buildFilterDescription(null, codeTables)).toBe('');
    });
  });

  describe('filterSummary', () => {
    it('joins the values on one line and quotes the search', () => {
      expect(filterSummary('Search: fuel break\nFire centres: Coastal Fire Centre\nFiscal years: FY 2026/27'))
        .toBe('“fuel break” · Coastal Fire Centre · FY 2026/27');
    });

    it('spells out "All" with the filter name', () => {
      expect(filterSummary('Fire centres: All\nFiscal years: FY 2026/27')).toBe('All fire centres · FY 2026/27');
    });

    it('says "All projects" when there are no filters', () => {
      expect(filterSummary('')).toBe('All projects');
      expect(filterSummary(null)).toBe('All projects');
    });
  });

  it('filterDescriptionLines splits the stored description back into label and values', () => {
    expect(filterDescriptionLines('Fire centres: Coastal: North\nloose text')).toEqual([
      { label: 'Fire centres', values: 'Coastal: North' },
      { label: '', values: 'loose text' }
    ]);
  });

  describe('formatRequestTime', () => {
    const now = new Date(2026, 8, 24, 16, 30);

    it('shows only the time for today', () => {
      expect(formatRequestTime(new Date(2026, 8, 24, 14, 14).toISOString(), now)).toBe('2:14 PM');
    });

    it('says "Yesterday" for yesterday', () => {
      expect(formatRequestTime(new Date(2026, 8, 23, 13, 30).toISOString(), now)).toBe('Yesterday 1:30 PM');
    });

    it('adds the date for anything older', () => {
      expect(formatRequestTime(new Date(2026, 8, 21, 9, 5).toISOString(), now)).toBe('Sep 21, 9:05 AM');
    });

    it('returns nothing for an unreadable timestamp', () => {
      expect(formatRequestTime('not a date', now)).toBe('');
    });
  });
});
