import { Component, Input} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { EvaluationCriteriaDialogComponent } from 'src/app/components/evaluation-criteria-dialog/evaluation-criteria-dialog.component';
import { Project } from 'src/app/components/models';
import { ExpansionIndicatorComponent } from 'src/app/components/shared/expansion-indicator/expansion-indicator.component';

@Component({
  selector: 'wfprev-evaluation-criteria',
  standalone: true,
  imports: [ExpansionIndicatorComponent,MatExpansionModule,MatIconModule ],
  templateUrl: './evaluation-criteria.component.html',
  styleUrl: './evaluation-criteria.component.scss'
})
export class EvaluationCriteriaComponent {
  @Input() project!: Project;
  constructor(
    private readonly dialog: MatDialog,
  ){
  }

  openEvaluationCriteriaPopUp(): void {
    this.project;
    const dialogRef = this.dialog.open(EvaluationCriteriaDialogComponent, {
      width: '1000px',
      disableClose: true,
      hasBackdrop: true,
      data: {
        project:this.project
      }
    });
    // Subscribe to the afterClosed method to handle the result
    dialogRef.afterClosed().subscribe((result) => {

    });
  }
}
