import { Component, inject, signal } from '@angular/core';
import {
  MatDialogRef,
  MatDialogModule,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule, MatChipInputEvent } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { COMMA, ENTER, SPACE } from '@angular/cdk/keycodes';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

@Component({
  selector: 'app-send-email-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>Email this summary</h2>
    <mat-dialog-content>
      <p class="muted intro">
        Send the summary and action items for "{{ data.title }}" to your team.
      </p>

      <mat-form-field appearance="outline" class="full">
        <mat-label>Recipients</mat-label>
        <mat-chip-grid #grid aria-label="Email recipients">
          @for (email of recipients(); track email) {
            <mat-chip-row (removed)="remove(email)">
              {{ email }}
              <button matChipRemove [attr.aria-label]="'Remove ' + email">
                <mat-icon>cancel</mat-icon>
              </button>
            </mat-chip-row>
          }
          <input
            placeholder="name@company.com"
            [matChipInputFor]="grid"
            [matChipInputSeparatorKeyCodes]="separators"
            (matChipInputTokenEnd)="add($event)"
          />
        </mat-chip-grid>
        <mat-hint>Press Enter, comma, or space to add each address.</mat-hint>
      </mat-form-field>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button
        mat-flat-button
        class="accent-btn"
        [disabled]="recipients().length === 0"
        (click)="send()"
      >
        Send to {{ recipients().length || '' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .intro { margin: 0 0 16px; }
      .full { width: 100%; min-width: 360px; }
    `,
  ],
})
export class SendEmailDialogComponent {
  private dialogRef = inject(MatDialogRef<SendEmailDialogComponent>);
  data = inject<{ title: string }>(MAT_DIALOG_DATA);

  separators = [ENTER, COMMA, SPACE];
  recipients = signal<string[]>([]);

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim().toLowerCase();
    if (value && EMAIL_RE.test(value) && !this.recipients().includes(value)) {
      this.recipients.update((list) => [...list, value]);
    }
    event.chipInput!.clear();
  }

  remove(email: string): void {
    this.recipients.update((list) => list.filter((e) => e !== email));
  }

  send(): void {
    this.dialogRef.close(this.recipients());
  }
}
