import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';

import { AuthApi } from '../api/auth-api';
import { AuthUser, LoginRequest } from '../models/auth.models';

const TOKEN_KEY = 'emrbridge.jwt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthApi);
  private readonly router = inject(Router);

  private readonly userSignal = signal<AuthUser | null>(null);
  private readonly tokenSignal = signal<string | null>(this.readStoredToken());

  readonly user = this.userSignal.asReadonly();
  readonly token = this.tokenSignal.asReadonly();
  readonly isLoggedIn = computed(() => !!this.tokenSignal());
  readonly isAdmin = computed(() => this.userSignal()?.admin === true);

  login(request: LoginRequest): Observable<AuthUser> {
    return this.api.login(request).pipe(
      tap((response) => {
        this.persistToken(response.accessToken);
        this.userSignal.set(response.user);
      }),
      map((response) => response.user),
    );
  }

  /** Restore session user from JWT; clears token on failure. */
  restoreSession(): Observable<AuthUser | null> {
    const token = this.tokenSignal();
    if (!token) {
      this.userSignal.set(null);
      return of(null);
    }
    return this.api.me().pipe(
      tap((user) => this.userSignal.set(user)),
      catchError(() => {
        this.clearSession();
        return of(null);
      }),
    );
  }

  logout(): void {
    this.clearSession();
    void this.router.navigateByUrl('/login');
  }

  private persistToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this.tokenSignal.set(token);
  }

  private clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.tokenSignal.set(null);
    this.userSignal.set(null);
  }

  private readStoredToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }
}
