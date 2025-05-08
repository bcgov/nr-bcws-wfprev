import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';

describe('ConfirmationDialogComponent', () => {
  let component: ConfirmationDialogComponent;
  let fixture: ComponentFixture<ConfirmationDialogComponent>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<ConfirmationDialogComponent>>;

  const setupComponentWithData = async (data: any) => {
    TestBed.resetTestingModule();
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [ConfirmationDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  describe('confirm-cancel', () => {
    beforeEach(async () => {
      await setupComponentWithData({ indicator: 'confirm-cancel' });
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Confirm Cancel');
    });

    it('should show the correct message', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to cancel?');
      expect(messageElement.textContent.trim()).toContain('This information will not be saved.');
    });
  });

  describe('duplicate-project', () => {
    beforeEach(async () => {
      await setupComponentWithData({ indicator: 'duplicate-project' });
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Duplicate Found');
    });

    it('should show the correct message', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('This Project already exists:');
    });
  });

  describe('onGoBack and onConfirm', () => {
    beforeEach(async () => {
      await setupComponentWithData({ indicator: 'confirm-cancel' });
    });

    it('should call dialogRef.close(false) when onGoBack is called', () => {
      component.onGoBack();
      expect(mockDialogRef.close).toHaveBeenCalledWith(false);
    });

    it('should call dialogRef.close(true) when onConfirm is called', () => {
      component.onConfirm();
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });
  });

  describe('confirmButtonText', () => {
    it('should return "Delete" if dialogUsage starts with "delete-"', async () => {
      await setupComponentWithData({ indicator: 'delete-activity' });
      expect(component.confirmButtonText).toBe('Delete');
    });

    it('should return "Continue" if dialogUsage does not start with "delete-"', async () => {
      await setupComponentWithData({ indicator: 'confirm-cancel' });
      expect(component.confirmButtonText).toBe('Continue');
    });
  });

  describe('isDeleteDialog', () => {
    it('should return true if dialogUsage starts with "delete-"', async () => {
      await setupComponentWithData({ indicator: 'delete-fiscal-year' });
      expect(component.isDeleteDialog).toBeTrue();
    });

    it('should return false if dialogUsage does not start with "delete-"', async () => {
      await setupComponentWithData({ indicator: 'confirm-cancel' });
      expect(component.isDeleteDialog).toBeFalse();
    });
  });

  describe('delete-fiscal-year', () => {
    beforeEach(async () => {
      await setupComponentWithData({ indicator: 'delete-fiscal-year', name: '2024' });
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Delete Fiscal Year');
    });

    it('should show the correct message', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to delete 2024?');
      expect(messageElement.textContent.trim()).toContain('This action cannot be reversed and will immediately remove the Fiscal Year from the Project scope.');
    });
  });

  describe('confirm-unsave', () => {
    beforeEach(async () => {
      await setupComponentWithData({ indicator: 'confirm-unsave' });
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Confirm Unsave');
    });

    it('should show the correct message', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to leave this page?');
      expect(messageElement.textContent.trim()).toContain('The changes you made will not be saved.');
    });
  });

  describe('delete-activity', () => {
    beforeEach(async () => {
      await setupComponentWithData({ indicator: 'delete-activity', name: 'Test Activity' });
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Delete Activity');
    });

    it('should show the correct message', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to delete Test Activity?');
      expect(messageElement.textContent.trim()).toContain('This action cannot be reversed and will immediately remove the activity from the Fiscal scope.');
    });
  });

  describe('button clicks', () => {
    beforeEach(async () => {
      await setupComponentWithData({ indicator: 'confirm-cancel' });
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
});
