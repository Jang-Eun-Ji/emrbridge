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
    return `백엔드에 연결하지 못했습니다. Spring Boot가 8080에서 실행 중인지 확인해 주세요. (${endpoint})`;
  }
  if (statusCode === 404) {
    return `조회 API가 아직 없습니다. ${endpoint} 를 백엔드에 추가하면 이 화면에 데이터가 표시됩니다.`;
  }
  return `데이터를 불러오지 못했습니다. (${endpoint})`;
}
