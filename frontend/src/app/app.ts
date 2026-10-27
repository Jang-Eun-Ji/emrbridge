import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

import { AuthService } from './core/auth/auth.service';

interface NavItem {
  path: string;
  label: string;
  caption: string;
  icon: string;
}

@Component({
  selector: 'app-root',
  imports: [
    MatIconModule,
    MatSidenavModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.auth.user;
  readonly isLoggedIn = this.auth.isLoggedIn;

  constructor() {
    if (this.auth.isLoggedIn() && !this.auth.user()) {
      this.auth.restoreSession().subscribe();
    }
  }

  readonly onLoginPage = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects.startsWith('/login')),
      startWith(this.router.url.startsWith('/login')),
    ),
    { initialValue: this.router.url.startsWith('/login') },
  );

  readonly showShell = computed(() => this.isLoggedIn() && !this.onLoginPage());

  readonly scopeLabel = computed(() => {
    const user = this.user();
    if (!user) {
      return '';
    }
    return user.admin ? 'All hospitals' : (user.hospitalName ?? 'Hospital');
  });

  readonly nav: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', caption: 'Clinical statistics', icon: 'dashboard' },
    { path: '/patients', label: 'Patients', caption: 'Patient list', icon: 'group' },
    { path: '/conditions', label: 'Conditions', caption: 'Disease analysis', icon: 'medical_information' },
    { path: '/pipeline', label: 'Data Pipeline', caption: 'Processing status', icon: 'account_tree' },
  ];

  logout(): void {
    this.auth.logout();
  }
}
