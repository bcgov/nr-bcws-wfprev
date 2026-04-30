import { Component, Input } from '@angular/core';

@Component({
    selector: 'wfprev-read-only-field',
    imports: [],
    templateUrl: './read-only-field.component.html',
    styleUrl: './read-only-field.component.scss'
})
export class ReadOnlyFieldComponent {
    @Input() label = '';
    @Input() value = '';
}
