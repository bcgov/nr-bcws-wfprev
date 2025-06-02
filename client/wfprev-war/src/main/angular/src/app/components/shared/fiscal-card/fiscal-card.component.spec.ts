import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FiscalCardComponent } from './fiscal-card.component';
import { By } from '@angular/platform-browser';

describe('FiscalCardComponent', () => {
  let component: FiscalCardComponent;
  let fixture: ComponentFixture<FiscalCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FiscalCardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FiscalCardComponent);
    component = fixture.componentInstance;

    component.fiscal = {
      fiscalYear: 2024,
      plannedAmount: 10000,
      statusCode: 'APPROVED'
    };

    component.getDescription = (key: string, value: any) => `${key}-${value}`;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('formatValue', () => {
    it('should format integer value without decimals', () => {
      expect(component.formatValue(1000)).toBe('1,000');
    });

    it('should format float value with two decimals', () => {
      expect(component.formatValue(1234.56)).toBe('1,234.56');
    });

    it('should return empty string for null/undefined', () => {
      expect(component.formatValue(undefined)).toBe('');
    });

    it('should add suffix if provided', () => {
      expect(component.formatValue(1000, ' kg')).toBe('1,000 kg');
    });
  });

  describe('formatCurrency', () => {
    it('should format number as currency', () => {
      expect(component.formatCurrency(5000)).toBe('$5,000');
    });

    it('should return empty string if value is undefined', () => {
      expect(component.formatCurrency(undefined)).toBe('');
    });
  });

  describe('getStatusIcon', () => {
    it('should return icon for a known status code', () => {
      const icon = component.getStatusIcon('COMPLETE');
      expect(icon).toBeTruthy();
    });

    it('should return undefined for unknown status code', () => {
      const icon = component.getStatusIcon('UNKNOWN_CODE');
      expect(icon).toBeUndefined();
    });
  });
});
