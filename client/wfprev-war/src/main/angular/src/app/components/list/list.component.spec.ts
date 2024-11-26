import { ComponentFixture, TestBed } from '@angular/core/testing';
import {ActivatedRoute, Router} from '@angular/router';

import { ListComponent } from './list.component';
import { By } from '@angular/platform-browser';
import {of, BehaviorSubject} from "rxjs";
import {ResourcesRoutes} from "../../utils";

describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let queryParamsSubject: BehaviorSubject<any>;

  beforeEach(async () => {
    queryParamsSubject = new BehaviorSubject({});
    await TestBed.configureTestingModule({
      imports: [ListComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),  // mock params as an Observable
            queryParams: queryParamsSubject  // mock queryParams as an Observable
          }
        }
      ]
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

  it('should filter projects when query parameter is provided', () => {
    const queryParams = { projectNumber: '1' };
    queryParamsSubject.next(queryParams);
    fixture.detectChanges();
    expect(component.filteredProjects.length).toBe(1);
    expect(component.filteredProjects[0].projectNumber).toBe(1);
  });

  it('should show all projects when no query parameter is provided', () => {
    queryParamsSubject.next({});  // Use queryParamsSubject directly here too
    fixture.detectChanges();
    expect(component.filteredProjects.length).toBe(component.projects.length);
  });

  it('should navigate to list with query params when viewProjectDetails is called', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = spyOn(router, 'navigate');
    const project = component.projects[0];

    component.viewProjectDetails(project);

    expect(navigateSpy).toHaveBeenCalledWith([ResourcesRoutes.LIST], {
      queryParams: { projectNumber: project.projectNumber }
    });
  });
});
