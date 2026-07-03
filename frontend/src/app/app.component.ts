import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  template: `
    @if (auth.isAuthenticated()) {
      <mat-toolbar class="topbar">
        <a routerLink="/dashboard" class="brand">
          <mat-icon>graphic_eq</mat-icon>
          <span class="display brand-name">Sprint Meet Summarizer</span>
        </a>
        <span class="spacer"></span>
        <a mat-flat-button routerLink="/upload" class="accent-btn new-btn">
          <mat-icon>add</mat-icon>
          New meeting
        </a>
        <button mat-icon-button [matMenuTriggerFor]="menu" aria-label="Account">
          <mat-icon>account_circle</mat-icon>
        </button>
        <mat-menu #menu="matMenu">
          <div class="menu-head">
            <div class="menu-name">{{ auth.user()?.fullName }}</div>
            <div class="menu-email faint">{{ auth.user()?.email }}</div>
          </div>
          <button mat-menu-item (click)="auth.logout()">
            <mat-icon>logout</mat-icon>
            <span>Sign out</span>
          </button>
        </mat-menu>
      </mat-toolbar>
    }
    <router-outlet />
  `,
  styles: [
    `
      .topbar {
        background: var(--surface);
        color: var(--ink);
        border-bottom: 1px solid var(--border);
        box-shadow: var(--shadow-sm);
        position: sticky;
        top: 0;
        z-index: 10;
      }
      .brand {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        text-decoration: none;
        color: var(--brand);
      }
      .brand mat-icon {
        color: var(--accent);
      }
      .brand-name {
        font-size: 20px;
        color: var(--ink);
      }
      .spacer { flex: 1 1 auto; }
      .new-btn { margin-right: 8px; }
      .menu-head { padding: 10px 16px; }
      .menu-name { font-weight: 600; }
      .menu-email { font-size: 12px; }
    `,
  ],
})
export class AppComponent {
  protected auth = inject(AuthService);
}
