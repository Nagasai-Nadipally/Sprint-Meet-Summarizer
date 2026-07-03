import { Component, DestroyRef, Input, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { interval, switchMap } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { MeetingService } from '../../core/services/meeting.service';
import {
  ActionItem,
  ActionItemStatus,
  IN_PROGRESS_STATUSES,
  MeetingDetail,
  MeetingStatus,
} from '../../core/models/models';
import {
  statusLabel,
  actionStatusLabel,
  actionStatusClass,
} from '../../shared/status.util';
import { environment } from '../../../environments/environment';
import { SendEmailDialogComponent } from './send-email-dialog.component';

@Component({
  selector: 'app-meeting-detail',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatExpansionModule,
    MatTooltipModule,
  ],
  template: `
    <div class="page">
      <a routerLink="/dashboard" class="back muted">
        <mat-icon>arrow_back</mat-icon> Back to meetings
      </a>

      @if (loading()) {
        <div class="center"><mat-spinner diameter="40" /></div>
      } @else {
        @if (meeting(); as m) {
        <header class="head">
          <div>
            <h1 class="display title">{{ m.title }}</h1>
            <div class="meta faint">
              <mat-icon>description</mat-icon> {{ m.originalFilename }}
              <span class="sep">·</span>
              {{ m.createdAt | date: 'medium' }}
            </div>
          </div>
          @if (m.status === 'COMPLETED') {
            <button mat-flat-button class="accent-btn" (click)="openEmailDialog(m)">
              <mat-icon>mail</mat-icon> Email summary
            </button>
          }
        </header>

        <!-- Processing / failed banners -->
        @if (isProcessing(m.status)) {
          <div class="card processing">
            <mat-spinner diameter="22" />
            <div>
              <strong>{{ statusText(m.status) }}</strong>
              <div class="muted">This updates automatically — no need to refresh.</div>
            </div>
          </div>
        } @else if (m.status === 'FAILED') {
          <div class="card failed">
            <mat-icon>error_outline</mat-icon>
            <div>
              <strong>Processing failed</strong>
              <div class="muted">{{ m.errorMessage || 'Something went wrong. Try uploading again.' }}</div>
            </div>
          </div>
        }

        <!-- Summary -->
        @if (m.overview) {
          <section class="card block">
            <h2 class="block-title"><mat-icon>summarize</mat-icon> Summary</h2>
            <p class="overview">{{ m.overview }}</p>

            @if (m.keyPoints.length) {
              <h3 class="sub-title">Key discussion points</h3>
              <ul class="points">
                @for (point of m.keyPoints; track point) {
                  <li>{{ point }}</li>
                }
              </ul>
            }
          </section>
        }

        <!-- Action items -->
        @if (m.actionItems.length) {
          <section class="card block">
            <h2 class="block-title"><mat-icon>task_alt</mat-icon> Action items</h2>
            <div class="items">
              @for (item of m.actionItems; track item.id) {
                <div class="item">
                  <div class="item-main">
                    <div class="item-task">{{ item.task }}</div>
                    <div class="item-sub faint">
                      <mat-icon>person</mat-icon> {{ item.owner }}
                      @if (item.deadline) {
                        <span class="sep">·</span>
                        <mat-icon>event</mat-icon> Due {{ item.deadline | date: 'mediumDate' }}
                      }
                    </div>
                  </div>
                  <button
                    class="pill status-btn"
                    [class]="actionPill(item.status)"
                    [matMenuTriggerFor]="statusMenu"
                    [disabled]="updatingId() === item.id"
                  >
                    {{ actionLabel(item.status) }}
                    <mat-icon class="caret">expand_more</mat-icon>
                  </button>
                  <mat-menu #statusMenu="matMenu">
                    <button mat-menu-item (click)="setStatus(item, 'PENDING')">Pending</button>
                    <button mat-menu-item (click)="setStatus(item, 'IN_PROGRESS')">In progress</button>
                    <button mat-menu-item (click)="setStatus(item, 'COMPLETED')">Completed</button>
                  </mat-menu>
                </div>
              }
            </div>
          </section>
        } @else if (m.status === 'COMPLETED') {
          <section class="card block">
            <h2 class="block-title"><mat-icon>task_alt</mat-icon> Action items</h2>
            <p class="muted">No action items were identified in this meeting.</p>
          </section>
        }

        <!-- Follow-up questions -->
        @if (m.followUpQuestions.length) {
          <section class="card block">
            <h2 class="block-title"><mat-icon>help_outline</mat-icon> Follow-up questions</h2>
            <ul class="points">
              @for (q of m.followUpQuestions; track q) {
                <li>{{ q }}</li>
              }
            </ul>
          </section>
        }

        <!-- Transcript (collapsed) -->
        @if (m.transcript) {
          <mat-expansion-panel class="transcript card">
            <mat-expansion-panel-header>
              <mat-panel-title><mat-icon>article</mat-icon>&nbsp; Full transcript</mat-panel-title>
            </mat-expansion-panel-header>
            <p class="transcript-text">{{ m.transcript }}</p>
          </mat-expansion-panel>
        }
      } @else {
          <div class="center muted">Meeting not found.</div>
        }
      }
    </div>
  `,
  styles: [
    `
      .back {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        text-decoration: none;
        font-size: 14px;
        margin-bottom: 18px;
      }
      .back mat-icon { font-size: 18px; height: 18px; width: 18px; }
      .center { display: flex; justify-content: center; padding: 64px 0; }
      .head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 18px;
      }
      .title { font-size: 28px; margin: 0 0 6px; }
      .meta { display: flex; align-items: center; gap: 6px; font-size: 13px; }
      .meta mat-icon { font-size: 16px; height: 16px; width: 16px; }
      .sep { opacity: 0.6; }
      .processing, .failed {
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 16px 18px;
        margin-bottom: 18px;
      }
      .processing { background: var(--accent-soft); border-color: #f0d9bf; }
      .failed { background: var(--danger-soft); border-color: #efc9c5; }
      .failed mat-icon { color: var(--danger); }
      .block { padding: 22px 24px; margin-bottom: 18px; }
      .block-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        margin: 0 0 14px;
      }
      .block-title mat-icon { color: var(--brand); }
      .overview { line-height: 1.7; font-size: 15.5px; margin: 0; color: var(--ink); }
      .sub-title { font-size: 14px; margin: 20px 0 8px; color: var(--ink-soft); }
      .points { margin: 0; padding-left: 20px; }
      .points li { line-height: 1.65; margin-bottom: 6px; }
      .items { display: flex; flex-direction: column; gap: 2px; }
      .item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        padding: 14px 0;
        border-bottom: 1px solid var(--border);
      }
      .item:last-child { border-bottom: none; }
      .item-task { font-weight: 500; line-height: 1.45; }
      .item-sub {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        margin-top: 5px;
      }
      .item-sub mat-icon { font-size: 15px; height: 15px; width: 15px; }
      .status-btn {
        border: none;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 2px;
        flex-shrink: 0;
      }
      .status-btn:disabled { opacity: 0.6; cursor: default; }
      .caret { font-size: 16px; height: 16px; width: 16px; }
      .transcript { margin-bottom: 18px; }
      .transcript mat-icon { color: var(--brand); vertical-align: middle; }
      .transcript-text { line-height: 1.7; white-space: pre-wrap; color: var(--ink-soft); }
    `,
  ],
})
export class MeetingDetailComponent implements OnInit {
  /** Bound from the :id route param via withComponentInputBinding(). */
  @Input() id!: string;

