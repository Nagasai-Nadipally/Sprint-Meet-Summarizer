import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MeetingService } from '../../core/services/meeting.service';
import { MeetingSummary, MeetingStatus } from '../../core/models/models';
import { statusLabel, statusClass } from '../../shared/status.util';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="page">
      <header class="head">
        <div>
          <h1 class="display title">Your meetings</h1>
          <p class="muted">Summaries and action items from your recordings.</p>
        </div>
        <a mat-flat-button routerLink="/upload" class="accent-btn">
          <mat-icon>add</mat-icon>
          New meeting
        </a>
      </header>

      <mat-form-field appearance="outline" class="search">
        <mat-icon matPrefix>search</mat-icon>
        <mat-label>Search title, summary, or transcript</mat-label>
        <input matInput [formControl]="search" placeholder="e.g. release timeline" />
      </mat-form-field>

      @if (loading()) {
        <div class="center"><mat-spinner diameter="36" /></div>
      } @else if (meetings().length === 0) {
        <div class="empty card">
          <mat-icon>podcasts</mat-icon>
          @if (search.value) {
            <h3>No meetings match "{{ search.value }}"</h3>
            <p class="muted">Try a different word, or clear the search.</p>
          } @else {
            <h3>No meetings yet</h3>
            <p class="muted">Upload your first recording to generate a summary and action items.</p>
            <a mat-flat-button routerLink="/upload" class="accent-btn">
              <mat-icon>add</mat-icon> Upload a recording
            </a>
          }
        </div>
      } @else {
        <div class="grid">
          @for (m of meetings(); track m.id) {
            <a class="card meeting" [routerLink]="['/meetings', m.id]">
              <div class="meeting-top">
                <span class="pill" [class]="pillClass(m.status)">
                  <span class="dot" [style.background]="dotColor(m.status)"></span>
                  {{ label(m.status) }}
                </span>
                <span class="faint date">{{ m.createdAt | date: 'mediumDate' }}</span>
              </div>
              <h3 class="meeting-title">{{ m.title }}</h3>
              <div class="meeting-foot muted">
                <mat-icon>task_alt</mat-icon>
                {{ m.actionItemCount }} action {{ m.actionItemCount === 1 ? 'item' : 'items' }}
              </div>
            </a>
          }
        </div>
      }
    </div>
  `,
  styles: [
    `
      .head {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 18px;
      }
      .title { font-size: 28px; margin: 0 0 4px; }
      .search { width: 100%; max-width: 520px; margin-bottom: 22px; }
      .center { display: flex; justify-content: center; padding: 60px 0; }
      .grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
        gap: 16px;
      }
      .meeting {
        display: block;
        padding: 18px;
        text-decoration: none;
        color: inherit;
        transition: box-shadow 0.15s ease, transform 0.15s ease, border-color 0.15s ease;
      }
      .meeting:hover {
        box-shadow: var(--shadow-md);
        transform: translateY(-2px);
        border-color: #cdd9e6;
      }
      .meeting:focus-visible { outline: 2px solid var(--brand); outline-offset: 2px; }
      .meeting-top {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;
      }
      .date { font-size: 12px; }
      .meeting-title { font-size: 17px; margin: 0 0 16px; line-height: 1.35; }
      .meeting-foot {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
      }
      .meeting-foot mat-icon { font-size: 17px; height: 17px; width: 17px; }
      .empty {
        text-align: center;
        padding: 56px 24px;
      }
      .empty mat-icon {
        font-size: 44px;
        height: 44px;
        width: 44px;
        color: var(--ink-faint);
      }
      .empty h3 { margin: 14px 0 6px; }
      .empty p { margin: 0 0 18px; }
    `,
  ],
})
export class DashboardComponent implements OnInit {
  private meetingService = inject(MeetingService);
  private router = inject(Router);

  meetings = signal<MeetingSummary[]>([]);
  loading = signal(true);
  search = new FormControl('', { nonNullable: true });

  ngOnInit(): void {
    this.load();

    this.search.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((term) =>
          term.trim()
            ? this.meetingService.search(term.trim())
            : this.meetingService.list(),
        ),
      )
      .subscribe({
        next: (list) => {
          this.meetings.set(list);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  private load(): void {
    this.loading.set(true);
    this.meetingService.list().subscribe({
      next: (list) => {
        this.meetings.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  label(status: MeetingStatus): string {
    return statusLabel(status);
  }

  pillClass(status: MeetingStatus): string {
    return statusClass(status);
  }

  dotColor(status: MeetingStatus): string {
    return status === 'COMPLETED'
      ? 'var(--ok)'
      : status === 'FAILED'
        ? 'var(--danger)'
        : 'var(--warn)';
  }
}
