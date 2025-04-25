import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';

describe('ConfirmationDialogComponent', () => {
  let component: ConfirmationDialogComponent;
  let fixture: ComponentFixture<ConfirmationDialogComponent>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<ConfirmationDialogComponent>>;

  beforeEach(() => {
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
  });

  describe('with "confirm-cancel" data', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: mockDialogRef },
          { provide: MAT_DIALOG_DATA, useValue: { indicator: 'confirm-cancel' } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should set dialogUsage to "confirm-cancel" when provided in data', () => {
      expect(component.dialogUsage).toBe('confirm-cancel');
    });

    it('should render the correct title for "confirm-cancel"', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Confirm Cancel');
    });

    it('should display the correct message for "confirm-cancel"', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to cancel?');
      expect(messageElement.textContent.trim()).toContain('This information will not be saved.');
    });
  });

  describe('with "duplicate-project" data', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: mockDialogRef },
          { provide: MAT_DIALOG_DATA, useValue: { indicator: 'duplicate-project' } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set dialogUsage to "duplicate-project" when provided in data', () => {
      expect(component.dialogUsage).toBe('duplicate-project');
    });

    it('should render the correct title for "duplicate-project"', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Duplicate Found');
    });

    it('should display the correct message for "duplicate-project"', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('This Project already exists:');
    });
  });

  describe('common behavior', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: mockDialogRef },
          { provide: MAT_DIALOG_DATA, useValue: { indicator: 'confirm-cancel' } }, // Default case
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should call dialogRef.close(false) when onGoBack is called', () => {
      component.onGoBack();
      expect(mockDialogRef.close).toHaveBeenCalledWith(false);
    });

    it('should call dialogRef.close(true) when onConfirm is called', () => {
      component.onConfirm();
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });

    it('should render Go Back and Confirm buttons', () => {
      const buttons = fixture.nativeElement.querySelectorAll('button');
      expect(buttons.length).toBe(2);
      expect(buttons[0].textContent.trim()).toBe('Cancel');
      expect(buttons[1].textContent.trim()).toBe('Continue');
    });

    it('should call onGoBack when Go Back button is clicked', () => {
      spyOn(component, 'onGoBack');
      const goBackButton = fixture.nativeElement.querySelector('button.secondary');
      goBackButton.click();

      expect(component.onGoBack).toHaveBeenCalled();
    });

    it('should call onConfirm when Confirm button is clicked', () => {
      spyOn(component, 'onConfirm');
      const confirmButton = fixture.nativeElement.querySelector('button.primary');
      confirmButton.click();

      expect(component.onConfirm).toHaveBeenCalled();
    });
  });

  describe('with "confirm-delete" data', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: mockDialogRef },
          { provide: MAT_DIALOG_DATA, useValue: { indicator: 'delete-fiscal-year', name: '2024' } },
        ],
      }).compileComponents();
  
      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });
  
    it('should set dialogUsage to "delete-fiscal-year"', () => {
      expect(component.dialogUsage).toBe('delete-fiscal-year');
    });
  
    it('should render the correct title for "delete-fiscal-year"', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Delete Fiscal Year');
    });
  
    it('should display the correct message for "delete-fiscal-year"', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to delete 2024?');
      expect(messageElement.textContent.trim()).toContain('This action cannot be reversed and will immediately remove the Fiscal Year from the Project scope.');
    });
  });

  describe('with "confirm-unsave" data', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: mockDialogRef },
          { provide: MAT_DIALOG_DATA, useValue: { indicator: 'confirm-unsave' } },
        ],
      }).compileComponents();
  
      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });
  
    it('should set dialogUsage to "confirm-unsave"', () => {
      expect(component.dialogUsage).toBe('confirm-unsave');
    });
  
    it('should render the correct title for "confirm-unsave"', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Confirm Unsave');
    });
  
    it('should display the correct message for "confirm-unsave"', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to leave this page?');
      expect(messageElement.textContent.trim()).toContain('The changes you made will not be saved.');
    });
  });

  describe('with "delete-activity" data', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: mockDialogRef },
          { provide: MAT_DIALOG_DATA, useValue: { indicator: 'delete-activity', name: 'Test Activity' } },
        ],
      }).compileComponents();
  
      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });
  
    it('should set dialogUsage to "delete-activity"', () => {
      expect(component.dialogUsage).toBe('delete-activity');
    });
  
    it('should render the correct title for "delete-activity"', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Delete Activity');
    });
  
    it('should display the correct message for "delete-activity"', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to delete Test Activity?');
      expect(messageElement.textContent.trim()).toContain('This action cannot be reversed and will immediately remove the activity from the Fiscal scope.');
    });
  });
  
  describe('confirmButtonText', () => {
    it('should return "Delete" when dialogUsage starts with "delete-"', () => {
      component.dialogUsage = 'delete-activity';
      expect(component.confirmButtonText).toBe('Delete');
    });
  
    it('should return "Continue" when dialogUsage does not start with "delete-"', () => {
      component.dialogUsage = 'confirm-cancel';
      expect(component.confirmButtonText).toBe('Continue');
    });
  });
  
  describe('isDeleteDialog', () => {
    it('should return true when dialogUsage starts with "delete-"', () => {
      component.dialogUsage = 'delete-fiscal-year';
      expect(component.isDeleteDialog).toBeTrue();
    });
  
    it('should return false when dialogUsage does not start with "delete-"', () => {
      component.dialogUsage = 'confirm-unsave';
      expect(component.isDeleteDialog).toBeFalse();
    });
  });
  
});
