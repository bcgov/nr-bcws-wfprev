import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnDestroy, Output, QueryList, SimpleChanges, ViewChild, ViewChildren } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule, MatExpansionPanel } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import moment from 'moment';
import { finalize, forkJoin, map, Observable, Subscription, take, tap } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { ProjectFilesComponent } from 'src/app/components/edit-project/project-details/project-files/project-files.component';
import { ActivityModel, FiscalActivityViewModel } from 'src/app/components/models';
import { IconButtonComponent } from 'src/app/components/shared/icon-button/icon-button.component';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';
import { TimestampComponent } from 'src/app/components/shared/timestamp/timestamp.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { CanComponentDeactivate } from 'src/app/services/util/can-deactive.guard';
import { Messages, ModalMessages, ModalTitles, NumericLimits } from 'src/app/utils/constants';
import { getUtcIsoTimestamp } from 'src/app/utils/tools';
import { ExpansionIndicatorComponent } from "../../shared/expansion-indicator/expansion-indicator.component";
import { NgxCurrency } from '@dintecom/ngx-currency';
import { StatusBadgeComponent } from '../../shared/status-badge/status-badge.component';


import { FiscalActivityItemComponent } from './fiscal-activity-item/fiscal-activity-item.component';



export const CUSTOM_DATE_FORMATS = {
  display: {
    dateInput: 'YYYY/MM/DD',
    monthYearLabel: 'YYYY MMM',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'YYYY MMMM',
  },
  parse: { dateInput: 'YYYY/MM/DD' },
};

@Component({
  selector: 'wfprev-activities',
  standalone: true,
  imports: [
    MatExpansionModule,
    ReactiveFormsModule,
    CommonModule,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    FormsModule,
    MatCheckboxModule,
    ProjectFilesComponent,
    ExpansionIndicatorComponent,
    IconButtonComponent,
    TimestampComponent,
    TextareaComponent,
    NgxCurrency,
    StatusBadgeComponent,
    MatProgressSpinnerModule,
    FiscalActivityItemComponent
  ],
  templateUrl: './activities.component.html',
  styleUrl: './activities.component.scss',
  providers: [
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: 'en-CA' }, // Change locale to Canada (YYYY/MM/DD format)
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] }
  ],
})

export class ActivitiesComponent implements OnChanges, OnDestroy, CanComponentDeactivate {
  @Input() fiscalGuid: string = '';
  @Input() isReadonly: boolean = false;
  @Output() boundariesUpdated = new EventEmitter<void>();
  @ViewChild('activitiesPanel') activitiesPanel?: MatExpansionPanel;
  @ViewChildren(ProjectFilesComponent) private readonly attachmentFiles!: QueryList<ProjectFilesComponent>;
  messages = Messages;
  isNewActivityBeingAdded = false;

  projectGuid = '';
  activityViews: FiscalActivityViewModel[] = [];

  // Deprecated properties/methods kept for unit test compatibility
  activities: any[] = [];
  activityForms: any[] = [];
  isActivityDirty: boolean[] = [];
  isActivitySaving: boolean[] = [];
  originalActivitiesValues: any[] = [];
  expandedPanels: boolean[] = [];
  getControl(index: number, controlName: string): any {}

  contractPhaseCode: any[] = [];
  fundingSourceCode: any[] = [];
  silvicultureBaseCode: any[] = [];
  silvicultureTechniqueCode: any[] = [];
  silvicultureMethodCode: any[] = [];

  filteredTechniqueCode: any[] = [];
  filteredMethodCode: any[] = [];

  projectTypeCode = '';
  isLoading = true;
  private dataSubscription?: Subscription;
  constructor(
    private readonly route: ActivatedRoute,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly fb: FormBuilder,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public cd: ChangeDetectorRef
  ) { }

