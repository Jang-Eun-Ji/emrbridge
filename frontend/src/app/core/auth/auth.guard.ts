import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';

import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn() && auth.user()) {
    return true;
  }

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  return auth.restoreSession().pipe(
    map((user) => (user ? true : router.createUrlTree(['/login']))),
  );
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return true;
  }

  if (auth.user()) {
    return router.createUrlTree(['/dashboard']);
  }

  return auth.restoreSession().pipe(
    map((user) => (user ? router.createUrlTree(['/dashboard']) : true)),
  );
};
