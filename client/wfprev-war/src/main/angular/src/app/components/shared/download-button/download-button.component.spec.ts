import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DownloadButtonComponent } from './download-button.component';

describe('DownloadButtonComponent', () => {
  let component: DownloadButtonComponent;
  let fixture: ComponentFixture<DownloadButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DownloadButtonComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadButtonComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return disabled state correctly', () => {
    component.disabled = true;
    expect(component.isDisabled()).toBeTrue();

    component.disabled = false;
    expect(component.isDisabled()).toBeFalse();
  });

  it('should emit download event when onDownload is called with csv', () => {
    spyOn(component.download, 'emit');

    component.onDownload('fiscal-csv');
    expect(component.download.emit).toHaveBeenCalledWith('fiscal-csv');
  });

  it('should emit download event when onDownload is called with excel', () => {
    spyOn(component.download, 'emit');

    component.onDownload('fiscal-xlsx');
    expect(component.download.emit).toHaveBeenCalledWith('fiscal-xlsx');
  });

  it('should render a menu item label and value for each format', () => {
    component.formats = [
      { label: 'Fiscal (CSV)', value: 'fiscal-csv' },
      { label: 'RESULTS (XLSX + Spatial)', value: 'results-xlsx' }
    ];
    fixture.detectChanges();

    fixture.debugElement.query(By.css('.download-button')).nativeElement.click();
    fixture.detectChanges();

    const items = Array.from(document.querySelectorAll('.mat-mdc-menu-item'));
    expect(items.map(i => i.textContent?.trim())).toEqual(['Fiscal (CSV)', 'RESULTS (XLSX + Spatial)']);
  });

  it('should disable the button when disabled input is true', () => {
    component.disabled = true;
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('button')).nativeElement;
    expect(button.disabled).toBeTrue();
  });

  it('should enable the button when disabled input is false', () => {
    component.disabled = false;
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('button')).nativeElement;
    expect(button.disabled).toBeFalse();
  });
});
