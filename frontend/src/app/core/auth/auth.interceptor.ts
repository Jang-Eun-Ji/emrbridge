import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();

  const authReq =
    token && !req.url.includes('/api/auth/login')
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authReq).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !req.url.includes('/api/auth/login') &&
        auth.isLoggedIn()
      ) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};
