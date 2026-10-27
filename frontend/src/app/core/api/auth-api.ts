import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AuthUser, LoginRequest, LoginResponse } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', body);
  }

  me(): Observable<AuthUser> {
    return this.http.get<AuthUser>('/api/auth/me');
  }
}
