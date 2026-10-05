import { HttpErrorResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, startWith } from 'rxjs/operators';

export type LoadState<T> =
  | { status: 'loading' }
  | { status: 'ready'; data: T }
  | { status: 'error'; statusCode?: number };

export function load<T>(source: Observable<T>): Observable<LoadState<T>> {
  return source.pipe(
    map((data): LoadState<T> => ({ status: 'ready', data })),
    catchError(
      (error: HttpErrorResponse): Observable<LoadState<T>> =>
        of({ status: 'error', statusCode: error.status }),
    ),
    startWith<LoadState<T>>({ status: 'loading' }),
  );
}

export function loadErrorMessage(statusCode: number | undefined, endpoint: string): string {
  if (statusCode === 0 || statusCode === 502 || statusCode === 503 || statusCode === 504) {
    return `Could not reach the backend. Make sure Spring Boot is running on port 8080. (${endpoint})`;
  }
  if (statusCode === 404) {
    return `Read API is not available yet. Add ${endpoint} on the backend to populate this view.`;
  }
  return `Failed to load data. (${endpoint})`;
}
