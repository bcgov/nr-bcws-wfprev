import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListComponent } from './list.component';
import { By } from '@angular/platform-browser';

describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should display the title in an <h2> tag', () => {
    const h2Element = fixture.debugElement.query(By.css('h2')).nativeElement;
    expect(h2Element.textContent).toContain('This is the List Component');
  });

  it('should render the list content', () => {
    const paragraphElement = fixture.debugElement.query(By.css('p')).nativeElement;
    expect(paragraphElement.textContent).toContain('List content goes here.');
  });
});
