import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../core/services/auth.service';
import { AuthShellComponent } from './auth-shell.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    AuthShellComponent,
  ],
  template: `
    <app-auth-shell>
      <h1 class="display title">Welcome back</h1>
      <p class="muted sub">Sign in to review your meetings and action items.</p>

      @if (loading()) {
        <mat-progress-bar mode="indeterminate" />
      }

      <form [formGroup]="form" (ngSubmit)="submit()" class="form">
        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="email" />
          @if (form.controls.email.touched && form.controls.email.invalid) {
            <mat-error>Enter a valid email address</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Password</mat-label>
          <input matInput type="password" formControlName="password" autocomplete="current-password" />
          @if (form.controls.password.touched && form.controls.password.invalid) {
            <mat-error>Password is required</mat-error>
          }
        </mat-form-field>

        @if (error()) {
          <div class="form-error">{{ error() }}</div>
        }

        <button mat-flat-button type="submit" class="accent-btn submit" [disabled]="loading()">
          Sign in
        </button>
      </form>

      <p class="switch muted">
        New here? <a routerLink="/register">Create an account</a>
      </p>
    </app-auth-shell>
  `,
  styles: [
    `
      .title { font-size: 30px; margin: 0 0 6px; }
      .sub { margin: 0 0 22px; }
      .form { display: flex; flex-direction: column; margin-top: 8px; }
      .submit { height: 46px; font-size: 15px; margin-top: 4px; }
      .switch { margin-top: 20px; font-size: 14px; }
      .switch a { color: var(--brand); font-weight: 600; }
      .form-error {
        background: var(--danger-soft);
        color: var(--danger);
        border-radius: 10px;
        padding: 10px 14px;
        font-size: 14px;
        margin-bottom: 14px;
      }
    `,
  ],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  error = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Could not sign in. Check your details and try again.');
      },
    });
  }
}
