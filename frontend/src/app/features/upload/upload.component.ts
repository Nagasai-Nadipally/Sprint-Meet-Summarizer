import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MeetingService } from '../../core/services/meeting.service';

const ALLOWED = ['.mp3', '.wav', '.mp4', '.m4a'];
const MAX_BYTES = 100 * 1024 * 1024; // 100 MB, matches backend default

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  template: `
    <div class="page narrow">
      <a routerLink="/dashboard" class="back muted">
        <mat-icon>arrow_back</mat-icon> Back to meetings
      </a>

      <h1 class="display title">New meeting</h1>
      <p class="muted sub">
        Upload a recording. We'll transcribe it, summarize the discussion, and pull
        out action items with owners and deadlines.
      </p>

      <div
        class="dropzone card"
        [class.dragover]="dragOver()"
        [class.has-file]="file()"
        (dragover)="onDragOver($event)"
        (dragleave)="onDragLeave($event)"
        (drop)="onDrop($event)"
        (click)="fileInput.click()"
        role="button"
        tabindex="0"
        (keydown.enter)="fileInput.click()"
      >
        <input
          #fileInput
          type="file"
          hidden
          [accept]="accept"
          (change)="onFilePicked($event)"
        />
        @if (file(); as f) {
          <mat-icon class="big">audio_file</mat-icon>
          <div class="file-name">{{ f.name }}</div>
          <div class="faint">{{ readableSize(f.size) }} — click to choose a different file</div>
        } @else {
          <mat-icon class="big">cloud_upload</mat-icon>
          <div class="dz-title">Drag a file here, or click to browse</div>
          <div class="faint">MP3, WAV, M4A, or MP4 — up to 100 MB</div>
        }
      </div>

      <mat-form-field appearance="outline" class="title-field">
        <mat-label>Meeting title (optional)</mat-label>
        <input matInput [formControl]="title" placeholder="Sprint planning — week 24" />
        <mat-hint>If left blank, we'll name it after the file.</mat-hint>
      </mat-form-field>

      @if (uploading()) {
        <mat-progress-bar mode="indeterminate" class="bar" />
        <p class="muted center-text">Uploading and starting transcription…</p>
      }

      <div class="actions">
        <button mat-flat-button routerLink="/dashboard">Cancel</button>
        <button
          mat-flat-button
          class="accent-btn"
          [disabled]="!file() || uploading()"
          (click)="submit()"
        >
          <mat-icon>auto_awesome</mat-icon>
          Generate notes
        </button>
      </div>
    </div>
  `,
  styles: [
    `
      .narrow { max-width: 680px; }
      .back {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        text-decoration: none;
        font-size: 14px;
        margin-bottom: 18px;
      }
      .back mat-icon { font-size: 18px; height: 18px; width: 18px; }
      .title { font-size: 28px; margin: 0 0 6px; }
      .sub { margin: 0 0 24px; max-width: 52ch; }
      .dropzone {
        padding: 46px 24px;
        text-align: center;
        cursor: pointer;
        border-style: dashed;
        border-width: 2px;
        border-color: #c8d4e2;
        transition: border-color 0.15s ease, background 0.15s ease;
      }
      .dropzone:hover, .dropzone:focus-visible {
        border-color: var(--brand);
        outline: none;
      }
      .dropzone.dragover { border-color: var(--accent); background: var(--accent-soft); }
      .dropzone.has-file { border-style: solid; border-color: var(--ok); background: var(--ok-soft); }
      .big { font-size: 46px; height: 46px; width: 46px; color: var(--brand); }
      .has-file .big { color: var(--ok); }
      .dz-title, .file-name { font-weight: 600; margin: 12px 0 4px; }
      .title-field { width: 100%; margin-top: 22px; }
      .bar { margin-top: 14px; }
      .center-text { text-align: center; margin-top: 10px; }
      .actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        margin-top: 26px;
      }
    `,
  ],
})
export class UploadComponent {
  private meetingService = inject(MeetingService);
  private router = inject(Router);
  private snack = inject(MatSnackBar);

  accept = ALLOWED.join(',');
  file = signal<File | null>(null);
  title = new FormControl('', { nonNullable: true });
  uploading = signal(false);
  dragOver = signal(false);

  onDragOver(e: DragEvent): void {
    e.preventDefault();
    this.dragOver.set(true);
  }

  onDragLeave(e: DragEvent): void {
    e.preventDefault();
    this.dragOver.set(false);
  }

  onDrop(e: DragEvent): void {
    e.preventDefault();
    this.dragOver.set(false);
    const dropped = e.dataTransfer?.files?.[0];
    if (dropped) this.validateAndSet(dropped);
  }

  onFilePicked(e: Event): void {
    const input = e.target as HTMLInputElement;
    const picked = input.files?.[0];
    if (picked) this.validateAndSet(picked);
  }

  private validateAndSet(f: File): void {
    const ext = f.name.slice(f.name.lastIndexOf('.')).toLowerCase();
    if (!ALLOWED.includes(ext)) {
      this.snack.open(`Unsupported file type. Use ${ALLOWED.join(', ')}.`, 'Dismiss', {
        duration: 5000,
      });
      return;
    }
    if (f.size > MAX_BYTES) {
      this.snack.open('That file is larger than 100 MB.', 'Dismiss', { duration: 5000 });
      return;
    }
    this.file.set(f);
  }

  submit(): void {
    const f = this.file();
    if (!f) return;

    this.uploading.set(true);
    this.meetingService.upload(f, this.title.value).subscribe({
      next: (meeting) => this.router.navigate(['/meetings', meeting.id]),
      error: (err) => {
        this.uploading.set(false);
        this.snack.open(
          err?.error?.message ?? 'Upload failed. Please try again.',
          'Dismiss',
          { duration: 6000 },
        );
      },
    });
  }

  readableSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }
}
