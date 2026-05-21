const TOKEN_KEY = 'smart-consult-token';
const USER_KEY = 'smart-consult-user';
export function getToken() {
    return localStorage.getItem(TOKEN_KEY) || '';
}
export function getStoredUser() {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
        return null;
    }
    try {
        return JSON.parse(raw);
    }
    catch {
        clearAuth();
        return null;
    }
}
export function setAuth(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
}
export function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}