  ngOnDestroy(): void {
    this.dataSubscription?.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscalGuid'] && changes['fiscalGuid'].currentValue) {
      this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
      if (this.projectGuid) {
        if (this.dataSubscription) {
          this.dataSubscription.unsubscribe();
        }
        this.dataSubscription = new Subscription();

        this.isLoading = true;
        this.activityViews = [];
        
        const sub = this.loadCodeTables().subscribe({
          next: () => {
            this.getActivities(() => {
              this.activityViews.forEach((view) => {
                if (view.form.get('isResultsReportableInd')?.value) {
                  this.toggleResultsReportableInd(view);
                }
              });
            });
          },

          error: (err) => {
            console.error('Error loading code tables', err);
            this.getActivities();
          }
        });
        this.dataSubscription?.add(sub);
      } else {
        this.isLoading = false;
      }
    } else if (changes['fiscalGuid']) {
      this.isLoading = false;
    }
  }


  loadCodeTables(): Observable<void> {
    return forkJoin([
      this.codeTableService.getContractPhaseCodes(),
      this.codeTableService.getFundingSourceCodes(),
      this.codeTableService.fetchCodeTable('silvicultureBaseCodes').pipe(
        map(data => [...(data?._embedded?.silvicultureBaseCode ?? [])]
          .sort((a: any, b: any) => (a.description ?? '').localeCompare(b.description ?? '')))
      ),
      this.codeTableService.fetchCodeTable('silvicultureTechniqueCodes').pipe(
        map(data => [...(data?._embedded?.silvicultureTechniqueCode ?? [])]
          .sort((a: any, b: any) => (a.description ?? '').localeCompare(b.description ?? '')))
      ),
      this.codeTableService.fetchCodeTable('silvicultureMethodCodes').pipe(
        map(data => [...(data?._embedded?.silvicultureMethodCode ?? [])]
          .sort((a: any, b: any) => (a.description ?? '').localeCompare(b.description ?? '')))
      ),
    ]).pipe(
      take(1),
      tap(([contract, funding, base, technique, method]) => {
        this.contractPhaseCode = contract;
        this.fundingSourceCode = funding;
        this.silvicultureBaseCode = base;
        this.silvicultureTechniqueCode = technique;
        this.silvicultureMethodCode = method;
      }),
      map(() => void 0)
    );
  }


  getActivities(callback?: () => void): void {
    if (!this.fiscalGuid) {
      this.isLoading = false;
      return;
    }

    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';

    if (this.projectGuid) {
      this.isLoading = true;
      this.getProjectType(this.projectGuid);

      const sub = this.projectService.getFiscalActivities(this.projectGuid, this.fiscalGuid)
        .pipe(finalize(() => {
          this.isLoading = false;
          this.cd.detectChanges();
        }))
        .subscribe({
          next: (data: any) => {
            let activities: any[] = [];
            if (data && data._embedded?.activities) {
              activities = data._embedded.activities;
              // Sort activities alphabetically by activityName (case insensitive)
              activities.sort((a: any, b: any) => {
                const nameA = (a.activityName || '').toLowerCase();
                const nameB = (b.activityName || '').toLowerCase();
                return nameA.localeCompare(nameB);
              });
            }

            this.isNewActivityBeingAdded = false;

            this.activityViews = (activities || []).map((activity, index) => {
              const view: FiscalActivityViewModel = {
                data: activity,
                originalData: JSON.parse(JSON.stringify(activity)),
                form: this.createActivityForm(activity),
                isExpanded: this.activityViews[index]?.isExpanded || false,
                isDirty: false,
                isSaving: false
              };
              
              // Bind form dirty state to view
              view.form.valueChanges.subscribe(() => {
                view.isDirty = view.form.dirty;
              });
              return view;
            });

            // do callback (e.g., scrolling, expanding panel) if provided
            if (callback) callback();
          },
          error: (error) => {
            console.error('Error fetching activities:', error);
            
            this.snackbarService.open(
              'Failed to load activities. Please try again later.',
              'OK',
              { duration: 5000, panelClass: 'snackbar-error' }
            );
          }
        });
      this.dataSubscription?.add(sub);
    } else {
      this.isLoading = false;
    }
  }

  getProjectType(projectGuid: string) {
    const sub = this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe({
      next: (data) => {
        this.projectTypeCode = data.projectTypeCode?.projectTypeCode
      },
      error: (err) => {
        console.error('Error fetching project:', err);
      },
    });
    this.dataSubscription?.add(sub);
  }

  getFormattedDate(date: string | null): string {
    return date ? moment.utc(date).format('YYYY-MM-DD') : '';
  }


  createActivityForm(activity?: any): FormGroup {
    const isCarryForwardInd = activity?.isCarryForwardInd || false;
    const outstandingObligationsInd = activity?.outstandingObligationsInd || false;

    const activityCommentValidators = [Validators.maxLength(500)];
    if (outstandingObligationsInd) activityCommentValidators.push(Validators.required);

    const finalOutcomeCommentsValidators = [Validators.maxLength(500)];
    if (isCarryForwardInd) finalOutcomeCommentsValidators.push(Validators.required);

    const form = this.fb.group({
      activityGuid: [activity?.activityGuid || ''],
      projectPlanFiscalGuid: [activity?.projectPlanFiscalGuid || ''],
      activityStatusCode: [activity?.activityStatusCode?.activityStatusCode || 'ACTIVE'],
      silvicultureBaseGuid: [activity?.silvicultureBaseGuid || ''],
      silvicultureTechniqueGuid: [activity?.silvicultureTechniqueGuid || { value: '', disabled: true }],
      silvicultureMethodGuid: [activity?.silvicultureMethodGuid || { value: '', disabled: true }],
      riskRatingCode: [activity?.riskRatingCode || { 'riskRatingCode': 'LOW_RISK' }],
      contractPhaseCode: [activity?.contractPhaseCode?.contractPhaseCode || ''],
      activityFundingSourceGuid: [activity?.activityFundingSourceGuid || ''],
      activityName: [activity?.activityName || '', [Validators.required, Validators.maxLength(4000)]],
      activityDescription: [activity?.activityDescription || '', [Validators.required, Validators.maxLength(500)]],
      activityDateRange: this.fb.group({
        activityStartDate: [activity?.activityStartDate ? moment.utc(activity.activityStartDate).format('YYYY-MM-DD') : '', Validators.required],
        activityEndDate: [activity?.activityEndDate ? moment.utc(activity.activityEndDate).format('YYYY-MM-DD') : '', Validators.required]
      }),
      plannedSpendAmount: [activity?.plannedSpendAmount ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      plannedTreatmentAreaHa: [activity?.plannedTreatmentAreaHa ?? '', [Validators.required, Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      reportedSpendAmount: [activity?.reportedSpendAmount ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      completedAreaHa: [activity?.completedAreaHa ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      isResultsReportableInd: [activity?.isResultsReportableInd || false],
      outstandingObligationsInd: [outstandingObligationsInd],
      activityComment: [activity?.activityComment || '', activityCommentValidators],
      isCarryForwardInd: [isCarryForwardInd],
      finalOutcomeComments: [activity?.finalOutcomeComments || '', finalOutcomeCommentsValidators],
      isSpatialAddedInd: [activity?.isSpatialAddedInd || false],
      createDate: [activity?.createDate || ''], // ISO 8601 date format
      filteredTechniqueCode: [[]], //  Store technique options inside form
      filteredMethodCode: [[]], //  Store method options inside form
    });
    if (activity?.silvicultureBaseGuid) {
      this.filteredTechniqueCode = this.silvicultureTechniqueCode.filter(t => t.silvicultureBaseGuid === activity.silvicultureBaseGuid);
    }
    if (activity?.silvicultureTechniqueGuid) {
      this.filteredMethodCode = this.filterActiveMethods(activity.silvicultureTechniqueGuid, activity.silvicultureMethodGuid);
    }
    if (this.isReadonly) {
      form.disable({ emitEvent: false });
    }

    this.updateTechniqueAndMethodOptions(form, activity);
    // Handle user selection changes
    form.get('silvicultureBaseGuid')?.valueChanges.subscribe((baseGuid) => this.onBaseChange(baseGuid, form));
    form.get('silvicultureTechniqueGuid')?.valueChanges.subscribe((techniqueGuid) => this.onTechniqueChange(techniqueGuid, form));
    form.get('silvicultureMethodGuid')?.valueChanges.subscribe((methodGuid) => this.onMethodChange(methodGuid, form));

    form.get('isCarryForwardInd')?.valueChanges.subscribe((isChecked) => {
      const field = form.get('finalOutcomeComments');
      if (isChecked) {
        field?.setValidators([Validators.maxLength(500), Validators.required]);
      } else {
        field?.setValidators([Validators.maxLength(500)]);
      }
      field?.updateValueAndValidity();
    });

    form.get('outstandingObligationsInd')?.valueChanges.subscribe((isChecked) => {
      const field = form.get('activityComment');
      if (isChecked) {
        field?.setValidators([Validators.maxLength(500), Validators.required]);
      } else {
        field?.setValidators([Validators.maxLength(500)]);
      }
      field?.updateValueAndValidity();
    });



    return form;
  }

  //Ensure Each Activity Resets its Technique and Method Options
  updateTechniqueAndMethodOptions(form: FormGroup, activity?: any) {
    const baseGuid = activity?.silvicultureBaseGuid;
    const techniqueGuid = activity?.silvicultureTechniqueGuid;

    const filteredTechniques = baseGuid
      ? this.silvicultureTechniqueCode.filter(t => t.silvicultureBaseGuid === baseGuid)
      : [];
    const currentMethod = form.get('silvicultureMethodGuid')?.value;
    const filteredMethods = techniqueGuid
      ? this.filterActiveMethods(techniqueGuid, currentMethod)
      : [];

    form.patchValue({
      filteredTechniqueCode: filteredTechniques,
      filteredMethodCode: filteredMethods
    });

    if (!this.isReadonly) {
      if (filteredTechniques.length > 0) {
        form.get('silvicultureTechniqueGuid')?.enable({ emitEvent: false });
      } else {
        form.get('silvicultureTechniqueGuid')?.disable({ emitEvent: false });
      }

      if (filteredMethods.length > 0) {
        form.get('silvicultureMethodGuid')?.enable({ emitEvent: false });
      } else {
        form.get('silvicultureMethodGuid')?.disable({ emitEvent: false });
      }
    }

    this.cd.detectChanges();
  }


  onBaseChange(baseGuid: string, form: FormGroup) {
    if (this.isReadonly) return;
    const techniqueControl = form.get('silvicultureTechniqueGuid');
    const methodControl = form.get('silvicultureMethodGuid');
    if (!baseGuid) {
      form.patchValue({
        silvicultureTechniqueGuid: null,
        silvicultureMethodGuid: null,
        filteredTechniqueCode: [],
        filteredMethodCode: []
      });
      techniqueControl?.disable();
      methodControl?.disable();
      return;
    }

    const filteredTechniques = this.silvicultureTechniqueCode.filter(t => t.silvicultureBaseGuid === baseGuid);

    const currentTechnique = form.get('silvicultureTechniqueGuid')?.value;
    const currentMethod = form.get('silvicultureMethodGuid')?.value;
    const validTechnique = filteredTechniques.find(t => t.silvicultureTechniqueGuid === currentTechnique);

    form.patchValue({
      filteredTechniqueCode: filteredTechniques,
      filteredMethodCode: validTechnique
        ? this.filterActiveMethods(currentTechnique, currentMethod)
        : []
    }, { emitEvent: false });

    if (!validTechnique) {
      form.patchValue({
        silvicultureTechniqueGuid: null,
        silvicultureMethodGuid: null
      }, { emitEvent: false });

      techniqueControl?.disable();
      methodControl?.disable();
    } else {
      techniqueControl?.enable();

      const validMethod = this.silvicultureMethodCode.find(
        m => m.silvicultureTechniqueGuid === currentTechnique && m.silvicultureMethodGuid === currentMethod
      );

      if (!validMethod) {
        form.patchValue({ silvicultureMethodGuid: null }, { emitEvent: false });
        form.get('silvicultureMethodGuid')?.disable();
      } else {
        form.get('silvicultureMethodGuid')?.enable();
      }
    }


    techniqueControl?.enable();
    this.updateActivityName(form);
  }

  toggleResultsReportableInd(view: any): void {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    const form = view?.form;
    if (!form) return;

    const isReportable = form.get('isResultsReportableInd')?.value;
    const baseField = form.get('silvicultureBaseGuid');
    const nameField = form.get('activityName');

    if (isReportable) {
      baseField?.setValidators([Validators.required]);
      nameField?.disable();
      nameField?.setValue(this.getActivityTitle(view)); // Set name initially

      if (!this.isReadonly) {
        form.get('silvicultureBaseGuid')?.valueChanges.subscribe(() => {
          if (form.get('isResultsReportableInd')?.value) {
            nameField?.setValue(this.getActivityTitle(view));
          }
        });

        form.get('silvicultureTechniqueGuid')?.valueChanges.subscribe(() => {
          if (form.get('isResultsReportableInd')?.value) {
            nameField?.setValue(this.getActivityTitle(view));
          }
        });

        form.get('silvicultureMethodGuid')?.valueChanges.subscribe(() => {
          if (form.get('isResultsReportableInd')?.value) {
            nameField?.setValue(this.getActivityTitle(view));
          }
        });
      }
    } else {
      baseField?.clearValidators();
      nameField?.enable();
      nameField?.setValue(''); // Clear name when toggle is OFF
    }

    baseField?.updateValueAndValidity();
    nameField?.updateValueAndValidity();
    this.cd.detectChanges();
  }



  onTechniqueChange(techniqueGuid: string, form: FormGroup) {
    if (this.isReadonly) return;
    const methodControl = form.get('silvicultureMethodGuid');

    if (!techniqueGuid) {
      form.patchValue({
        silvicultureMethodGuid: null,
        filteredMethodCode: []
      }, { emitEvent: false });

      methodControl?.disable();
      return;
    }

    const currentMethod = methodControl?.value;
    const filteredMethods = this.filterActiveMethods(techniqueGuid, currentMethod);
    const validMethod = filteredMethods.find(
      m => m.silvicultureMethodGuid === currentMethod
    );

    form.patchValue({
      filteredMethodCode: filteredMethods
    }, { emitEvent: false });

    if (filteredMethods.length > 0) {
      methodControl?.enable();
    } else {
      methodControl?.disable();
    }

    if (!validMethod) {
      form.patchValue({ silvicultureMethodGuid: null }, { emitEvent: false });
    }

    this.updateActivityName(form);
  }

  onMethodChange(methodGuid: string, form: FormGroup) {
    if (this.isReadonly) return;
    if (methodGuid) {
      this.updateActivityName(form);
    }
  }

  updateActivityName(form: FormGroup) {
    if (form.get('isResultsReportableInd')?.value) {
      const baseGuid = form.get('silvicultureBaseGuid')?.value;
      const techniqueGuid = form.get('silvicultureTechniqueGuid')?.value;
      const methodGuid = form.get('silvicultureMethodGuid')?.value;

      const baseDesc = this.silvicultureBaseCode.find(b => b.silvicultureBaseGuid === baseGuid)?.description;
      const techniqueDesc = this.silvicultureTechniqueCode.find(t => t.silvicultureTechniqueGuid === techniqueGuid && t.silvicultureBaseGuid === baseGuid)?.description;
      const methodDesc = this.silvicultureMethodCode.find(m => m.silvicultureMethodGuid === methodGuid && m.silvicultureTechniqueGuid === techniqueGuid)?.description;
      //  reset technique/method if no longer valid
      if (!techniqueDesc) {
        form.get('silvicultureTechniqueGuid')?.setValue(null, { emitEvent: false });
        form.get('silvicultureMethodGuid')?.setValue(null, { emitEvent: false });
      }
      if (!methodDesc) {
        form.get('silvicultureMethodGuid')?.setValue(null, { emitEvent: false });
      }

      //  Generate new activity name
      const newName = [baseDesc, techniqueDesc, methodDesc].filter(Boolean).join(' - ');

      form.get('activityName')?.setValue(newName, { emitEvent: false });

      this.cd.detectChanges();
    }
  }

  getActivityTitle(view: any): string {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    const activity = view?.form?.getRawValue();
    if (!activity) return '';

    // If Results Reportable is ON, construct Base - Technique - Method dynamically
    if (activity.isResultsReportableInd) {
      const parts: string[] = [];

      const base = this.silvicultureBaseCode.find(b => b.silvicultureBaseGuid === activity.silvicultureBaseGuid)?.description;
      const technique = this.silvicultureTechniqueCode.find(t => t.silvicultureTechniqueGuid === activity.silvicultureTechniqueGuid)?.description;
      const method = this.silvicultureMethodCode.find(m => m.silvicultureMethodGuid === activity.silvicultureMethodGuid)?.description;

      if (base) parts.push(base);
      if (technique) parts.push(technique);
      if (method) parts.push(method);

      return parts.length ? parts.join(' - ') : '';
    }

    // If Results Reportable is OFF, use the Activity Name (if available)
    return activity.activityName?.trim() || '';
  }



  getLastUpdated(view: any) {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    const activity = view?.data;
    if (!activity) return ''; // Handle missing data

    return moment.utc(activity.updateDate).format('YYYY-MM-DD');
  }

  toggleActivityStatus(view: any) {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    const currentStatus = view?.form?.value?.activityStatusCode;
    const newStatus = currentStatus === 'COMPLETED' ? 'ACTIVE' : 'COMPLETED';

    view.form.patchValue({
      activityStatusCode: newStatus
    });
    view.isDirty = true;
  }

  addActivity(): void {
    if (this.isNewActivityBeingAdded) return;

    const createAndFocusNewActivity = () => {
      this.isNewActivityBeingAdded = true;
      const newActivity: ActivityModel = {};

      const form = this.createActivityForm(newActivity);
      const view: FiscalActivityViewModel = {
        data: newActivity,
        originalData: {},
        form: form,
        isExpanded: true,
        isDirty: false,
        isSaving: false
      };
      
      form.valueChanges.subscribe(() => {
        view.isDirty = form.dirty;
      });

      this.activityViews.unshift(view);
      this.activityViews = [...this.activityViews];

      this.cd.detectChanges();

      setTimeout(() => {
        const panelEl = document.getElementById('activity-0');
        panelEl?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 100);
    };

    if (this.activitiesPanel && !this.activitiesPanel.expanded) {
      const sub = this.activitiesPanel.opened.pipe(take(1)).subscribe(() => {
        sub.unsubscribe();
        createAndFocusNewActivity();
      });
      this.activitiesPanel.open();
    } else {
      createAndFocusNewActivity();
    }
  }


  getRiskIcon(riskCode: string): string {
    const riskMap: { [key: string]: string } = {
      'LOW_RISK': 'low-risk',
      'MODRT_RISK': 'medium-risk',
      'HIGH_RISK': 'high-risk'
    };

    return riskMap[riskCode] || 'none-risk';
  }

  getRiskDescription(description: string | null | undefined): string {
    return description || 'None'; // Default to "None" if description is empty or null
  }

  onSaveActivity(view: any): void {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    if (!view) return;
    if (view.isSaving) return;
    view.isSaving = true;
    const originalData = view.data;
    const form = view.form;
    if (!form) {
      view.isSaving = false;
      return;
    }
    let formData = { ...form.getRawValue() };

    //extract start and end dates separetely
    const activityStartDate = formData.activityDateRange?.activityStartDate
      ? moment.utc(formData.activityDateRange.activityStartDate, 'YYYY-MM-DD').toISOString()
      : null;

    const activityEndDate = formData.activityDateRange?.activityEndDate
      ? moment.utc(formData.activityDateRange.activityEndDate, 'YYYY-MM-DD').toISOString()
      : null;
    let updatedData: any = {
      ...originalData, // Include all original data and overwrite with form data
      ...formData,
      activityStartDate,
      activityEndDate,
      contractPhaseCode: typeof formData.contractPhaseCode === 'string'
        ? { contractPhaseCode: formData.contractPhaseCode }
        : formData.contractPhaseCode,
      activityStatusCode: typeof formData.activityStatusCode === 'string'
        ? { activityStatusCode: formData.activityStatusCode }
        : formData.activityStatusCode,

    };
    updatedData.lastUpdatedTimestamp = getUtcIsoTimestamp();
    delete updatedData.activityDateRange;

    if (!updatedData.projectPlanFiscalGuid) {
      updatedData.projectPlanFiscalGuid = this.fiscalGuid;
    }
    // Remove empty or null values
    updatedData = this.removeEmptyFields(updatedData, [
      'projectPlanFiscalGuid',
      'activityStartDate',
      'activityEndDate',
      'activityName'
    ]);

    const isUpdate = !!view.data?.activityGuid;

    if (isUpdate) {
      // Update existing activity
      this.projectService.updateFiscalActivities(this.projectGuid, this.fiscalGuid, updatedData.activityGuid, updatedData).subscribe({
        next: () => {
          this.snackbarService.open(
            this.messages.activityUpdatedSuccess,
            'OK',
            { duration: 5000, panelClass: 'snackbar-success' }
          );
          view.isDirty = false;
          view.form.markAsPristine(); // reset dirty tracking
          this.activityViews.forEach(v => v.isExpanded = (v === view));
          this.getActivities(() => this.expandAndScrollToActivity(updatedData.activityGuid));
          view.isSaving = false;
        },
        error: () => {
          this.snackbarService.open(
            this.messages.activityUpdatedFailure,
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
          view.isSaving = false;
        }
      });
    } else {
      // Create new activity
      this.projectService.createFiscalActivity(this.projectGuid, this.fiscalGuid, updatedData).subscribe({
        next: (response) => {
          this.snackbarService.open(
            this.messages.activityCreatedSuccess,
            'OK',
            { duration: 5000, panelClass: 'snackbar-success' }
          );
          view.isDirty = false;
          view.form.markAsPristine(); // Reset dirty tracking
          this.getActivities(() => this.expandAndScrollToActivity(response.activityGuid));
          view.isSaving = false;
        },
        error: () => {
          this.snackbarService.open(
            this.messages.activityCreatedFailure,
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
          view.isSaving = false;
        }
      });
    }
    this.isNewActivityBeingAdded = false;
  }

  expandAndScrollToActivity(activityGuid: string): void {
    // Find the index of the newly saved activity
    const index = this.activityViews.findIndex(view => view.data?.activityGuid === activityGuid);

    if (index !== -1) {
      // Expand only the newly saved activity
      this.activityViews.forEach((v, i) => v.isExpanded = (i === index));

      // Scroll to the newly saved activity
      setTimeout(() => {
        const savedActivityElement = document.getElementById(`activity-${index}`);
        if (savedActivityElement) {
          savedActivityElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }, 100);
    }
  }

  removeEmptyFields(obj: any, alwaysInclude: string[] = []): any {
    return Object.fromEntries(
      Object.entries(obj).filter(([key, value]) => {
        if (alwaysInclude.includes(key)) return true;
        if (value === undefined || value === '') return false;
        if (typeof value === 'object' && value !== null && Object.keys(value).length > 0) {
          const cleanedValue = this.removeEmptyFields(value, alwaysInclude);
          return Object.keys(cleanedValue).length > 0;
        }
        return true;
      })
    );
  }


  onCancelActivity(view: any): void {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    if (!view || !view.form) return;

    const isNewEntry = !view.data?.activityGuid;
    const originalData = view.originalData;

    if (isNewEntry) {
      // Remove the new entry
      this.activityViews = this.activityViews.filter(v => v !== view);

      this.isNewActivityBeingAdded = false;
    } else {
      // Reset to original values
      view.form.reset(this.createActivityForm(originalData).getRawValue(), { emitEvent: false });

      view.form.get('activityDateRange')?.setValue({
        activityStartDate: originalData.activityStartDate ? moment.utc(originalData.activityStartDate).format('YYYY-MM-DD') : '',
        activityEndDate: originalData.activityEndDate ? moment.utc(originalData.activityEndDate).format('YYYY-MM-DD') : '',
      }, { emitEvent: false });

      view.form.markAsPristine();
      view.form.markAsUntouched();
      view.isDirty = false;

      this.cd.detectChanges();
    }
  }

  onDeleteActivity(view: any): void {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    if (!view) return;
    const data = view.form?.value;
    const activityGuid = data.activityGuid;
    const activityName = data?.activityName || 'this activity';
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        indicator: 'delete-activity', name: activityName,
        title: ModalTitles.DELETE_ACTIVITY_TITLE,
        message: `Are you sure you want to delete ${activityName}? This action cannot be reversed and will immediately remove the activity from the Fiscal scope.`
      },
      width: '600px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        if (!activityGuid) {
          // If no activityGuid, simply remove it from the local list
          this.activityViews = this.activityViews.filter(v => v !== view);
          this.isNewActivityBeingAdded = false;
          return;
        }

        // Block deletion if there are attachments
        const index = this.activityViews.indexOf(view);
        const activityAttachments = this.attachmentFiles?.toArray?.()[index];
        if (activityAttachments?.hasAttachments) {
          this.snackbarService.open(
            this.messages.activityWithAttachmentDeleteFailure,
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
          return;
        }

        // Delete from the service call if it's a saved fiscal activity
        this.projectService.deleteActivity(this.projectGuid, this.fiscalGuid, activityGuid)
          .subscribe({
            next: () => {
              this.snackbarService.open(
                this.messages.activityDeletedSuccess,
                'OK',
                { duration: 5000, panelClass: 'snackbar-success' }
              );
              this.getActivities();
            },
            error: () => {
              this.snackbarService.open(
                this.messages.activityDeletedFailure,
                'OK',
                { duration: 5000, panelClass: 'snackbar-error' }
              );
            }
          });
      }
    });
  }

  canDeleteActivity(view: any): boolean {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    const activity = view?.data;
    if (!activity) return false;

    // Delete is available when:
    // Activity is not set to Complete
    // Does not have a Performance update on the Fiscal
    // Delete is in an inactive state when:
    // The user does not have permission to delete
    // The Activity has been started ie. there is a Performance Update on the Fiscal
    // The Activity has polygon files associated to it
    // The activity is marked as Complete

    // We dont have permissions and performance implemneted yet. Check single condition that prevent deletion
    return activity.activityStatusCode?.activityStatusCode !== 'COMPLETED';
  }

  getDeleteIcon(view: any): string {
    view = typeof view === 'number' ? this.activityViews[view] : view;
    return (!this.isReadonly && this.canDeleteActivity(view)) ? '/assets/delete-icon.svg' : '/assets/delete-disabled-icon.svg';
  }

  isFormDirty(): boolean {
    return this.activityViews.some((view) => view.form.dirty);
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.isFormDirty()) {
      const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        data: {
          indicator: 'confirm-unsave',
          title: ModalTitles.CONFIRM_UNSAVE_TITLE,
          message: ModalMessages.CONFIRM_UNSAVE_MESSAGE
        },
        width: '600px',
      });
      return dialogRef.afterClosed();
    }
    return true;
  }

  onFilesChanged() {
    this.boundariesUpdated.emit(); // Notify ProjectFiscalsComponent
    this.getActivities();
  }



  private filterActiveMethods(techniqueGuid: string, currentMethodGuid: string): any[] {
    return this.silvicultureMethodCode.filter(m =>
      m.silvicultureTechniqueGuid === techniqueGuid &&
      (!m.systemEndTimestamp || moment(m.systemEndTimestamp).isAfter(moment()) || m.silvicultureMethodGuid === currentMethodGuid)
    );
  }
}