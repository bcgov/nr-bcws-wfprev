import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';
import { ModalMessages, ModalTitles } from 'src/app/utils/constants';

describe('ConfirmationDialogComponent', () => {
  let component: ConfirmationDialogComponent;
  let fixture: ComponentFixture<ConfirmationDialogComponent>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<ConfirmationDialogComponent>>;

  async function setupComponentWithData(data: {
    indicator: string;
    title?: string;
    message?: string;
    name?: string;
  }) {
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [ConfirmationDialogComponent],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            ...data,
            title: data.title,
            message: data.message,
            name: data.name
          }
        },
        {
          provide: MatDialogRef,
          useValue: mockDialogRef
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  describe('confirm-cancel', () => {
    beforeEach(async () => {
      await setupComponentWithData({
        indicator: 'confirm-cancel',
        title: ModalTitles.CONFIRM_CANCEL_TITLE,
        message: ModalMessages.CONFIRM_CANCEL_MESSAGE
      });
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Confirm Cancel');
    });

    it('should display the correct message for "confirm-cancel', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to cancel?');
      expect(messageElement.textContent.trim()).toContain('This information will not be saved.');
    });
  });

  describe('with "duplicate-project" data', () => {
    beforeEach(async () => {
      await setupComponentWithData({
        indicator: 'duplicate-project',
        message: ModalMessages.DUPLICATE_FOUND_MESSAGE
      });
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
      await setupComponentWithData({
        indicator: 'confirm-cancel',
        title: ModalTitles.CONFIRM_CANCEL_TITLE,
        message: ModalMessages.CONFIRM_UNSAVE_MESSAGE
      });
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
      await setupComponentWithData({
        indicator: 'delete-fiscal-year', name: '2024',
        title: ModalTitles.DELETE_FISCAL_YEAR_TITLE,
        message: `Are you sure you want to delete 2024? This action cannot be reversed and will immediately remove the Fiscal Year from the Project scope.`
      });
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Delete Fiscal Year');
    });

    it('should display the correct message for "delete-fiscal-year', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('Are you sure you want to delete 2024?');
      expect(messageElement.textContent.trim()).toContain('This action cannot be reversed and will immediately remove the Fiscal Year from the Project scope.');
    });
  });

  describe('with "confirm-unsave" data', () => {
    beforeEach(async () => {
      await setupComponentWithData({
        indicator: 'confirm-unsave',
        title: ModalTitles.CONFIRM_UNSAVE_TITLE,
        message: ModalMessages.CONFIRM_UNSAVE_MESSAGE
      });
    });

    it('should show the correct title', () => {
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
      await setupComponentWithData({ indicator: 'delete-activity',
        title: ModalTitles.DELETE_ACTIVITY_TITLE,
        message: `Are you sure you want to delete Test Activity? This action cannot be reversed and will immediately remove the activity from the Fiscal scope.` });
    });

    it('should show the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Delete Activity');
    });

    it('should display the correct message for "delete-activity"', () => {
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

  describe('confirm-fiscal-status-update', () => {
    beforeEach(async () => {
      await setupComponentWithData({
        indicator: 'confirm-fiscal-status-update',
        title: 'Confirm Change to Complete',
        message: 'You are about to change the status of this Fiscal Activity from In Progress to Complete. Do you wish to continue?'
      });
    });

    it('should generate the correct title', () => {
      const titleElement = fixture.nativeElement.querySelector('.title-bar');
      expect(titleElement.textContent.trim()).toBe('Confirm Change to Complete');
    });

    it('should display the correct message for "confirm-fiscal-status-update"', () => {
      const messageElement = fixture.nativeElement.querySelector('.dialog-content p');
      expect(messageElement.textContent.trim()).toContain('You are about to change the status of this Fiscal Activity from In Progress to Complete.');
      expect(messageElement.textContent.trim()).toContain('Do you wish to continue?');
    });
  });
});
