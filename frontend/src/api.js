let csrf;
export async function api(path, options = {}) {
  const method = options.method || 'GET';
  const headers = { ...options.headers };
  if (options.body !== undefined) headers['Content-Type'] = 'application/json';
  if (!['GET','HEAD'].includes(method)) {
    if (!csrf) {
      const res = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
      if (!res.ok) throw new Error('Could not establish a secure session. Please reload.');
      csrf = await res.json();
    }
    headers[csrf.headerName] = csrf.token;
  }
  let response;
  try {
    response = await fetch(`/api${path}`, { ...options, method, headers, credentials: 'same-origin', body: options.body === undefined ? undefined : JSON.stringify(options.body) });
  } catch { throw new Error('Cannot reach the server. Check that Jobify+ is running and try again.'); }
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 403) csrf = undefined;
    const error = new Error(data.message || (response.status === 403 ? 'This action is not permitted. Please refresh and try again.' : 'Something went wrong. Please try again.'));
    error.status = response.status; throw error;
  }
  return data;
}