  private meetingService = inject(MeetingService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);
  private destroyRef = inject(DestroyRef);

  meeting = signal<MeetingDetail | null>(null);
  loading = signal(true);
  updatingId = signal<number | null>(null);
  private polling = false;

  private get meetingId(): number {
    return Number(this.id);
  }

  ngOnInit(): void {
    this.fetch(true);
  }

  private fetch(initial = false): void {
    this.meetingService.get(this.meetingId).subscribe({
      next: (m) => {
        this.meeting.set(m);
        this.loading.set(false);
        if (initial && this.isProcessing(m.status)) {
          this.startPolling();
        }
      },
      error: () => this.loading.set(false),
    });
  }

  /** Poll until the meeting leaves an in-progress state, then stop. */
  private startPolling(): void {
    if (this.polling) return;
    this.polling = true;

    interval(environment.pollIntervalMs)
      .pipe(
        switchMap(() => this.meetingService.get(this.meetingId)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (m) => {
          this.meeting.set(m);
          if (!this.isProcessing(m.status)) {
            this.polling = false;
            // interval keeps emitting; we simply ignore once not processing.
          }
        },
      });
  }

  setStatus(item: ActionItem, status: ActionItemStatus): void {
    if (item.status === status) return;
    this.updatingId.set(item.id);
    this.meetingService.updateActionItemStatus(item.id, status).subscribe({
      next: (updated) => {
        this.meeting.update((m) => {
          if (!m) return m;
          return {
            ...m,
            actionItems: m.actionItems.map((ai) =>
              ai.id === updated.id ? updated : ai,
            ),
          };
        });
        this.updatingId.set(null);
      },
      error: () => {
        this.updatingId.set(null);
        this.snack.open('Could not update the status. Try again.', 'Dismiss', {
          duration: 4000,
        });
      },
    });
  }

  openEmailDialog(m: MeetingDetail): void {
    const ref = this.dialog.open(SendEmailDialogComponent, {
      data: { title: m.title },
    });
    ref.afterClosed().subscribe((recipients: string[] | undefined) => {
      if (!recipients?.length) return;
      this.meetingService.sendEmail(m.id, recipients).subscribe({
        next: (res) =>
          this.snack.open(res.message, 'Done', { duration: 4000 }),
        error: (err) =>
          this.snack.open(
            err?.error?.message ?? 'Could not send the email.',
            'Dismiss',
            { duration: 5000 },
          ),
      });
    });
  }

  isProcessing(status: MeetingStatus): boolean {
    return IN_PROGRESS_STATUSES.includes(status);
  }

  statusText(status: MeetingStatus): string {
    switch (status) {
      case 'UPLOADED':
        return 'Queued for processing…';
      case 'TRANSCRIBING':
        return 'Transcribing the audio…';
      case 'SUMMARIZING':
        return 'Summarizing and extracting action items…';
      default:
        return statusLabel(status);
    }
  }

  actionLabel(status: ActionItemStatus): string {
    return actionStatusLabel(status);
  }

  actionPill(status: ActionItemStatus): string {
    return actionStatusClass(status);
  }
}
