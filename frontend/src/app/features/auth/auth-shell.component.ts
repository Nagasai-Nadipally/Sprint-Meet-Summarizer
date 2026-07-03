import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Shared visual frame for the auth screens: a brand panel on the left that
 * states what the product does, and a slot for the form on the right.
 */
@Component({
  selector: 'app-auth-shell',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="auth">
      <aside class="brand-panel">
        <div class="brand-mark">
          <mat-icon>graphic_eq</mat-icon>
          <span class="display">Sprint Meet Summarizer</span>
        </div>
        <h2 class="display headline">
          Turn recordings into decisions.
        </h2>
        <p class="blurb">
          Upload a meeting and get a clean summary, the key discussion points,
          and a list of action items with owners and deadlines — searchable
          whenever you need them.
        </p>
        <ul class="features">
          <li><mat-icon>graphic_eq</mat-icon> Automatic transcription</li>
          <li><mat-icon>summarize</mat-icon> Summary &amp; key points</li>
          <li><mat-icon>task_alt</mat-icon> Action items with owners</li>
          <li><mat-icon>search</mat-icon> Searchable history</li>
        </ul>
      </aside>

      <main class="form-panel">
        <div class="form-inner">
          <ng-content />
        </div>
      </main>
    </div>
  `,
  styles: [
    `
      .auth {
        min-height: 100vh;
        display: grid;
        grid-template-columns: 1.05fr 1fr;
      }
      .brand-panel {
        background: linear-gradient(160deg, var(--brand) 0%, var(--brand-dark) 100%);
        color: #eaf1f8;
        padding: 56px 52px;
        display: flex;
        flex-direction: column;
      }
      .brand-mark {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        font-size: 19px;
        color: #fff;
      }
      .brand-mark mat-icon { color: var(--accent); }
      .headline {
        font-size: 40px;
        line-height: 1.1;
        margin: auto 0 18px;
        max-width: 12ch;
        color: #fff;
      }
      .blurb {
        font-size: 16px;
        line-height: 1.6;
        color: #cdddec;
        max-width: 42ch;
        margin: 0 0 28px;
      }
      .features {
        list-style: none;
        padding: 0;
        margin: 0;
        display: grid;
        gap: 12px;
      }
      .features li {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 14px;
        color: #dceaf6;
      }
      .features mat-icon {
        color: var(--accent);
        font-size: 20px;
        height: 20px;
        width: 20px;
      }
      .form-panel {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 40px 24px;
      }
      .form-inner { width: 100%; max-width: 380px; }

      @media (max-width: 880px) {
        .auth { grid-template-columns: 1fr; }
        .brand-panel { display: none; }
      }
    `,
  ],
})
export class AuthShellComponent {}
