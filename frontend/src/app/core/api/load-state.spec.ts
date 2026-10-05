import { loadErrorMessage } from './load-state';

describe('loadErrorMessage', () => {
  it('explains a missing backend', () => {
    expect(loadErrorMessage(502, 'GET /api/analytics/dashboard')).toContain('8080');
  });

  it('explains a missing read API', () => {
    expect(loadErrorMessage(404, 'GET /api/analytics/dashboard')).toContain('조회 API가 아직 없습니다');
  });
});