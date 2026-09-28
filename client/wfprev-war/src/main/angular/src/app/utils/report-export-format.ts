import { ProjectFilter } from 'src/app/components/models';
import { WildfireOrgUnitTypeCodes } from 'src/app/utils/constants';

const ALL = '__ALL__';

/** A filter as the download tray shows it. */
export interface FilterSummaryLine {
  label: string;
  values: string;
}

type Lookup = (value: string) => string;

/**
 * Describes a download's filters as one "Label: values" line per filter, in the order people
 * scan for: search, location, fiscal year, then the rest. It is built once, when the download
 * starts, and stored with the job so it doesn't change if code tables change later.
 */
export function buildFilterDescription(filter: ProjectFilter | null | undefined, codeTables: any): string {
  if (!filter) {
    return '';
  }
  const tables = codeTables ?? {};
  const byKey = (items: any[] | undefined, key: string, labelKey: string): Lookup => {
    const map = new Map((items ?? []).map(item => [String(item?.[key]), String(item?.[labelKey])]));
    return value => map.get(value) ?? value;
  };
  const fireCentres = (tables.wildfireOrgUnit ?? []).filter(
    (unit: any) => unit?.wildfireOrgUnitTypeCode?.wildfireOrgUnitTypeCode === WildfireOrgUnitTypeCodes.FIRE_CENTRE
  );

  const lines: FilterSummaryLine[] = [];
  const search = filter.searchText?.trim();
  if (search) {
    lines.push({ label: 'Search', values: search });
  }
  addLine(lines, 'Fire centres', filter.fireCentreOrgUnitIds, byKey(fireCentres, 'orgUnitIdentifier', 'orgUnitName'));
  addLine(lines, 'Forest regions', filter.forestRegionOrgUnitIds, byKey(tables.forestRegions, 'orgUnitId', 'orgUnitName'));
  addLine(lines, 'Forest districts', filter.forestDistrictOrgUnitIds, byKey(tables.forestDistricts, 'orgUnitId', 'orgUnitName'));
  addLine(lines, 'Fiscal years', filter.fiscalYears, fiscalYearLabel);
  addLine(lines, 'Business areas', filter.programAreaGuids, byKey(tables.businessAreas, 'programAreaGuid', 'programAreaName'));
  addLine(lines, 'Fiscal statuses', filter.planFiscalStatusCodes, byKey(tables.planFiscalStatusCode, 'planFiscalStatusCode', 'description'));
  addLine(lines, 'Activity categories', filter.activityCategoryCodes, byKey(tables.activityCategoryCode, 'activityCategoryCode', 'description'));
  addLine(lines, 'Project types', filter.projectTypeCodes, byKey(tables.projectTypeCode, 'projectTypeCode', 'description'));

  return lines.map(line => `${line.label}: ${line.values}`).join('\n');
}

function addLine(lines: FilterSummaryLine[], label: string, values: string[] | undefined, lookup: Lookup): void {
  if (!values?.length) {
    return;
  }
  if (values.includes(ALL)) {
    lines.push({ label, values: 'All' });
    return;
  }
  lines.push({ label, values: values.map(value => lookup(String(value))).join(', ') });
}

function fiscalYearLabel(value: string): string {
  if (value === 'null') {
    return 'No year assigned';
  }
  const year = Number(value);
  if (!Number.isInteger(year)) {
    return value;
  }
  return `FY ${year}/${String(year + 1).slice(-2)}`;
}

/** The stored description split back into its lines, for the tooltip. */
export function filterDescriptionLines(description: string | null | undefined): FilterSummaryLine[] {
  return (description ?? '')
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)
    .map(line => {
      const separator = line.indexOf(': ');
      return separator < 0
        ? { label: '', values: line }
        : { label: line.slice(0, separator), values: line.slice(separator + 2) };
    });
}

/**
 * The one-line summary under each download's time: the values alone, in order, e.g.
 * “fuel break” · Coastal, Northwest · FY 2026/27. The tray shortens it with an ellipsis.
 */
export function filterSummary(description: string | null | undefined): string {
  const parts = filterDescriptionLines(description).map(line => {
    if (line.label === 'Search') {
      return `“${line.values}”`;
    }
    if (line.values === 'All' && line.label) {
      return `All ${line.label.toLowerCase()}`;
    }
    return line.values;
  });
  return parts.length ? parts.join(' · ') : 'All projects';
}

/** "2:14 PM" today, "Yesterday 2:14 PM", otherwise "Sep 22, 2:14 PM" (local time). */
export function formatRequestTime(isoTimestamp: string, now: Date = new Date()): string {
  const requested = new Date(isoTimestamp);
  if (Number.isNaN(requested.getTime())) {
    return '';
  }
  const time = requested.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const startOfYesterday = new Date(startOfToday.getTime());
  startOfYesterday.setDate(startOfYesterday.getDate() - 1);
  if (requested >= startOfToday) {
    return time;
  }
  if (requested >= startOfYesterday) {
    return `Yesterday ${time}`;
  }
  const date = requested.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  return `${date}, ${time}`;
}
